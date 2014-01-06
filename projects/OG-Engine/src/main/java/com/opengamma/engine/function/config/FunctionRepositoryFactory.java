/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.NoOpFunction;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;

/**
 * Constructs and bootstraps an {@link InMemoryFunctionRepository} based on configuration provided in a Fudge-encoded stream.
 */
public abstract class FunctionRepositoryFactory implements ChangeProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionRepositoryFactory.class);

  /**
   * The set of functions that are always in a constructed repository regardless of the {@link FunctionConfigurationBundle} document used.
   */
  private static final List<FunctionDefinition> INTRINSIC_FUNCTIONS = ImmutableList.<FunctionDefinition>of(NoOpFunction.INSTANCE, MarketDataAliasingFunction.INSTANCE,
      StructureManipulationFunction.INSTANCE);

  /**
   * The number of functions that are always in a constructed repository regardless of the {@link FunctionConfigurationBundle} document used. For example:
   * <ul>
   * <li>The no-op function used for execution suppression ({@link NoOpFunction})</li>
   * <li>The value aliasing function ({@link MarketDataAliasingFunction})</li>
   * </ul>
   */
  public static final int INTRINSIC_FUNCTION_COUNT = INTRINSIC_FUNCTIONS.size();

  /**
   * Creates a new repository, with functions from the given version timestamp.
   * 
   * @param configurationVersion the version timestamp, not null
   * @return the function repository, not null
   */
  public abstract FunctionRepository constructRepository(Instant configurationVersion);

  /**
   * Creates a new factory that always returns the same repository.
   * 
   * @param staticFunctions the function repository to return, not null
   * @return the repository factory, not null
   */
  public static FunctionRepositoryFactory constructRepositoryFactory(final FunctionRepository staticFunctions) {
    ArgumentChecker.notNull(staticFunctions, "staticFunctions");
    return new FunctionRepositoryFactory() {

      @Override
      public FunctionRepository constructRepository(final Instant configurationVersion) {
        return staticFunctions;
      }

      @Override
      public ChangeManager changeManager() {
        return DummyChangeManager.INSTANCE;
      }

    };
  }

  /**
   * Creates a new factory that queries a {@link FunctionConfigurationSource} for function definitions.
   * <p>
   * The source is queried with each call, but if it returns the same document then the same repository instance is returned.
   * 
   * @param dynamicFunctions the configuration source, not null
   * @return the repository factory, not null
   */
  public static FunctionRepositoryFactory constructRepositoryFactory(final FunctionConfigurationSource dynamicFunctions) {
    ArgumentChecker.notNull(dynamicFunctions, "dynamicFunctions");
    return new FunctionRepositoryFactory() {

      private FunctionConfigurationBundle _previousConfiguration;
      private FunctionRepository _previousRepository;

      @Override
      public synchronized FunctionRepository constructRepository(final Instant configurationVersion) {
        final FunctionConfigurationBundle repositoryConfiguration = dynamicFunctions.getFunctionConfiguration(configurationVersion);
        if ((_previousConfiguration == null) || !_previousConfiguration.equals(repositoryConfiguration)) {
          _previousConfiguration = repositoryConfiguration;
          _previousRepository = constructRepository(repositoryConfiguration);
        }
        return _previousRepository;
      }

      @Override
      public ChangeManager changeManager() {
        return dynamicFunctions.changeManager();
      }

    };
  }

  /**
   * Constructs a repository from the configuration.
   * 
   * @param configuration the configuration, not null
   * @return the repository, not null
   */
  public static InMemoryFunctionRepository constructRepository(final FunctionConfigurationBundle configuration) {
    final InMemoryFunctionRepository repository = constructRepositoryWithIntrinsicFunctions();
    if (configuration.getFunctions() != null) {
      for (final FunctionConfiguration functionConfig : configuration.getFunctions()) {
        if (functionConfig instanceof ParameterizedFunctionConfiguration) {
          addParameterizedFunctionConfiguration(repository, (ParameterizedFunctionConfiguration) functionConfig);
        } else if (functionConfig instanceof StaticFunctionConfiguration) {
          addStaticFunctionConfiguration(repository, (StaticFunctionConfiguration) functionConfig);
        } else {
          s_logger.error("Unhandled function configuration {}, ignoring", functionConfig);
        }
      }
    }
    return repository;
  }

  private static InMemoryFunctionRepository constructRepositoryWithIntrinsicFunctions() {
    final InMemoryFunctionRepository repository = new InMemoryFunctionRepository();
    for (FunctionDefinition intrinsicFunction : INTRINSIC_FUNCTIONS) {
      repository.addFunction(intrinsicFunction);
    }
    return repository;
  }

  //-------------------------------------------------------------------------
  protected static void addParameterizedFunctionConfiguration(final InMemoryFunctionRepository repository, final ParameterizedFunctionConfiguration functionConfig) {
    try {
      final Class<?> definitionClass = ReflectionUtils.loadClass(functionConfig.getDefinitionClassName());
      final AbstractFunction functionDefinition = createParameterizedFunction(definitionClass, functionConfig.getParameter());
      repository.addFunction(functionDefinition);
    } catch (final RuntimeException ex) {
      s_logger.error("Unable to add function definition {}, ignoring", functionConfig);
      s_logger.info("Caught exception", ex);
    }
  }

  protected static AbstractFunction createParameterizedFunction(final Class<?> definitionClass, final List<String> parameterList) {
    try {
      constructors: for (final Constructor<?> constructor : definitionClass.getConstructors()) { //CSIGNORE
        final Class<?>[] parameters = constructor.getParameterTypes();
        final Object[] args = new Object[parameters.length];
        int used = 0;
        for (int i = 0; i < parameters.length; i++) {
          if (parameters[i] == String.class) {
            if (i < parameterList.size()) {
              args[i] = parameterList.get(i);
              used++;
            } else {
              continue constructors;
            }
          } else {
            if (i == parameters.length - 1) {
              used = parameterList.size();
              if (parameters[i] == String[].class) {
                args[i] = parameterList.subList(i, used).toArray(new String[used - i]);
              } else if (parameters[i].isAssignableFrom(List.class)) {
                args[i] = parameterList.subList(i, used);
              } else if (parameters[i].isAssignableFrom(Set.class)) {
                args[i] = new HashSet<String>(parameterList.subList(i, used));
              } else {
                continue constructors;
              }
            } else {
              continue constructors;
            }
          }
        }
        if (used != parameterList.size()) {
          continue;
        }
        return (AbstractFunction) constructor.newInstance(args);
      }
      throw new NoSuchMethodException("No suitable constructor found: " + definitionClass + ": " + parameterList);

    } catch (final RuntimeException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new OpenGammaRuntimeException("Unable to create static function: " + definitionClass + ": " + parameterList, ex);
    }
  }

  //-------------------------------------------------------------------------
  protected static void addStaticFunctionConfiguration(final InMemoryFunctionRepository repository, final StaticFunctionConfiguration functionConfig) {
    try {
      final Class<?> definitionClass = ReflectionUtils.loadClass(functionConfig.getDefinitionClassName());
      final AbstractFunction functionDefinition = createStaticFunction(definitionClass);
      repository.addFunction(functionDefinition);
    } catch (final RuntimeException ex) {
      s_logger.error("Unable to add function definition {}, ignoring", functionConfig);
      s_logger.info("Caught exception", ex);
    }
  }

  protected static AbstractFunction createStaticFunction(final Class<?> definitionClass) {
    try {
      return (AbstractFunction) definitionClass.newInstance();
    } catch (final RuntimeException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new OpenGammaRuntimeException("Unable to create static function: " + definitionClass, ex);
    }
  }

}

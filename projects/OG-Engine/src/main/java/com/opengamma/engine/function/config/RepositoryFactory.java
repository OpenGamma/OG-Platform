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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.NoOpFunction;
import com.opengamma.util.ReflectionUtils;

/**
 * Constructs and bootstraps an {@link InMemoryFunctionRepository} based on configuration
 * provided in a Fudge-encoded stream.
 */
public class RepositoryFactory {
  private static final Logger s_logger = LoggerFactory.getLogger(RepositoryFactory.class);

  /**
   * Constructs a repository from the configuration.
   * 
   * @param configuration  the configuration, not null
   * @return the repository, not null
   */
  public static InMemoryFunctionRepository constructRepository(RepositoryConfiguration configuration) {
    InMemoryFunctionRepository repository = new InMemoryFunctionRepository();
    repository.addFunction(new NoOpFunction());
    if (configuration.getFunctions() != null) {
      for (FunctionConfiguration functionConfig : configuration.getFunctions()) {
        if (functionConfig instanceof ParameterizedFunctionConfiguration) {
          addParameterizedFunctionConfiguration(repository, (ParameterizedFunctionConfiguration) functionConfig);
        } else if (functionConfig instanceof StaticFunctionConfiguration) {
          addStaticFunctionConfiguration(repository, (StaticFunctionConfiguration) functionConfig);
        } else {
          s_logger.warn("Unhandled function configuration {}, ignoring", functionConfig);
        }
      }
    }
    return repository;
  }

  //-------------------------------------------------------------------------
  protected static void addParameterizedFunctionConfiguration(InMemoryFunctionRepository repository, ParameterizedFunctionConfiguration functionConfig) {
    try {
      Class<?> definitionClass = ReflectionUtils.loadClass(functionConfig.getDefinitionClassName());
      AbstractFunction functionDefinition = createParameterizedFunction(definitionClass, functionConfig.getParameter());
      repository.addFunction(functionDefinition);
      
    } catch (RuntimeException ex) {
      throw new OpenGammaRuntimeException("Unable to add parameterized function: " + functionConfig, ex);
    }
  }

  protected static AbstractFunction createParameterizedFunction(Class<?> definitionClass, List<String> parameterList) {
    try {
    constructors:
      for (Constructor<?> constructor : definitionClass.getConstructors()) {
        final Class<?>[] parameters = constructor.getParameterTypes();
        final Object[] args = new Object[parameters.length];
        int used = 0;
        for (int i = 0; i < parameters.length; i++) {
          if (parameters[i] == String.class) {
            if (i < parameterList.size()) {
              args[i] = (String) parameterList.get(i);
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
      
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      s_logger.error("Exception creating parameterized function", ex);
      throw new OpenGammaRuntimeException("Unable to create static function: " + definitionClass + ": " + parameterList, ex);
    }
  }

  //-------------------------------------------------------------------------
  protected static void addStaticFunctionConfiguration(InMemoryFunctionRepository repository, StaticFunctionConfiguration functionConfig) {
    try {
      Class<?> definitionClass = ReflectionUtils.loadClass(functionConfig.getDefinitionClassName());
      AbstractFunction functionDefinition = createStaticFunction(definitionClass);
      repository.addFunction(functionDefinition);
      
    } catch (RuntimeException ex) {
      throw new OpenGammaRuntimeException("Unable to add static function: " + functionConfig, ex);
    }
  }

  protected static AbstractFunction createStaticFunction(Class<?> definitionClass) {
    try {
      return (AbstractFunction) definitionClass.newInstance();
      
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      s_logger.error("Exception creating static function", ex);
      throw new OpenGammaRuntimeException("Unable to create static function: " + definitionClass, ex);
    }
  }

}

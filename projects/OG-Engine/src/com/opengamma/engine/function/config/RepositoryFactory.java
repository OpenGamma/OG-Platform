/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.InMemoryFunctionRepository;

/**
 * Constructs and bootstraps an {@link InMemoryFunctionRepository} based on configuration
 * provided in a Fudge-encoded stream.
 */
public class RepositoryFactory {
  private static final Logger s_logger = LoggerFactory.getLogger(RepositoryFactory.class);
  private static final Class<?> STRING_ARRAY_CLASS = (new String[0]).getClass();

  public static InMemoryFunctionRepository constructRepository(RepositoryConfiguration configuration) {
    InMemoryFunctionRepository repository = new InMemoryFunctionRepository();

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

  protected static void addParameterizedFunctionConfiguration(InMemoryFunctionRepository repository, ParameterizedFunctionConfiguration functionConfig) {
    // TODO kirk 2010-02-17 -- This method needs to be WAY more robust.
    // TODO kirk 2010-05-24 -- One way is to separate out the various error cases
    // to make them more clear by limiting what goes on in the individual try{} blocks.
    try {
      Class<?> definitionClass = Class.forName(functionConfig.getDefinitionClassName());
      AbstractFunction functionDefinition = instantiateDefinition(definitionClass, functionConfig.getParameter());
      repository.addFunction(functionDefinition);
    } catch (ClassNotFoundException e) {
      throw new OpenGammaRuntimeException("Unable to resolve classes", e);
    } catch (InstantiationException e) {
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    } catch (IllegalAccessException e) {
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    } catch (SecurityException e) {
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    } catch (NoSuchMethodException e) {
      throw new OpenGammaRuntimeException("No available constructor for " + functionConfig.getDefinitionClassName(), e);
    } catch (IllegalArgumentException e) {
      throw new OpenGammaRuntimeException("Arguments to constructor not right constructing " + functionConfig.getDefinitionClassName(), e);
    } catch (InvocationTargetException e) {
      throw new OpenGammaRuntimeException("Invocation of constructor failed", e);
    }
  }

  protected static AbstractFunction instantiateDefinition(Class<?> definitionClass, List<String> parameterList) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
      InstantiationException {
    try {
      Class<?>[] parameterTypes = new Class<?>[parameterList.size()];
      Object[] parameters = new Object[parameterList.size()];
      for (int i = 0; i < parameterList.size(); i++) {
        parameters[i] = parameterList.get(i);
        parameterTypes[i] = String.class;
      }
      Constructor<?> constructor = definitionClass.getConstructor(parameterTypes);
      return (AbstractFunction) constructor.newInstance(parameters);
    } catch (NoSuchMethodException nsme) {
      // This is fine. Next trap will check as well.
    }

    try {
      Class<?>[] parameterTypes = new Class<?>[1];
      parameterTypes[0] = STRING_ARRAY_CLASS;
      Object[] parameters = new Object[1];
      String[] actualParameters = parameterList.toArray(new String[0]);
      parameters[0] = actualParameters;
      Constructor<?> constructor = definitionClass.getConstructor(parameterTypes);
      return (AbstractFunction) constructor.newInstance(parameters);
    } catch (NoSuchMethodException nsme) {
      // This is fine. Next trap will check as well.
    }

    // TODO kirk 2010-05-24 -- Support Collection constructors as well.

    throw new NoSuchMethodException("No constructor for individual strings or string arrays found in class " + definitionClass);
  }

  protected static void addStaticFunctionConfiguration(InMemoryFunctionRepository repository, StaticFunctionConfiguration functionConfig) {
    // TODO kirk 2010-02-17 -- This method needs to be WAY more robust.
    try {
      Class<?> definitionClass = Class.forName(functionConfig.getDefinitionClassName());
      AbstractFunction functionDefinition = (AbstractFunction) definitionClass.newInstance();
      repository.addFunction(functionDefinition);
    } catch (ClassNotFoundException e) {
      throw new OpenGammaRuntimeException("Unable to resolve classes", e);
    } catch (InstantiationException e) {
      s_logger.error("Exception instantiating function", e);
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    } catch (IllegalAccessException e) {
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    }

  }

}

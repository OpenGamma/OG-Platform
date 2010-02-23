/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.InMemoryFunctionRepository;

/**
 * 
 *
 * @author kirk
 */
public class RepositoryFactory {
  private static final Logger s_logger = LoggerFactory.getLogger(RepositoryFactory.class);
  
  public static InMemoryFunctionRepository constructRepository(RepositoryConfiguration configuration) {
    InMemoryFunctionRepository repository = new InMemoryFunctionRepository();
    
    for(FunctionConfiguration functionConfig : configuration.getFunctions()) {
      if(functionConfig instanceof ParameterizedFunctionConfiguration) {
        addParameterizedFunctionConfiguration(repository, (ParameterizedFunctionConfiguration) functionConfig);
      } else if(functionConfig instanceof StaticFunctionConfiguration) {
        addStaticFunctionConfiguration(repository, (StaticFunctionConfiguration)functionConfig);
      } else {
        s_logger.warn("Unhandled function configuration {}, ignoring", functionConfig);
      }
    }
    
    return repository;
  }

  /**
   * @param repository
   * @param functionConfig
   */
  protected static void addParameterizedFunctionConfiguration(
      InMemoryFunctionRepository repository,
      ParameterizedFunctionConfiguration functionConfig) {
    // TODO kirk 2010-02-17 -- This method needs to be WAY more robust.
    try {
      Class<?> definitionClass = Class.forName(functionConfig.getDefinitionClassName());
      Object[] parameters = new Object[functionConfig.getParameter().size()];
      Class<?>[] parameterTypes = new Class<?>[functionConfig.getParameter().size()];
      for(int i = 0; i < parameters.length; i++) {
        parameters[i] = functionConfig.getParameter().get(i);
        parameterTypes[i] = String.class;
      }
      Constructor<?> constructor = definitionClass.getConstructor(parameterTypes);
      AbstractFunction functionDefinition = (AbstractFunction) constructor.newInstance(parameters);
      FunctionInvoker invoker = null;
      if(functionDefinition instanceof FunctionInvoker) {
        invoker = (FunctionInvoker) functionDefinition;
      } else if(functionConfig.getInvokerClassName() != null) {
        Class<?> invokerClass = Class.forName(functionConfig.getInvokerClassName());
        invoker = (FunctionInvoker)invokerClass.newInstance();
      } else {
        throw new IllegalArgumentException("Function definition doesn't invoke, but no invoker class name provided.");
      }
      repository.addFunction(functionDefinition, invoker);
    } catch (ClassNotFoundException e) {
      throw new OpenGammaRuntimeException("Unable to resolve classes", e);
    } catch (InstantiationException e) {
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    } catch (IllegalAccessException e) {
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    } catch (SecurityException e) {
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    } catch (NoSuchMethodException e) {
      throw new OpenGammaRuntimeException("No available constructor", e);
    } catch (IllegalArgumentException e) {
      throw new OpenGammaRuntimeException("Arguments to constructor not right", e);
    } catch (InvocationTargetException e) {
      throw new OpenGammaRuntimeException("Invocation of constructor failed", e);
    }
  }

  /**
   * @param repository
   * @param functionConfig
   */
  protected static void addStaticFunctionConfiguration(
      InMemoryFunctionRepository repository,
      StaticFunctionConfiguration functionConfig) {
    // TODO kirk 2010-02-17 -- This method needs to be WAY more robust.
    try {
      Class<?> definitionClass = Class.forName(functionConfig.getDefinitionClassName());
      AbstractFunction functionDefinition = (AbstractFunction)definitionClass.newInstance();
      FunctionInvoker invoker = null;
      if(functionDefinition instanceof FunctionInvoker) {
        invoker = (FunctionInvoker) functionDefinition;
      } else if(functionConfig.getInvokerClassName() != null) {
        Class<?> invokerClass = Class.forName(functionConfig.getInvokerClassName());
        invoker = (FunctionInvoker)invokerClass.newInstance();
      } else {
        throw new IllegalArgumentException("Function definition doesn't invoke, but no invoker class name provided.");
      }
      repository.addFunction(functionDefinition, invoker);
    } catch (ClassNotFoundException e) {
      throw new OpenGammaRuntimeException("Unable to resolve classes", e);
    } catch (InstantiationException e) {
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    } catch (IllegalAccessException e) {
      throw new OpenGammaRuntimeException("Unable to instantiate classes", e);
    }
    
  }

}

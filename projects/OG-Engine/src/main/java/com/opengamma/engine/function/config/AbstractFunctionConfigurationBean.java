/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean template for creating a simple {@link FunctionConfigurationSource} which returns a static configuration. The configuration may be generated from additional parameters set on the bean
 * or entirely from static data.
 */
public abstract class AbstractFunctionConfigurationBean extends SingletonFactoryBean<FunctionConfigurationSource> {

  protected static <F extends FunctionDefinition> FunctionConfiguration functionConfiguration(final Class<F> clazz, final String... args) {
    if (Modifier.isAbstract(clazz.getModifiers())) {
      throw new IllegalStateException("Attempting to register an abstract class - " + clazz);
    }
    if (args.length == 0) {
      return new StaticFunctionConfiguration(clazz.getName());
    }
    return new ParameterizedFunctionConfiguration(clazz.getName(), Arrays.asList(args));
  }

  protected abstract void addAllConfigurations(final List<FunctionConfiguration> functions);

  protected FunctionConfigurationBundle createRepositoryConfiguration() {
    final List<FunctionConfiguration> functions = new LinkedList<FunctionConfiguration>();
    addAllConfigurations(functions);
    return new FunctionConfigurationBundle(functions);
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return new SimpleFunctionConfigurationSource(createRepositoryConfiguration());
  }

}

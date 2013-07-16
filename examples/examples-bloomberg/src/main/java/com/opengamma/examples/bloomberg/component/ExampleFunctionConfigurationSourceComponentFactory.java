/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.component;

import java.util.List;

import com.opengamma.component.factory.source.FunctionConfigurationSourceComponentFactory;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.examples.bloomberg.function.ExampleStandardFunctionConfiguration;

/**
 * Component factory for the repository configuration source.
 */
public class ExampleFunctionConfigurationSourceComponentFactory extends FunctionConfigurationSourceComponentFactory {

  @Override
  protected FunctionConfigurationSource standardConfiguration() {
    return ExampleStandardFunctionConfiguration.instance();
  }

  @Override
  protected List<FunctionConfigurationSource> initSources() {
    final List<FunctionConfigurationSource> sources = super.initSources();
    return sources;
  }
//
//  @Override
//  protected List<FunctionConfigurationSource> initSources() {
//    final List<FunctionConfigurationSource> sources = super.initSources();
//    final FunctionConfigurationBundle configuration = new FunctionConfigurationBundle();
//    configuration.addFunctions(new ParameterizedFunctionConfiguration(FXOptionBlackSurfaceDefaults.class.getName(), Arrays.asList(DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR,
//        LINEAR_EXTRAPOLATOR, "USD", "EUR", "DEFAULT")));
//    sources.add(new SimpleFunctionConfigurationSource(configuration));
//    return sources;
//  }

}

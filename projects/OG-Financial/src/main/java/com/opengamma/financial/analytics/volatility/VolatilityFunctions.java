/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleFunctionConfigurationSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeFunctions;
import com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class VolatilityFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new VolatilityFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(VolatilitySurfaceSpecificationFunction.class));
    functions.add(functionConfiguration(VolatilitySurfaceDefinitionFunction.class));
  }

  /**
   * Returns volatility cube functions.
   * @return A list of volatility cube functions
   */
  protected FunctionConfigurationSource cubeFunctionConfiguration() {
    return VolatilityCubeFunctions.instance();
  }

  /**
   * Returns an empty list
   * @return An empty list
   */
  protected FunctionConfigurationSource fittedResultsFunctionConfiguration() {
    // TODO
    return new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  /**
   * Returns surface functions.
   * @return A list of surface functions
   */
  protected FunctionConfigurationSource surfaceFunctionConfiguration() {
    return SurfaceFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), cubeFunctionConfiguration(), fittedResultsFunctionConfiguration(), surfaceFunctionConfiguration());
  }

}

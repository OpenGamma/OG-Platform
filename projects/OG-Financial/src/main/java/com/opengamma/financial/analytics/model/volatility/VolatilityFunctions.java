/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.volatility.cube.CubeFunctions;
import com.opengamma.financial.analytics.model.volatility.local.LocalFunctions;
import com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions;

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
    // No functions, just sub-packages
  }

  protected FunctionConfigurationSource cubeFunctionConfiguration() {
    return CubeFunctions.instance();
  }

  protected FunctionConfigurationSource localFunctionConfiguration() {
    return LocalFunctions.instance();
  }

  protected FunctionConfigurationSource surfaceFunctionConfiguration() {
    return SurfaceFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), cubeFunctionConfiguration(), localFunctionConfiguration(), surfaceFunctionConfiguration());
  }

}

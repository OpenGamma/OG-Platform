/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class LocalFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new LocalFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ForexDupireLocalVolatilitySurfaceFunction.SABR.class));
    functions.add(functionConfiguration(ForexDupireLocalVolatilitySurfaceFunction.MixedLogNormal.class));
    functions.add(functionConfiguration(ForexDupireLocalVolatilitySurfaceFunction.Spline.class));
    functions.add(functionConfiguration(EquityDupireLocalVolatilitySurfaceFunction.SABR.class));
    functions.add(functionConfiguration(EquityDupireLocalVolatilitySurfaceFunction.MixedLogNormal.class));
    functions.add(functionConfiguration(EquityDupireLocalVolatilitySurfaceFunction.Spline.class));
  }

}

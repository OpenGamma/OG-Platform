/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class SurfaceFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new SurfaceFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(CommodityOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(DefaultVolatilitySurfaceShiftFunction.class));
    functions.add(functionConfiguration(EquityOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(IRFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawBondFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawEquityOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawIRFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawFXVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawSoybeanFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawSwaptionATMVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(SwaptionATMVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(VolatilitySurfaceShiftFunction.class));
  }

}

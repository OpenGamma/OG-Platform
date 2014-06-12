/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleFunctionConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class BlackFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new BlackFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BlackVolatilitySurfaceMixedLogNormalInterpolatorFunction.class));
    functions.add(functionConfiguration(BlackVolatilitySurfaceSABRInterpolatorFunction.class));
    functions.add(functionConfiguration(BlackVolatilitySurfaceSplineInterpolatorFunction.Exception.class));
    functions.add(functionConfiguration(BlackVolatilitySurfaceSplineInterpolatorFunction.Flat.class));
    functions.add(functionConfiguration(BlackVolatilitySurfaceSplineInterpolatorFunction.Quiet.class));
    functions.add(functionConfiguration(CommodityBlackVolatilitySurfaceFunction.SABR.class));
    functions.add(functionConfiguration(CommodityBlackVolatilitySurfaceFunction.Spline.class));
    functions.add(functionConfiguration(EquityBlackVolatilitySurfaceFunction.SABR.class));
    functions.add(functionConfiguration(EquityBlackVolatilitySurfaceFunction.Spline.class));
    functions.add(functionConfiguration(EquityFutureBlackVolatilitySurfaceFunction.SABR.class));
    functions.add(functionConfiguration(EquityFutureBlackVolatilitySurfaceFunction.Spline.class));
    functions.add(functionConfiguration(EquityBlackVolatilitySurfaceFromSinglePriceFunction.class));
    functions.add(functionConfiguration(ForexBlackVolatilitySurfaceFunction.MixedLogNormal.class));
    functions.add(functionConfiguration(ForexBlackVolatilitySurfaceFunction.SABR.class));
    functions.add(functionConfiguration(ForexBlackVolatilitySurfaceFunction.Spline.class));
    
  }

  protected FunctionConfigurationSource pureFunctionConfiguration() {
    // TODO
    return new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), pureFunctionConfiguration());
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class BlackFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
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
    functions.add(functionConfiguration(ForexBlackVolatilitySurfaceFunction.MixedLogNormal.class));
    functions.add(functionConfiguration(ForexBlackVolatilitySurfaceFunction.SABR.class));
    functions.add(functionConfiguration(ForexBlackVolatilitySurfaceFunction.Spline.class));
  }

  protected RepositoryConfigurationSource pureFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), pureFunctionConfiguration());
  }

}

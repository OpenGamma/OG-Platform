/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class SurfaceFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   * 
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
    return new SurfaceFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BlackScholesMertonImpliedVolatilitySurfaceFunction.class));
    functions.add(functionConfiguration(HestonFourierIRFutureSurfaceFittingFunction.class));
    functions.add(functionConfiguration(SABRNonLinearLeastSquaresIRFutureOptionSurfaceFittingFunction.class));
  }

  protected RepositoryConfigurationSource blackFunctionConfiguration() {
    return BlackFunctions.instance();
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), blackFunctionConfiguration());
  }

}

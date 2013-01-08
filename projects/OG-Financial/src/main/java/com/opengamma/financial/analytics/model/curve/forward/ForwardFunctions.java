/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class ForwardFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new ForwardFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ForwardSwapCurveFromMarketQuotesFunction.class));
    functions.add(functionConfiguration(ForwardSwapCurveMarketDataFunction.class));
    functions.add(functionConfiguration(FXForwardCurveFromMarketQuotesFunction.class));
    functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesFunction.class));
    functions.add(functionConfiguration(FXForwardCurveMarketDataFunction.class));
  }
}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class InterestRateCurveFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new InterestRateCurveFunctions()).getObjectCreating();

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(InterpolatedYieldCurveFunction.class));
    functions.add(functionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class, MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING));
    functions.add(functionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
  }

}

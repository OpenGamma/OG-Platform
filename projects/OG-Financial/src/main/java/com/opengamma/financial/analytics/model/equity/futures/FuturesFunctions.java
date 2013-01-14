/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FuturesFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new FuturesFunctions()).getObjectCreating();

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityDividendYieldForwardFuturesFunction.class));
    functions.add(functionConfiguration(EquityDividendYieldPresentValueFuturesFunction.class));
    functions.add(functionConfiguration(EquityDividendYieldPV01FuturesFunction.class));
    functions.add(functionConfiguration(EquityDividendYieldSpotFuturesFunction.class));
    functions.add(functionConfiguration(EquityDividendYieldValueDeltaFuturesFunction.class));
    functions.add(functionConfiguration(EquityDividendYieldValueRhoFuturesFunction.class));
    functions.add(functionConfiguration(EquityDividendYieldFuturesYCNSFunction.class));
    // TODO: add other package functions
  }

}

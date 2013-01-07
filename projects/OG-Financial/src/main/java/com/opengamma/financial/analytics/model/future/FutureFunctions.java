/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FutureFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new FutureFunctions()).getObjectCreating();

  public static RepositoryConfigurationSource deprecated() {
    final List<FunctionConfiguration> functions = new ArrayList<FunctionConfiguration>();
    functions.add(functionConfiguration(InterestRateFuturePresentValueFunctionDeprecated.class));
    functions.add(functionConfiguration(InterestRateFuturePV01FunctionDeprecated.class));
    functions.add(functionConfiguration(InterestRateFutureYieldCurveNodeSensitivitiesFunctionDeprecated.class));
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(functions));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondFutureGrossBasisFromCurvesFunction.class));
    functions.add(functionConfiguration(BondFutureNetBasisFromCurvesFunction.class));
    functions.add(functionConfiguration(InterestRateFuturePresentValueFunction.class));
    functions.add(functionConfiguration(InterestRateFuturePV01Function.class));
    functions.add(functionConfiguration(InterestRateFutureYieldCurveNodeSensitivitiesFunction.class));
    functions.add(functionConfiguration(MarkToMarketForwardFuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketPresentValueFuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketPV01FuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketSpotFuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketValueDeltaFuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketValueRhoFuturesFunction.class));
    // TODO: add functions from package
  }

}

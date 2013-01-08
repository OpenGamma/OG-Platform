/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FutureFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new FutureFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource deprecated() {
    return new Deprecated().getObjectCreating();
  }

  /**
   * Function repository configuration source for the deprecated functions contained in this package.
   */
  public static class Deprecated extends AbstractRepositoryConfigurationBean {

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(InterestRateFuturePresentValueFunctionDeprecated.class));
      functions.add(functionConfiguration(InterestRateFuturePV01FunctionDeprecated.class));
      functions.add(functionConfiguration(InterestRateFutureYieldCurveNodeSensitivitiesFunctionDeprecated.class));
    }

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

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

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
public class IRFutureOptionFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new IRFutureOptionFunctions()).getObjectCreating();

  public static RepositoryConfigurationSource deprecated() {
    final List<FunctionConfiguration> functions = new ArrayList<FunctionConfiguration>();
    // TODO: add functions
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(functions));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(InterestRateFutureOptionMarketUnderlyingPriceFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackVolatilitySensitivityFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackImpliedVolatilityFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackPV01Function.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackYieldCurveNodeSensitivitiesFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackGammaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackPriceFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionHestonPresentValueFunction.class));
    functions.add(functionConfiguration(IRFutureOptionSABRPresentValueFunction.class));
    functions.add(functionConfiguration(IRFutureOptionSABRSensitivitiesFunction.class));
    functions.add(functionConfiguration(IRFutureOptionSABRYCNSFunction.class));
    // TODO: add functions from package
  }

}

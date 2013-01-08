/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class BondFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new BondFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondCouponPaymentDiaryFunction.class));
    functions.add(functionConfiguration(BondTenorFunction.class));
    functions.add(functionConfiguration(BondMarketCleanPriceFunction.class));
    functions.add(functionConfiguration(BondMarketDirtyPriceFunction.class));
    functions.add(functionConfiguration(BondMarketYieldFunction.class));
    functions.add(functionConfiguration(BondYieldFromCurvesFunction.class));
    functions.add(functionConfiguration(BondCleanPriceFromCurvesFunction.class));
    functions.add(functionConfiguration(BondDirtyPriceFromCurvesFunction.class));
    functions.add(functionConfiguration(BondMacaulayDurationFromCurvesFunction.class));
    functions.add(functionConfiguration(BondModifiedDurationFromCurvesFunction.class));
    functions.add(functionConfiguration(BondCleanPriceFromYieldFunction.class));
    functions.add(functionConfiguration(BondDirtyPriceFromYieldFunction.class));
    functions.add(functionConfiguration(BondMacaulayDurationFromYieldFunction.class));
    functions.add(functionConfiguration(BondModifiedDurationFromYieldFunction.class));
    functions.add(functionConfiguration(BondZSpreadFromCurveCleanPriceFunction.class));
    functions.add(functionConfiguration(BondZSpreadFromMarketCleanPriceFunction.class));
    functions.add(functionConfiguration(BondZSpreadPresentValueSensitivityFromCurveCleanPriceFunction.class));
    functions.add(functionConfiguration(BondZSpreadPresentValueSensitivityFromMarketCleanPriceFunction.class));
    functions.add(functionConfiguration(NelsonSiegelSvenssonBondCurveFunction.class));
  }

}

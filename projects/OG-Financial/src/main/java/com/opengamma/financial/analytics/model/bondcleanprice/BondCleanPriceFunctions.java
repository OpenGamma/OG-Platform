/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcleanprice;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves.InflationBondZspreadFromCurvesFunction;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class BondCleanPriceFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from
   * this package.
   * @return The configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new BondCleanPriceFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondAccruedInterestFromCleanPriceFunction.class));
    functions.add(functionConfiguration(BondConvexityFromCleanPriceFunction.class));
    functions.add(functionConfiguration(BondMacaulayDurationFromCleanPriceFunction.class));
    functions.add(functionConfiguration(BondModifiedDurationFromCleanPriceFunction.class));
    functions.add(functionConfiguration(BondPresentValueFromCleanPriceFunction.class));
    functions.add(functionConfiguration(BondYieldFromCleanPriceFunction.class));
    functions.add(functionConfiguration(BondZSpreadFromCleanPriceFunction.class));
    functions.add(functionConfiguration(BondNetMarketValueFromCleanPriceFunction.class));
    functions.add(functionConfiguration(InflationBondZspreadFromCurvesFunction.class));
  }
}

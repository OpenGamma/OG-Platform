/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class BondCurveFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from
   * this package.
   * @return The configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new BondCurveFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondAccruedInterestFromCurvesFunction.class));
    functions.add(functionConfiguration(BondCleanPriceFromCurvesFunction.class));
    functions.add(functionConfiguration(BondConvexityFromCurvesFunction.class));
    functions.add(functionConfiguration(BondMacaulayDurationFromCurvesFunction.class));
    functions.add(functionConfiguration(BondModifiedDurationFromCurvesFunction.class));
    functions.add(functionConfiguration(BondPresentValueFromCurvesFunction.class));
    functions.add(functionConfiguration(BondYieldFromCurvesFunction.class));
  }
}

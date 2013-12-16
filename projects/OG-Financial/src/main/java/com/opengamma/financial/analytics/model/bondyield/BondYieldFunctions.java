/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondyield;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class BondYieldFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from
   * this package.
   * @return The configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new BondYieldFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondAccruedInterestFromYieldFunction.class));
    functions.add(functionConfiguration(BondCleanPriceFromYieldFunction.class));
    functions.add(functionConfiguration(BondConvexityFromYieldFunction.class));
    functions.add(functionConfiguration(BondMacaulayDurationFromYieldFunction.class));
    functions.add(functionConfiguration(BondModifiedDurationFromYieldFunction.class));
    functions.add(functionConfiguration(BondPresentValueFromYieldFunction.class));
    functions.add(functionConfiguration(BondZSpreadFromYieldFunction.class));
  }
}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class InflationBondCurveFunctions extends AbstractFunctionConfigurationBean {

  /**
  * Default instance of a repository configuration source exposing the functions from
  * this package.
  * @return The configuration source exposing functions from this package
  */
  public static FunctionConfigurationSource instance() {
    return new InflationBondCurveFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(InflationBondCleanPriceFromCurvesFunction.class));
    functions.add(functionConfiguration(InflationBondConvexityFromCurvesFunction.class));
    functions.add(functionConfiguration(InflationBondModifiedDurationFromCurvesFunction.class));
    functions.add(functionConfiguration(InflationBondYieldFromCurvesFunction.class));
    functions.add(functionConfiguration(InflationBondZspreadFromCurvesFunction.class));
    functions.add(functionConfiguration(InflationBondPresentValueFromCurvesFunction.class));
    functions.add(functionConfiguration(InflationBondPV01Function.class));
    functions.add(functionConfiguration(InflationBondYCNSFromCurvesFunction.class));
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class CDSFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new CDSFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ISDAApproxCDSPriceFlatSpreadFunction.class));
    functions.add(functionConfiguration(ISDAApproxCDSPriceHazardCurveFunction.class));
    functions.add(functionConfiguration(ISDAApproxFlatSpreadFunction.class));
    functions.add(functionConfiguration(ISDAApproxDiscountCurveFunction.class));
  }

}

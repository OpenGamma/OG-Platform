/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.hullwhitediscounting;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 *
 */
public class HullWhitePricingFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new HullWhitePricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(HullWhiteConvexityAdjustmentFunction.class));
    functions.add(functionConfiguration(HullWhiteDiscountingBCSFunction.class));
    functions.add(functionConfiguration(HullWhiteDiscountingParRateFunction.class));
    functions.add(functionConfiguration(HullWhiteDiscountingPVFunction.class));
    functions.add(functionConfiguration(HullWhiteMarketQuoteFunction.class));
    functions.add(functionConfiguration(HullWhiteMonteCarloDiscountingPVFunction.class));
    functions.add(functionConfiguration(HullWhiteDiscountingPV01Function.class));
    functions.add(functionConfiguration(HullWhiteDiscountingYCNSFunction.class));
  }
}

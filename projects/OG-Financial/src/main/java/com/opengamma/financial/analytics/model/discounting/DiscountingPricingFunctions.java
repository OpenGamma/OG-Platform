/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 *
 */
public class DiscountingPricingFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Creates an instance of this function configuration source.
   * @return A function configuration source populated with pricing functions
   * from this package.
   */
  public static FunctionConfigurationSource instance() {
    return new DiscountingPricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(DiscountingAllPV01Function.class));
    functions.add(functionConfiguration(DiscountingBCSFunction.class));
    functions.add(functionConfiguration(DiscountingCurrencyExposureFunction.class));
    functions.add(functionConfiguration(DiscountingFXPVFunction.class));
    functions.add(functionConfiguration(DiscountingMarketQuoteFunction.class));
    functions.add(functionConfiguration(DiscountingPVFunction.class));
    functions.add(functionConfiguration(DiscountingParRateFunction.class));
    functions.add(functionConfiguration(DiscountingPV01Function.class));
    functions.add(functionConfiguration(DiscountingSwapLegDetailFunction.class, "false"));
    functions.add(functionConfiguration(DiscountingSwapLegDetailFunction.class, "true"));
    functions.add(functionConfiguration(DiscountingYCNSFunction.class));
    
    functions.add(functionConfiguration(DiscountingInterpolatedPVFunction.class));
    functions.add(functionConfiguration(DiscountingInterpolatedAllPV01Function.class));
    functions.add(functionConfiguration(DiscountingInterpolatedPV01Function.class));
    functions.add(functionConfiguration(DiscountingInterpolatedParRateFunction.class));

    functions.add(functionConfiguration(DiscountingInflationBCSFunction.class));
    functions.add(functionConfiguration(DiscountingInflationPVFunction.class));
    functions.add(functionConfiguration(DiscountingInflationParSpreadFunction.class));
    functions.add(functionConfiguration(DiscountingInflationPV01Function.class));
    functions.add(functionConfiguration(DiscountingInflationYCNSFunction.class));
  }
}

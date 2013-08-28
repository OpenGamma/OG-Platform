/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Adds Black pricing and risk functions to the function configuration.
 */
public class BlackDiscountingPricingFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Gets an instance of this class.
   * @return The instance
   */
  public static FunctionConfigurationSource instance() {
    return new BlackDiscountingPricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BlackDiscountingPVFXOptionFunction.class));

    functions.add(functionConfiguration(BlackDiscountingBCSSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingImpliedVolatilitySwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01SwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVegaSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingBCSSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingImpliedVolatilitySwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingPVSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingPV01SwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingValueVegaSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingYCNSSwaptionFunction.class));
  }
}

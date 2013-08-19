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
 *
 */
public class BlackDiscountingPricingFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new BlackDiscountingPricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BlackDiscountingBCSFunction.class));
    functions.add(functionConfiguration(BlackDiscountingImpliedVolatilityFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01Function.class));
    functions.add(functionConfiguration(BlackDiscountingValueVegaFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSFunction.class));
  }
}

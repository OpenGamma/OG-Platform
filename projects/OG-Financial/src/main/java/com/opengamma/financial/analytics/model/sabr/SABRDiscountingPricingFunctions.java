/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabr;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 *
 */
public class SABRDiscountingPricingFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new SABRDiscountingPricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(NoExtrapolationSABRDiscountingBCSFunction.class));
    functions.add(functionConfiguration(NoExtrapolationSABRDiscountingImpliedVolFunction.class));
    functions.add(functionConfiguration(NoExtrapolationSABRDiscountingPVFunction.class));
    functions.add(functionConfiguration(NoExtrapolationSABRDiscountingPV01Function.class));
    functions.add(functionConfiguration(NoExtrapolationSABRDiscountingYCNSFunction.class));
    functions.add(functionConfiguration(RightExtrapolationSABRDiscountingBCSFunction.class));
    functions.add(functionConfiguration(RightExtrapolationSABRDiscountingImpliedVolFunction.class));
    functions.add(functionConfiguration(RightExtrapolationSABRDiscountingPVFunction.class));
    functions.add(functionConfiguration(RightExtrapolationSABRDiscountingPV01Function.class));
    functions.add(functionConfiguration(RightExtrapolationSABRDiscountingYCNSFunction.class));
  }
}

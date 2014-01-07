/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.g2ppdiscounting;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 *
 */
public class G2ppPricingFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new G2ppPricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(G2ppDiscountingParRateFunction.class));
    functions.add(functionConfiguration(G2ppDiscountingPVFunction.class));
  }
}

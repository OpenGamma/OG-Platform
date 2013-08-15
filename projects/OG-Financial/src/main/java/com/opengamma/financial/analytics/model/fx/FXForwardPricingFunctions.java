/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fx;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 *
 */
public class FXForwardPricingFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new FXForwardPricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXForwardPointsBCSFunction.class));
    functions.add(functionConfiguration(FXForwardPointsCurrencyExposureFunction.class));
    functions.add(functionConfiguration(FXForwardPointsFCNSFunction.class));
    functions.add(functionConfiguration(FXForwardPointsPVFunction.class));
    functions.add(functionConfiguration(FXForwardPointsYCNSFunction.class));
    functions.add(functionConfiguration(FXForwardPointsCurrencyExposurePnLFunction.class));
    functions.add(functionConfiguration(FXForwardPointsFCNSPnLFunction.class));
    functions.add(functionConfiguration(FXForwardPointsYCNSPnLFunction.class));
  }
}

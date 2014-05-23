/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.carrlee;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Adds Carr-Lee pricing and risk functions to the function configuration.
 */
public class CarrLeeFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Gets an instance of this class.
   * @return The instance
   */
  public static FunctionConfigurationSource instance() {
    return new CarrLeeFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(CarrLeeFairValueFXVolatilitySwapFunction.class));
    functions.add(functionConfiguration(CarrLeeCurrencyExposureFXVolatilitySwapFunction.class));
    functions.add(functionConfiguration(CarrLeeDeltaFXVolatilitySwapFunction.class));
    functions.add(functionConfiguration(CarrLeePresentValueFXVolatilitySwapFunction.class));
    functions.add(functionConfiguration(CarrLeeThetaFXVolatilitySwapFunction.class));
    functions.add(functionConfiguration(CarrLeeVegaFXVolatilitySwapFunction.class));
  }
}

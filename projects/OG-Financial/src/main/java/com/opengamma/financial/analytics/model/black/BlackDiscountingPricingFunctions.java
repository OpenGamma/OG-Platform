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
    functions.add(functionConfiguration(BlackDiscountingBCSCapFloorFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVCapFloorFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01CapFloorFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSCapFloorFunction.class));

    functions.add(functionConfiguration(BlackDiscountingBCSFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingCurrencyExposureFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardDeltaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardDriftlessThetaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardGammaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardVegaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingImpliedVolatilityFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01FXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingFXPVFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingSpotDeltaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingSpotGammaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueDeltaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueGammaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueGammaSpotFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueThetaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVannaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVommaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingVegaMatrixFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingVegaQuoteMatrixFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSFXOptionFunction.class));

    functions.add(functionConfiguration(BlackDiscountingBCSIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingDeltaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingGammaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingImpliedVolatilityIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPositionDeltaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPositionGammaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPositionVegaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01IRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueDeltaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueGammaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVegaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingWeightedVegaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSIRFutureOptionFunction.class));

    functions.add(functionConfiguration(BlackDiscountingBCSSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingImpliedVolatilitySwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01SwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVegaSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingVegaMatrixSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingBCSSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingImpliedVolatilitySwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingPVSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingPV01SwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingValueVegaSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingYCNSSwaptionFunction.class));
  }
}

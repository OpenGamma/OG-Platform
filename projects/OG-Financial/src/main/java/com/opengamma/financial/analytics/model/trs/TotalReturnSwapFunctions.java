/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Adds pricing and risk functions for total return swaps to the function configuration.
 */
public class TotalReturnSwapFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Gets an instance of this class.
   * @return The instance
   */
  public static FunctionConfigurationSource instance() {
    return new TotalReturnSwapFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondTotalReturnSwapAssetLegDetailsFunction.class));
    functions.add(functionConfiguration(BondTotalReturnSwapAssetLegPVFunction.class));
    functions.add(functionConfiguration(BondTotalReturnSwapBCSFunction.class));
    functions.add(functionConfiguration(BondTotalReturnSwapYCNSFunction.class));
    functions.add(functionConfiguration(BondTotalReturnSwapConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(BondTotalReturnSwapFundingLegDetailsFunction.class));
    functions.add(functionConfiguration(BondTotalReturnSwapGammaPV01Function.class));
    functions.add(functionConfiguration(BondTotalReturnSwapFundingLegPVFunction.class));
    functions.add(functionConfiguration(BondTotalReturnSwapPVFunction.class));
    functions.add(functionConfiguration(BondTotalReturnSwapPV01Function.class));
    functions.add(functionConfiguration(BondTotalReturnSwapCurrencyExposureFunction.class));

    functions.add(functionConfiguration(EquityTotalReturnSwapAssetLegPVFunction.class));
    functions.add(functionConfiguration(EquityTotalReturnSwapFundingLegDetailsFunction.class));
    functions.add(functionConfiguration(EquityTotalReturnSwapFundingLegPVFunction.class));
    functions.add(functionConfiguration(EquityTotalReturnSwapGammaPV01Function.class));
    functions.add(functionConfiguration(EquityTotalReturnSwapPV01Function.class));
    functions.add(functionConfiguration(EquityTotalReturnSwapPVFunction.class));
    functions.add(functionConfiguration(EquityTotalReturnSwapValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityTotalReturnSwapConstantSpreadThetaFunction.class));
  }
}

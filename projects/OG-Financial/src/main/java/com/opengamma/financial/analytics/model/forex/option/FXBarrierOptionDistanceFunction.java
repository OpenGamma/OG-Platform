/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.futureoption.BarrierOptionDistanceFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;

// logic in getBarrierLevel() / getMarketDataRequirement() probably implies this should be refactored

/**
 * Function to compute barrier distance for equity options Defined as absolute difference (optionally expressed as a percentage) between barrier level and market price
 */
public class FXBarrierOptionDistanceFunction extends BarrierOptionDistanceFunction {

  /**
   */
  public FXBarrierOptionDistanceFunction() {
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_BARRIER_OPTION_SECURITY
        .or(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY);
  }

  @Override
  protected ValueRequirement getMarketDataRequirement(FinancialSecurity security) {
    if (security instanceof FXBarrierOptionSecurity) {
      final FXBarrierOptionSecurity barrierOption = (FXBarrierOptionSecurity) security;
      return ConventionBasedFXRateFunction.getSpotRateRequirement(barrierOption.getCallCurrency(), barrierOption.getPutCurrency());
    } else if (security instanceof FXDigitalOptionSecurity) {
      final FXDigitalOptionSecurity digitalOption = (FXDigitalOptionSecurity) security;
      return ConventionBasedFXRateFunction.getSpotRateRequirement(digitalOption.getCallCurrency(), digitalOption.getPutCurrency());
    } else {
      throw new OpenGammaRuntimeException("Got unexpected security type " + security);
    }

  }

  @Override
  protected Double getSpot(final FunctionInputs inputs) {
    final Object spotObject = inputs.getValue(ValueRequirementNames.SPOT_RATE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot for " + inputs.getAllValues().iterator().next().getSpecification().getTargetSpecification());
    }
    return (Double) spotObject;
  }

  @Override
  public double getBarrierLevel(FinancialSecurity security) {
    // yes this should be a visitor
    if (security instanceof FXBarrierOptionSecurity) {
      return ((FXBarrierOptionSecurity) security).getBarrierLevel();
    } else if (security instanceof FXDigitalOptionSecurity) {
      final FXDigitalOptionSecurity digitalSecurity = (FXDigitalOptionSecurity) security;
      return digitalSecurity.getCallAmount() / digitalSecurity.getPutAmount();
    } else {
      throw new OpenGammaRuntimeException("Got unexpected security type " + security);
    }
  }

}

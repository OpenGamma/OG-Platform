/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.futureoption.BarrierOptionDistanceFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;


/**
 * Function to compute barrier distance for equity options
 *
 * Defined as absolute difference (optionally expressed as a percentage) between barrier level and market price
 *
 */
public class EquityVanillaBarrierOptionDistanceFunction extends BarrierOptionDistanceFunction {

  /**
   */
  public EquityVanillaBarrierOptionDistanceFunction() {
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY;
  }

  @Override
  protected ValueRequirement getMarketDataRequirement(FinancialSecurity security) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, FinancialSecurityUtils.getUnderlyingId(security));
  }

  @Override
  protected Double getSpot(final FunctionInputs inputs) {
    final Object spotObject = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot for " + inputs.getAllValues().iterator().next().getSpecification().getTargetSpecification());
    }
    return (Double) spotObject;
  }

  @Override
  protected double getBarrierLevel(FinancialSecurity security) {
    return ((EquityBarrierOptionSecurity) security).getBarrierLevel();
  }

}

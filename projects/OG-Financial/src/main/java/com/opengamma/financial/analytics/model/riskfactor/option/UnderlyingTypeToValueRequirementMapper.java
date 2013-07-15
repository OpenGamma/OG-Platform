/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 * Maps an underlying type.
 */
public class UnderlyingTypeToValueRequirementMapper {

  public static ValueRequirement getValueRequirement(final UnderlyingType underlying, final Security security) {
    if (security instanceof EquityOptionSecurity) {
      final EquityOptionSecurity option = (EquityOptionSecurity) security;
      switch (underlying) {
        case SPOT_PRICE:
          return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, option.getUnderlyingId());
        case SPOT_VOLATILITY:
          throw new NotImplementedException("Don't know how to get spot volatility for " + option.getUniqueId());
        case IMPLIED_VOLATILITY:
          throw new NotImplementedException("Don't know how to get implied volatility for " + option.getUniqueId());
        case INTEREST_RATE:
          return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.SECURITY, option.getUniqueId());
        case COST_OF_CARRY:
          throw new NotImplementedException("Don't know how to get cost of carry for " + option.getUniqueId());
        default:
          throw new NotImplementedException("Don't know how to get ValueRequirement for " + underlying);
      }
    } else if (security instanceof EquitySecurity) {
      final EquitySecurity equity = (EquitySecurity) security;
      if (underlying == UnderlyingType.SPOT_PRICE) {
        return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, equity.getUniqueId());
      } else {
        throw new NotImplementedException("Don't know how to get ValueRequirement for " + underlying);
      }
    } else {
      throw new NotImplementedException("Can only get ValueRequirements for EquityOptionSecurity and EquitySecurity. Was " + security + ")");
    }
  }

}

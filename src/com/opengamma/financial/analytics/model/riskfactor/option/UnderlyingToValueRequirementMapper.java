/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.pnl.Underlying;
import com.opengamma.financial.security.option.OptionSecurity;

public class UnderlyingToValueRequirementMapper {

  public static ValueRequirement getValueRequirement(final Underlying underlying, final Security security) {
    if (security instanceof OptionSecurity) {
      final OptionSecurity option = (OptionSecurity) security;
      switch (underlying) {
        case SPOT_PRICE:
          return new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY,
              option.getUnderlyingIdentityKey());
        case SPOT_VOLATILITY:
          throw new NotImplementedException("Don't know how to get spot volatility for " + option.getIdentityKey());
        case IMPLIED_VOLATILITY:
          throw new NotImplementedException("Don't know how to get implied volatility for " + option.getIdentityKey());
        case INTEREST_RATE:
          return new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, option
              .getIdentityKey());
        case COST_OF_CARRY:
          throw new NotImplementedException("Don't know how to get cost of carry for " + option.getIdentityKey());
        default:
          throw new NotImplementedException("Don't know how to get ValueRequirement for " + underlying);
      }
    }
    throw new NotImplementedException("Can only get ValueRequirements for options");
  }
}

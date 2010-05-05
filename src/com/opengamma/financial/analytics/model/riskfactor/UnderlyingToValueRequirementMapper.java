/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.pnl.Underlying;
import com.opengamma.id.Identifier;

public class UnderlyingToValueRequirementMapper {

  public ValueRequirement getValueRequirement(final Underlying underlying, final Identifier id) {
    switch (underlying) {
    case SPOT_PRICE:
      return new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, id);
    case SPOT_VOLATILITY:
      throw new NotImplementedException("Don't know how to get spot volatility for " + id);
    case IMPLIED_VOLATILITY:
      throw new NotImplementedException("Don't know how to get implied volatility for " + id);
    case INTEREST_RATE:
      return new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, id);
    case COST_OF_CARRY:
      throw new NotImplementedException("Don't know how to get cost of carry for " + id);
    default:
      throw new NotImplementedException("Don't know how to get ValueRequirement for " + underlying);
    }
  }
}

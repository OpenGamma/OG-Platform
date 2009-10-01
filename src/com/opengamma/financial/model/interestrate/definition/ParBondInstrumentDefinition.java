/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import com.opengamma.util.time.Expiry;

/**
 * 
 * Dummy implementation for now.
 * 
 * @author emcleod
 */
public class ParBondInstrumentDefinition extends FixedInterestRateInstrumentDefinition {

  public ParBondInstrumentDefinition(final Expiry expiry, final Double coupon) {
    super(expiry, coupon);
  }

  // TODO necessary? just forces bond terminology into other fixed income
  // instrument terms
  public Double getCoupon() {
    return getRate();
  }
}

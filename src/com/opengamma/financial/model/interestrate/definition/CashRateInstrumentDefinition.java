/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class CashRateInstrumentDefinition extends FixedInterestRateInstrumentDefinition {

  public CashRateInstrumentDefinition(final Expiry expiry, final Double rate) {
    super(expiry, rate);
  }
}

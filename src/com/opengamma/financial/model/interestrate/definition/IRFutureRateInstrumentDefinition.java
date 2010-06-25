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
public class IRFutureRateInstrumentDefinition extends FixedInterestRateInstrumentDefinition {

  public IRFutureRateInstrumentDefinition(final Expiry expiry, final Double rate) {
    super(expiry, rate);
  }

  @Override
  public Double getRate() {
    return 1 - super.getRate();
  }
}

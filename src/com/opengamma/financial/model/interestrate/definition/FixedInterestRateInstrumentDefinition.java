/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * Dummy implementation for now
 * 
 */
public class FixedInterestRateInstrumentDefinition {
  // TODO have a tenor rather than expiry here
  private final Expiry _expiry;
  private final double _rate;

  public FixedInterestRateInstrumentDefinition(final Expiry expiry, final double rate) {
    Validate.notNull(expiry);

    _expiry = expiry;
    _rate = rate;
  }

  public Expiry getExpiry() {
    return _expiry;
  }

  public double getRate() {
    return _rate;
  }

  public Double getTenor(final ZonedDateTime date) {
    return DateUtil.getDifferenceInYears(date, getExpiry().getExpiry());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_expiry == null) ? 0 : _expiry.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FixedInterestRateInstrumentDefinition other = (FixedInterestRateInstrumentDefinition) obj;
    if (_expiry == null) {
      if (other._expiry != null) {
        return false;
      }
    } else if (!_expiry.equals(other._expiry)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

}

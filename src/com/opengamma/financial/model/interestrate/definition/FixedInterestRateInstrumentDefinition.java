/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * Dummy implementation for now
 * 
 */
public class FixedInterestRateInstrumentDefinition {
  // TODO have a tenor rather than expiry here
  private final Expiry _expiry;
  private final Double _rate;

  public FixedInterestRateInstrumentDefinition(final Expiry expiry, final Double rate) {
    _expiry = expiry;
    _rate = rate;
  }

  public Expiry getExpiry() {
    return _expiry;
  }

  public Double getRate() {
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
    result = prime * result + ((_rate == null) ? 0 : _rate.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FixedInterestRateInstrumentDefinition other = (FixedInterestRateInstrumentDefinition) obj;
    if (getExpiry() == null) {
      if (other.getExpiry() != null) {
        return false;
      }
    } else if (!getExpiry().equals(other.getExpiry())) {
      return false;
    }
    if (getRate() == null) {
      if (other.getRate() != null) {
        return false;
      }
    } else if (!getRate().equals(other.getRate())) {
      return false;
    }
    return true;
  }

}

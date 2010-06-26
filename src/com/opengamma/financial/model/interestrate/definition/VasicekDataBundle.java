/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 */
public class VasicekDataBundle {
  private final double _shortRate;
  private final double _longTermInterestRate;
  private final double _reversionSpeed;
  private final double _shortRateVolatility;
  private final ZonedDateTime _date;

  public VasicekDataBundle(final double shortRate, final double longTermInterestRate, final double reversionSpeed, final double shortRateVolatility,
      final ZonedDateTime date) {
    _shortRate = shortRate;
    _longTermInterestRate = longTermInterestRate;
    _reversionSpeed = reversionSpeed;
    _shortRateVolatility = shortRateVolatility;
    _date = date;
  }

  public double getShortRate() {
    return _shortRate;
  }

  public double getLongTermInterestRate() {
    return _longTermInterestRate;
  }

  public double getReversionSpeed() {
    return _reversionSpeed;
  }

  public double getShortRateVolatility() {
    return _shortRateVolatility;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_longTermInterestRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_reversionSpeed);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_shortRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_shortRateVolatility);
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
    VasicekDataBundle other = (VasicekDataBundle) obj;
    if (_date == null) {
      if (other._date != null) {
        return false;
      }
    } else if (!_date.equals(other._date)) {
      return false;
    }
    if (Double.doubleToLongBits(_longTermInterestRate) != Double.doubleToLongBits(other._longTermInterestRate)) {
      return false;
    }
    if (Double.doubleToLongBits(_reversionSpeed) != Double.doubleToLongBits(other._reversionSpeed)) {
      return false;
    }
    if (Double.doubleToLongBits(_shortRate) != Double.doubleToLongBits(other._shortRate)) {
      return false;
    }
    if (Double.doubleToLongBits(_shortRateVolatility) != Double.doubleToLongBits(other._shortRateVolatility)) {
      return false;
    }
    return true;
  }
}

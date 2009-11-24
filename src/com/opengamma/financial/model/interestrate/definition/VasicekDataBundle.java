/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 * @author emcleod
 */
public class VasicekDataBundle {
  private final Double _shortRate;
  private final Double _longTermInterestRate;
  private final Double _reversionSpeed;
  private final Double _shortRateVolatility;
  private final ZonedDateTime _date;

  public VasicekDataBundle(final Double shortRate, final Double longTermInterestRate, final Double reversionSpeed, final Double shortRateVolatility, final ZonedDateTime date) {
    _shortRate = shortRate;
    _longTermInterestRate = longTermInterestRate;
    _reversionSpeed = reversionSpeed;
    _shortRateVolatility = shortRateVolatility;
    _date = date;
  }

  public Double getShortRate() {
    return _shortRate;
  }

  public Double getLongTermInterestRate() {
    return _longTermInterestRate;
  }

  public Double getReversionSpeed() {
    return _reversionSpeed;
  }

  public Double getShortRateVolatility() {
    return _shortRateVolatility;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_date == null ? 0 : _date.hashCode());
    result = prime * result + (_longTermInterestRate == null ? 0 : _longTermInterestRate.hashCode());
    result = prime * result + (_reversionSpeed == null ? 0 : _reversionSpeed.hashCode());
    result = prime * result + (_shortRate == null ? 0 : _shortRate.hashCode());
    result = prime * result + (_shortRateVolatility == null ? 0 : _shortRateVolatility.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final VasicekDataBundle other = (VasicekDataBundle) obj;
    if (_date == null) {
      if (other._date != null)
        return false;
    } else if (!_date.equals(other._date))
      return false;
    if (_longTermInterestRate == null) {
      if (other._longTermInterestRate != null)
        return false;
    } else if (!_longTermInterestRate.equals(other._longTermInterestRate))
      return false;
    if (_reversionSpeed == null) {
      if (other._reversionSpeed != null)
        return false;
    } else if (!_reversionSpeed.equals(other._reversionSpeed))
      return false;
    if (_shortRate == null) {
      if (other._shortRate != null)
        return false;
    } else if (!_shortRate.equals(other._shortRate))
      return false;
    if (_shortRateVolatility == null) {
      if (other._shortRateVolatility != null)
        return false;
    } else if (!_shortRateVolatility.equals(other._shortRateVolatility))
      return false;
    return true;
  }
}

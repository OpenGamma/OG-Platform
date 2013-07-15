/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;

/**
 * 
 */
public class StandardDiscountBondModelDataBundle {
  private final ZonedDateTime _date;
  private final YieldAndDiscountCurve _shortRateCurve;
  private final VolatilityCurve _shortRateVolatilityCurve;

  public StandardDiscountBondModelDataBundle(final YieldAndDiscountCurve shortRateCurve, final VolatilityCurve shortRateVolatilityCurve, final ZonedDateTime date) {
    Validate.notNull(shortRateCurve);
    Validate.notNull(shortRateVolatilityCurve);
    Validate.notNull(date);
    _shortRateCurve = shortRateCurve;
    _shortRateVolatilityCurve = shortRateVolatilityCurve;
    _date = date;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public YieldAndDiscountCurve getShortRateCurve() {
    return _shortRateCurve;
  }

  public VolatilityCurve getShortRateVolatilityCurve() {
    return _shortRateVolatilityCurve;
  }

  public double getShortRate(final double t) {
    return _shortRateCurve.getInterestRate(t);
  }

  public double getShortRateVolatility(final double t) {
    return _shortRateVolatilityCurve.getVolatility(t);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
    result = prime * result + ((_shortRateVolatilityCurve == null) ? 0 : _shortRateVolatilityCurve.hashCode());
    result = prime * result + ((_shortRateCurve == null) ? 0 : _shortRateCurve.hashCode());
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
    final StandardDiscountBondModelDataBundle other = (StandardDiscountBondModelDataBundle) obj;
    return ObjectUtils.equals(_date, other._date) && ObjectUtils.equals(_shortRateVolatilityCurve, other._shortRateVolatilityCurve) && ObjectUtils.equals(_shortRateCurve, other._shortRateCurve);
  }

}

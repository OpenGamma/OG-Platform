/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdayieldcurve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.daycount.DayCount;

/**
 * 
 */
public class ZCurve {
  private final ZonedDateTime _valueDate;
  private final double[] _rates;
  private final ZonedDateTime[] _dates;
  private final double[] _discountFactors;
  private final long _basis;
  private final DayCount _dayCount;

  public ZCurve(final ZonedDateTime valueDate, final double[] rates, final ZonedDateTime[] dates, final double[] discountFactors, final long basis, final DayCount dayCount) {
    _valueDate = valueDate;
    _rates = rates;
    _dates = dates;
    _discountFactors = discountFactors;
    _basis = basis;
    _dayCount = dayCount;
  }

  public ZonedDateTime getValueDate() {
    return _valueDate;
  }

  public double[] getRates() {
    return _rates;
  }

  public ZonedDateTime[] getDates() {
    return _dates;
  }

  public double[] getDiscountFactors() {
    return _discountFactors;
  }

  public long getBasis() {
    return _basis;
  }

  public DayCount getDayCount() {
    return _dayCount;
  }
}

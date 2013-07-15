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
public class TCurve {
  private final double[] _rates;
  private final ZonedDateTime[] _dates;
  private final ZonedDateTime _baseDate;
  private final long _basis;
  private final DayCount _dayCount;

  public TCurve(final ZonedDateTime baseDate, final double[] rates, final ZonedDateTime[] dates, final long basis, final DayCount dayCount) {
    _baseDate = baseDate;
    _rates = rates;
    _dates = dates;
    _basis = basis;
    _dayCount = dayCount;
  }

  public double[] getRates() {
    return _rates;
  }

  public ZonedDateTime[] getDates() {
    return _dates;
  }

  public ZonedDateTime getBaseDate() {
    return _baseDate;
  }

  public long getBasis() {
    return _basis;
  }

  public DayCount getDayCount() {
    return _dayCount;
  }
}

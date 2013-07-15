/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDAYieldCurveAndSpreadsProvider {
  private final ZonedDateTime[] _marketDates;
  private final double[] _marketSpreads;
  private final ISDADateCurve _yieldCurve;

  /**
   * @param marketDates The dates of the credit spreads, not null
   * @param marketSpreads The credit spreads, not null
   * @param yieldCurve The yield curve, not null
   */
  public ISDAYieldCurveAndSpreadsProvider(final ZonedDateTime[] marketDates, final double[] marketSpreads, final ISDADateCurve yieldCurve) {
    ArgumentChecker.notNull(marketDates, "market dates");
    ArgumentChecker.notNull(marketSpreads, "market spreads");
    ArgumentChecker.notNull(yieldCurve, "yield curve");
    final int n = marketDates.length;
    ArgumentChecker.isTrue(n > 0, "Cannot have empty data");
    ArgumentChecker.isTrue(n == marketSpreads.length, "Number of spreads {} was not equals to number of dates {}", n, marketSpreads.length);
    _marketDates = new ZonedDateTime[n];
    _marketSpreads = new double[n];
    _yieldCurve = yieldCurve;
    System.arraycopy(marketDates, 0, _marketDates, 0, n);
    System.arraycopy(marketSpreads, 0, _marketSpreads, 0, n);
  }

  /**
   * Gets the spread curve dates.
   * @return The spread curve dates
   */
  public ZonedDateTime[] getMarketDates() {
    return _marketDates;
  }

  /**
   * Gets the spreads.
   * @return The spreads.
   */
  public double[] getMarketSpreads() {
    return _marketSpreads;
  }

  /**
   * Gets the yield curve.
   * @return The yield curve
   */
  public ISDADateCurve getYieldCurve() {
    return _yieldCurve;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_marketDates);
    result = prime * result + Arrays.hashCode(_marketSpreads);
    result = prime * result + _yieldCurve.hashCode();
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
    if (!(obj instanceof ISDAYieldCurveAndSpreadsProvider)) {
      return false;
    }
    final ISDAYieldCurveAndSpreadsProvider other = (ISDAYieldCurveAndSpreadsProvider) obj;
    if (!Arrays.equals(_marketDates, other._marketDates)) {
      return false;
    }
    if (!Arrays.equals(_marketSpreads, other._marketSpreads)) {
      return false;
    }
    if (!ObjectUtils.equals(_yieldCurve, other._yieldCurve)) {
      return false;
    }
    return true;
  }

}

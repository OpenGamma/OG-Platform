/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACompliantDateCurve extends ISDACompliantCurve implements ISDACompliantCurveWithDates {

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private final LocalDate _baseDate;
  private final LocalDate[] _dates;
  private final DayCount _dayCount;

  protected static ISDACompliantCurve makeISDACompliantCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    return makeISDACompliantCurve(baseDate, dates, rates, ACT_365);
  }

  protected static ISDACompliantCurve makeISDACompliantCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    double[] t = checkAndGetTimes(baseDate, dates, rates, dayCount);
    return new ISDACompliantCurve(t, rates);
  }

  /**
   * Builds a curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates. The times (year-fractions)
   * between the baseDate and the knot dates is calculated using ACT/365  
   * @param baseDate The base date for the curve (i.e. this is time zero)
   * @param dates Knot dates on the curve. These must be ascending with the first date after the baseDate
   * @param rates Continually compounded zero rates at given knot dates
   */
  public ISDACompliantDateCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    this(baseDate, dates, rates, ACT_365);
  }

  /**
   * Builds a curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates. The times (year-fractions)
   * between the baseDate and the knot dates is calculated using the specified day-count-convention   
   * @param baseDate The base date for the curve (i.e. this is time zero)
   * @param dates Knot dates on the curve. These must be ascending with the first date after the baseDate
   * @param rates Continually compounded zero rates at given knot dates
   * @param dayCount The day-count-convention
   */
  public ISDACompliantDateCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    this(baseDate, dates, dayCount, makeISDACompliantCurve(baseDate, dates, rates, dayCount));
  }

  private ISDACompliantDateCurve(final LocalDate baseDate, final LocalDate[] dates, final DayCount dayCount, final ISDACompliantCurve baseCurve) {
    super(baseCurve);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  public final LocalDate getBaseDate() {
    return _baseDate;
  }

  public final LocalDate getCurveDate(final int index) {
    return _dates[index];
  }

  public final LocalDate[] getCurveDates() {
    LocalDate[] res = new LocalDate[getNumberOfKnots()];
    // TODO since this is only copying references anyway, do we need it
    System.arraycopy(_dates, 0, res, 0, getNumberOfKnots());
    return res;
  }

  @Override
  public ISDACompliantDateCurve withRate(final double rate, final int index) {
    ISDACompliantCurve temp = super.withRate(rate, index);
    return new ISDACompliantDateCurve(_baseDate, _dates, _dayCount, temp);
  }

  protected static double[] checkAndGetTimes(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    return checkAndGetTimes(baseDate, dates, rates, ACT_365);
  }

  protected static double[] checkAndGetTimes(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    ArgumentChecker.notNull(baseDate, "null baseDate");
    ArgumentChecker.notNull(dayCount, "null dayCount");
    ArgumentChecker.noNulls(dates, "null dates");
    ArgumentChecker.notEmpty(rates, "empty rates");
    ArgumentChecker.isTrue(dates[0].isAfter(baseDate), "first date is not after base date");
    final int n = dates.length;
    ArgumentChecker.isTrue(rates.length == n, "rates and dates different lengths");
    final double[] t = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = dayCount.getDayCountFraction(baseDate, dates[i]);
      if (i > 0) {
        ArgumentChecker.isTrue(t[i] > t[i - 1], "dates are not ascending");
      }
    }
    return t;
  }

  @Override
  public double getZeroRate(LocalDate date) {
    double t = _dayCount.getDayCountFraction(_baseDate, date);
    return getZeroRate(t);
  }

}

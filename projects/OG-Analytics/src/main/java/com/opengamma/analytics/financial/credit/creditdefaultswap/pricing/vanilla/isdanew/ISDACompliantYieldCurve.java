/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import org.joda.time.base.BaseDateTime;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACompliantYieldCurve extends ISDACompliantCurve {

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private final LocalDate _baseDate;
  private final LocalDate[] _dates;

  /**
   * Builds a yield curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates. The times (year-fractions)
   * between the baseDate and the knot dates is calculated using ACT/365  
   * @param baseDate The base date for the curve (i.e. this is time zero)
   * @param dates Knot dates on the curve. These must be ascending with the first date after the baseDate
   * @param rates Continually compounded zero rates at given knot dates
   */
  public ISDACompliantYieldCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    this(baseDate, dates, rates, ACT_365);
  }

  /**
   * Builds a yield curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates. The times (year-fractions)
   * between the baseDate and the knot dates is calculated using the specified day-count-convention   
   * @param baseDate The base date for the curve (i.e. this is time zero)
   * @param dates Knot dates on the curve. These must be ascending with the first date after the baseDate
   * @param rates Continually compounded zero rates at given knot dates
   * @param dayCount The day-count-convention
   */
  public ISDACompliantYieldCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    super(checkAndGetTimes(baseDate, dates, rates, dayCount), rates);
    _baseDate = baseDate;
    _dates = dates;
  }

  /**
   * Converter from the old ISDADateCurve to ISDACompliantYieldCurve. Not this only works if offset = 0.0 and and baseDate is set.
   * @param yieldCurve a ISDADateCurve yieldCurve 
   * @return A ISDACompliantYieldCurve
   */
  public static ISDACompliantYieldCurve fromISDADateCurve(final ISDADateCurve yieldCurve) {

    ArgumentChecker.isTrue(yieldCurve.getOffset() == 0, "offset not zero - cannot convert");
    final ZonedDateTime bDate = yieldCurve.getBaseDate();
    ArgumentChecker.isTrue(bDate != null, "baseDate null - cannot convert");
    final LocalDate baseDate = bDate.toLocalDate();

    final ZonedDateTime[] curveDates = yieldCurve.getCurveDates();
    final Double[] temp = yieldCurve.getCurve().getYData();
    final int n = temp.length;
    final double[] r = new double[n];
    for (int i = 0; i < n; i++) {
      r[i] = temp[i];
    }

    final LocalDate[] dates = ISDACompliantScheduleGenerator.toLocalDate(curveDates);
    return new ISDACompliantYieldCurve(baseDate, dates, r);
  }

  final LocalDate getBaseDate() {
    return _baseDate;
  }

  final LocalDate getCurveDate(final int index) {
    return _dates[index];
  }

  final LocalDate[] getCurveDates() {
    LocalDate[] res = new LocalDate[getNumberOfKnots()];
    // TODO since this is only copying references anyway, do we need it
    System.arraycopy(_dates, 0, res, 0, getNumberOfKnots());
    return res;
  }

  private static double[] checkAndGetTimes(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
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

}

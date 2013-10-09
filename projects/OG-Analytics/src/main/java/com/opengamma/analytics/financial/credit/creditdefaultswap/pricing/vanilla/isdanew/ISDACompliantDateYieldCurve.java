/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantDateCurve.checkAndGetTimes;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ISDACompliantDateYieldCurve extends ISDACompliantYieldCurve implements ISDACompliantCurveWithDates {

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private final LocalDate _baseDate;
  private final LocalDate[] _dates;
  private final DayCount _dayCount;

  /**
   * Builds a yield curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates. The times (year-fractions)
   * between the baseDate and the knot dates is calculated using ACT/365
   * @param baseDate The base date for the curve (i.e. this is time zero)
   * @param dates Knot dates on the curve. These must be ascending with the first date after the baseDate
   * @param rates Continually compounded zero rates at given knot dates
   */
  public ISDACompliantDateYieldCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
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
  public ISDACompliantDateYieldCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    super(checkAndGetTimes(baseDate, dates, rates, dayCount), rates);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  private ISDACompliantDateYieldCurve(final LocalDate baseDate, final LocalDate[] dates, final DayCount dayCount, final ISDACompliantYieldCurve baseCurve) {
    super(baseCurve);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  /**
   * Converter from the old ISDADateCurve to ISDACompliantYieldCurve. Not this only works if offset = 0.0 and and baseDate is set.
   * @param yieldCurve a ISDADateCurve yieldCurve
   * @return A ISDACompliantYieldCurve
   */
  public static ISDACompliantDateYieldCurve fromISDADateCurve(final ISDADateCurve yieldCurve) {

    ArgumentChecker.isTrue(yieldCurve.getOffset() == 0, "offset not zero - cannot convert");
    final ZonedDateTime bDate = yieldCurve.getBaseDate();
    ArgumentChecker.notNull(bDate, "base date");
    final LocalDate baseDate = bDate.toLocalDate();

    final ZonedDateTime[] curveDates = yieldCurve.getCurveDates();
    final Double[] temp = yieldCurve.getCurve().getYData();
    final int n = temp.length;
    final double[] r = new double[n];
    for (int i = 0; i < n; i++) {
      r[i] = temp[i];
    }

    final LocalDate[] dates = ISDACompliantScheduleGenerator.toLocalDate(curveDates);
    return new ISDACompliantDateYieldCurve(baseDate, dates, r);
  }

  @Override
  public LocalDate getBaseDate() {
    return _baseDate;
  }

  @Override
  public LocalDate getCurveDate(final int index) {
    return _dates[index];
  }

  @Override
  public LocalDate[] getCurveDates() {
    final LocalDate[] res = new LocalDate[getNumberOfKnots()];
    // TODO since this is only copying references anyway, do we need it
    System.arraycopy(_dates, 0, res, 0, getNumberOfKnots());
    return res;
  }

  @Override
  public ISDACompliantDateYieldCurve withRate(final double rate, final int index) {
    final ISDACompliantYieldCurve temp = super.withRate(rate, index);
    return new ISDACompliantDateYieldCurve(_baseDate, _dates, _dayCount, temp);
  }


  @Override
  public double getZeroRate(final LocalDate date) {
    final double t = _dayCount.getDayCountFraction(_baseDate, date);
    return getZeroRate(t);
  }

}

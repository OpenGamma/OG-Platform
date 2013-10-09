/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantDateCurve.checkAndGetTimes;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACompliantDateCreditCurve extends ISDACompliantCreditCurve implements ISDACompliantCurveWithDates {

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private final LocalDate _baseDate;
  private final LocalDate[] _dates;
  private final DayCount _dayCount;

  /**
   * Builds a credit curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates. The times (year-fractions)
   * between the baseDate and the knot dates is calculated using ACT/365  
   * @param baseDate The base date for the curve (i.e. this is time zero)
   * @param dates Knot dates on the curve. These must be ascending with the first date after the baseDate
   * @param rates Continually compounded zero hazard rates at given knot dates
   */
  public ISDACompliantDateCreditCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    this(baseDate, dates, rates, ACT_365);
  }

  /**
   * Builds a credit curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates. The times (year-fractions)
   * between the baseDate and the knot dates is calculated using the specified day-count-convention   
   * @param baseDate The base date for the curve (i.e. this is time zero)
   * @param dates Knot dates on the curve. These must be ascending with the first date after the baseDate
   * @param rates Continually compounded zero hazard rates at given knot dates
   * @param dayCount The day-count-convention
   */
  public ISDACompliantDateCreditCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    super(checkAndGetTimes(baseDate, dates, rates, dayCount), rates);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  private ISDACompliantDateCreditCurve(final LocalDate baseDate, final LocalDate[] dates, final DayCount dayCount, ISDACompliantCreditCurve baseCurve) {
    super(baseCurve);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  /**
  * Converter from the old HazardRateCurve to ISDACompliantDateCreditCurve. Not this only works if offset = 0.0.
  * @param hazardCurve a HazardRateCurve hazard curve
  * @return A ISDACompliantDateCreditCurve
  */
  public static ISDACompliantDateCreditCurve fromHazardRateCurve(final HazardRateCurve hazardCurve) {

    ArgumentChecker.isTrue(hazardCurve.getOffset() == 0, "offset not zero - cannot convert");
    final LocalDate[] dates = ISDACompliantScheduleGenerator.toLocalDate(hazardCurve.getCurveTenors());
    final double[] t = hazardCurve.getTimes();
    ISDACompliantCreditCurve temp = new ISDACompliantCreditCurve(t, hazardCurve.getRates());

    // back out the missing baseDate (assuming ACT/365 was used)
    int days = (int) Math.round(365 * t[0]);
    LocalDate baseDate = dates[0].minusDays(days);

    return new ISDACompliantDateCreditCurve(baseDate, dates, ACT_365, temp);
  }

  @Override
  public LocalDate getBaseDate() {
    return _baseDate;
  }

  @Override
  public LocalDate getCurveDate(int index) {
    return _dates[index];
  }

  @Override
  public LocalDate[] getCurveDates() {
    LocalDate[] res = new LocalDate[getNumberOfKnots()];
    // TODO since this is only copying references anyway, do we need it
    System.arraycopy(_dates, 0, res, 0, getNumberOfKnots());
    return res;
  }

  @Override
  public ISDACompliantDateCreditCurve withRate(final double rate, final int index) {
    ISDACompliantCreditCurve temp = super.withRate(rate, index);
    return new ISDACompliantDateCreditCurve(_baseDate, _dates, _dayCount, temp);
  }

  @Override
  public double getZeroRate(LocalDate date) {
    final double t = _dayCount.getDayCountFraction(_baseDate, date);
    return getZeroRate(t);
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * This converts and stores all the date logic as doubles for CDS pricing on a particular date 
 */
public class CDSAnalytic {

  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  private final double _lgd;
  private final int _nPayments;
  private final double[] _creditObsTimes;
  private final double[] _paymentTimes;
  private final double[] _accFractions;
  private final double[] _accStart;
  private final double[] _accEnd;

  private final double _stepin;
  private final double _protectionStart;
  private final double _protectionEnd;
  private final double _valuationTime;
  private final boolean _payAccOnDefault;
  private final boolean _protectionFromStartOfDay;
  private final double _accrued;

  private final double _curveOneDay = 1. / 365; // TODO do not hard code

  /**
   * Generates an analytic description of a CDS trade on a particular date. This can then be passed to a analytic CDS pricer.<br>
   * This using a weekend only calendar with a following convention. ACT/360 is used for accrual and  ACT/365 to convert
   * payment dates to year-fractions (doubles) 
   * @param today The 'current' date
   * @param stepinDate Date when party assumes ownership. This is normally today + 1 (T+1). Aka assignment date or effective date.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param startDate The protection start date. If protectStart = true, then protections starts at the beginning of the day, otherwise it
   * is at the end.
   * @param endDate The protection end date (the protection ends at end of day)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param tenor The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart Does protection start at the beginning of the day
   * @param recoveryRate The recovery rate 
   */
  public CDSAnalytic(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate, final boolean payAccOnDefault,
      final Period tenor, StubType stubType, final boolean protectStart, final double recoveryRate) {
    this(today, stepinDate, valueDate, startDate, endDate, payAccOnDefault, tenor, stubType, protectStart, FOLLOWING, DEFAULT_CALENDAR, ACT_360, ACT_365, recoveryRate);
  }

  /**
   * Generates an analytic description of a CDS trade on a particular date. This can then be passed to a analytic CDS pricer 
   * @param today The 'current' date
   * @param stepinDate Date when party assumes ownership. This is normally today + 1 (T+1). Aka assignment date or effective date.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param startDate The protection start date. If protectStart = true, then protections starts at the beginning of the day, otherwise it
   * is at the end.
   * @param endDate The protection end date (the protection ends at end of day)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param tenor The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart Does protection start at the beginning of the day
   * @param businessdayAdjustmentConvention How are adjustments for non-business days made 
   * @param calendar Calendar defining what is a non-business day
   * @param accrualDayCount Day count used for accrual 
   * @param curveDayCount Day count used on curve (NOTE ISDA uses ACT/365 and it is not recommended to change this)
   * @param recoveryRate The recovery rate 
   */
  public CDSAnalytic(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate, final boolean payAccOnDefault,
      final Period tenor, StubType stubType, final boolean protectStart, final BusinessDayConvention businessdayAdjustmentConvention, final Calendar calendar, final DayCount accrualDayCount,
      final DayCount curveDayCount, final double recoveryRate) {
    ArgumentChecker.notNull(today, "null today");
    ArgumentChecker.notNull(stepinDate, "null stepinDate");
    ArgumentChecker.notNull(valueDate, "null valueDate");
    ArgumentChecker.notNull(startDate, "null startDate");
    ArgumentChecker.notNull(endDate, "null endDate");
    ArgumentChecker.notNull(tenor, "null tenor");
    ArgumentChecker.notNull(stubType, "null stubType");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "null businessdayAdjustmentConvention");
    ArgumentChecker.notNull(accrualDayCount, "null accuralDayCount");
    ArgumentChecker.notNull(curveDayCount, "null curveDayCount");
    ArgumentChecker.isFalse(valueDate.isBefore(today), "Require valueDate >= today");
    ArgumentChecker.isFalse(stepinDate.isBefore(today), "Require stepin >= today");
    ArgumentChecker.isFalse(today.isAfter(endDate), "CDS has expired");

    _payAccOnDefault = payAccOnDefault;
    _protectionFromStartOfDay = protectStart;

    final LocalDate temp = stepinDate.isAfter(startDate) ? stepinDate : startDate;
    final LocalDate effectiveStartDate = protectStart ? temp.minusDays(1) : temp;

    _stepin = curveDayCount.getDayCountFraction(today, stepinDate);
    _valuationTime = curveDayCount.getDayCountFraction(today, valueDate);
    _protectionStart = curveDayCount.getDayCountFraction(today, effectiveStartDate);
    _protectionEnd = curveDayCount.getDayCountFraction(today, endDate);

    _lgd = 1 - recoveryRate;

    final ISDAPremiumLegSchedule fullPaymentSchedule = new ISDAPremiumLegSchedule(startDate, endDate, tenor, stubType, businessdayAdjustmentConvention, calendar, protectStart);
    final ISDAPremiumLegSchedule paymentSchedule = ISDAPremiumLegSchedule.truncateSchedule(stepinDate, fullPaymentSchedule);

    _nPayments = paymentSchedule.getNumPayments();
    _paymentTimes = new double[_nPayments];
    _creditObsTimes = new double[_nPayments];
    _accStart = new double[_nPayments];
    _accEnd = new double[_nPayments];
    _accFractions = new double[_nPayments];

    for (int i = 0; i < _nPayments; i++) {
      LocalDate paymentDate = paymentSchedule.getPaymentDate(i);
      _paymentTimes[i] = curveDayCount.getDayCountFraction(today, paymentDate);
      final LocalDate accStart = paymentSchedule.getAccStartDate(i);
      final LocalDate accEnd = paymentSchedule.getAccEndDate(i);
      final LocalDate obsEnd = protectStart ? accEnd.minusDays(1) : accEnd;
      _accFractions[i] = accrualDayCount.getDayCountFraction(accStart, accEnd);
      _accStart[i] = accStart.isBefore(today) ? -curveDayCount.getDayCountFraction(accStart, today) : curveDayCount.getDayCountFraction(today, accStart);
      _accEnd[i] = curveDayCount.getDayCountFraction(today, accEnd);
      _creditObsTimes[i] = curveDayCount.getDayCountFraction(today, obsEnd); // TODO this looks odd - check again with ISDA c code
    }
    final LocalDate accStart = paymentSchedule.getAccStartDate(0);
    _accrued = accStart.isBefore(stepinDate) ? accrualDayCount.getDayCountFraction(accStart, stepinDate) : 0.0;
  }

  public int getNumPayments() {
    return _nPayments;
  }

  /**
   * Gets the payAccOnDefault.
   * @return the payAccOnDefault
   */
  public boolean isPayAccOnDefault() {
    return _payAccOnDefault;
  }

  /**
   * Gets the protectionFromStartOfDay.
   * @return the protectionFromStartOfDay
   */
  public boolean isProtectionFromStartOfDay() {
    return _protectionFromStartOfDay;
  }

  /**
   * Gets the year fraction value of one day for the day count used for curves (i.e. discounting) 
   * @return the curveOneDay
   */
  public double getCurveOneDay() {
    return _curveOneDay;
  }

  public double getLGD() {
    return _lgd;
  }

  /**
   * Gets the stepin.
   * @return the stepin
   */
  public double getStepin() {
    return _stepin;
  }

  /**
   * Gets the valuationTime.
   * @return the valuationTime
   */
  public double getValuationTime() {
    return _valuationTime;
  }

  /**
   * Gets the protectionStart.
   * @return the protectionStart
   */
  public double getProtectionStart() {
    return _protectionStart;
  }

  /**
   * Gets the protectionEnd.
   * @return the protectionEnd
   */
  public double getProtectionEnd() {
    return _protectionEnd;
  }

  /**
   * Gets the accStart for a particular payment period 
   * @param index the index of the payment period
   * @return the accStart
   */
  public double getAccStart(final int index) {
    return _accStart[index];
  }

  /**
   * Gets the accEnd for a particular payment period 
   * @param index the index of the payment period
   * @return the accEnd
   */
  public double getAccEnd(final int index) {
    return _accEnd[index];
  }

  /**
   * Gets the payment time for a particular payment period 
   * @param index the index of the payment period
   * @return the paymentTime
   */
  public double getPaymentTime(final int index) {
    return _paymentTimes[index];
  }

  public double getCreditObservationTime(final int index) {
    return _creditObsTimes[index];
  }

  /**
   * Gets the accrual fraction for a particular payment period 
   * @param index the index of the payment period
   * @return the accFraction
   */
  public double getAccrualFraction(final int index) {
    return _accFractions[index];
  }

  /**
   * Gets the accrued premium.
   * @return the accrued
   */
  public double getAccrued() {
    return _accrued;
  }
}

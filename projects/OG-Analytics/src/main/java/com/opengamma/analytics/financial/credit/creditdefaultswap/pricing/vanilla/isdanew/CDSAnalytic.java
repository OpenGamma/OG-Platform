/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.temporal.JulianFields;

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
  // private static final Calendar DEFAULT_CALENDAR = new NoHolidayCalendar();
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  /** Curve daycount generally fixed to Act/365 in ISDA */
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  private final double _lgd;
  private final int _nPayments;
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
  private final int _accruedDays;

  private final double _curveOneDay = 1. / 365; // TODO do not hard code

  /**
   * Generates an analytic description of a CDS trade on a particular date. This can then be passed to a analytic CDS pricer.<br>
   * This using a weekend only calendar with a following convention. ACT/360 is used for accrual and  ACT/365 to convert
   * payment dates to year-fractions (doubles)
   * @param tradeDate The trade date 
   * @param stepinDate Date when party assumes ownership. This is normally today + 1 (T+1). Aka assignment date or effective date.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param startDate The protection start date. If protectStart = true, then protections starts at the beginning of the day, otherwise it
   * is at the end.
   * @param endDate The protection end date (the protection ends at end of day)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param paymentInterval The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart Does protection start at the beginning of the day
   * @param recoveryRate The recovery rate
   */
  public CDSAnalytic(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate, final boolean payAccOnDefault,
      final Period paymentInterval, final StubType stubType, final boolean protectStart, final double recoveryRate) {
    this(tradeDate, stepinDate, valueDate, startDate, endDate, payAccOnDefault, paymentInterval, stubType, protectStart, recoveryRate, FOLLOWING, DEFAULT_CALENDAR, ACT_360, ACT_365);
  }

  /**
   * Generates an analytic description of a CDS trade on a particular date. This can then be passed to a analytic CDS pricer.<br>
   * This using a weekend only calendar with a following convention. ACT/360 is used for accrual and  ACT/365 to convert
   * payment dates to year-fractions (doubles)
   *
   * Note this uses a curve daycount of ACT/365 to match the ISDA methodology.
   *
   * @param tradeDate The trade date
   * @param stepinDate Date when party assumes ownership. This is normally today + 1 (T+1). Aka assignment date or effective date.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param startDate The protection start date. If protectStart = true, then protections starts at the beginning of the day, otherwise it
   * is at the end.
   * @param endDate The protection end date (the protection ends at end of day)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param paymentInterval The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart Does protection start at the beginning of the day
   * @param recoveryRate The recovery rate
   * @param businessdayAdjustmentConvention How are adjustments for non-business days made
   * @param calendar Calendar defining what is a non-business day
   * @param accrualDayCount Day count used for accrual
   */
  public CDSAnalytic(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate, final boolean payAccOnDefault,
      final Period paymentInterval, final StubType stubType, final boolean protectStart, final double recoveryRate, final BusinessDayConvention businessdayAdjustmentConvention,
      final Calendar calendar, final DayCount accrualDayCount) {
    this(tradeDate, stepinDate, valueDate, startDate, endDate, payAccOnDefault, paymentInterval, stubType, protectStart, recoveryRate, businessdayAdjustmentConvention, calendar, accrualDayCount,
        ACT_365);
  }

  /**
   * Generates an analytic description of a CDS trade on a particular date. This can then be passed to a analytic CDS pricer
   * @param tradeDate The trade date
   * @param stepinDate Date when party assumes ownership. This is normally today + 1 (T+1). Aka assignment date or effective date.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param startDate The protection start date. If protectStart = true, then protections starts at the beginning of the day, otherwise it
   * is at the end.
   * @param endDate The protection end date (the protection ends at end of day)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param paymentInterval The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart Does protection start at the beginning of the day
   * @param recoveryRate The recovery rate
   * @param businessdayAdjustmentConvention How are adjustments for non-business days made
   * @param calendar Calendar defining what is a non-business day
   * @param accrualDayCount Day count used for accrual
   * @param curveDayCount Day count used on curve (NOTE ISDA uses ACT/365 and it is not recommended to change this)
   */
  public CDSAnalytic(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate, final boolean payAccOnDefault,
      final Period paymentInterval, final StubType stubType, final boolean protectStart, final double recoveryRate, final BusinessDayConvention businessdayAdjustmentConvention,
      final Calendar calendar, final DayCount accrualDayCount, final DayCount curveDayCount) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(stepinDate, "stepinDate");
    ArgumentChecker.notNull(valueDate, "valueDate");
    ArgumentChecker.notNull(startDate, "startDate");
    ArgumentChecker.notNull(endDate, "endDate");
    ArgumentChecker.notNull(paymentInterval, "tenor");
    ArgumentChecker.notNull(stubType, "stubType");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "businessdayAdjustmentConvention");
    ArgumentChecker.notNull(accrualDayCount, "accuralDayCount");
    ArgumentChecker.notNull(curveDayCount, "curveDayCount");
    ArgumentChecker.isInRangeInclusive(0, 1, recoveryRate);
    ArgumentChecker.isFalse(valueDate.isBefore(tradeDate), "Require valueDate >= today");
    ArgumentChecker.isFalse(stepinDate.isBefore(tradeDate), "Require stepin >= today");
    ArgumentChecker.isFalse(tradeDate.isAfter(endDate), "CDS has expired");

    _payAccOnDefault = payAccOnDefault;
    _protectionFromStartOfDay = protectStart;

    final LocalDate temp = stepinDate.isAfter(startDate) ? stepinDate : startDate;
    final LocalDate effectiveStartDate = protectStart ? temp.minusDays(1) : temp;

    _stepin = curveDayCount.getDayCountFraction(tradeDate, stepinDate, calendar);
    _valuationTime = curveDayCount.getDayCountFraction(tradeDate, valueDate, calendar);
    _protectionStart = curveDayCount.getDayCountFraction(tradeDate, effectiveStartDate, calendar);
    _protectionEnd = curveDayCount.getDayCountFraction(tradeDate, endDate, calendar);

    _lgd = 1 - recoveryRate;

    final ISDAPremiumLegSchedule fullPaymentSchedule = new ISDAPremiumLegSchedule(startDate, endDate, paymentInterval, stubType, businessdayAdjustmentConvention, calendar, protectStart);
    final ISDAPremiumLegSchedule paymentSchedule = ISDAPremiumLegSchedule.truncateSchedule(stepinDate, fullPaymentSchedule);

    _nPayments = paymentSchedule.getNumPayments();
    _paymentTimes = new double[_nPayments];
    _accStart = new double[_nPayments];
    _accEnd = new double[_nPayments];
    _accFractions = new double[_nPayments];

    for (int i = 0; i < _nPayments; i++) {
      final LocalDate paymentDate = paymentSchedule.getPaymentDate(i);
      _paymentTimes[i] = curveDayCount.getDayCountFraction(tradeDate, paymentDate, calendar);
      final LocalDate accStart = paymentSchedule.getAccStartDate(i);
      final LocalDate accEnd = paymentSchedule.getAccEndDate(i);
      _accFractions[i] = accrualDayCount.getDayCountFraction(accStart, accEnd, calendar);
      _accStart[i] = accStart.isBefore(tradeDate) ? -curveDayCount.getDayCountFraction(accStart, tradeDate, calendar) : curveDayCount.getDayCountFraction(tradeDate, accStart, calendar);
      _accEnd[i] = curveDayCount.getDayCountFraction(tradeDate, accEnd, calendar);
    }
    final LocalDate accStart = paymentSchedule.getAccStartDate(0);

    final long firstJulianDate = accStart.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long secondJulianDate = stepinDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    _accruedDays = secondJulianDate > firstJulianDate ? (int) (secondJulianDate - firstJulianDate) : 0;
    _accrued = accStart.isBefore(stepinDate) ? accrualDayCount.getDayCountFraction(accStart, stepinDate, calendar) : 0.0;
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

  /**
   * Gets the accrual fraction for a particular payment period
   * @param index the index of the payment period
   * @return the accFraction
   */
  public double getAccrualFraction(final int index) {
    return _accFractions[index];
  }

  /**
   * Gets the accrued premium per unit of (fractional) spread - i.e. if the quoted spread (coupon)  was 500bps the actual
   * accrued premium paid would be this times 0.05
   * @return the accrued premium per unit of (fractional) spread (and unit of notional)
   */
  public double getAccruedPremiumPerUnitSpread() {
    return _accrued;
  }

  /**
   * Gets the accrued premium per unit of notional
   * @param fractionalSpread The <b>fraction</b> spread
   * @return the accrued premium
   */
  public double getAccruedPremium(final double fractionalSpread) {
    return _accrued * fractionalSpread;
  }

  /**
   * Get the number of days of accrued premium.
   * @return Accrued days
   */
  public int getAccuredDays() {
    return _accruedDays;
  }

  private CDSAnalytic(final double lgd, final int nPayments, final double[] paymentTimes, final double[] accFractions, final double[] accStart, final double[] accEnd, final double stepin,
      final double protectionStart, final double protectionEnd, final double valuationTime, final boolean payAccOnDefault, final boolean protectionFromStartOfDay, final double accrued,
      final int accruedDays) {
    _lgd = lgd;
    _nPayments = nPayments;
    _paymentTimes = paymentTimes;
    _accFractions = accFractions;
    _accStart = accStart;
    _accEnd = accEnd;
    _stepin = stepin;
    _protectionStart = protectionStart;
    _protectionEnd = protectionEnd;
    _valuationTime = valuationTime;
    _payAccOnDefault = payAccOnDefault;
    _protectionFromStartOfDay = protectionFromStartOfDay;
    _accrued = accrued;
    _accruedDays = accruedDays;
  }

  public CDSAnalytic withRecoveryRate(final double recoveryRate) {
    ArgumentChecker.isInRangeInclusive(0, 1, recoveryRate);
    return new CDSAnalytic(1 - recoveryRate, _nPayments, _paymentTimes, _accFractions, _accStart, _accEnd, _stepin, _protectionStart, _protectionEnd, _valuationTime, _payAccOnDefault,
        _protectionFromStartOfDay, _accrued, _accruedDays);
  }

}

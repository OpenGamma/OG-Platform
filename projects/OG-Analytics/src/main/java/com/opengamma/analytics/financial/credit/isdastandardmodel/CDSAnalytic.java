/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.CDSCoupon.makeCoupons;

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
 * This converts and stores all the date logic as doubles for CDS pricing on a particular date.<p>
 * For convenient ways to generate sets of CDSs
 * @see CDSAnalyticFactory
 */
public class CDSAnalytic {
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  /** Curve daycount generally fixed to Act/365 in ISDA */
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  private final double _lgd;

  private final CDSCoupon[] _coupons;

  private final double _effectiveProtectionStart;
  //  private final double _protectionStart;
  private final double _protectionEnd;
  private final double _valuationTime;
  private final boolean _payAccOnDefault;
  private final boolean _protectionFromStartOfDay;
  private final double _start;
  private final double _accrued;
  private final int _accruedDays;

  /**
   * Generates an analytic description of a CDS trade on a particular date. This can then be passed to a analytic CDS pricer.<br>
   * This using a weekend only calendar with a following convention. ACT/360 is used for accrual and  ACT/365 to convert
   * payment dates to year-fractions (doubles)
   * @param tradeDate The trade date 
     * @param stepinDate (aka Protection Effective sate or assignment date). Date when party assumes ownership. This is usually T+1. This is when protection
   * (and risk) starts in terms of the model. Note, this is sometimes just called the Effective Date, however this can cause
   * confusion with the legal effective date which is T-60 or T-90.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param accStartDate This is when the CDS nominally starts in terms of premium payments.  i.e. the number of days in the first 
   * period (and thus the amount of the first premium payment) is counted from this date.
   * @param endDate (aka maturity date) This is when the contract expires and protection ends - any default after this date does not
   *  trigger a payment. (the protection ends at end of day)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param paymentInterval The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart  If protectStart = true, then protections starts at the beginning of the day, otherwise it is at the end.
   * @param recoveryRate The recovery rate
   */
  public CDSAnalytic(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate accStartDate, final LocalDate endDate, final boolean payAccOnDefault,
      final Period paymentInterval, final StubType stubType, final boolean protectStart, final double recoveryRate) {
    this(tradeDate, stepinDate, valueDate, accStartDate, endDate, payAccOnDefault, paymentInterval, stubType, protectStart, recoveryRate, FOLLOWING, DEFAULT_CALENDAR, ACT_360, ACT_365);
  }

  /**
   * Generates an analytic description of a CDS trade on a particular date. This can then be passed to a analytic CDS pricer.<br>
   * This using a weekend only calendar with a following convention. ACT/360 is used for accrual and  ACT/365 to convert
   * payment dates to year-fractions (doubles)
   *
   * Note this uses a curve daycount of ACT/365 to match the ISDA methodology.
   *
   * @param tradeDate The trade date
   * @param stepinDate (aka Protection Effective sate or assignment date). Date when party assumes ownership. This is usually T+1. This is when protection
   * (and risk) starts in terms of the model. Note, this is sometimes just called the Effective Date, however this can cause
   * confusion with the legal effective date which is T-60 or T-90.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param accStartDate This is when the CDS nominally starts in terms of premium payments.  i.e. the number of days in the first 
   * period (and thus the amount of the first premium payment) is counted from this date.
   * @param endDate (aka maturity date) This is when the contract expires and protection ends - any default after this date does not
   *  trigger a payment. (the protection ends at end of day)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param paymentInterval The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart  If protectStart = true, then protections starts at the beginning of the day, otherwise it is at the end.
   * @param recoveryRate The recovery rate
   * @param businessdayAdjustmentConvention How are adjustments for non-business days made
   * @param calendar Calendar defining what is a non-business day
   * @param accrualDayCount Day count used for accrual
   */
  public CDSAnalytic(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate accStartDate, final LocalDate endDate, final boolean payAccOnDefault,
      final Period paymentInterval, final StubType stubType, final boolean protectStart, final double recoveryRate, final BusinessDayConvention businessdayAdjustmentConvention,
      final Calendar calendar, final DayCount accrualDayCount) {
    this(tradeDate, stepinDate, valueDate, accStartDate, endDate, payAccOnDefault, paymentInterval, stubType, protectStart, recoveryRate, businessdayAdjustmentConvention, calendar, accrualDayCount,
        ACT_365);
  }

  /**
   * Generates an analytic description of a CDS trade on a particular date. This can then be passed to a analytic CDS pricer
   * @param tradeDate The trade date
   * @param stepinDate (aka Protection Effective sate or assignment date). Date when party assumes ownership. This is usually T+1. This is when protection
   * (and risk) starts in terms of the model. Note, this is sometimes just called the Effective Date, however this can cause
   * confusion with the legal effective date which is T-60 or T-90.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param accStartDate This is when the CDS nominally starts in terms of premium payments.  i.e. the number of days in the first 
   * period (and thus the amount of the first premium payment) is counted from this date.
   * @param endDate (aka maturity date) This is when the contract expires and protection ends - any default after this date does not
   *  trigger a payment. (the protection ends at end of day)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param paymentInterval The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param isProtectStart  If protectStart = true, then protections starts at the beginning of the day, otherwise it is at the end.
   * @param recoveryRate The recovery rate
   * @param businessdayAdjustmentConvention How are adjustments for non-business days made
   * @param calendar Calendar defining what is a non-business day
   * @param accrualDayCount Day count used for accrual
   * @param curveDayCount Day count used on curve (NOTE ISDA uses ACT/365 (fixed) and it is not recommended to change this)
   */
  public CDSAnalytic(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate accStartDate, final LocalDate endDate, final boolean payAccOnDefault,
      final Period paymentInterval, final StubType stubType, final boolean isProtectStart, final double recoveryRate, final BusinessDayConvention businessdayAdjustmentConvention,
      final Calendar calendar, final DayCount accrualDayCount, final DayCount curveDayCount) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(stepinDate, "stepinDate");
    ArgumentChecker.notNull(valueDate, "valueDate");
    ArgumentChecker.notNull(accStartDate, "accStartDate");
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
    _protectionFromStartOfDay = isProtectStart;

    final LocalDate startDate = stepinDate.isAfter(accStartDate) ? stepinDate : accStartDate;
    final LocalDate effectiveStartDate = isProtectStart ? startDate.minusDays(1) : startDate;

    _start = accStartDate.isBefore(tradeDate) ? -curveDayCount.getDayCountFraction(accStartDate, tradeDate) : curveDayCount.getDayCountFraction(tradeDate, accStartDate);
    _valuationTime = curveDayCount.getDayCountFraction(tradeDate, valueDate);
    //   _protectionStart = curveDayCount.getDayCountFraction(tradeDate, startDate);
    _effectiveProtectionStart = curveDayCount.getDayCountFraction(tradeDate, effectiveStartDate);
    _protectionEnd = curveDayCount.getDayCountFraction(tradeDate, endDate);
    _lgd = 1 - recoveryRate;

    final ISDAPremiumLegSchedule fullPaymentSchedule = new ISDAPremiumLegSchedule(accStartDate, endDate, paymentInterval, stubType, businessdayAdjustmentConvention, calendar, isProtectStart);
    final ISDAPremiumLegSchedule paymentSchedule = ISDAPremiumLegSchedule.truncateSchedule(stepinDate, fullPaymentSchedule);
    _coupons = makeCoupons(tradeDate, paymentSchedule, isProtectStart, accrualDayCount, curveDayCount);

    final LocalDate accStart = paymentSchedule.getAccStartDate(0);
    final long firstJulianDate = accStart.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long secondJulianDate = stepinDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    _accruedDays = secondJulianDate > firstJulianDate ? (int) (secondJulianDate - firstJulianDate) : 0;
    _accrued = accStart.isBefore(stepinDate) ? accrualDayCount.getDayCountFraction(accStart, stepinDate) : 0.0;
  }

  public int getNumPayments() {
    return _coupons.length;
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

  public double getLGD() {
    return _lgd;
  }

  //  /**
  //   * Gets the stepin.
  //   * @return the stepin
  //   */
  //  public double getAStepin() {
  //    return _stepin;
  //  }

  /**
   * Gets the valuationTime.
   * @return the valuationTime
   */
  public double getValuationTime() {
    return _valuationTime;
  }

  public double getStart() {
    return _start;
  }

  //  /**
  //   * Gets the protection Start
  //   * @return the effectiveProtectionStart
  //   */
  //  public double getProtectionStart() {
  //    return _protectionStart;
  //  }

  /**
   * Gets the effective protection Start - i.e. adjusted back one day if protection is from start of day.
   * @return the effectiveProtectionStart
   */
  public double getEffectiveProtectionStart() {
    return _effectiveProtectionStart;
  }

  /**
   * Gets the protectionEnd.
   * @return the protectionEnd
   */
  public double getProtectionEnd() {
    return _protectionEnd;
  }

  public CDSCoupon[] getCoupons() {
    return _coupons;
  }

  public CDSCoupon getCoupon(final int index) {
    return _coupons[index];
  }

  /**
   * Gets the accStart for a particular payment period
   * @param index the index of the payment period
   * @return the accStart
   */
  public double getEffectiveAccStart(final int index) {
    return _coupons[index].getEffStart();
  }

  /**
   * Gets the accEnd for a particular payment period
   * @param index the index of the payment period
   * @return the accEnd
   */
  public double getEffectiveAccEnd(final int index) {
    return _coupons[index].getEffEnd();
  }

  /**
   * Gets the payment time for a particular payment period
   * @param index the index of the payment period
   * @return the paymentTime
   */
  public double getPaymentTime(final int index) {
    return _coupons[index].getPaymentTime();
  }

  /**
   * Gets the accrual fraction for a particular payment period
   * @param index the index of the payment period
   * @return the accFraction
   */
  public double getAccrualFraction(final int index) {
    return _coupons[index].getYearFrac();
  }

  public double getAccRatio(final int index) {
    return _coupons[index].getYFRatio();
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

  private CDSAnalytic(final double lgd, final CDSCoupon[] coupons, final double start, final double effectiveProtectionStart, /*final double protectionStart, */final double protectionEnd,
      final double valuationTime, final boolean payAccOnDefault, final boolean protectionFromStartOfDay, final double accrued, final int accruedDays) {
    ArgumentChecker.noNulls(coupons, "coupons");
    _lgd = lgd;
    _coupons = coupons;
    _start = start;
    _effectiveProtectionStart = effectiveProtectionStart;
    //   _protectionStart = protectionStart;
    _protectionEnd = protectionEnd;
    _valuationTime = valuationTime;
    _payAccOnDefault = payAccOnDefault;
    _protectionFromStartOfDay = protectionFromStartOfDay;
    _accrued = accrued;
    _accruedDays = accruedDays;
  }

  public CDSAnalytic withRecoveryRate(final double recoveryRate) {
    ArgumentChecker.isInRangeInclusive(0, 1, recoveryRate);
    return new CDSAnalytic(1 - recoveryRate, _coupons, _start, _effectiveProtectionStart, /*_protectionStart,*/_protectionEnd, _valuationTime, _payAccOnDefault, _protectionFromStartOfDay, _accrued,
        _accruedDays);
  }
}

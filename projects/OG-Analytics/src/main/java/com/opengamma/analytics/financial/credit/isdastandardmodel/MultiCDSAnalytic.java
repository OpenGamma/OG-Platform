/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.temporal.JulianFields;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class MultiCDSAnalytic {

  private final double _lgd;

  private final double _accStart;
  private final double _effProtectionStart;
  private final double _valuationTime;
  private final boolean _payAccOnDefault;
  private final boolean _protectionFromStartOfDay;

  private final int _totalPayments;
  private final CDSCoupon[] _standardCoupons; //these will be common across many CDSs

  private final int _nMaturities;
  private final int[] _matIndexToPayments;
  private final CDSCoupon[] _terminalCoupons; //these are the final coupons for each CDS 
  private final double[] _protectionEnd;
  private final double[] _accrued;
  private final int[] _accruedDays;
  private final double[] _couponAmts;

  /**
   * Set up a strip of increasing maturity CDSs that have some coupons in common.  The trade date, step-in date and valuation date and
   * accrual start date are all common, as is the payment frequency. The maturities are expressed as integer multiples of the
   * payment interval from a reference date (the next IMM date after the trade date for standard CDSs) - this guarantees that premiums 
   * will be the same across several CDSs.
   * @param tradeDate The trade date
   * @param stepinDate (aka Protection Effective sate or assignment date). Date when party assumes ownership. This is usually T+1. This is when protection
   * (and risk) starts in terms of the model. Note, this is sometimes just called the Effective Date, however this can cause
   * confusion with the legal effective date which is T-60 or T-90.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param accStartDate  Accrual Start Date. This is when the CDS nominally starts in terms of premium payments.  i.e. the number 
   * of days in the first period (and thus the amount of the first premium payment) is counted from this date.
   * @param maturityReferanceDate A reference date that maturities are measured from. For standard CDSSs, this is the next IMM  date after
   * the trade date, so the actually maturities will be some fixed periods after this.  
   * @param maturityIndexes The maturities are fixed integer multiples of the payment interval, so for 6M, 1Y and 2Y tenors with a 3M 
   * payment interval, would require 2, 4, and 8 as the indices    
   * @param coupons The fractional coupons of the individual CDSs 
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param paymentInterval The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart If protectStart = true, then protections starts at the beginning of the day, otherwise it is at the end.
   * @param recoveryRate The recovery rate
   * @param businessdayAdjustmentConvention How are adjustments for non-business days made
   * @param calendar Calendar defining what is a non-business day
   * @param accrualDayCount Day count used for accrual
   * @param curveDayCount Day count used on curve (NOTE ISDA uses ACT/365 and it is not recommended to change this)
   */
  public MultiCDSAnalytic(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate accStartDate, final LocalDate maturityReferanceDate,
      final int[] maturityIndexes, final double[] couponAmts, final boolean payAccOnDefault, final Tenor paymentInterval, final StubType stubType, final boolean protectStart,
      final double recoveryRate, final BusinessDayConvention businessdayAdjustmentConvention, final Calendar calendar, final DayCount accrualDayCount, final DayCount curveDayCount) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(stepinDate, "stepinDate");
    ArgumentChecker.notNull(valueDate, "valueDate");
    ArgumentChecker.notNull(accStartDate, "accStartDate");
    ArgumentChecker.notNull(maturityReferanceDate, "maturityReferanceDate");
    ArgumentChecker.notNull(paymentInterval, "tenor");
    ArgumentChecker.notNull(stubType, "stubType");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "businessdayAdjustmentConvention");
    ArgumentChecker.notNull(accrualDayCount, "accuralDayCount");
    ArgumentChecker.notNull(curveDayCount, "curveDayCount");
    ArgumentChecker.isFalse(valueDate.isBefore(tradeDate), "Require valueDate >= today");
    ArgumentChecker.isFalse(stepinDate.isBefore(tradeDate), "Require stepin >= today");
    //  ArgumentChecker.isFalse(tradeDate.isAfter(maturityReferanceDate), "First CDS has expired");
    ArgumentChecker.notEmpty(maturityIndexes, "maturityIndexes");
    ArgumentChecker.notEmpty(couponAmts, "couponAmts");

    _nMaturities = maturityIndexes.length;
    ArgumentChecker.isTrue(_nMaturities == couponAmts.length, "There are {} maturities but {} coupons", _nMaturities, couponAmts.length);
    ArgumentChecker.isTrue(maturityIndexes[0] >= 0, "first maturity index < 0");
    for (int i = 1; i < _nMaturities; i++) {
      ArgumentChecker.isTrue(maturityIndexes[i] > maturityIndexes[i - 1], "maturityIndexes not ascending");
    }
    _couponAmts = new double[_nMaturities];
    System.arraycopy(couponAmts, 0, _couponAmts, 0, _nMaturities);

    _payAccOnDefault = payAccOnDefault;
    _protectionFromStartOfDay = protectStart;

    _accStart = accStartDate.isBefore(tradeDate) ? -curveDayCount.getDayCountFraction(accStartDate, tradeDate) : curveDayCount.getDayCountFraction(tradeDate, accStartDate);
    final LocalDate temp = stepinDate.isAfter(accStartDate) ? stepinDate : accStartDate;
    final LocalDate effectiveStartDate = protectStart ? temp.minusDays(1) : temp;

    _valuationTime = curveDayCount.getDayCountFraction(tradeDate, valueDate);
    _effProtectionStart = curveDayCount.getDayCountFraction(tradeDate, effectiveStartDate);
    _lgd = 1 - recoveryRate;

    final LocalDate[] maturities = new LocalDate[_nMaturities];
    _protectionEnd = new double[_nMaturities];
    final Period period = paymentInterval.getPeriod();
    for (int i = 0; i < _nMaturities; i++) {
      final Period tStep = period.multipliedBy(maturityIndexes[i]);
      maturities[i] = maturityReferanceDate.plus(tStep);
      _protectionEnd[i] = curveDayCount.getDayCountFraction(tradeDate, maturities[i]);
    }

    final ISDAPremiumLegSchedule fullPaymentSchedule = new ISDAPremiumLegSchedule(accStartDate, maturities[_nMaturities - 1], period, stubType, businessdayAdjustmentConvention, calendar, protectStart);
    //remove already expired coupons
    final ISDAPremiumLegSchedule paymentSchedule = fullPaymentSchedule.truncateSchedule(stepinDate);
    final int couponOffset = fullPaymentSchedule.getNumPayments() - paymentSchedule.getNumPayments();

    _totalPayments = paymentSchedule.getNumPayments();
    _standardCoupons = new CDSCoupon[_totalPayments - 1];
    for (int i = 0; i < (_totalPayments - 1); i++) { //The last coupon is actually a terminal coupon, so not included here
      _standardCoupons[i] = new CDSCoupon(tradeDate, paymentSchedule.getAccPaymentDateTriplet(i), protectStart, accrualDayCount, curveDayCount);
    }

    //find the terminal coupons 
    _terminalCoupons = new CDSCoupon[_nMaturities];
    _matIndexToPayments = new int[_nMaturities];
    _accruedDays = new int[_nMaturities];
    _accrued = new double[_nMaturities];
    final long secondJulianDate = stepinDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    for (int i = 0; i < _nMaturities; i++) {
      final int index = fullPaymentSchedule.getNominalPaymentDateIndex(maturities[i]);
      if (index < 0) {
        throw new OpenGammaRuntimeException("should never see this. There is a bug in the code.");
      }
      //maturity is unadjusted, but if protectionStart=true (i.e. standard CDS) there is effectively an extra day of accrued interest
      final LocalDate accEnd = protectStart ? maturities[i].plusDays(1) : maturities[i];
      _terminalCoupons[i] = new CDSCoupon(tradeDate, fullPaymentSchedule.getAccStartDate(index), accEnd, fullPaymentSchedule.getPaymentDate(index), protectStart, accrualDayCount, curveDayCount);
      _matIndexToPayments[i] = index - couponOffset;
      //This will only matter for the edge case when the trade date is 1 day before maturity      
      final LocalDate tDate2 = _matIndexToPayments[i] < 0 ? fullPaymentSchedule.getAccStartDate(couponOffset - 1) : paymentSchedule.getAccStartDate(0);
      //final LocalDate tDate2 = i == 0 && (stepinDate.isEqual(paymentSchedule.getAccStartDate(0))) ? fullPaymentSchedule.getAccStartDate(couponOffset - 1) : paymentSchedule.getAccStartDate(0);
      final long firstJulianDate = tDate2.getLong(JulianFields.MODIFIED_JULIAN_DAY);
      _accruedDays[i] = secondJulianDate > firstJulianDate ? (int) (secondJulianDate - firstJulianDate) : 0;
      _accrued[i] = tDate2.isBefore(stepinDate) ? accrualDayCount.getDayCountFraction(tDate2, stepinDate) : 0.0;
    }
  }

  public int getNumMaturities() {
    return _nMaturities;
  }

  /**
   * This is the number of payments for the largest maturity CDS 
   * @return totalPayments 
   */
  public int getTotalPayments() {
    return _totalPayments;
  }

  /**
   * get payment index for a particular maturity index
   * @param matIndex maturity index (0 for first maturity, etc)
   * @return payment index 
   */
  public int getPaymentIndexForMaturity(final int matIndex) {
    return _matIndexToPayments[matIndex];
  }

  public double getCoupon(final int matIndex) {
    return _couponAmts[matIndex];
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
  //   */
  //  public double getCurveOneDay() {
  //    return _curveOneDay;
  //  }

  public double getLGD() {
    return _lgd;
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
  public double getEffectiveProtectionStart() {
    return _effProtectionStart;
  }

  public double getAccStart() {
    return _accStart;
  }

  /**
   * Gets the protectionEnd.
   * @return the protectionEnd
   */
  public double getProtectionEnd(final int matIndex) {
    return _protectionEnd[matIndex];
  }

  public CDSCoupon getTerminalCoupon(final int matIndex) {
    return _terminalCoupons[matIndex];
  }

  public CDSCoupon getStandardCoupon(final int index) {
    return _standardCoupons[index];
  }

  /**
   * Gets the accrued premium per unit of (fractional) spread - i.e. if the quoted spread (coupon)  was 500bps the actual
   * accrued premium paid would be this times 0.05
   * @return the accrued premium per unit of (fractional) spread (and unit of notional)
   */
  public double getAccruedPremiumPerUnitSpread(final int matIndex) {
    return _accrued[matIndex];
  }

  /**
   * Gets the accrued premium per unit of notional
   * @param fractionalSpread The <b>fraction</b> spread
   * @return the accrued premium
   */
  public double getAccruedPremium(final int matIndex, final double fractionalSpread) {
    return _accrued[matIndex] * fractionalSpread;
  }

  /**
   * Get the number of days of accrued premium.
   * @return Accrued days
   */
  public int getAccuredDays(final int matIndex) {
    return _accruedDays[matIndex];
  }

}

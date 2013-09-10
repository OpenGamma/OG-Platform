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
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class MultiCDSAnalytic {

  private final double _lgd;
  private final int _totalPayments;
  private final int _nMaturities;
  private final int[] _matIndexToPayments;
  private final double[] _creditObsTimes;
  private final double[] _paymentTimes;
  private final double[] _accFractions;
  private final double[] _accStart;
  private final double[] _accEnd;

  private final double _stepin;
  private final double _protectionStart;
  private final double[] _protectionEnd;
  private final double _valuationTime;
  private final boolean _payAccOnDefault;
  private final boolean _protectionFromStartOfDay;
  private final double _accrued;
  private final int _accruedDays;

  private final double[] _matAccEnd;
  private final double[] _matCreditObsTimes;
  private final double[] _matAccFractions;
  private final double[] _coupons;

  private final double _curveOneDay = 1. / 365; // TODO do not hard code

  public MultiCDSAnalytic(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate maturityReferanceDate,
      final int[] maturityIndexes, final double[] coupons, final boolean payAccOnDefault, final Tenor paymentInterval, final StubType stubType, final boolean protectStart, final double recoveryRate,
      final BusinessDayConvention businessdayAdjustmentConvention, final Calendar calendar, final DayCount accrualDayCount, final DayCount curveDayCount) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(stepinDate, "stepinDate");
    ArgumentChecker.notNull(valueDate, "valueDate");
    ArgumentChecker.notNull(startDate, "startDate");
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
    ArgumentChecker.notEmpty(coupons, "coupons");

    _nMaturities = maturityIndexes.length;
    ArgumentChecker.isTrue(_nMaturities == coupons.length, "There are {} maturities but {} coupons", _nMaturities, coupons.length);
    ArgumentChecker.isTrue(maturityIndexes[0] >= 0, "first maturity index < 0");
    for (int i = 1; i < _nMaturities; i++) {
      ArgumentChecker.isTrue(maturityIndexes[i] > maturityIndexes[i - 1], "maturityIndexes not ascending");
    }
    _coupons = new double[_nMaturities];
    System.arraycopy(coupons, 0, _coupons, 0, _nMaturities);

    _payAccOnDefault = payAccOnDefault;
    _protectionFromStartOfDay = protectStart;

    final LocalDate temp = stepinDate.isAfter(startDate) ? stepinDate : startDate;
    final LocalDate effectiveStartDate = protectStart ? temp.minusDays(1) : temp;

    _stepin = curveDayCount.getDayCountFraction(tradeDate, stepinDate, calendar);
    _valuationTime = curveDayCount.getDayCountFraction(tradeDate, valueDate, calendar);
    _protectionStart = curveDayCount.getDayCountFraction(tradeDate, effectiveStartDate, calendar);
    _lgd = 1 - recoveryRate;

    final LocalDate[] maturities = new LocalDate[_nMaturities];
    _protectionEnd = new double[_nMaturities];
    final Period period = paymentInterval.getPeriod();
    LocalDate tMat = maturityReferanceDate;
    for (int i = 0; i < _nMaturities; i++) {
      final int steps = i == 0 ? maturityIndexes[0] : maturityIndexes[i] - maturityIndexes[i - 1];
      for (int j = 0; j < steps; j++) {
        tMat = tMat.plus(period);
      }
      maturities[i] = tMat;
      _protectionEnd[i] = curveDayCount.getDayCountFraction(tradeDate, maturities[i], calendar);
    }

    final ISDAPremiumLegSchedule fullPaymentSchedule = new ISDAPremiumLegSchedule(startDate, maturities[_nMaturities - 1], period, stubType, businessdayAdjustmentConvention, calendar, protectStart);
    final ISDAPremiumLegSchedule paymentSchedule = fullPaymentSchedule.truncateSchedule(stepinDate);

    _totalPayments = paymentSchedule.getNumPayments();
    _paymentTimes = new double[_totalPayments];
    _creditObsTimes = new double[_totalPayments];
    _accStart = new double[_totalPayments];
    _accEnd = new double[_totalPayments];
    _accFractions = new double[_totalPayments];

    for (int i = 0; i < _totalPayments; i++) {
      final LocalDate paymentDate = paymentSchedule.getPaymentDate(i);
      _paymentTimes[i] = curveDayCount.getDayCountFraction(tradeDate, paymentDate, calendar);
      final LocalDate accStart = paymentSchedule.getAccStartDate(i);
      final LocalDate accEnd = paymentSchedule.getAccEndDate(i);
      final LocalDate obsEnd = protectStart ? accEnd.minusDays(1) : accEnd;
      _accFractions[i] = accrualDayCount.getDayCountFraction(accStart, accEnd, calendar);
      _accStart[i] = accStart.isBefore(tradeDate) ? -curveDayCount.getDayCountFraction(accStart, tradeDate, calendar) : curveDayCount.getDayCountFraction(tradeDate, accStart, calendar);
      _accEnd[i] = curveDayCount.getDayCountFraction(tradeDate, accEnd, calendar);
      _creditObsTimes[i] = curveDayCount.getDayCountFraction(tradeDate, obsEnd, calendar); // TODO this looks odd - check again with ISDA c code
    }
    final LocalDate accStart = paymentSchedule.getAccStartDate(0);

    final long firstJulianDate = accStart.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long secondJulianDate = stepinDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    _accruedDays = secondJulianDate > firstJulianDate ? (int) (secondJulianDate - firstJulianDate) : 0;
    _accrued = accStart.isBefore(stepinDate) ? accrualDayCount.getDayCountFraction(accStart, stepinDate, calendar) : 0.0;

    int index = paymentSchedule.getPaymentDateIndex(maturities[0]);
    if (index < 0) {
      index = -index - 1;
    }
    _matIndexToPayments = new int[_nMaturities];
    final int base = index - maturityIndexes[0];
    for (int matIndex = 0; matIndex < _nMaturities; matIndex++) {
      _matIndexToPayments[matIndex] = base + maturityIndexes[matIndex];
    }

    //Since the final accrual date is not business-day adjusted we must keep a separate list of payment schemes for final
    //periods 
    _matAccEnd = new double[_nMaturities];
    _matAccFractions = new double[_nMaturities];
    _matCreditObsTimes = new double[_nMaturities];
    for (int matIndex = 0; matIndex < _nMaturities; matIndex++) {
      final int paymentIndex = _matIndexToPayments[matIndex];
      final LocalDate accS = paymentSchedule.getAccStartDate(paymentIndex);
      final LocalDate nomPaymentDate = paymentSchedule.getNominalPaymentDate(paymentIndex);
      final LocalDate accEnd = ISDAPremiumLegSchedule.getFinalAccEndDate(nomPaymentDate, protectStart);
      _matAccFractions[matIndex] = accrualDayCount.getDayCountFraction(accS, accEnd, calendar);
      _matAccEnd[matIndex] = curveDayCount.getDayCountFraction(tradeDate, accEnd, calendar);
      final LocalDate obsEnd = protectStart ? accEnd.minusDays(1) : accEnd;
      _matCreditObsTimes[matIndex] = curveDayCount.getDayCountFraction(tradeDate, obsEnd, calendar);
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
    return _coupons[matIndex];
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
  public double getProtectionEnd(final int matIndex) {
    return _protectionEnd[matIndex];
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

  public double getMatAccEnd(final int matIndex) {
    return _matAccEnd[matIndex];
  }

  public double getMatCreditObservationTime(final int matIndex) {
    return _matCreditObsTimes[matIndex];
  }

  public double getMatAccFrac(final int matIndex) {
    return _matAccFractions[matIndex];
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

}

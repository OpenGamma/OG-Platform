/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getPrevIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;

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
 * 
 */
public class CDSAnalyticFactory {

  private static final int DEFAULT_STEPIN = 1;
  private static final int DEFAULT_CASH_SETTLE = 3;
  private static final boolean DEFAULT_PAY_ACC = true;
  private static final Period DEFAULT_COUPON_INT = Period.ofMonths(3);
  private static final StubType DEFAULT_STUB_TYPE = StubType.FRONTSHORT;
  private static final boolean PROT_START = true;
  private static final double DEFAULT_RR = 0.4;
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  /** Curve daycount generally fixed to Act/365 in ISDA */
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  private final int _stepIn;
  private final int _cashSettle;
  private final boolean _payAccOnDefault;
  private final Period _coponInterval;
  private final StubType _stubType;
  private final boolean _protectStart;
  private final double _recoveryRate;
  private final BusinessDayConvention _businessdayAdjustmentConvention;
  private final Calendar _calendar;
  private final DayCount _accrualDayCount;
  private final DayCount _curveDayCount;

  public CDSAnalyticFactory() {
    _stepIn = DEFAULT_STEPIN;
    _cashSettle = DEFAULT_CASH_SETTLE;
    _payAccOnDefault = DEFAULT_PAY_ACC;
    _coponInterval = DEFAULT_COUPON_INT;
    _stubType = DEFAULT_STUB_TYPE;
    _protectStart = PROT_START;
    _recoveryRate = DEFAULT_RR;
    _businessdayAdjustmentConvention = FOLLOWING;
    _calendar = DEFAULT_CALENDAR;
    _accrualDayCount = ACT_360;
    _curveDayCount = ACT_365;
  }

  public CDSAnalyticFactory(final double recoveryRate) {
    _stepIn = DEFAULT_STEPIN;
    _cashSettle = DEFAULT_CASH_SETTLE;
    _payAccOnDefault = DEFAULT_PAY_ACC;
    _coponInterval = DEFAULT_COUPON_INT;
    _stubType = DEFAULT_STUB_TYPE;
    _protectStart = PROT_START;
    _recoveryRate = recoveryRate;
    _businessdayAdjustmentConvention = FOLLOWING;
    _calendar = DEFAULT_CALENDAR;
    _accrualDayCount = ACT_360;
    _curveDayCount = ACT_365;
  }

  public CDSAnalyticFactory(final Period couponInterval) {
    ArgumentChecker.notNull(couponInterval, "couponInterval");
    _stepIn = DEFAULT_STEPIN;
    _cashSettle = DEFAULT_CASH_SETTLE;
    _payAccOnDefault = DEFAULT_PAY_ACC;
    _coponInterval = couponInterval;
    _stubType = DEFAULT_STUB_TYPE;
    _protectStart = PROT_START;
    _recoveryRate = DEFAULT_RR;
    _businessdayAdjustmentConvention = FOLLOWING;
    _calendar = DEFAULT_CALENDAR;
    _accrualDayCount = ACT_360;
    _curveDayCount = ACT_365;
  }

  public CDSAnalyticFactory(final double recoveryRate, final Period couponInterval) {
    ArgumentChecker.notNull(couponInterval, "couponInterval");
    _stepIn = DEFAULT_STEPIN;
    _cashSettle = DEFAULT_CASH_SETTLE;
    _payAccOnDefault = DEFAULT_PAY_ACC;
    _coponInterval = couponInterval;
    _stubType = DEFAULT_STUB_TYPE;
    _protectStart = PROT_START;
    _recoveryRate = recoveryRate;
    _businessdayAdjustmentConvention = FOLLOWING;
    _calendar = DEFAULT_CALENDAR;
    _accrualDayCount = ACT_360;
    _curveDayCount = ACT_365;
  }

  public CDSAnalyticFactory(final CDSAnalyticFactory other) {
    ArgumentChecker.notNull(other, "other");
    _stepIn = other._stepIn;
    _cashSettle = other._cashSettle;
    _payAccOnDefault = other._payAccOnDefault;
    _coponInterval = other._coponInterval;
    _stubType = other._stubType;
    _protectStart = other._protectStart;
    _recoveryRate = other._recoveryRate;
    _businessdayAdjustmentConvention = other._businessdayAdjustmentConvention;
    _calendar = other._calendar;
    _accrualDayCount = other._accrualDayCount;
    _curveDayCount = other._curveDayCount;
  }

  protected CDSAnalyticFactory(final int stepIn, final int cashSettle, final boolean payAccOnDefault, final Period couponInterval, final StubType stubType, final boolean protectStart,
      final double recoveryRate, final BusinessDayConvention businessdayAdjustmentConvention, final Calendar calendar, final DayCount accrualDayCount, final DayCount curveDayCount) {
    _stepIn = stepIn;
    _cashSettle = cashSettle;
    _payAccOnDefault = payAccOnDefault;
    _coponInterval = couponInterval;
    _stubType = stubType;
    _protectStart = protectStart;
    _recoveryRate = recoveryRate;
    _businessdayAdjustmentConvention = businessdayAdjustmentConvention;
    _calendar = calendar;
    _accrualDayCount = accrualDayCount;
    _curveDayCount = curveDayCount;
  }

  public CDSAnalyticFactory withStepIn(final int stepIn) {
    return new CDSAnalyticFactory(stepIn, _cashSettle, _payAccOnDefault, _coponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  public CDSAnalyticFactory withCashSettle(final int cashSettle) {
    return new CDSAnalyticFactory(_stepIn, cashSettle, _payAccOnDefault, _coponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  public CDSAnalyticFactory withPayAccOnDefault(final boolean payAcc) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, payAcc, _coponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount, _curveDayCount);
  }

  public CDSAnalyticFactory with(final Period couponInterval) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, couponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  public CDSAnalyticFactory with(final StubType stubType) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _coponInterval, stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  public CDSAnalyticFactory withProtectionStart(final boolean protectionStart) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _coponInterval, _stubType, protectionStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  public CDSAnalyticFactory withRecoveryRate(final double recovery) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _coponInterval, _stubType, _protectStart, recovery, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  public CDSAnalyticFactory with(final BusinessDayConvention busDay) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _coponInterval, _stubType, _protectStart, _recoveryRate, busDay, _calendar, _accrualDayCount, _curveDayCount);
  }

  public CDSAnalyticFactory with(final Calendar calendar) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _coponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, calendar, _accrualDayCount,
        _curveDayCount);
  }

  public CDSAnalyticFactory withAccualDCC(final DayCount accDCC) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _coponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, accDCC, _curveDayCount);
  }

  public CDSAnalyticFactory withCurveDCC(final DayCount curveDCC) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _coponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        curveDCC);
  }

  public CDSAnalytic makeIMMCDS(final LocalDate tradeDate, final Period tenor) {
    return makeIMMCDS(tradeDate, tenor, true);
  }

  public CDSAnalytic makeIMMCDS(final LocalDate tradeDate, final Period tenor, final boolean makeEffBusDay) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(tenor, "tenor");
    final LocalDate effectiveDate = makeEffBusDay ? _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(tradeDate)) : getPrevIMMDate(tradeDate);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final LocalDate maturity = nextIMM.plus(tenor);
    return makeCDS(tradeDate, effectiveDate, maturity);
  }

  public CDSAnalytic[] makeIMMCDS(final LocalDate tradeDate, final Period[] tenors) {
    return makeIMMCDS(tradeDate, tenors, true);
  }

  public CDSAnalytic[] makeIMMCDS(final LocalDate tradeDate, final Period[] tenors, final boolean makeEffBusDay) {
    final LocalDate effectiveDate = makeEffBusDay ? _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(tradeDate)) : getPrevIMMDate(tradeDate);
    return makeIMMCDS(tradeDate, effectiveDate, tenors);
  }

  public CDSAnalytic[] makeIMMCDS(final LocalDate tradeDate, final LocalDate effectiveDate, final Period[] tenors) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(effectiveDate, "effectiveDate");
    ArgumentChecker.noNulls(tenors, "tenors");
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final LocalDate[] maturities = getIMMDateSet(nextIMM, tenors);
    return makeCDS(tradeDate, effectiveDate, maturities);
  }

  public CDSAnalytic makeCDS(final LocalDate tradeDate, final LocalDate effectiveDate, final LocalDate maturity) {
    final LocalDate stepinDate = tradeDate.plusDays(_stepIn);
    final LocalDate valueDate = addWorkDays(tradeDate, _cashSettle, _calendar);
    return new CDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, maturity, _payAccOnDefault, _coponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention,
        _calendar, _accrualDayCount, _curveDayCount);
  }

  public CDSAnalytic makeCDS(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate effectiveDate, final LocalDate maturity) {
    return new CDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, maturity, _payAccOnDefault, _coponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention,
        _calendar, _accrualDayCount, _curveDayCount);
  }

  public CDSAnalytic[] makeCDS(final LocalDate tradeDate, final LocalDate effectiveDate, final LocalDate[] maturities) {
    final LocalDate stepinDate = tradeDate.plusDays(_stepIn);
    final LocalDate valueDate = addWorkDays(tradeDate, _cashSettle, _calendar);
    return makeCDSs(tradeDate, stepinDate, valueDate, effectiveDate, maturities);
  }

  public CDSAnalytic[] makeCDSs(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate effectiveDate, final LocalDate[] maturities) {
    ArgumentChecker.noNulls(maturities, "maturities");
    final int n = maturities.length;
    final CDSAnalytic[] cds = new CDSAnalytic[n];
    for (int i = 0; i < n; i++) {
      cds[i] = new CDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, maturities[i], _payAccOnDefault, _coponInterval, _stubType, _protectStart, _recoveryRate,
          _businessdayAdjustmentConvention, _calendar, _accrualDayCount, _curveDayCount);
    }
    return cds;
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIndexRollDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getPrevIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

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
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  /** Curve daycount generally fixed to Act/365 in ISDA */
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  private static final DayCount ACT_360 = DayCounts.ACT_360;

  private final int _stepIn;
  private final int _cashSettle;
  private final boolean _payAccOnDefault;
  private final Period _couponInterval;
  private final Tenor _couponIntervalTenor;
  private final StubType _stubType;
  private final boolean _protectStart;
  private final double _recoveryRate;
  private final BusinessDayConvention _businessdayAdjustmentConvention;
  private final Calendar _calendar;
  private final DayCount _accrualDayCount;
  private final DayCount _curveDayCount;

  /**
   * Produce CDSs with the following default values:<P>
   * Step-in: T+1<br>
   * Cash-Settle: T+3 working days<br>
   * Pay accrual on Default: true<br>
   * CouponInterval: 3M<br>
   * Stub type: front-short<br>
   * Protection from start of day: true<br>
   * Recovery rate: 40%<br>
   * Business-day Adjustment: Following<br>
   * Calendar: weekend only<br>
   * Accrual day count: ACT/360<br>
   * Curve day count: ACT/365 (fixed)<p>
   * These defaults can be overridden using the with methods 
   */
  public CDSAnalyticFactory() {
    _stepIn = DEFAULT_STEPIN;
    _cashSettle = DEFAULT_CASH_SETTLE;
    _payAccOnDefault = DEFAULT_PAY_ACC;
    _couponInterval = DEFAULT_COUPON_INT;
    _stubType = DEFAULT_STUB_TYPE;
    _protectStart = PROT_START;
    _recoveryRate = DEFAULT_RR;
    _businessdayAdjustmentConvention = FOLLOWING;
    _calendar = DEFAULT_CALENDAR;
    _accrualDayCount = ACT_360;
    _curveDayCount = ACT_365;
    _couponIntervalTenor = Tenor.of(_couponInterval);
  }

  /**
  * Produce CDSs with the following default values and a supplied recovery rate:<P>
   * Step-in: T+1<br>
   * Cash-Settle: T+3 working days<br>
   * Pay accrual on Default: true<br>
   * CouponInterval: 3M<br>
   * Stub type: front-short<br>
   * Protection from start of day: true<br>
   * Business-day Adjustment: Following<br>
   * Calendar: weekend only<br>
   * Accrual day count: ACT/360<br>
   * Curve day count: ACT/365 (fixed)
   * @param recoveryRate The recovery rate
   */
  public CDSAnalyticFactory(final double recoveryRate) {
    _stepIn = DEFAULT_STEPIN;
    _cashSettle = DEFAULT_CASH_SETTLE;
    _payAccOnDefault = DEFAULT_PAY_ACC;
    _couponInterval = DEFAULT_COUPON_INT;
    _stubType = DEFAULT_STUB_TYPE;
    _protectStart = PROT_START;
    _recoveryRate = recoveryRate;
    _businessdayAdjustmentConvention = FOLLOWING;
    _calendar = DEFAULT_CALENDAR;
    _accrualDayCount = ACT_360;
    _curveDayCount = ACT_365;
    _couponIntervalTenor = Tenor.of(_couponInterval);
  }

  /**
  * Produce CDSs with the following default values and a supplied coupon interval:<P> 
   * Step-in: T+1<br>
   * Cash-Settle: T+3 working days<br>
   * Pay accrual on Default: true<br>
   * Stub type: front-short<br>
   * Protection from start of day: true<br>
   * Recovery rate: 40%<br>
   * Business-day Adjustment: Following<br>
   * Calendar: weekend only<br>
   * Accrual day count: ACT/360<br>
   * Curve day count: ACT/365 (fixed) 
   * @param couponInterval The coupon interval
   */
  public CDSAnalyticFactory(final Period couponInterval) {
    ArgumentChecker.notNull(couponInterval, "couponInterval");
    _stepIn = DEFAULT_STEPIN;
    _cashSettle = DEFAULT_CASH_SETTLE;
    _payAccOnDefault = DEFAULT_PAY_ACC;
    _couponInterval = couponInterval;
    _stubType = DEFAULT_STUB_TYPE;
    _protectStart = PROT_START;
    _recoveryRate = DEFAULT_RR;
    _businessdayAdjustmentConvention = FOLLOWING;
    _calendar = DEFAULT_CALENDAR;
    _accrualDayCount = ACT_360;
    _curveDayCount = ACT_365;
    _couponIntervalTenor = Tenor.of(_couponInterval);
  }

  /**
  * Produce CDSs with the following default values and a supplied recovery rate and coupon interval:<P> 
   * Step-in: T+1<br>
   * Cash-Settle: T+3 working days<br>
   * Pay accrual on Default: true<br>
   * Stub type: front-short<br>
   * Protection from start of day: true<br>
   * Business-day Adjustment: Following<br>
   * Calendar: weekend only<br>
   * Accrual day count: ACT/360<br>
   * Curve day count: ACT/365 (fixed) 
   * @param recoveryRate The recovery rate
   * @param couponInterval The coupon interval
   */
  public CDSAnalyticFactory(final double recoveryRate, final Period couponInterval) {
    ArgumentChecker.notNull(couponInterval, "couponInterval");
    _stepIn = DEFAULT_STEPIN;
    _cashSettle = DEFAULT_CASH_SETTLE;
    _payAccOnDefault = DEFAULT_PAY_ACC;
    _couponInterval = couponInterval;
    _stubType = DEFAULT_STUB_TYPE;
    _protectStart = PROT_START;
    _recoveryRate = recoveryRate;
    _businessdayAdjustmentConvention = FOLLOWING;
    _calendar = DEFAULT_CALENDAR;
    _accrualDayCount = ACT_360;
    _curveDayCount = ACT_365;
    _couponIntervalTenor = Tenor.of(_couponInterval);
  }

  /**
   * Copy constructor 
   * @param other The factory to copy
   */
  public CDSAnalyticFactory(final CDSAnalyticFactory other) {
    ArgumentChecker.notNull(other, "other");
    _stepIn = other._stepIn;
    _cashSettle = other._cashSettle;
    _payAccOnDefault = other._payAccOnDefault;
    _couponInterval = other._couponInterval;
    _stubType = other._stubType;
    _protectStart = other._protectStart;
    _recoveryRate = other._recoveryRate;
    _businessdayAdjustmentConvention = other._businessdayAdjustmentConvention;
    _calendar = other._calendar;
    _accrualDayCount = other._accrualDayCount;
    _curveDayCount = other._curveDayCount;
    _couponIntervalTenor = Tenor.of(_couponInterval);
  }

  protected CDSAnalyticFactory(final int stepIn, final int cashSettle, final boolean payAccOnDefault, final Period couponInterval, final StubType stubType, final boolean protectStart,
      final double recoveryRate, final BusinessDayConvention businessdayAdjustmentConvention, final Calendar calendar, final DayCount accrualDayCount, final DayCount curveDayCount) {
    _stepIn = stepIn;
    _cashSettle = cashSettle;
    _payAccOnDefault = payAccOnDefault;
    _couponInterval = couponInterval;
    _stubType = stubType;
    _protectStart = protectStart;
    _recoveryRate = recoveryRate;
    _businessdayAdjustmentConvention = businessdayAdjustmentConvention;
    _calendar = calendar;
    _accrualDayCount = accrualDayCount;
    _curveDayCount = curveDayCount;
    _couponIntervalTenor = Tenor.of(_couponInterval);
  }

  //************************************************************************************************************************
  // with methods - use these to override defaults
  //************************************************************************************************************************

  /**
   * The Step-in (Protection Effective Date or sometimes just Effective Date) is usually T+1. This is when protection (and risk)
   * starts in terms of the model.
   * @param stepIn Zero or more days (after trade day)
   * @return A new factory with the step-in days set. 
   */
  public CDSAnalyticFactory withStepIn(final int stepIn) {
    ArgumentChecker.notNegative(stepIn, "stepIn");
    return new CDSAnalyticFactory(stepIn, _cashSettle, _payAccOnDefault, _couponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  /**
   * Valuation or Cash-settle Date. This is the date for which the present value (PV) of the CDS is calculated. It is usually three 
   * working dates after the trade date. 
   * @param cashSettle Zero or more days (after trade day)
   * @return A new factory with the cash-settle days set.
   */
  public CDSAnalyticFactory withCashSettle(final int cashSettle) {
    return new CDSAnalyticFactory(_stepIn, cashSettle, _payAccOnDefault, _couponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  /**
   *  Is the accrued premium paid in the event of a default (default value is true)
   * @param payAcc Set to true to pay accrued on default 
   * @return A new factory with the payAccOnDefault set
   */
  public CDSAnalyticFactory withPayAccOnDefault(final boolean payAcc) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, payAcc, _couponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount, _curveDayCount);
  }

  /**
   * Set the coupon interval (default is 3M)
   * @param couponInterval The coupon interval
   * @return  A new factory with the coupon interval set
   */
  public CDSAnalyticFactory with(final Period couponInterval) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, couponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  /**
   * stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE (default is FRONTSHORT)
   *  - <b>Note</b> in this code NONE is not allowed
   * @param stubType The stub type 
   * @return  A new factory with the stub-type interval set
   */
  public CDSAnalyticFactory with(final StubType stubType) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _couponInterval, stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  /**
   * If protectStart = true, then protections starts at the beginning of the day, otherwise it is at the end.
   * @param protectionStart Protected from start of day?
   * @return A new factory with protectStart set
   */
  public CDSAnalyticFactory withProtectionStart(final boolean protectionStart) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _couponInterval, _stubType, protectionStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  /**
   * Set the recovery rate (default is 40%)
   * @param recovery The recovery rate
   * @return  A new factory with recovery rate set
   */
  public CDSAnalyticFactory withRecoveryRate(final double recovery) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _couponInterval, _stubType, _protectStart, recovery, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        _curveDayCount);
  }

  /**
   * Set how adjustments for non-business are days made. Default is following.
   * @param busDay business-day adjustment convention
   * @return A new factory with business-day adjustment convention set
   */
  public CDSAnalyticFactory with(final BusinessDayConvention busDay) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _couponInterval, _stubType, _protectStart, _recoveryRate, busDay, _calendar, _accrualDayCount, _curveDayCount);
  }

  /**
   * Set the calendar. Default is weekend-only 
   * @param calendar Calendar defining what is a non-business day
   * @return A new factory with calendar set
   */
  public CDSAnalyticFactory with(final Calendar calendar) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _couponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, calendar, _accrualDayCount,
        _curveDayCount);
  }

  /**
   * Set the day count used for accrual calculations (i.e. premium payments). Default is ACT/360
   * @param accDCC Day count used for accrual
   * @return A new factory with accDCC set
   */
  public CDSAnalyticFactory withAccrualDCC(final DayCount accDCC) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _couponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, accDCC, _curveDayCount);
  }

  /**
   * Set the day count used on curve
   * @param curveDCC Day count used on curve (NOTE ISDA uses ACT/365 (fixed) and it is not recommended to change this)
   * @return A new factory with curveDCC set
   */
  public CDSAnalyticFactory withCurveDCC(final DayCount curveDCC) {
    return new CDSAnalyticFactory(_stepIn, _cashSettle, _payAccOnDefault, _couponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention, _calendar, _accrualDayCount,
        curveDCC);
  }

  //************************************************************************************************************************
  // Make CDSAnalytic  
  //************************************************************************************************************************

  /**
   * Set up an on-the-run index represented as a single name CDS (i.e. by CDSAnalytic). The index roll dates (when new indices are issued) are 20 Mar & Sep,
   *  and the index is defined to have a maturity that is its nominal tenor plus 3M on issuance, so a 5Y index on the 6-Feb-2014 will have a maturity of
   *  20-Dec-2018 (5Y3M on the issue date of 20-Sep-2013). 
   *  The accrual start date will be the previous IMM date (before the trade date), business-day adjusted.  <b>Note</b> it payment interval is changed from the
   * default of 3M, this will produce a (possibly incorrect) non-standard first coupon.    
   * will have a maturity of 
   * @param tradeDate The trade date
   * @param tenor The nominal length of the index 
   * @return A CDS analytic description 
   */
  public CDSAnalytic makeCDX(final LocalDate tradeDate, final Period tenor) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(tenor, "tenor");
    final LocalDate effectiveDate = _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(tradeDate));
    final LocalDate roll = getNextIndexRollDate(tradeDate);
    final LocalDate maturity = roll.plus(tenor).minusMonths(3);
    return makeCDS(tradeDate, effectiveDate, maturity);
  }

  /**
   * Set up a strip of on-the-run indexes represented as a single name CDSs (i.e. by CDSAnalytic). The index roll dates (when new indices are issued) are 20 Mar & Sep,
   *  and the index is defined to have a maturity that is its nominal tenor plus 3M on issuance, so a 5Y index on the 6-Feb-2014 will have a maturity of
   *  20-Dec-2018 (5Y3M on the issue date of 20-Sep-2013). 
   *  The accrual start date will be the previous IMM date (before the trade date), business-day adjusted.  <b>Note</b> it payment interval is changed from the
   * default of 3M, this will produce a (possibly incorrect) non-standard first coupon.    
   * will have a maturity of 
   * @param tradeDate The trade date
   * @param tenors The nominal lengths of the indexes
   * @return An array of CDS analytic descriptions 
   */
  public CDSAnalytic[] makeCDX(final LocalDate tradeDate, final Period[] tenors) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.noNulls(tenors, "tenors");
    final LocalDate effectiveDate = _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(tradeDate));
    final LocalDate mid = getNextIndexRollDate(tradeDate).minusMonths(3);
    final LocalDate[] maturities = getIMMDateSet(mid, tenors);
    return makeCDS(tradeDate, effectiveDate, maturities);
  }

  /**
   * Make a CDS with a maturity date the given period on from the next IMM date after the trade-date. The accrual start date will
   * be the previous IMM date (before the trade date), business-day adjusted.  <b>Note</b> it payment interval is changed from the
   * default of 3M, this will produce a (possibly incorrect) non-standard first coupon.   
   * @param tradeDate The trade date
   * @param tenor The tenor (length) of the CDS
   * @return A CDS analytic description 
   */
  public CDSAnalytic makeIMMCDS(final LocalDate tradeDate, final Period tenor) {
    return makeIMMCDS(tradeDate, tenor, true);
  }

  /**
   * Make a CDS with a maturity date the given period on from the next IMM date after the trade-date. The accrual start date will
   * be the previous IMM date (before the trade date).  <b>Note</b> it payment interval is changed from the
   * default of 3M, this will produce a (possibly incorrect) non-standard first coupon.   
   * @param tradeDate The trade date
   * @param tenor The tenor (length) of the CDS
   * @param makeEffBusDay Is the accrual start day business-day adjusted.
   * @return A CDS analytic description 
   */
  public CDSAnalytic makeIMMCDS(final LocalDate tradeDate, final Period tenor, final boolean makeEffBusDay) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(tenor, "tenor");
    final LocalDate effectiveDate = makeEffBusDay ? _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(tradeDate)) : getPrevIMMDate(tradeDate);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final LocalDate maturity = nextIMM.plus(tenor);
    return makeCDS(tradeDate, effectiveDate, maturity);
  }

  /**
   * Make a set of CDSs with a common trade date and maturities dates the given periods after the next IMM date (after the trade-date).
   * The accrual start date will  be the previous IMM date (before the trade date), business-day adjusted. 
   * <b>Note</b> it payment interval is changed from the default of 3M, this will produce a (possibly incorrect) non-standard first coupon.   
   * @param tradeDate The trade date
   * @param tenors The tenors (lengths) of the CDSs
   * @return An array of CDS analytic descriptions 
   */
  public CDSAnalytic[] makeIMMCDS(final LocalDate tradeDate, final Period[] tenors) {
    return makeIMMCDS(tradeDate, tenors, true);
  }

  /**
   * Make a set of CDSs with a common trade date and maturities dates the given periods after the next IMM date (after the trade-date).
   * The accrual start date will  be the previous IMM date (before the trade date).
   * <b>Note</b> it payment interval is changed from the default of 3M, this will produce a (possibly incorrect) non-standard first coupon.   
   * @param tradeDate The trade date
   * @param tenors The tenors (lengths) of the CDSs
   * @param makeEffBusDay Is the accrual start day business-day adjusted.
   * @return An array of CDS analytic descriptions 
   */
  public CDSAnalytic[] makeIMMCDS(final LocalDate tradeDate, final Period[] tenors, final boolean makeEffBusDay) {
    final LocalDate effectiveDate = makeEffBusDay ? _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(tradeDate)) : getPrevIMMDate(tradeDate);
    return makeIMMCDS(tradeDate, effectiveDate, tenors);
  }

  /**
   * Make a set of CDSs with a common trade date and maturities dates the given periods after the next IMM date (after the trade-date).
   * @param tradeDate The trade date
   * @param accStartDate This is when the CDS nominally starts in terms of premium payments. For a standard CDS this is  the previous
   *  IMM date, and for a `legacy' CDS it is T+1
   * @param tenors  The tenors (lengths) of the CDSs
   * @return An array of CDS analytic descriptions 
   */
  public CDSAnalytic[] makeIMMCDS(final LocalDate tradeDate, final LocalDate accStartDate, final Period[] tenors) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(accStartDate, "effectiveDate");
    ArgumentChecker.noNulls(tenors, "tenors");
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final LocalDate[] maturities = getIMMDateSet(nextIMM, tenors);
    return makeCDS(tradeDate, accStartDate, maturities);
  }

  /**
   * Make a CDS by specifying key dates 
   * @param tradeDate The trade date 
   * @param accStartDate This is when the CDS nominally starts in terms of premium payments. For a standard CDS this is  the previous
   *  IMM date, and for a `legacy' CDS it is T+1
   * @param maturity The maturity. For a standard CDS this is an IMM  date
   * @return A CDS analytic description 
   */
  public CDSAnalytic makeCDS(final LocalDate tradeDate, final LocalDate accStartDate, final LocalDate maturity) {
    final LocalDate stepinDate = tradeDate.plusDays(_stepIn);
    final LocalDate valueDate = addWorkDays(tradeDate, _cashSettle, _calendar);
    return new CDSAnalytic(tradeDate, stepinDate, valueDate, accStartDate, maturity, _payAccOnDefault, _couponInterval, _stubType, _protectStart, _recoveryRate, _businessdayAdjustmentConvention,
        _calendar, _accrualDayCount, _curveDayCount);
  }

  /**
   * Make a CDS by specifying all dates
   * @param tradeDate The trade date
   * @param stepinDate (aka Protection Effective sate or assignment date). Date when party assumes ownership. This is usually T+1. This is when protection
   * (and risk) starts in terms of the model. Note, this is sometimes just called the Effective Date, however this can cause
   * confusion with the legal effective date which is T-60 or T-90.
   * @param cashSettlementDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param accStartDate This is when the CDS nominally starts in terms of premium payments.  i.e. the number of days in the first 
   * period (and thus the amount of the first premium payment) is counted from this date.
   * @param  maturity (aka end date) This is when the contract expires and protection ends - any default after this date does not
   *  trigger a payment. (the protection ends at end of day)
   * @return A CDS analytic description 
   */
  public CDSAnalytic makeCDS(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate cashSettlementDate, final LocalDate accStartDate, final LocalDate maturity) {
    return new CDSAnalytic(tradeDate, stepinDate, cashSettlementDate, accStartDate, maturity, _payAccOnDefault, _couponInterval, _stubType, _protectStart, _recoveryRate,
        _businessdayAdjustmentConvention, _calendar, _accrualDayCount, _curveDayCount);
  }

  /**
   * Make a set of CDS by specifying key dates 
   * @param tradeDate The trade date 
   * @param accStartDate This is when the CDS nominally starts in terms of premium payments. For a standard CDS this is  the previous
   *  IMM date, and for a `legacy' CDS it is T+1
   * @param maturities The maturities of the CDSs. For a standard CDS these are IMM  dates
   * @return An array of CDS analytic descriptions 
   */
  public CDSAnalytic[] makeCDS(final LocalDate tradeDate, final LocalDate accStartDate, final LocalDate[] maturities) {
    final LocalDate stepinDate = tradeDate.plusDays(_stepIn);
    final LocalDate valueDate = addWorkDays(tradeDate, _cashSettle, _calendar);
    return makeCDSs(tradeDate, stepinDate, valueDate, accStartDate, maturities);
  }

  /**
   * Make a set of CDS by specifying all dates
   * @param tradeDate The trade date
   * @param stepinDate (aka Protection Effective sate or assignment date). Date when party assumes ownership. This is usually T+1. This is when protection
   * (and risk) starts in terms of the model. Note, this is sometimes just called the Effective Date, however this can cause
   * confusion with the legal effective date which is T-60 or T-90.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param accStartDate This is when the CDS nominally starts in terms of premium payments.  i.e. the number of days in the first 
   * period (and thus the amount of the first premium payment) is counted from this date.
   * @param maturities  The maturities of the CDSs. For a standard CDS these are IMM  dates
   * @return An array of CDS analytic descriptions 
   */
  public CDSAnalytic[] makeCDSs(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate accStartDate, final LocalDate[] maturities) {
    ArgumentChecker.noNulls(maturities, "maturities");
    final int n = maturities.length;
    final CDSAnalytic[] cds = new CDSAnalytic[n];
    for (int i = 0; i < n; i++) {
      cds[i] = new CDSAnalytic(tradeDate, stepinDate, valueDate, accStartDate, maturities[i], _payAccOnDefault, _couponInterval, _stubType, _protectStart, _recoveryRate,
          _businessdayAdjustmentConvention, _calendar, _accrualDayCount, _curveDayCount);
    }
    return cds;
  }

  //************************************************************************************************************************
  //Make forward starting CDS 
  //************************************************************************************************************************

  /**
   * A forward starting CDS starts on some date after today (the trade date). The stepin date and cash settlement date are taken from the forward start date
   * (1 day and 3 working days by default)
   * @param tradeDate The trade date (i.e. today)
   * @param forwardStartDate The forward start date
   * @param maturity The maturity of the CDS 
   * @return A CDS analytic description for a forward starting CDS
   */
  public CDSAnalytic makeForwardStartingCDS(final LocalDate tradeDate, final LocalDate forwardStartDate, final LocalDate maturity) {
    ArgumentChecker.isFalse(forwardStartDate.isBefore(tradeDate), "forwardStartDate of {} is before trade date of {}", forwardStartDate, tradeDate);
    final LocalDate stepinDate = forwardStartDate.plusDays(_stepIn);
    final LocalDate valueDate = addWorkDays(forwardStartDate, _cashSettle, _calendar);
    final LocalDate accStartDate = _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(forwardStartDate));
    return makeCDS(tradeDate, stepinDate, valueDate, accStartDate, maturity);
  }

  /**
  * A forward starting CDS starts on some date after today (the trade date). The accrual start must be specified (would normally use this for T+1 accrual atart).
  *  The stepin date and cash settlement date are taken from the forward start date  (1 day and 3 working days by default)
   * @param tradeDate The trade date (i.e. today)
   * @param forwardStartDate The forward start date
   * @param accStartDate The accrual start date 
   * @param maturity The maturity of the CDS 
   * @return  A CDS analytic description for a forward starting CDS
   */
  public CDSAnalytic makeForwardStartingCDS(final LocalDate tradeDate, final LocalDate forwardStartDate, final LocalDate accStartDate, final LocalDate maturity) {
    ArgumentChecker.isFalse(forwardStartDate.isBefore(tradeDate), "forwardStartDate of {} is before trade date of {}", forwardStartDate, tradeDate);
    final LocalDate stepinDate = forwardStartDate.plusDays(_stepIn);
    final LocalDate valueDate = addWorkDays(forwardStartDate, _cashSettle, _calendar);
    return makeCDS(tradeDate, stepinDate, valueDate, accStartDate, maturity);
  }

  /** A forward starting CDS starts on some date after today (the trade date). The stepin date and cash settlement date are taken from the forward start date
   * (1 day and 3 working days by default). The period is from the next IMM date after the forward-start-date, so for a trade-date of 13-Feb-2014, a forward-start-date
   * of 25-Mar-2014 and a tenor of 1Y, the maturity will be 20-Jun-2015. 
   * @param tradeDate The trade date (i.e. today)
   * @param forwardStartDate The forward start date
   * @param tenor The tenor (length) of the CDS at the forwardStartDate
   * @return A CDS analytic description for a forward starting CDS
   */
  public CDSAnalytic makeForwardStartingIMMCDS(final LocalDate tradeDate, final LocalDate forwardStartDate, final Period tenor) {
    final LocalDate nextIMM = getNextIMMDate(forwardStartDate);
    final LocalDate maturity = nextIMM.plus(tenor);
    return makeForwardStartingCDS(tradeDate, forwardStartDate, maturity);
  }

  /**
   * /** A forward starting index starts on some date after today (the trade date). The stepin date and cash settlement date are taken from the forward start date
   * (1 day and 3 working days by default). 
   *The maturity (of the index) is taken from the forward-start-date. The index roll dates (when new indices are issued) are 20 Mar & Sep,
   *  and the index is defined to have a maturity that is its nominal tenor plus 3M on issuance, so a 5Y index on the 6-Feb-2014 will have a maturity of
   *  20-Dec-2018 (5Y3M on the issue date of 20-Sep-2013).  However for a trade-date of 6-Feb-2014, a forward-start-date
   * of 25-Mar-2014 and a tenor of 5Y, the maturity will be 20-Jun-2019. 
   * @param tradeDate The trade date (i.e. today)
   * @param forwardStartDate   The forward start date
   * @param tenor The tenor (nominal length) of the index at the forwardStartDate
   * @return A CDS analytic description for a forward starting index
   */
  public CDSAnalytic makeForwardStartingCDX(final LocalDate tradeDate, final LocalDate forwardStartDate, final Period tenor) {
    final LocalDate roll = getNextIndexRollDate(forwardStartDate);
    final LocalDate maturity = roll.plus(tenor).minusMonths(3);
    return makeForwardStartingCDS(tradeDate, forwardStartDate, maturity);
  }

  //************************************************************************************************************************
  //Make MultiCDSAnalytic
  //************************************************************************************************************************

  /**
   * Make a CDS represented as a MultiCDSAnalytic instance. Note, this is mainly for testing, since if you want only a single CDS should use a method that returns a 
   * {@link CDSAnalytic}
   * @param tradeDate The trade day
   * @param maturityReferanceDate  A reference date that maturities are measured from. For standard CDSSs, this is the next IMM  date after
   * the trade date, so the actually maturities will be some fixed periods after this.  
   * @param termMatIndex The maturities are fixed integer multiples of the payment interval, so  2Y tenor with a 3M 
   * payment interval, this would be 8
   * @return a a CDS represented as a MultiCDSAnalytic
   */
  public MultiCDSAnalytic makeMultiCDS(final LocalDate tradeDate, final LocalDate maturityReferanceDate, final int termMatIndex) {
    final int[] maturityIndexes = new int[termMatIndex + 1];
    for (int i = 0; i <= termMatIndex; i++) {
      maturityIndexes[i] = i;
    }
    final LocalDate accStartDate = _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(tradeDate));
    return makeMultiCDS(tradeDate, accStartDate, maturityReferanceDate, maturityIndexes);
  }

  /**
   * Make a set of CDS represented as a MultiCDSAnalytic instance. 
   * @param tradeDate  The trade day
   * @param accStartDate This is when the CDS nominally starts in terms of the accrual calculation for premium payments. For a standard CDS this is  the previous
   *  IMM date, and for a `legacy' CDS it is T+1
   * @param maturityReferanceDate A reference date that maturities are measured from. For standard CDSSs, this is the next IMM  date after
   * the trade date, so the actually maturities will be some fixed periods after this.  
   * @param maturityIndexes  The maturities are fixed integer multiples of the payment interval, so for 6M, 1Y and 2Y tenors with a 3M 
   * payment interval, would require 2, 4, and 8 as the indices 
   * @return Make a set of CDS represented as a MultiCDSAnalytic
   */
  public MultiCDSAnalytic makeMultiCDS(final LocalDate tradeDate, final LocalDate accStartDate, final LocalDate maturityReferanceDate, final int[] maturityIndexes) {
    final LocalDate stepinDate = tradeDate.plusDays(_stepIn);
    final LocalDate valueDate = addWorkDays(tradeDate, _cashSettle, _calendar);
    return makeMultiCDS(tradeDate, stepinDate, valueDate, accStartDate, maturityReferanceDate, maturityIndexes);
  }

  /**
   * Make a set of CDS represented as a MultiCDSAnalytic instance. 
   * @param tradeDate The trade date
   * @param stepinDate (aka Protection Effective sate or assignment date). Date when party assumes ownership. This is usually T+1. This is when protection
   * (and risk) starts in terms of the model. Note, this is sometimes just called the Effective Date, however this can cause
   * confusion with the legal effective date which is T-60 or T-90.
   * @param cashSettlementDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param accStartDate This is when the CDS nominally starts in terms of premium payments.  i.e. the number of days in the first 
   * period (and thus the amount of the first premium payment) is counted from this date.
   * @param maturityReferanceDate  A reference date that maturities are measured from. For standard CDSSs, this is the next IMM  date after
   * the trade date, so the actually maturities will be some fixed periods after this.  
   * @param maturityIndexes  The maturities are fixed integer multiples of the payment interval, so for 6M, 1Y and 2Y tenors with a 3M 
   * payment interval, would require 2, 4, and 8 as the indices 
   * @return A set of CDS represented as a MultiCDSAnalytic
   */
  public MultiCDSAnalytic makeMultiCDS(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate cashSettlementDate, final LocalDate accStartDate, final LocalDate maturityReferanceDate,
      final int[] maturityIndexes) {
    return new MultiCDSAnalytic(tradeDate, stepinDate, cashSettlementDate, accStartDate, maturityReferanceDate, maturityIndexes, _payAccOnDefault, _couponIntervalTenor, _stubType, _protectStart,
        _recoveryRate, _businessdayAdjustmentConvention, DEFAULT_CALENDAR, _accrualDayCount, _curveDayCount);
  }

  /**
   * Make a set of standard CDS represented as a MultiCDSAnalytic instance.  
   * @param tradeDate the trade date
   * @param tenors The tenors (length) of the CDS
   * @return A set of CDS represented as a MultiCDSAnalytic
   */
  public MultiCDSAnalytic makeMultiIMMCDS(final LocalDate tradeDate, final Period[] tenors) {
    final LocalDate accStartDate = _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(tradeDate));
    return makeMultiIMMCDS(tradeDate, accStartDate, tenors);
  }

  /**
   * Make a set of standard CDS represented as a MultiCDSAnalytic instance.  
   * @param tradeDate the trade date
   * @param accStartDate The accrual start date 
   * @param tenors The tenors (length) of the CDS
   * @return A set of CDS represented as a MultiCDSAnalytic
   */
  public MultiCDSAnalytic makeMultiIMMCDS(final LocalDate tradeDate, final LocalDate accStartDate, final Period[] tenors) {

    ArgumentChecker.noNulls(tenors, "tenors");
    final int n = tenors.length;

    final int immNMonths = (int) DEFAULT_COUPON_INT.toTotalMonths();
    final int[] matIndices = new int[n];
    for (int i = 0; i < n; i++) {
      final int months = (int) tenors[i].toTotalMonths();
      if (months % immNMonths != 0) {
        throw new IllegalArgumentException("tenors index " + i + " is not a multiple of " + DEFAULT_COUPON_INT.toString());
      }
      matIndices[i] = months / immNMonths;
    }

    return makeMultiIMMCDS(tradeDate, accStartDate, matIndices);
  }

  /**
   * Make a set of standard CDS represented as a MultiCDSAnalytic instance. The first CDS with have a tenor of firstTenor, while the last CDS will have a tenor of lastTenor;
   * the remaining CDS will consist of all the (multiple of 3 month) tenors between the first and last tenor, e.g. if firstTenor = 6M and lastTenor = 5Y, there will be a total
   * of 22 CDS with tenors of 6M, 9M, 1Y,....4Y9M, 5Y 
   * @param tradeDate The trade date
   * @param firstTenor The first tenor 
   * @param lastTenor The last tenor
   * @return A set of CDS represented as a MultiCDSAnalytic
   */
  public MultiCDSAnalytic makeMultiIMMCDS(final LocalDate tradeDate, final Period firstTenor, final Period lastTenor) {
    ArgumentChecker.notNull(firstTenor, "firstTenor");
    ArgumentChecker.notNull(lastTenor, "lastTenor");
    final int immNMonths = (int) DEFAULT_COUPON_INT.toTotalMonths();
    final int m1 = (int) firstTenor.toTotalMonths();
    final int m2 = (int) lastTenor.toTotalMonths();
    if (m1 % immNMonths != 0 || m2 % immNMonths != 0) {
      throw new IllegalArgumentException("tenors is not a multiple of " + DEFAULT_COUPON_INT.toString());
    }
    final int firstIndex = m1 / immNMonths;
    final int lastIndex = m2 / immNMonths;
    return makeMultiIMMCDS(tradeDate, firstIndex, lastIndex);
  }

  /**
   * Make a set of standard CDS represented as a MultiCDSAnalytic instance. The maturities of the CDS are measured from the next IMM date (after the trade date), and 
   * the first and last tenors are the firstIndex and lastIndex multiplied by the coupon interval (3 months), which the remaining tenors being everything in between 
   * @param tradeDate The trade date 
   * @param firstIndex First index
   * @param lastIndex last index  
   * @return A set of CDS represented as a MultiCDSAnalytic
   */
  public MultiCDSAnalytic makeMultiIMMCDS(final LocalDate tradeDate, final int firstIndex, final int lastIndex) {
    ArgumentChecker.isTrue(lastIndex > firstIndex, "Require lastIndex>firstIndex");
    ArgumentChecker.isTrue(firstIndex >= 0, "Require positive indices");
    final int n = lastIndex - firstIndex + 1;
    final int[] matIndices = new int[n];
    for (int i = 0; i < n; i++) {
      matIndices[i] = i + firstIndex;
    }
    return makeMultiIMMCDS(tradeDate, matIndices);
  }

  /**
   * Make a set of standard CDS represented as a MultiCDSAnalytic instance. The maturities of the CDS are measured from the next IMM date (after the trade date), and 
   * the tenors are the given matIndices multiplied by the coupon interval (3 months)
   * @param tradeDate the trade date
   * @param matIndices The CDS tenors are these multiplied by the coupon interval (3 months)
   * @return  A set of CDS represented as a MultiCDSAnalytic
   */
  public MultiCDSAnalytic makeMultiIMMCDS(final LocalDate tradeDate, final int[] matIndices) {
    final LocalDate accStartDate = _businessdayAdjustmentConvention.adjustDate(_calendar, getPrevIMMDate(tradeDate));
    return makeMultiIMMCDS(tradeDate, accStartDate, matIndices);
  }

  /**
   * Make a set of standard CDS represented as a MultiCDSAnalytic instance. The maturities of the CDS are measured from the next IMM date (after the trade date), and 
   * the tenors are the given matIndices multiplied by the coupon interval (3 months)
   * @param tradeDate the trade date
   * @param accStartDate The accrual start date 
   * @param matIndices The CDS tenors are these multiplied by the coupon interval (3 months)
   * @return  A set of CDS represented as a MultiCDSAnalytic
   */
  public MultiCDSAnalytic makeMultiIMMCDS(final LocalDate tradeDate, final LocalDate accStartDate, final int[] matIndices) {
    if (!_couponInterval.equals(DEFAULT_COUPON_INT)) {
      throw new IllegalArgumentException("coupon interval must be 3M for this method. However it is set to " + _couponInterval.toString());
    }
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notEmpty(matIndices, "matIndicies");

    //final LocalDate nextIMM = isIMMDate(tradeDate) ? tradeDate : getNextIMMDate(tradeDate);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final LocalDate stepinDate = tradeDate.plusDays(_stepIn);
    final LocalDate valueDate = addWorkDays(tradeDate, _cashSettle, _calendar);

    return new MultiCDSAnalytic(tradeDate, stepinDate, valueDate, accStartDate, nextIMM, matIndices, _payAccOnDefault, _couponIntervalTenor, _stubType, _protectStart, _recoveryRate,
        _businessdayAdjustmentConvention, DEFAULT_CALENDAR, _accrualDayCount, _curveDayCount);
  }

}

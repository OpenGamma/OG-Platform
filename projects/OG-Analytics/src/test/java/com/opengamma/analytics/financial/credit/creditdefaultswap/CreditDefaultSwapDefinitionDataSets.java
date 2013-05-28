/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.ObligorDataSets.getObligor1;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.ObligorDataSets.getObligor2;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.ObligorDataSets.getObligor3;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyFixedRecoveryCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyForwardStartingCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyMuniCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyQuantoCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyRecoveryLockCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacySovereignCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardFixedRecoveryCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardForwardStartingCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardMuniCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardQuantoCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardRecoveryLockCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardSovereignCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardVanillaCreditDefaultSwapDefinition;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class CreditDefaultSwapDefinitionDataSets {
  private static final BuySellProtection BUY_SELL_PROTECTION = BuySellProtection.BUY;

  private static final Currency CURRENCY = Currency.EUR;

  private static final DebtSeniority DEBT_SENIORITY = DebtSeniority.SENIOR;
  private static final RestructuringClause RESTRUCTURING_CLAUSE = RestructuringClause.NORE;

  private static final Calendar CALENDAR = new MondayToFridayCalendar("TestCalendar");

  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2007, 10, 22);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2007, 10, 23);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2022, 12, 20);

  private static final StubType STUB_TYPE = StubType.FRONTSHORT;
  private static final PeriodFrequency COUPON_FREQUENCY = PeriodFrequency.QUARTERLY;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean IMM_ADJUST_MATURITY_DATE = true;
  private static final boolean ADJUST_EFFECTIVE_DATE = true;
  private static final boolean ADJUST_MATURITY_DATE = true;

  private static final double NOTIONAL = 10000000.0;
  private static final double RECOVERY_RATE = 0.40;
  private static final double FIXED_RECOVERY_RATE = 0.20;
  private static final double RECOVERY_LOCK_RATE = 0.75;
  private static final boolean INCLUDE_ACCRUED_PREMIUM = true;
  private static final boolean PROTECTION_START = true;

  private static final double PAR_SPREAD = 123.0;

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinition() {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyFixedRecoveryCreditDefaultSwapDefinition getLegacyFixedRecoveryDefinition() {
    return new LegacyFixedRecoveryCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, FIXED_RECOVERY_RATE);
  }

  public static LegacyForwardStartingCreditDefaultSwapDefinition getLegacyForwardStartingDefinition() {
    return new LegacyForwardStartingCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, START_DATE.minusDays(100));
  }

  public static LegacyMuniCreditDefaultSwapDefinition getLegacyMuniDefinition() {
    return new LegacyMuniCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyQuantoCreditDefaultSwapDefinition getLegacyQuantoDefinition() {
    return new LegacyQuantoCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyRecoveryLockCreditDefaultSwapDefinition getLegacyRecoveryLockDefinition() {
    return new LegacyRecoveryLockCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, RECOVERY_LOCK_RATE);
  }

  public static LegacySovereignCreditDefaultSwapDefinition getLegacySovereignDefinition() {
    return new LegacySovereignCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static StandardVanillaCreditDefaultSwapDefinition getStandardVanillaDefinition() {
    return new StandardVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, StandardCDSCoupon._1000bps,
        100, EFFECTIVE_DATE, false);
  }

  public static StandardFixedRecoveryCreditDefaultSwapDefinition getStandardFixedRecoveryDefinition() {
    return new StandardFixedRecoveryCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, StandardCDSCoupon._1000bps,
        100, EFFECTIVE_DATE, false, FIXED_RECOVERY_RATE);
  }

  public static StandardForwardStartingCreditDefaultSwapDefinition getStandardForwardStartingDefinition() {
    return new StandardForwardStartingCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, StandardCDSCoupon._1000bps,
        100, EFFECTIVE_DATE, false, START_DATE.minusDays(100));
  }

  public static StandardMuniCreditDefaultSwapDefinition getStandardMuniDefinition() {
    return new StandardMuniCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, StandardCDSCoupon._1000bps,
        100, EFFECTIVE_DATE, false);
  }

  public static StandardQuantoCreditDefaultSwapDefinition getStandardQuantoDefinition() {
    return new StandardQuantoCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, StandardCDSCoupon._1000bps,
        100, EFFECTIVE_DATE, false);
  }

  public static StandardRecoveryLockCreditDefaultSwapDefinition getStandardRecoveryLockDefinition() {
    return new StandardRecoveryLockCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, StandardCDSCoupon._1000bps,
        100, EFFECTIVE_DATE, false);
  }

  public static StandardSovereignCreditDefaultSwapDefinition getStandardSovereignpDefinition() {
    return new StandardSovereignCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, StandardCDSCoupon._1000bps,
        100, EFFECTIVE_DATE, false);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinition(final BuySellProtection buySell) {
    return new LegacyVanillaCreditDefaultSwapDefinition(buySell, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static StandardVanillaCreditDefaultSwapDefinition getStandardVanillaDefinition(final BuySellProtection buySell) {
    return new StandardVanillaCreditDefaultSwapDefinition(buySell, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD, StandardCDSCoupon._1000bps,
        100, EFFECTIVE_DATE, false);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinitionWithSeniority(final DebtSeniority seniority) {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        seniority, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinitionWithClause(final RestructuringClause clause) {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, clause, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinitionWithStartDate(final ZonedDateTime startDate) {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, startDate, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinitionWithMaturityDate(final ZonedDateTime maturityDate) {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, maturityDate, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinitionWithStubType(final StubType stubType) {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, stubType,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinitionWithProtectionStart(final boolean protectionStart) {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, protectionStart, PAR_SPREAD);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinitionWithNotional(final double notional) {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        notional, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinitionWithRecoveryRate(final double recoveryRate) {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, recoveryRate, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, PAR_SPREAD);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition getLegacyVanillaDefinitionWithParSpread(final double parSpread) {
    return new LegacyVanillaCreditDefaultSwapDefinition(BUY_SELL_PROTECTION, getObligor1(), getObligor2(), getObligor3(), CURRENCY,
        DEBT_SENIORITY, RESTRUCTURING_CLAUSE, CALENDAR, START_DATE, EFFECTIVE_DATE, MATURITY_DATE, STUB_TYPE,
        COUPON_FREQUENCY, DAY_COUNT, BUSINESS_DAY, IMM_ADJUST_MATURITY_DATE, ADJUST_EFFECTIVE_DATE, ADJUST_MATURITY_DATE,
        NOTIONAL, RECOVERY_RATE, INCLUDE_ACCRUED_PREMIUM, PROTECTION_START, parSpread);
  }

}

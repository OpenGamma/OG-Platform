/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.index.IndexPrice;
import com.opengamma.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflation;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.financial.interestrate.market.MarketDataSets;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

public class BondCapitalIndexedSecurityDefinitionTest {
  // Index-Linked Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final String NAME_INDEX_UK = "UK RPI";
  private static final Period LAG_INDEX_UK = Period.ofDays(14);
  private static final IndexPrice PRICE_INDEX_UKRPI = new IndexPrice(NAME_INDEX_UK, Currency.GBP, Currency.GBP, LAG_INDEX_UK);
  private static final Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY_GBP = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount DAY_COUNT_GILT_1 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final boolean IS_EOM_GILT_1 = false;
  private static final ZonedDateTime START_DATE_GILT_1 = DateUtils.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime FIRST_COUPON_DATE_GILT_1 = DateUtils.getUTCDate(2003, 1, 26);
  private static final ZonedDateTime MATURITY_DATE_GILT_1 = DateUtils.getUTCDate(2035, 1, 26);
  private static final YieldConvention YIELD_CONVENTION_GILT_1 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG_GILT_1 = 8;
  private static final double INDEX_START_GILT_1 = 173.60; // November 2001 
  private static final double NOTIONAL_GILT_1 = 1.00;
  private static final double REAL_RATE_GILT_1 = 0.02;
  private static final Period COUPON_PERIOD_GILT_1 = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR_GILT_1 = 2;
  private static final int SETTLEMENT_DAYS_GILT_1 = 2;
  private static final String ISSUER_UK = "UK GOVT";
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> BOND_GILT_1_SECURITY_DEFINITION = BondCapitalIndexedSecurityDefinition.fromMonthly(
      PRICE_INDEX_UKRPI, MONTH_LAG_GILT_1, START_DATE_GILT_1, INDEX_START_GILT_1, FIRST_COUPON_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1, NOTIONAL_GILT_1, REAL_RATE_GILT_1,
      BUSINESS_DAY_GBP, SETTLEMENT_DAYS_GILT_1, CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, ISSUER_UK);

  @Test
  public void getter() {
    assertEquals("Capital Index Bond", DAY_COUNT_GILT_1, BOND_GILT_1_SECURITY_DEFINITION.getDayCount());
    assertEquals("Capital Index Bond", IS_EOM_GILT_1, BOND_GILT_1_SECURITY_DEFINITION.isEOM());
    assertEquals("Capital Index Bond", YIELD_CONVENTION_GILT_1, BOND_GILT_1_SECURITY_DEFINITION.getYieldConvention());
    assertEquals("Capital Index Bond", COUPON_PER_YEAR_GILT_1, BOND_GILT_1_SECURITY_DEFINITION.getCouponPerYear());
    assertEquals("Capital Index Bond", MONTH_LAG_GILT_1, BOND_GILT_1_SECURITY_DEFINITION.getMonthLag());
    assertEquals("Capital Index Bond", INDEX_START_GILT_1, BOND_GILT_1_SECURITY_DEFINITION.getIndexStartValue());
    assertEquals("Capital Index Bond", PRICE_INDEX_UKRPI, BOND_GILT_1_SECURITY_DEFINITION.getPriceIndex());
  }

  @Test
  /**
   * Tests the bond constructors.
   */
  public void constructorGilts() {
    // Nominal construction
    CouponInflationZeroCouponMonthlyGearingDefinition nominalPayment = CouponInflationZeroCouponMonthlyGearingDefinition.from(START_DATE_GILT_1, MATURITY_DATE_GILT_1, NOTIONAL_GILT_1,
        PRICE_INDEX_UKRPI, INDEX_START_GILT_1, MONTH_LAG_GILT_1, true, 1.0);
    AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> nominalAnnuity = new AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition>(
        new CouponInflationZeroCouponMonthlyGearingDefinition[] {nominalPayment});
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(FIRST_COUPON_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, BUSINESS_DAY_GBP, CALENDAR_GBP);
    final CouponInflationZeroCouponMonthlyGearingDefinition[] coupons = new CouponInflationZeroCouponMonthlyGearingDefinition[paymentDates.length + 1];
    coupons[0] = CouponInflationZeroCouponMonthlyGearingDefinition.from(ScheduleCalculator.getAdjustedDate(FIRST_COUPON_DATE_GILT_1, 0, CALENDAR_GBP), START_DATE_GILT_1, FIRST_COUPON_DATE_GILT_1,
        NOTIONAL_GILT_1, PRICE_INDEX_UKRPI, INDEX_START_GILT_1, MONTH_LAG_GILT_1, true, REAL_RATE_GILT_1);
    coupons[1] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[0], FIRST_COUPON_DATE_GILT_1, paymentDatesUnadjusted[0], NOTIONAL_GILT_1, PRICE_INDEX_UKRPI, INDEX_START_GILT_1,
        MONTH_LAG_GILT_1, true, REAL_RATE_GILT_1);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn + 1] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], NOTIONAL_GILT_1,
          PRICE_INDEX_UKRPI, INDEX_START_GILT_1, MONTH_LAG_GILT_1, true, REAL_RATE_GILT_1);
    }
    AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> couponAnnuity = new AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition>(coupons);
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> bond = new BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition>(
        nominalAnnuity, couponAnnuity, INDEX_START_GILT_1, 0, 2, CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, MONTH_LAG_GILT_1, ISSUER_UK);
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> bondFrom = BondCapitalIndexedSecurityDefinition.fromMonthly(PRICE_INDEX_UKRPI, MONTH_LAG_GILT_1,
        START_DATE_GILT_1, INDEX_START_GILT_1, FIRST_COUPON_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1, NOTIONAL_GILT_1, REAL_RATE_GILT_1, BUSINESS_DAY_GBP, SETTLEMENT_DAYS_GILT_1,
        CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, ISSUER_UK);
    assertEquals("Capital Index Bond: constructor", bond, bondFrom);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative1Coupon() {
    DoubleTimeSeries<ZonedDateTime> ukRpi = MarketDataSets.ukRpiFrom2010();
    ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 8, 3); // One coupon fixed
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> bondFromDefinition = BondCapitalIndexedSecurityDefinition.fromMonthly(PRICE_INDEX_UKRPI, MONTH_LAG_GILT_1,
        START_DATE_GILT_1, INDEX_START_GILT_1, FIRST_COUPON_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1, NOTIONAL_GILT_1, REAL_RATE_GILT_1, BUSINESS_DAY_GBP, SETTLEMENT_DAYS_GILT_1,
        CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, ISSUER_UK);
    BondCapitalIndexedSecurity<Coupon> bond = bondFromDefinition.toDerivative(pricingDate, ukRpi, "Not used");
    ZonedDateTime referenceDateNextCoupon = DateUtils.getUTCDate(2011, 5, 1); // May 11
    double referenceIndexNextCoupon = ukRpi.getValue(referenceDateNextCoupon);
    double amountNextCoupon = referenceIndexNextCoupon / INDEX_START_GILT_1 * NOTIONAL_GILT_1 * REAL_RATE_GILT_1;
    assertEquals("Capital Index Bond: toDerivative", amountNextCoupon, ((CouponFixed) bond.getCoupon().getNthPayment(0)).getAmount());
    for (int loopcpn = 1; loopcpn < bond.getCoupon().getNumberOfPayments(); loopcpn++) {
      assertTrue("Capital Index Bond: toDerivative", (bond.getCoupon().getNthPayment(loopcpn) instanceof CouponInflationZeroCouponMonthlyGearing));
      assertEquals("Capital Index Bond: toDerivative", ((CouponInflationZeroCouponMonthlyGearing) bond.getCoupon().getNthPayment(loopcpn)).getIndexStartValue(), INDEX_START_GILT_1);
      assertEquals("Capital Index Bond: toDerivative", ((CouponInflationZeroCouponMonthlyGearing) bond.getCoupon().getNthPayment(loopcpn)).getPriceIndex(), PRICE_INDEX_UKRPI);
    }
    @SuppressWarnings("unchecked")
    final GenericAnnuity<Coupon> nominal = (GenericAnnuity<Coupon>) bondFromDefinition.getNominal().toDerivative(pricingDate, "Not used");
    @SuppressWarnings("unchecked")
    final GenericAnnuity<Coupon> coupon = (GenericAnnuity<Coupon>) bondFromDefinition.getCoupon().toDerivative(pricingDate, ukRpi, "Not used");
    ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(pricingDate, SETTLEMENT_DAYS_GILT_1, CALENDAR_GBP);
    final double settleTime = TimeCalculator.getTimeBetween(pricingDate, spot);
    @SuppressWarnings("unchecked")
    AnnuityDefinition<CouponDefinition> couponDefinition = (AnnuityDefinition<CouponDefinition>) bondFromDefinition.getCoupon().trimBefore(spot);
    final double accruedInterest = bondFromDefinition.accruedInterest(spot);
    final double factorSpot = DAY_COUNT_GILT_1.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spot, couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0,
        COUPON_PER_YEAR_GILT_1);
    final double factorPeriod = DAY_COUNT_GILT_1.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), couponDefinition
        .getNthPayment(0).getAccrualEndDate(), 1.0, COUPON_PER_YEAR_GILT_1);
    final double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    CouponInflationDefinition nominalLast = bondFromDefinition.getNominal().getNthPayment(bondFromDefinition.getNominal().getNumberOfPayments() - 1);
    ZonedDateTime settlementDate2 = spot;
    double notional = 1.0;
    CouponInflationDefinition settlementDefinition = nominalLast.with(settlementDate2, nominalLast.getAccrualStartDate(), settlementDate2, notional);
    CouponInflation settlement = (CouponInflation) settlementDefinition.toDerivative(pricingDate, "Not used");
    final BondCapitalIndexedSecurity<Coupon> bondSecurityExpected = new BondCapitalIndexedSecurity<Coupon>(nominal, coupon, settleTime, accruedInterest, factorToNextCoupon, YIELD_CONVENTION_GILT_1,
        COUPON_PER_YEAR_GILT_1, settlement, INDEX_START_GILT_1, ISSUER_UK);
    assertEquals("Capital Index Bond: toDerivative", bondSecurityExpected, bond);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative2Coupon() {
    DoubleTimeSeries<ZonedDateTime> ukRpi = MarketDataSets.ukRpiFrom2010();
    ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 15); // Two coupons fixed
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> bondFromDefinition = BondCapitalIndexedSecurityDefinition.fromMonthly(PRICE_INDEX_UKRPI, MONTH_LAG_GILT_1,
        START_DATE_GILT_1, INDEX_START_GILT_1, FIRST_COUPON_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1, NOTIONAL_GILT_1, REAL_RATE_GILT_1, BUSINESS_DAY_GBP, SETTLEMENT_DAYS_GILT_1,
        CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, ISSUER_UK);
    BondCapitalIndexedSecurity<Coupon> bond = bondFromDefinition.toDerivative(pricingDate, ukRpi, "Not used");
    ZonedDateTime[] referenceDateNextCoupon = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 1), DateUtils.getUTCDate(2011, 5, 1)}; // Nov 10, May 11
    double[] referenceIndexNextCoupon = new double[] {ukRpi.getValue(referenceDateNextCoupon[0]), ukRpi.getValue(referenceDateNextCoupon[1])};
    for (int loopcpn = 0; loopcpn < 2; loopcpn++) {
      double amountNextCoupon = referenceIndexNextCoupon[loopcpn] / INDEX_START_GILT_1 * NOTIONAL_GILT_1 * REAL_RATE_GILT_1;
      assertEquals("Capital Index Bond: toDerivative", amountNextCoupon, ((CouponFixed) bond.getCoupon().getNthPayment(loopcpn)).getAmount());
    }
    for (int loopcpn = 2; loopcpn < bond.getCoupon().getNumberOfPayments(); loopcpn++) {
      assertTrue("Capital Index Bond: toDerivative", (bond.getCoupon().getNthPayment(loopcpn) instanceof CouponInflationZeroCouponMonthlyGearing));
      assertEquals("Capital Index Bond: toDerivative", ((CouponInflationZeroCouponMonthlyGearing) bond.getCoupon().getNthPayment(loopcpn)).getIndexStartValue(), INDEX_START_GILT_1);
      assertEquals("Capital Index Bond: toDerivative", ((CouponInflationZeroCouponMonthlyGearing) bond.getCoupon().getNthPayment(loopcpn)).getPriceIndex(), PRICE_INDEX_UKRPI);
    }
  }

  // 2% 10-YEAR TREASURY INFLATION-PROTECTED SECURITIES (TIPS) Due January 15, 2016 - US912828ET33
  private static final String NAME_INDEX_US = "US CPI-U";
  private static final Period LAG_INDEX_US = Period.ofDays(14);
  private static final IndexPrice PRICE_INDEX_USCPI = new IndexPrice(NAME_INDEX_US, Currency.USD, Currency.USD, LAG_INDEX_US);
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  private static final BusinessDayConvention BUSINESS_DAY_USD = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount DAY_COUNT_TIPS_1 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final boolean IS_EOM_TIPS_1 = false;
  private static final ZonedDateTime START_DATE_TIPS_1 = DateUtils.getUTCDate(2006, 1, 15);
  private static final ZonedDateTime MATURITY_DATE_TIPS_1 = DateUtils.getUTCDate(2016, 1, 15);
  private static final YieldConvention YIELD_CONVENTION_TIPS_1 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG_TIPS_1 = 3;
  private static final double INDEX_START_TIPS_1 = 198.47742; // Date: 
  private static final double NOTIONAL_TIPS_1 = 100.00;
  private static final double REAL_RATE_TIPS_1 = 0.02;
  private static final Period COUPON_PERIOD_TIPS_1 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS_TIPS_1 = 2;
  private static final String ISSUER_US = "US GOVT";

  @Test
  /**
   * Tests the bond constructors for a TIPS.
   */
  public void constructorTips() {
    // Nominal construction
    CouponInflationZeroCouponInterpolationGearingDefinition nominalPayment = CouponInflationZeroCouponInterpolationGearingDefinition.from(START_DATE_TIPS_1, MATURITY_DATE_TIPS_1, NOTIONAL_TIPS_1,
        PRICE_INDEX_USCPI, INDEX_START_TIPS_1, MONTH_LAG_TIPS_1, true, 1.0);
    AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> nominalAnnuity = new AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>(
        new CouponInflationZeroCouponInterpolationGearingDefinition[] {nominalPayment});
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(START_DATE_TIPS_1, MATURITY_DATE_TIPS_1, COUPON_PERIOD_TIPS_1);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, BUSINESS_DAY_USD, CALENDAR_USD);
    final CouponInflationZeroCouponInterpolationGearingDefinition[] coupons = new CouponInflationZeroCouponInterpolationGearingDefinition[paymentDates.length];
    coupons[0] = CouponInflationZeroCouponInterpolationGearingDefinition.from(paymentDates[0], START_DATE_TIPS_1, paymentDatesUnadjusted[0], NOTIONAL_TIPS_1, PRICE_INDEX_USCPI, INDEX_START_TIPS_1,
        MONTH_LAG_TIPS_1, true, REAL_RATE_TIPS_1);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationZeroCouponInterpolationGearingDefinition.from(paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], NOTIONAL_TIPS_1,
          PRICE_INDEX_USCPI, INDEX_START_TIPS_1, MONTH_LAG_TIPS_1, true, REAL_RATE_TIPS_1);
    }
    AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> couponAnnuity = new AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>(coupons);
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> bond = new BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>(
        nominalAnnuity, couponAnnuity, INDEX_START_TIPS_1, 0, 2, CALENDAR_USD, DAY_COUNT_TIPS_1, YIELD_CONVENTION_TIPS_1, IS_EOM_TIPS_1, MONTH_LAG_TIPS_1, ISSUER_US);
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> bondFrom = BondCapitalIndexedSecurityDefinition.fromInterpolation(PRICE_INDEX_USCPI,
        MONTH_LAG_TIPS_1, START_DATE_TIPS_1, INDEX_START_TIPS_1, MATURITY_DATE_TIPS_1, COUPON_PERIOD_TIPS_1, NOTIONAL_TIPS_1, REAL_RATE_TIPS_1, BUSINESS_DAY_USD, SETTLEMENT_DAYS_TIPS_1, CALENDAR_USD,
        DAY_COUNT_TIPS_1, YIELD_CONVENTION_TIPS_1, IS_EOM_TIPS_1, ISSUER_US);
    assertEquals("Capital Index Bond: constructor", bond, bondFrom);
  }

}

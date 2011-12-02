/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;

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
import com.opengamma.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.financial.instrument.index.IndexPrice;
import com.opengamma.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflation;
import com.opengamma.financial.interestrate.market.MarketDataSets;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Tests the construction of Inflation Capital index bonds.
 */
public class BondCapitalIndexedSecurityTest {
  // Index-Lined Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final String NAME = "UK RPI";
  private static final Currency CUR = Currency.GBP;
  private static final Currency REGION = Currency.GBP;
  private static final Period LAG = Period.ofDays(14);
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR, REGION, LAG);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount DAY_COUNT_GILT_1 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final boolean IS_EOM_GILT_1 = false;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime FIRST_COUPON_DATE = DateUtils.getUTCDate(2003, 1, 26);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2035, 1, 26);
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG = 8;
  private static final double INDEX_START = 173.60; // November 2001 
  private static final double REAL_RATE = 0.02;
  private static final double NOTIONAL_GILT_1 = 1.00;
  private static final Period COUPON_PERIOD = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR_GILT_1 = 2;
  private static final int SETTLEMENT_DAYS = 2;
  private static final String ISSUER_UK = "UK GOVT";
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 8);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(PRICING_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> BOND_SECURITY_DEFINITION = BondCapitalIndexedSecurityDefinition.fromMonthly(PRICE_INDEX,
      MONTH_LAG, START_DATE, INDEX_START, FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD, NOTIONAL_GILT_1, REAL_RATE, BUSINESS_DAY, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_GILT_1, YIELD_CONVENTION,
      IS_EOM_GILT_1, ISSUER_UK);
  private static final double SETTLEMENT_TIME = TimeCalculator.getTimeBetween(PRICING_DATE, SPOT_DATE);
  private static final DoubleTimeSeries<ZonedDateTime> UK_RPI = MarketDataSets.ukRpiFrom2010();
  @SuppressWarnings("unchecked")
  private static final GenericAnnuity<Coupon> NOMINAL = (GenericAnnuity<Coupon>) BOND_SECURITY_DEFINITION.getNominal().toDerivative(PRICING_DATE, "Not used");
  @SuppressWarnings("unchecked")
  private static final GenericAnnuity<Coupon> COUPON = (GenericAnnuity<Coupon>) BOND_SECURITY_DEFINITION.getCoupon().toDerivative(PRICING_DATE, UK_RPI, "Not used");
  private static final double ACCRUED_INTEREST = BOND_SECURITY_DEFINITION.accruedInterest(SPOT_DATE);
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<CouponDefinition> COUPON_DEFINITION = (AnnuityDefinition<CouponDefinition>) BOND_SECURITY_DEFINITION.getCoupon().trimBefore(SPOT_DATE);
  private static final double factorSpot = DAY_COUNT_GILT_1.getAccruedInterest(COUPON_DEFINITION.getNthPayment(0).getAccrualStartDate(), SPOT_DATE, COUPON_DEFINITION.getNthPayment(0)
      .getAccrualEndDate(), 1.0, COUPON_PER_YEAR_GILT_1);
  private static final double factorPeriod = DAY_COUNT_GILT_1.getAccruedInterest(COUPON_DEFINITION.getNthPayment(0).getAccrualStartDate(), COUPON_DEFINITION.getNthPayment(0).getAccrualEndDate(),
      COUPON_DEFINITION.getNthPayment(0).getAccrualEndDate(), 1.0, COUPON_PER_YEAR_GILT_1);
  private static final double FACTOR_TO_NEXT = (factorPeriod - factorSpot) / factorPeriod;

  private static final CouponInflationDefinition NOMINAL_LAST = BOND_SECURITY_DEFINITION.getNominal().getNthPayment(BOND_SECURITY_DEFINITION.getNominal().getNumberOfPayments() - 1);
  private static final CouponInflationDefinition SETTLEMENT_DEFINITION = NOMINAL_LAST.with(SPOT_DATE, NOMINAL_LAST.getAccrualStartDate(), SPOT_DATE, 1.0);
  private static final CouponInflation SETTLEMENT = (CouponInflation) SETTLEMENT_DEFINITION.toDerivative(PRICING_DATE, "Not used");

  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY = new BondCapitalIndexedSecurity<Coupon>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION,
      COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, ISSUER_UK);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondCapitalIndexedSecurity<Coupon>(null, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondCapitalIndexedSecurity<Coupon>(NOMINAL, null, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    new BondCapitalIndexedSecurity<Coupon>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, null, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettle() {
    new BondCapitalIndexedSecurity<Coupon>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, null, INDEX_START, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIssuer() {
    new BondCapitalIndexedSecurity<Coupon>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, null);
  }

  @Test
  public void getter() {
    assertEquals("Inflation Capital Indexed bond: getter", YIELD_CONVENTION, BOND_SECURITY.getYieldConvention());
    assertEquals("Inflation Capital Indexed bond: getter", NOMINAL, BOND_SECURITY.getNominal());
    assertEquals("Inflation Capital Indexed bond: getter", COUPON, BOND_SECURITY.getCoupon());
    assertEquals("Inflation Capital Indexed bond: getter", ACCRUED_INTEREST, BOND_SECURITY.getAccruedInterest());
    assertEquals("Inflation Capital Indexed bond: getter", FACTOR_TO_NEXT, BOND_SECURITY.getAccrualFactorToNextCoupon());
    assertEquals("Inflation Capital Indexed bond: getter", COUPON_PER_YEAR_GILT_1, BOND_SECURITY.getCouponPerYear());
    assertEquals("Inflation Capital Indexed bond: getter", INDEX_START, BOND_SECURITY.getIndexStartValue());
  }

}

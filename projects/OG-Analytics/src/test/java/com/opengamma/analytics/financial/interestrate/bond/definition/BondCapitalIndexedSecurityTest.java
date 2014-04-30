/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the construction of Inflation Capital index bonds.
 */
@Test(groups = TestGroup.UNIT)
public class BondCapitalIndexedSecurityTest {
  // Index-Lined Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final String NAME = "UK RPI";
  private static final Currency CUR = Currency.GBP;
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final DayCount DAY_COUNT_GILT_1 = DayCounts.ACT_ACT_ISDA;
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
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(PRICING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> BOND_SECURITY_DEFINITION = BondCapitalIndexedSecurityDefinition.fromMonthly(PRICE_INDEX,
      MONTH_LAG, START_DATE, INDEX_START, FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD, NOTIONAL_GILT_1, REAL_RATE, BUSINESS_DAY, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_GILT_1, YIELD_CONVENTION,
      IS_EOM_GILT_1, ISSUER_UK);
  private static final double SETTLEMENT_TIME = TimeCalculator.getTimeBetween(PRICING_DATE, SPOT_DATE);
  private static final DoubleTimeSeries<ZonedDateTime> UK_RPI = MulticurveProviderDiscountDataSets.ukRpiFrom2010();
  private static final Annuity<Coupon> NOMINAL = (Annuity<Coupon>) BOND_SECURITY_DEFINITION.getNominal().toDerivative(PRICING_DATE);
  private static final Annuity<Coupon> COUPON = (Annuity<Coupon>) BOND_SECURITY_DEFINITION.getCoupons().toDerivative(PRICING_DATE, UK_RPI);
  private static final double ACCRUED_INTEREST = BOND_SECURITY_DEFINITION.accruedInterest(SPOT_DATE);
  private static final AnnuityDefinition<CouponDefinition> COUPON_DEFINITION = (AnnuityDefinition<CouponDefinition>) BOND_SECURITY_DEFINITION.getCoupons().trimBefore(SPOT_DATE);
  private static final double factorSpot = DAY_COUNT_GILT_1.getAccruedInterest(COUPON_DEFINITION.getNthPayment(0).getAccrualStartDate(), SPOT_DATE, COUPON_DEFINITION.getNthPayment(0)
      .getAccrualEndDate(), 1.0, COUPON_PER_YEAR_GILT_1);
  private static final double factorPeriod = DAY_COUNT_GILT_1.getAccruedInterest(COUPON_DEFINITION.getNthPayment(0).getAccrualStartDate(), COUPON_DEFINITION.getNthPayment(0).getAccrualEndDate(),
      COUPON_DEFINITION.getNthPayment(0).getAccrualEndDate(), 1.0, COUPON_PER_YEAR_GILT_1);
  private static final double FACTOR_TO_NEXT = (factorPeriod - factorSpot) / factorPeriod;

  private static final CouponInflationDefinition NOMINAL_LAST = BOND_SECURITY_DEFINITION.getNominal().getNthPayment(BOND_SECURITY_DEFINITION.getNominal().getNumberOfPayments() - 1);
  private static final CouponInflationDefinition SETTLEMENT_DEFINITION = NOMINAL_LAST.with(SPOT_DATE, NOMINAL_LAST.getAccrualStartDate(), SPOT_DATE, 1.0);
  private static final CouponInflation SETTLEMENT = (CouponInflation) SETTLEMENT_DEFINITION.toDerivative(PRICING_DATE);

  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY = new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0,
      YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondCapitalIndexedSecurity<>(null, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0, YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondCapitalIndexedSecurity<>(NOMINAL, null, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0, YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0, null, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettle() {
    new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0, YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, null, INDEX_START, 100, 100, 1.0, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIssuer1() {
    new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0, YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, (String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIssuer2() {
    new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0, YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, (LegalEntity) null);
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

  @Test
  public void testHashCodeEquals() {
    final BondCapitalIndexedSecurity<Coupon> bond = new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0,
        YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
    BondCapitalIndexedSecurity<Coupon> other = new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0,
        YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
    assertEquals(bond, other);
    assertEquals(bond.hashCode(), other.hashCode());
    other = new BondCapitalIndexedSecurity<>(COUPON, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0,
        YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
    assertFalse(other.equals(bond));
    other = new BondCapitalIndexedSecurity<>(NOMINAL, NOMINAL, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0,
        YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
    assertFalse(other.equals(bond));
    other = new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST + 1, FACTOR_TO_NEXT, 0,
        YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
    assertFalse(other.equals(bond));
    other = new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT + 1, 0,
        YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
    assertFalse(other.equals(bond));
    other = new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0,
        SimpleYieldConvention.AUSTRIA_ISMA_METHOD, COUPON_PER_YEAR_GILT_1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
    assertFalse(other.equals(bond));
    other = new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0,
        YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1 + 1, SETTLEMENT, INDEX_START, 100, 100, 1.0, ISSUER_UK);
    assertFalse(other.equals(bond));
    other = new BondCapitalIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, 0,
        YIELD_CONVENTION, COUPON_PER_YEAR_GILT_1, (CouponInflation) SETTLEMENT_DEFINITION.toDerivative(PRICING_DATE.minusDays(1)), INDEX_START, 100, 100, 1.0, ISSUER_UK);
    assertFalse(other.equals(bond));
  }
}

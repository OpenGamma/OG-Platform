/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.instrument.bond.BondInterestIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
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
 *  Tests the construction of Inflation Interest index bonds.
 */
@Test(groups = TestGroup.UNIT)
public class BondInterestIndexedSecurityTest {

  private static final String NAME = "UK RPI";
  private static final Currency CUR = Currency.GBP;
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ISDA;
  private static final boolean IS_EOM = false;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime FIRST_COUPON_DATE = DateUtils.getUTCDate(2003, 1, 26);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2035, 1, 26);
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG = 8;
  private static final double REAL_RATE = 0.02;
  private static final double NOTIONAL = 1.00;
  private static final Period COUPON_PERIOD = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR = 2;
  private static final int SETTLEMENT_DAYS = 2;
  private static final String ISSUER_UK = "UK GOVT";
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 8);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(PRICING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> BOND_SECURITY_DEFINITION = BondInterestIndexedSecurityDefinition
      .fromMonthly(PRICE_INDEX, MONTH_LAG, START_DATE, FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD, NOTIONAL, REAL_RATE, BUSINESS_DAY, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT,
          YIELD_CONVENTION, IS_EOM, ISSUER_UK);
  private static final double SETTLEMENT_TIME = TimeCalculator.getTimeBetween(PRICING_DATE, SPOT_DATE);
  private static final DoubleTimeSeries<ZonedDateTime> UK_RPI = MulticurveProviderDiscountDataSets.ukRpiFrom2010();
  private static final Annuity<PaymentFixed> NOMINAL = (Annuity<PaymentFixed>) BOND_SECURITY_DEFINITION.getNominal().toDerivative(PRICING_DATE);
  private static final Annuity<Coupon> COUPON = (Annuity<Coupon>) BOND_SECURITY_DEFINITION.getCoupons().toDerivative(PRICING_DATE, UK_RPI);
  private static final double ACCRUED_INTEREST = BOND_SECURITY_DEFINITION.accruedInterest(SPOT_DATE);
  private static final AnnuityDefinition<CouponDefinition> COUPON_DEFINITION = (AnnuityDefinition<CouponDefinition>) BOND_SECURITY_DEFINITION.getCoupons().trimBefore(SPOT_DATE);
  private static final double FACTOR_SPOT = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION.getNthPayment(0).getAccrualStartDate(), SPOT_DATE, COUPON_DEFINITION.getNthPayment(0)
      .getAccrualEndDate(), 1.0, COUPON_PER_YEAR);
  private static final double FACTOR_PERIOD = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION.getNthPayment(0).getAccrualStartDate(), COUPON_DEFINITION.getNthPayment(0).getAccrualEndDate(),
      COUPON_DEFINITION.getNthPayment(0).getAccrualEndDate(), 1.0, COUPON_PER_YEAR);
  private static final double FACTOR_TO_NEXT = (FACTOR_PERIOD - FACTOR_SPOT) / FACTOR_PERIOD;

  private static final PaymentFixedDefinition NOMINAL_LAST = BOND_SECURITY_DEFINITION.getNominal().getNthPayment(BOND_SECURITY_DEFINITION.getNominal().getNumberOfPayments() - 1);
  final ZonedDateTime SETTLEMENT_DATE = SPOT_DATE.isBefore(PRICING_DATE) ? PRICING_DATE : SPOT_DATE;
  private static final PaymentFixedDefinition SETTLEMENT_DEFINITION = new PaymentFixedDefinition(NOMINAL_LAST.getCurrency(), NOMINAL_LAST.getPaymentDate(), NOMINAL_LAST.getReferenceAmount());
  private static final PaymentFixed SETTLEMENT = SETTLEMENT_DEFINITION.toDerivative(PRICING_DATE);

  private static final BondInterestIndexedSecurity<PaymentFixed, Coupon> BOND_SECURITY = new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST,
      FACTOR_TO_NEXT,
      YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondInterestIndexedSecurity<>(null, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondInterestIndexedSecurity<>(NOMINAL, null, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK,
        PRICE_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, null, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettle() {
    new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, null, ISSUER_UK, PRICE_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIssuer1() {
    new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, (String) null, PRICE_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIssuer2() {
    new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, (LegalEntity) null, PRICE_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPriceIndex() {
    new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST, FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, null);
  }

  @Test
  public void getter() {
    assertEquals("Inflation Interest Indexed bond: getter", YIELD_CONVENTION, BOND_SECURITY.getYieldConvention());
    assertEquals("Inflation Interest Indexed bond: getter", NOMINAL, BOND_SECURITY.getNominal());
    assertEquals("Inflation Interest Indexed bond: getter", COUPON, BOND_SECURITY.getCoupon());
    assertEquals("Inflation Interest Indexed bond: getter", ACCRUED_INTEREST, BOND_SECURITY.getAccruedInterest());
    assertEquals("Inflation Interest Indexed bond: getter", FACTOR_TO_NEXT, BOND_SECURITY.getAccrualFactorToNextCoupon());
    assertEquals("Inflation Interest Indexed bond: getter", COUPON_PER_YEAR, BOND_SECURITY.getCouponPerYear());
    assertEquals("Inflation Interest Indexed bond: getter", PRICE_INDEX, BOND_SECURITY.getPriceIndex());
    assertEquals("Inflation Interest Indexed bond: getter", CUR, BOND_SECURITY.getCurrency());
    assertEquals("Inflation Interest Indexed bond: getter", new LegalEntity(null, ISSUER_UK, null, null, null), BOND_SECURITY.getIssuerEntity());
  }

  @Test
  public void testHashCodeEquals() {
    final BondInterestIndexedSecurity<PaymentFixed, Coupon> bond = new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST,
        FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
    BondInterestIndexedSecurity<PaymentFixed, Coupon> other = new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST,
        FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
    assertEquals(bond, other);
    assertEquals(bond.hashCode(), other.hashCode());
    other = new BondInterestIndexedSecurity<>((Annuity<PaymentFixed>) BOND_SECURITY_DEFINITION.getNominal().toDerivative(PRICING_DATE.minusDays(1)), COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST,
        FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
    assertFalse(other.equals(bond));
    other = new BondInterestIndexedSecurity<>(NOMINAL, (Annuity<Coupon>) BOND_SECURITY_DEFINITION.getCoupons().toDerivative(PRICING_DATE.minusDays(1), UK_RPI), SETTLEMENT_TIME, ACCRUED_INTEREST,
        FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
    assertFalse(other.equals(bond));
    other = new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST + 1,
        FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
    assertFalse(other.equals(bond));
    other = new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST,
        FACTOR_TO_NEXT + 1, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
    assertFalse(other.equals(bond));
    other = new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST,
        FACTOR_TO_NEXT, SimpleYieldConvention.AUSTRIA_ISMA_METHOD, COUPON_PER_YEAR, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
    assertFalse(other.equals(bond));
    other = new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST,
        FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR + 1, SETTLEMENT, ISSUER_UK, PRICE_INDEX);
    assertFalse(other.equals(bond));
    other = new BondInterestIndexedSecurity<>(NOMINAL, COUPON, SETTLEMENT_TIME, ACCRUED_INTEREST,
        FACTOR_TO_NEXT, YIELD_CONVENTION, COUPON_PER_YEAR, SETTLEMENT_DEFINITION.toDerivative(PRICING_DATE.plusDays(1)), ISSUER_UK, PRICE_INDEX);
    assertFalse(other.equals(bond));
  }
}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthlyWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondinterestIndexedSecurityDefinitionTest {
  //Index-Linked Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final String NAME_INDEX_UK = "UK RPI";
  private static final IndexPrice PRICE_INDEX_UKRPI = new IndexPrice(NAME_INDEX_UK, Currency.GBP);
  private static final Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY_GBP = BusinessDayConventions.FOLLOWING;
  private static final DayCount DAY_COUNT_1 = DayCounts.ACT_ACT_ISDA;
  private static final boolean IS_EOM_1 = false;
  private static final ZonedDateTime START_DATE_1 = DateUtils.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime FIRST_COUPON_DATE_1 = DateUtils.getUTCDate(2003, 1, 26);
  private static final ZonedDateTime MATURITY_DATE_1 = DateUtils.getUTCDate(2012, 1, 26);
  private static final YieldConvention YIELD_CONVENTION_1 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG_1 = 8;
  private static final double NOTIONAL_1 = 1.00;
  private static final double REAL_RATE_1 = 0.02;
  private static final Period COUPON_PERIOD_1 = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR_1 = 2;
  private static final int SETTLEMENT_DAYS_1 = 2;
  private static final double FACTOR = 0.02;
  private static final String ISSUER_UK = "UK GOVT";
  private static final BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> BOND_SECURITY_DEFINITION = BondInterestIndexedSecurityDefinition
      .fromMonthly(PRICE_INDEX_UKRPI, MONTH_LAG_1, START_DATE_1, FIRST_COUPON_DATE_1, MATURITY_DATE_1, COUPON_PERIOD_1,
          NOTIONAL_1, REAL_RATE_1, BUSINESS_DAY_GBP, SETTLEMENT_DAYS_1, CALENDAR_GBP, DAY_COUNT_1, YIELD_CONVENTION_1, IS_EOM_1, ISSUER_UK);

  @Test
  public void getter() {
    assertEquals("Capital Index Bond", DAY_COUNT_1, BOND_SECURITY_DEFINITION.getDayCount());
    assertEquals("Capital Index Bond", IS_EOM_1, BOND_SECURITY_DEFINITION.isEOM());
    assertEquals("Capital Index Bond", YIELD_CONVENTION_1, BOND_SECURITY_DEFINITION.getYieldConvention());
    assertEquals("Capital Index Bond", COUPON_PER_YEAR_1, BOND_SECURITY_DEFINITION.getCouponPerYear());
    assertEquals("Capital Index Bond", MONTH_LAG_1, BOND_SECURITY_DEFINITION.getMonthLag());
    assertEquals("Capital Index Bond", CALENDAR_GBP, BOND_SECURITY_DEFINITION.getCalendar());
    assertEquals("Capital Index Bond", PRICE_INDEX_UKRPI, BOND_SECURITY_DEFINITION.getPriceIndex());
    assertEquals("Capital Index Bond", PRICE_INDEX_UKRPI.getCurrency(), BOND_SECURITY_DEFINITION.getCurrency());
  }

  @Test
  /**
   * Tests the bond constructors.
   */
  public void constructorBondsWithFirstCouponDate() {
    // Nominal construction
    final PaymentFixedDefinition nominalPayment = new PaymentFixedDefinition(PRICE_INDEX_UKRPI.getCurrency(), BUSINESS_DAY_GBP.adjustDate(CALENDAR_GBP, MATURITY_DATE_1), NOTIONAL_1);
    final AnnuityDefinition<PaymentFixedDefinition> nominalAnnuity = new AnnuityDefinition<>(new PaymentFixedDefinition[] {nominalPayment }, CALENDAR_GBP);
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(FIRST_COUPON_DATE_1, MATURITY_DATE_1, COUPON_PERIOD_1,
        true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, BUSINESS_DAY_GBP, CALENDAR_GBP, false);
    final CouponInflationYearOnYearMonthlyWithMarginDefinition[] coupons = new CouponInflationYearOnYearMonthlyWithMarginDefinition[paymentDates.length + 1];
    coupons[0] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(FACTOR, START_DATE_1,
        ScheduleCalculator.getAdjustedDate(FIRST_COUPON_DATE_1, 0, CALENDAR_GBP), NOTIONAL_1, PRICE_INDEX_UKRPI, MONTH_LAG_1, true);
    coupons[1] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(FACTOR, FIRST_COUPON_DATE_1, paymentDates[0], NOTIONAL_1,
        PRICE_INDEX_UKRPI, MONTH_LAG_1, true);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn + 1] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(FACTOR, paymentDatesUnadjusted[loopcpn - 1], paymentDates[loopcpn],
          NOTIONAL_1, PRICE_INDEX_UKRPI, MONTH_LAG_1, true);
    }
    final AnnuityDefinition<CouponInflationYearOnYearMonthlyWithMarginDefinition> couponAnnuity = new AnnuityDefinition<>(
        coupons, CALENDAR_GBP);
    final BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> bond = new BondInterestIndexedSecurityDefinition<>(
        nominalAnnuity, couponAnnuity, 0, 2, CALENDAR_GBP, DAY_COUNT_1, YIELD_CONVENTION_1, IS_EOM_1, MONTH_LAG_1, ISSUER_UK);
    final BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> bondFrom = BondInterestIndexedSecurityDefinition.fromMonthly(
        PRICE_INDEX_UKRPI, MONTH_LAG_1, START_DATE_1, FIRST_COUPON_DATE_1, MATURITY_DATE_1, COUPON_PERIOD_1,
        NOTIONAL_1, FACTOR, BUSINESS_DAY_GBP, SETTLEMENT_DAYS_1, CALENDAR_GBP, DAY_COUNT_1, YIELD_CONVENTION_1, IS_EOM_1, ISSUER_UK);
    assertEquals("Interest Index Bond: constructor", bond, bondFrom);
  }

  @Test
  /**
   * Tests the bond constructors.
   */
  public void constructorBondsWithoutFirstCouponDate() {
    // Nominal construction
    final PaymentFixedDefinition nominalPayment = new PaymentFixedDefinition(PRICE_INDEX_UKRPI.getCurrency(), BUSINESS_DAY_GBP.adjustDate(CALENDAR_GBP, MATURITY_DATE_1), NOTIONAL_1);
    final AnnuityDefinition<PaymentFixedDefinition> nominalAnnuity = new AnnuityDefinition<>(new PaymentFixedDefinition[] {nominalPayment }, CALENDAR_GBP);
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(START_DATE_1, MATURITY_DATE_1, COUPON_PERIOD_1,
        true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, BUSINESS_DAY_GBP, CALENDAR_GBP, false);
    final CouponInflationYearOnYearMonthlyWithMarginDefinition[] coupons = new CouponInflationYearOnYearMonthlyWithMarginDefinition[paymentDates.length];

    coupons[0] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(FACTOR, START_DATE_1, paymentDates[0], NOTIONAL_1,
        PRICE_INDEX_UKRPI, MONTH_LAG_1, true);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(FACTOR, paymentDatesUnadjusted[loopcpn - 1], paymentDates[loopcpn],
          NOTIONAL_1, PRICE_INDEX_UKRPI, MONTH_LAG_1, true);
    }
    final AnnuityDefinition<CouponInflationYearOnYearMonthlyWithMarginDefinition> couponAnnuity = new AnnuityDefinition<>(
        coupons, CALENDAR_GBP);
    final BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> bond = new BondInterestIndexedSecurityDefinition<>(
        nominalAnnuity, couponAnnuity, 0, 2, CALENDAR_GBP, DAY_COUNT_1, YIELD_CONVENTION_1, IS_EOM_1, MONTH_LAG_1, ISSUER_UK);
    final BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> bondFrom = BondInterestIndexedSecurityDefinition.fromMonthly(
        PRICE_INDEX_UKRPI, MONTH_LAG_1, START_DATE_1, MATURITY_DATE_1, COUPON_PERIOD_1, NOTIONAL_1, FACTOR, BUSINESS_DAY_GBP, SETTLEMENT_DAYS_1, CALENDAR_GBP, DAY_COUNT_1, YIELD_CONVENTION_1,
        IS_EOM_1, ISSUER_UK);
    assertEquals("Interest Index Bond: constructor", bond, bondFrom);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative1Coupon() {
    final DoubleTimeSeries<ZonedDateTime> ukRpi = MulticurveProviderDiscountDataSets.ukRpiFrom2010();
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 8, 3); // One coupon fixed
    final BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> bondFromDefinition = BondInterestIndexedSecurityDefinition.fromMonthly(
        PRICE_INDEX_UKRPI, MONTH_LAG_1, START_DATE_1, FIRST_COUPON_DATE_1, MATURITY_DATE_1, COUPON_PERIOD_1,
        NOTIONAL_1, FACTOR, BUSINESS_DAY_GBP, SETTLEMENT_DAYS_1, CALENDAR_GBP, DAY_COUNT_1, YIELD_CONVENTION_1, IS_EOM_1, ISSUER_UK);
    final BondInterestIndexedSecurity<PaymentFixed, Coupon> bond = bondFromDefinition.toDerivative(pricingDate, ukRpi);
    final ZonedDateTime referenceDateNextCoupon = DateUtils.getUTCDate(2011, 5, 31); // May 11
    final double referenceIndexNextCoupon = ukRpi.getValue(referenceDateNextCoupon);
    final ZonedDateTime referenceStartDateNextCoupon = DateUtils.getUTCDate(2010, 11, 30); // May 11
    final double referenceStartIndexNextCoupon = ukRpi.getValue(referenceStartDateNextCoupon);
    final double amountNextCoupon = (referenceIndexNextCoupon / referenceStartIndexNextCoupon + FACTOR) * NOTIONAL_1;
    assertEquals("Interest Index Bond: toDerivative", amountNextCoupon, ((CouponFixed) bond.getCoupon().getNthPayment(0)).getAmount());
    for (int loopcpn = 1; loopcpn < bond.getCoupon().getNumberOfPayments(); loopcpn++) {
      assertTrue("Interest Index Bond: toDerivative", (bond.getCoupon().getNthPayment(loopcpn) instanceof CouponInflationYearOnYearMonthlyWithMargin));
      assertEquals("Interest Index Bond: toDerivative", ((CouponInflationYearOnYearMonthlyWithMargin) bond.getCoupon().getNthPayment(loopcpn)).getPriceIndex(),
          PRICE_INDEX_UKRPI);
    }
    final Annuity<PaymentFixed> nominal = (Annuity<PaymentFixed>) bondFromDefinition.getNominal().toDerivative(pricingDate);
    final Annuity<Coupon> coupon = (Annuity<Coupon>) bondFromDefinition.getCoupons().toDerivative(pricingDate, ukRpi);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(pricingDate, SETTLEMENT_DAYS_1, CALENDAR_GBP);
    final double settleTime = TimeCalculator.getTimeBetween(pricingDate, spot);
    final AnnuityDefinition<CouponDefinition> couponDefinition = (AnnuityDefinition<CouponDefinition>) bondFromDefinition.getCoupons().trimBefore(spot);
    final double accruedInterest = bondFromDefinition.accruedInterest(spot);
    final double factorSpot = DAY_COUNT_1.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spot, couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), 1.0, COUPON_PER_YEAR_1);
    final double factorPeriod = DAY_COUNT_1.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, COUPON_PER_YEAR_1);
    final double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    final PaymentFixedDefinition nominalLast = bondFromDefinition.getNominal().getNthPayment(bondFromDefinition.getNominal().getNumberOfPayments() - 1);
    final ZonedDateTime settlementDate2 = spot;
    final double notional = 1.0;
    final PaymentFixedDefinition settlementDefinition = new PaymentFixedDefinition(nominalLast.getCurrency(), settlementDate2, notional);
    final PaymentFixed settlement = settlementDefinition.toDerivative(pricingDate);
    final BondInterestIndexedSecurity<PaymentFixed, Coupon> bondSecurityExpected = new BondInterestIndexedSecurity<>(nominal, coupon, settleTime, accruedInterest,
        factorToNextCoupon, YIELD_CONVENTION_1, COUPON_PER_YEAR_1, settlement, ISSUER_UK, PRICE_INDEX_UKRPI);
    assertEquals("Interest Index Bond: toDerivative", bondSecurityExpected, bond);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative2Coupon() {
    final DoubleTimeSeries<ZonedDateTime> ukRpi = MulticurveProviderDiscountDataSets.ukRpiFrom2010();
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 15); // Two coupons fixed
    final BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> bondFromDefinition = BondInterestIndexedSecurityDefinition.fromMonthly(
        PRICE_INDEX_UKRPI, MONTH_LAG_1, START_DATE_1, FIRST_COUPON_DATE_1, MATURITY_DATE_1, COUPON_PERIOD_1,
        NOTIONAL_1, FACTOR, BUSINESS_DAY_GBP, SETTLEMENT_DAYS_1, CALENDAR_GBP, DAY_COUNT_1, YIELD_CONVENTION_1, IS_EOM_1, ISSUER_UK);
    final BondInterestIndexedSecurity<PaymentFixed, Coupon> bond = bondFromDefinition.toDerivative(pricingDate, ukRpi);
    final ZonedDateTime[] referenceDateNextCoupon = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 5, 31), DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2011, 5, 31) }; // Nov 10, May 11
    final double[] referenceIndexNextCoupon = new double[] {ukRpi.getValue(referenceDateNextCoupon[0]), ukRpi.getValue(referenceDateNextCoupon[1]), ukRpi.getValue(referenceDateNextCoupon[2]) };
    for (int loopcpn = 0; loopcpn < 2; loopcpn++) {
      final double amountNextCoupon = (referenceIndexNextCoupon[loopcpn + 1] / referenceIndexNextCoupon[loopcpn] + FACTOR) * NOTIONAL_1;
      assertEquals("Capital Index Bond: toDerivative", amountNextCoupon, ((CouponFixed) bond.getCoupon().getNthPayment(loopcpn)).getAmount());
    }
    for (int loopcpn = 2; loopcpn < bond.getCoupon().getNumberOfPayments(); loopcpn++) {
      assertTrue("Capital Index Bond: toDerivative", (bond.getCoupon().getNthPayment(loopcpn) instanceof CouponInflationZeroCouponMonthlyGearing));
      assertEquals("Capital Index Bond: toDerivative", ((CouponInflationYearOnYearMonthlyWithMargin) bond.getCoupon().getNthPayment(loopcpn)).getPriceIndex(),
          PRICE_INDEX_UKRPI);
    }
  }

}

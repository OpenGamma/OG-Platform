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
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponFirstOfMonthDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponFirstOfMonth;
import com.opengamma.financial.interestrate.market.MarketDataSets;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

public class BondCapitalIndexedSecurityDefinitionTest {
  // Index-Lined Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final String NAME = "UK RPI";
  private static final Currency CUR = Currency.GBP;
  private static final Currency REGION = Currency.GBP;
  private static final Period LAG = Period.ofDays(14);
  private static final PriceIndex PRICE_INDEX = new PriceIndex(NAME, CUR, REGION, LAG);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final ZonedDateTime START_DATE = DateUtil.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime FIRST_COUPON_DATE = DateUtil.getUTCDate(2003, 1, 26);
  private static final ZonedDateTime MATURITY_DATE = DateUtil.getUTCDate(2035, 1, 26);
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG = 8;
  private static final double INDEX_START = 173.60; // November 2001 
  private static final double NOTIONAL = 1.00;
  private static final double REAL_RATE = 0.02;
  private static final Period COUPON_PERIOD = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final String ISSUER_UK = "UK GOVT";

  @Test
  /**
   * Tests the bond constructors.
   */
  public void constructor() {
    // Nominal construction
    CouponInflationZeroCouponFirstOfMonthDefinition nominalPayment = CouponInflationZeroCouponFirstOfMonthDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, PRICE_INDEX, INDEX_START, MONTH_LAG,
        true);
    AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> nominalAnnuity = new AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition>(
        new CouponInflationZeroCouponFirstOfMonthDefinition[] {nominalPayment});
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, BUSINESS_DAY, CALENDAR);
    final CouponInflationZeroCouponFirstOfMonthDefinition[] coupons = new CouponInflationZeroCouponFirstOfMonthDefinition[paymentDates.length + 1];
    coupons[0] = CouponInflationZeroCouponFirstOfMonthDefinition.from(START_DATE, FIRST_COUPON_DATE, NOTIONAL * REAL_RATE, PRICE_INDEX, INDEX_START, MONTH_LAG, true);
    for (int loopcpn = 0; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn + 1] = CouponInflationZeroCouponFirstOfMonthDefinition.from(START_DATE, paymentDates[loopcpn], NOTIONAL * REAL_RATE, PRICE_INDEX, INDEX_START, MONTH_LAG, true);
    }
    AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> couponAnnuity = new AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition>(coupons);
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> bond = new BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition>(
        nominalAnnuity, couponAnnuity, 0, 2, CALENDAR, YIELD_CONVENTION, ISSUER_UK);
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> bondFrom = BondCapitalIndexedSecurityDefinition.fromFirstOfMonth(PRICE_INDEX, MONTH_LAG, START_DATE,
        INDEX_START, FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD, REAL_RATE, BUSINESS_DAY, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ISSUER_UK);
    assertEquals("Capital Index Bond: constructor", bond, bondFrom);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative1Coupon() {
    DoubleTimeSeries<ZonedDateTime> ukRpi = MarketDataSets.ukRpiFrom2010();
    ZonedDateTime pricingDate = DateUtil.getUTCDate(2011, 8, 3); // One coupon fixed
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> bondFromDefinition = BondCapitalIndexedSecurityDefinition.fromFirstOfMonth(PRICE_INDEX, MONTH_LAG,
        START_DATE, INDEX_START, FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD, REAL_RATE, BUSINESS_DAY, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ISSUER_UK);
    BondCapitalIndexedSecurity<Coupon> bond = bondFromDefinition.toDerivative(pricingDate, ukRpi, "Not used");
    ZonedDateTime referenceDateNextCoupon = DateUtil.getUTCDate(2011, 5, 1); // May 11
    double referenceIndexNextCoupon = ukRpi.getValue(referenceDateNextCoupon);
    double amountNextCoupon = referenceIndexNextCoupon / INDEX_START * NOTIONAL * REAL_RATE;
    assertEquals("Capital Index Bond: toDerivative", amountNextCoupon, ((CouponFixed) bond.getCoupon().getNthPayment(0)).getAmount());
    for (int loopcpn = 1; loopcpn < bond.getCoupon().getNumberOfPayments(); loopcpn++) {
      assertTrue("Capital Index Bond: toDerivative", (bond.getCoupon().getNthPayment(loopcpn) instanceof CouponInflationZeroCouponFirstOfMonth));
      assertEquals("Capital Index Bond: toDerivative", ((CouponInflationZeroCouponFirstOfMonth) bond.getCoupon().getNthPayment(loopcpn)).getIndexStartValue(), INDEX_START);
      assertEquals("Capital Index Bond: toDerivative", ((CouponInflationZeroCouponFirstOfMonth) bond.getCoupon().getNthPayment(loopcpn)).getPriceIndex(), PRICE_INDEX);
    }
    @SuppressWarnings("unchecked")
    final GenericAnnuity<Coupon> nominal = (GenericAnnuity<Coupon>) bondFromDefinition.getNominal().toDerivative(pricingDate, "Not used");
    @SuppressWarnings("unchecked")
    final GenericAnnuity<Coupon> coupon = (GenericAnnuity<Coupon>) bondFromDefinition.getCoupon().toDerivative(pricingDate, ukRpi, "Not used");
    final double settleTime = TimeCalculator.getTimeBetween(pricingDate, ScheduleCalculator.getAdjustedDate(pricingDate, CALENDAR, SETTLEMENT_DAYS));
    final BondCapitalIndexedSecurity<Coupon> bondSecurityExpected = new BondCapitalIndexedSecurity<Coupon>(nominal, coupon, settleTime, YIELD_CONVENTION, ISSUER_UK);
    assertEquals("Capital Index Bond: toDerivative", bondSecurityExpected, bond);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative2Coupon() {
    DoubleTimeSeries<ZonedDateTime> ukRpi = MarketDataSets.ukRpiFrom2010();
    ZonedDateTime pricingDate = DateUtil.getUTCDate(2011, 7, 15); // Two coupons fixed
    BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> bondFromDefinition = BondCapitalIndexedSecurityDefinition.fromFirstOfMonth(PRICE_INDEX, MONTH_LAG,
        START_DATE, INDEX_START, FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD, REAL_RATE, BUSINESS_DAY, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ISSUER_UK);
    BondCapitalIndexedSecurity<Coupon> bond = bondFromDefinition.toDerivative(pricingDate, ukRpi, "Not used");
    ZonedDateTime[] referenceDateNextCoupon = new ZonedDateTime[] {DateUtil.getUTCDate(2010, 11, 1), DateUtil.getUTCDate(2011, 5, 1)}; // Nov 10, May 11
    double[] referenceIndexNextCoupon = new double[] {ukRpi.getValue(referenceDateNextCoupon[0]), ukRpi.getValue(referenceDateNextCoupon[1])};
    for (int loopcpn = 0; loopcpn < 2; loopcpn++) {
      double amountNextCoupon = referenceIndexNextCoupon[loopcpn] / INDEX_START * NOTIONAL * REAL_RATE;
      assertEquals("Capital Index Bond: toDerivative", amountNextCoupon, ((CouponFixed) bond.getCoupon().getNthPayment(loopcpn)).getAmount());
    }
    for (int loopcpn = 2; loopcpn < bond.getCoupon().getNumberOfPayments(); loopcpn++) {
      assertTrue("Capital Index Bond: toDerivative", (bond.getCoupon().getNthPayment(loopcpn) instanceof CouponInflationZeroCouponFirstOfMonth));
      assertEquals("Capital Index Bond: toDerivative", ((CouponInflationZeroCouponFirstOfMonth) bond.getCoupon().getNthPayment(loopcpn)).getIndexStartValue(), INDEX_START);
      assertEquals("Capital Index Bond: toDerivative", ((CouponInflationZeroCouponFirstOfMonth) bond.getCoupon().getNthPayment(loopcpn)).getPriceIndex(), PRICE_INDEX);
    }
  }

  // TODO: TIPS

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationGearing;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueInflationCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.method.BondCapitalIndexedSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponInterpolationGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponMonthlyGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.market.MarketBundle;
import com.opengamma.analytics.financial.interestrate.market.MarketDataSets;
import com.opengamma.analytics.financial.interestrate.payments.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Tests the present value of Capital inflation indexed bonds.
 */
public class BondCapitalIndexedSecurityDiscountingMethodTest {
  private static final MarketBundle MARKET = MarketDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[0]);
  private static final IndexPrice PRICE_INDEX_USCPI = PRICE_INDEXES[0];
  private static final IndexPrice PRICE_INDEX_UKRPI = PRICE_INDEXES[2];
  private static final String[] ISSUER_NAMES = MARKET.getIssuers().toArray(new String[0]);
  private static final String ISSUER_UK_GOVT = ISSUER_NAMES[0];
  private static final String ISSUER_US_GOVT = ISSUER_NAMES[1];

  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 8);
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_INFLATION = new BondCapitalIndexedSecurityDiscountingMethod();
  private static final CouponInflationZeroCouponMonthlyGearingDiscountingMethod METHOD_INFLATION_ZC_MONTHLY = new CouponInflationZeroCouponMonthlyGearingDiscountingMethod();
  private static final CouponInflationZeroCouponInterpolationGearingDiscountingMethod METHOD_INFLATION_ZC_INTERPOLATION = new CouponInflationZeroCouponInterpolationGearingDiscountingMethod();
  private static final PresentValueInflationCalculator PVIC = PresentValueInflationCalculator.getInstance();

  // Index-Lined Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
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
  private static final int SETTLEMENT_DAYS_GILT_1 = 2;
  // TODO: ex-coupon 7 days
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> BOND_SECURITY_GILT_1_DEFINITION = BondCapitalIndexedSecurityDefinition.fromMonthly(
      PRICE_INDEX_UKRPI, MONTH_LAG_GILT_1, START_DATE_GILT_1, INDEX_START_GILT_1, FIRST_COUPON_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1, NOTIONAL_GILT_1, REAL_RATE_GILT_1,
      BUSINESS_DAY_GBP, SETTLEMENT_DAYS_GILT_1, CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, ISSUER_UK_GOVT);
  private static final DoubleTimeSeries<ZonedDateTime> UK_RPI = MarketDataSets.ukRpiFrom2010();
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY_GILT_1 = BOND_SECURITY_GILT_1_DEFINITION.toDerivative(PRICING_DATE, UK_RPI, "Not used");

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValueGilt1() {
    MarketBundle marketUKGovt = new MarketBundle();
    marketUKGovt.setCurve(BOND_SECURITY_GILT_1.getCurrency(), MARKET.getCurve(ISSUER_UK_GOVT));
    marketUKGovt.setCurve(PRICE_INDEX_UKRPI, MARKET.getCurve(PRICE_INDEX_UKRPI));
    CurrencyAmount pvNominal = METHOD_INFLATION_ZC_MONTHLY.presentValue(BOND_SECURITY_GILT_1.getNominal().getNthPayment(0), marketUKGovt);
    CurrencyAmount pvCoupon = CurrencyAmount.of(BOND_SECURITY_GILT_1.getCurrency(), 0.0);
    for (int loopcpn = 0; loopcpn < BOND_SECURITY_GILT_1.getCoupon().getNumberOfPayments(); loopcpn++) {
      pvCoupon = pvCoupon.plus(PVIC.visit(BOND_SECURITY_GILT_1.getCoupon().getNthPayment(loopcpn), marketUKGovt));
    }
    CurrencyAmount pvExpectd = pvNominal.plus(pvCoupon);
    CurrencyAmount pv = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY_GILT_1, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getAmount(), pv.getAmount(), 1.0E-2);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getCurrency(), pv.getCurrency());
  }

  @Test
  /**
   * Tests the present value Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    CurrencyAmount pvMethod = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY_GILT_1, MARKET);
    CurrencyAmount pvCalculator = PVIC.visit(BOND_SECURITY_GILT_1, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", pvMethod, pvCalculator);
  }

  // 2% 10-YEAR TREASURY INFLATION-PROTECTED SECURITIES (TIPS) Due January 15, 2016 - US912828ET33
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  private static final BusinessDayConvention BUSINESS_DAY_USD = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount DAY_COUNT_TIPS_1 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final boolean IS_EOM_TIPS_1 = false;
  private static final ZonedDateTime START_DATE_TIPS_1 = DateUtils.getUTCDate(2006, 1, 15);
  private static final ZonedDateTime MATURITY_DATE_TIPS_1 = DateUtils.getUTCDate(2016, 1, 15);
  private static final YieldConvention YIELD_CONVENTION_TIPS_1 = SimpleYieldConvention.US_IL_REAL;
  private static final int MONTH_LAG_TIPS_1 = 3;
  private static final double INDEX_START_TIPS_1 = 198.47742; // Date: 
  private static final double NOTIONAL_TIPS_1 = 100.00;
  private static final double REAL_RATE_TIPS_1 = 0.02;
  private static final Period COUPON_PERIOD_TIPS_1 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS_TIPS_1 = 1;

  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> BOND_SECURITY_TIPS_1_DEFINITION = BondCapitalIndexedSecurityDefinition
      .fromInterpolation(PRICE_INDEX_USCPI, MONTH_LAG_TIPS_1, START_DATE_TIPS_1, INDEX_START_TIPS_1, MATURITY_DATE_TIPS_1, COUPON_PERIOD_TIPS_1, NOTIONAL_TIPS_1, REAL_RATE_TIPS_1, BUSINESS_DAY_USD,
          SETTLEMENT_DAYS_TIPS_1, CALENDAR_USD, DAY_COUNT_TIPS_1, YIELD_CONVENTION_TIPS_1, IS_EOM_TIPS_1, ISSUER_US_GOVT);
  private static final DoubleTimeSeries<ZonedDateTime> US_CPI = MarketDataSets.usCpiFrom2009();
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY_TIPS_1 = BOND_SECURITY_TIPS_1_DEFINITION.toDerivative(PRICING_DATE, US_CPI, "Not used");

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValueTips1() {
    MarketBundle marketUSGovt = new MarketBundle();
    marketUSGovt.setCurve(BOND_SECURITY_TIPS_1.getCurrency(), MARKET.getCurve(ISSUER_US_GOVT));
    marketUSGovt.setCurve(PRICE_INDEX_USCPI, MARKET.getCurve(PRICE_INDEX_USCPI));
    CurrencyAmount pvNominal = METHOD_INFLATION_ZC_INTERPOLATION.presentValue(BOND_SECURITY_TIPS_1.getNominal().getNthPayment(0), marketUSGovt);
    CurrencyAmount pvCoupon = CurrencyAmount.of(BOND_SECURITY_TIPS_1.getCurrency(), 0.0);
    for (int loopcpn = 0; loopcpn < BOND_SECURITY_TIPS_1.getCoupon().getNumberOfPayments(); loopcpn++) {
      pvCoupon = pvCoupon.plus(PVIC.visit(BOND_SECURITY_TIPS_1.getCoupon().getNthPayment(loopcpn), marketUSGovt));
    }
    CurrencyAmount pvExpectd = pvNominal.plus(pvCoupon);
    CurrencyAmount pv = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY_TIPS_1, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getAmount(), pv.getAmount(), 1.0E-2);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getCurrency(), pv.getCurrency());
  }

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValueFromCleanPriceRealTips1() {
    double cleanPriceReal = 1.05;
    CurrencyAmount pv = METHOD_BOND_INFLATION.presentValueFromCleanPriceReal(BOND_SECURITY_TIPS_1, MARKET, cleanPriceReal);

    double dirtyReal = cleanPriceReal + BOND_SECURITY_TIPS_1.getAccruedInterest() / NOTIONAL_TIPS_1;
    ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(PRICING_DATE, SETTLEMENT_DAYS_TIPS_1, CALENDAR_USD);
    ZonedDateTime refInterpolatedDate = spot.minusMonths(MONTH_LAG_TIPS_1);
    ZonedDateTime[] referenceEndDate = new ZonedDateTime[2];
    referenceEndDate[0] = refInterpolatedDate.withDayOfMonth(1);
    referenceEndDate[1] = referenceEndDate[0].plusMonths(1);
    double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(PRICING_DATE, referenceEndDate[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(PRICING_DATE, referenceEndDate[1]);
    double estimatedIndexMonth0 = MARKET.getPriceIndex(PRICE_INDEX_USCPI, referenceEndTime[0]);
    double estimatedIndexMonth1 = MARKET.getPriceIndex(PRICE_INDEX_USCPI, referenceEndTime[1]);
    double weight = 1.0 - (spot.getDayOfMonth() - 1.0) / spot.getMonthOfYear().getLastDayOfMonth(spot.isLeapYear());
    double estimatedIndex = weight * estimatedIndexMonth0 + (1 - weight) * estimatedIndexMonth1;
    double pvAtSettle = dirtyReal * estimatedIndex / BOND_SECURITY_TIPS_1.getIndexStartValue() * NOTIONAL_TIPS_1;
    double dfSettle = MARKET.getDiscountingFactor(BOND_SECURITY_TIPS_1.getCurrency(), BOND_SECURITY_TIPS_1.getSettlementTime());
    double pvExpected = pvAtSettle * dfSettle;
    assertEquals("Inflation Capital Indexed bond: present value from clean real price", pvExpected, pv.getAmount(), 1.0E-6);
  }

  @Test
  /**
   * Tests the clean real price from the dirty real price.
   */
  public void cleanRealFromDirtyRealTips1() {
    double dirtyReal = 1.01;
    double cleanReal = METHOD_BOND_INFLATION.cleanRealPriceFromDirtyRealPrice(BOND_SECURITY_TIPS_1, dirtyReal);
    double cleanRealExpected = dirtyReal - BOND_SECURITY_TIPS_1.getAccruedInterest() / NOTIONAL_TIPS_1;
    assertEquals("Inflation Capital Indexed bond: clean from dirty", cleanRealExpected, cleanReal, 1.0E-8);
  }

  @Test
  /**
   * Tests the dirty real price computation from the real yield in the "US I/L real" convention.
   */
  public void dirtyRealPriceFromRealYieldTips1() {
    final double[] yield = new double[] {-0.01, 0.00, 0.01, 0.02, 0.03};
    final int nbCoupon = BOND_SECURITY_TIPS_1.getCoupon().getNumberOfPayments();
    double[] dirtyRealPrice = new double[yield.length];
    double[] dirtyRealPriceExpected = new double[yield.length];
    for (int loopyield = 0; loopyield < yield.length; loopyield++) {
      dirtyRealPrice[loopyield] = METHOD_BOND_INFLATION.dirtyRealPriceFromYieldReal(BOND_SECURITY_TIPS_1, yield[loopyield]);

      final double factorOnPeriod = 1 + yield[loopyield] / BOND_SECURITY_TIPS_1.getCouponPerYear();
      double pvAtFirstCoupon = 0;

      for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
        pvAtFirstCoupon += ((CouponInflationGearing) BOND_SECURITY_TIPS_1.getCoupon().getNthPayment(loopcpn)).getFactor() / BOND_SECURITY_TIPS_1.getCouponPerYear() / Math.pow(factorOnPeriod, loopcpn);
      }
      pvAtFirstCoupon += 1.0 / Math.pow(factorOnPeriod, nbCoupon - 1);
      dirtyRealPriceExpected[loopyield] = pvAtFirstCoupon / (1 + BOND_SECURITY_TIPS_1.getAccrualFactorToNextCoupon() * yield[loopyield] / BOND_SECURITY_TIPS_1.getCouponPerYear());
      assertEquals("Inflation Capital Indexed bond: yield " + loopyield, dirtyRealPriceExpected[loopyield], dirtyRealPrice[loopyield], 1.0E-8);
    }
  }

  @Test
  /**
   * Tests the clean real price from the dirty real price.
   */
  public void yieldRealFromDirtyRealTips1() {
    final double[] yield = new double[] {-0.01, 0.00, 0.01, 0.02, 0.03};
    double[] dirtyRealPrice = new double[yield.length];
    double[] yieldComputed = new double[yield.length];
    for (int loopyield = 0; loopyield < yield.length; loopyield++) {
      dirtyRealPrice[loopyield] = METHOD_BOND_INFLATION.dirtyRealPriceFromYieldReal(BOND_SECURITY_TIPS_1, yield[loopyield]);
      yieldComputed[loopyield] = METHOD_BOND_INFLATION.yieldRealFromDirtyRealPrice(BOND_SECURITY_TIPS_1, dirtyRealPrice[loopyield]);
      assertEquals("Inflation Capital Indexed bond: yield " + loopyield, yield[loopyield], yieldComputed[loopyield], 1.0E-8);
    }
  }

  @Test
  /**
   * Tests the clean, dirty and yield vs external hard-coded values.
   */
  public void priceYieldExternalValues1() {
    double m1 = 1000000; // Notional of the external figures.
    final ZonedDateTime pricingDate20110817 = DateUtils.getUTCDate(2011, 8, 17); // Spot 18-Aug-2011
    MarketBundle market = MarketDataSets.createMarket1(pricingDate20110817);
    double cleanRealPrice = 1.00;
    final BondCapitalIndexedSecurity<Coupon> bond_110817 = BOND_SECURITY_TIPS_1_DEFINITION.toDerivative(pricingDate20110817, US_CPI, "Not used");
    double referenceIndexExpected = 225.83129;
    double referenceIndex = bond_110817.getSettlement().estimatedIndex(market);
    assertEquals("Inflation Capital Indexed bond: index", referenceIndexExpected, referenceIndex, 1.0E-5);
    double indexRatioExpected = 1.13782;
    assertEquals("Inflation Capital Indexed bond: indexRatio", indexRatioExpected, referenceIndex / INDEX_START_TIPS_1, 1.0E-5);
    double yieldExpected = 1.999644 / 100.0;
    double dirtyRealPriceComputed = METHOD_BOND_INFLATION.dirtyRealPriceFromCleanRealPrice(bond_110817, cleanRealPrice);
    double yieldComputed = METHOD_BOND_INFLATION.yieldRealFromDirtyRealPrice(bond_110817, dirtyRealPriceComputed);
    assertEquals("Inflation Capital Indexed bond: yield ", yieldExpected, yieldComputed, 1.0E-8);
    double accruedExpected = 2102.49;
    double accruedRealExpected = accruedExpected / m1 / indexRatioExpected;
    double accruedReal = bond_110817.getAccruedInterest();
    assertEquals("Inflation Capital Indexed bond: accrued", accruedRealExpected, accruedReal / NOTIONAL_TIPS_1, 1.0E-8);
    double netAmountExpected = 1139922.49; // For 1m; uses the rounding rules.
    double netAmount2 = indexRatioExpected * m1 * cleanRealPrice + accruedExpected;
    assertEquals("Inflation Capital Indexed bond: net amount", netAmountExpected, netAmount2, 1.0E-2);
    double netAmount = METHOD_BOND_INFLATION.netAmount(bond_110817, market, cleanRealPrice);
    assertEquals("Inflation Capital Indexed bond: net amount", netAmountExpected, netAmount * m1 / NOTIONAL_TIPS_1, 2.0E+0); // The difference is due to rounding.
  }

  @Test
  /**
   * Tests the clean, dirty and yield vs external hard-coded values.
   */
  public void priceYieldExternalValues2() {
    double m1 = 1000000; // Notional of the external figures.
    final ZonedDateTime pricingDate20110817 = DateUtils.getUTCDate(2011, 8, 17); // Spot 18-Aug-2011
    MarketBundle market = MarketDataSets.createMarket1(pricingDate20110817);
    double cleanRealPrice = 1.13 + 0.01 / 32;
    final BondCapitalIndexedSecurity<Coupon> bond_110817 = BOND_SECURITY_TIPS_1_DEFINITION.toDerivative(pricingDate20110817, US_CPI, "Not used");
    double referenceIndexExpected = 225.83129;
    double referenceIndex = bond_110817.getSettlement().estimatedIndex(market);
    assertEquals("Inflation Capital Indexed bond: index", referenceIndexExpected, referenceIndex, 1.0E-5);
    double indexRatioExpected = 1.13782;
    assertEquals("Inflation Capital Indexed bond: indexRatio", indexRatioExpected, referenceIndex / INDEX_START_TIPS_1, 1.0E-5);
    double yieldExpected = -0.892152 / 100.0;
    double dirtyRealPriceComputed = METHOD_BOND_INFLATION.dirtyRealPriceFromCleanRealPrice(bond_110817, cleanRealPrice);
    double yieldComputed = METHOD_BOND_INFLATION.yieldRealFromDirtyRealPrice(bond_110817, dirtyRealPriceComputed);
    assertEquals("Inflation Capital Indexed bond: yield ", yieldExpected, yieldComputed, 1.0E-8);
    double accruedExpected = 2102.49;
    double accruedRealExpected = accruedExpected / m1 / indexRatioExpected;
    double accruedReal = bond_110817.getAccruedInterest();
    assertEquals("Inflation Capital Indexed bond: accrued", accruedRealExpected, accruedReal / NOTIONAL_TIPS_1, 1.0E-8);
    double netAmountExpected = 1288194.66; // For 1m; uses the rounding rules.
    double netAmount2 = indexRatioExpected * m1 * cleanRealPrice + accruedExpected;
    assertEquals("Inflation Capital Indexed bond: net amount", netAmountExpected, netAmount2, 1.0E-2);
    double netAmount = METHOD_BOND_INFLATION.netAmount(bond_110817, market, cleanRealPrice);
    assertEquals("Inflation Capital Indexed bond: net amount", netAmountExpected, netAmount * m1 / NOTIONAL_TIPS_1, 2.0E+0); // The difference is due to rounding.
  }

  @Test
  /**
   * Tests the clean, dirty and yield vs external hard-coded values.
   */
  public void priceYieldExternalValues3() {
    double m1 = 1000000; // Notional of the external figures.
    final ZonedDateTime pricingDate20110817 = DateUtils.getUTCDate(2011, 8, 18); // Spot 19-Aug-2011
    MarketBundle market = MarketDataSets.createMarket1(pricingDate20110817);
    double cleanRealPrice = 1.00;
    final BondCapitalIndexedSecurity<Coupon> bond_110817 = BOND_SECURITY_TIPS_1_DEFINITION.toDerivative(pricingDate20110817, US_CPI, "Not used");
    double referenceIndexExpected = 225.82348;
    double referenceIndex = bond_110817.getSettlement().estimatedIndex(market);
    assertEquals("Inflation Capital Indexed bond: index", referenceIndexExpected, referenceIndex, 1.0E-5);
    double indexRatioExpected = 1.13778;
    assertEquals("Inflation Capital Indexed bond: indexRatio", indexRatioExpected, referenceIndex / INDEX_START_TIPS_1, 1.0E-5);
    double yieldExpected = 1.999636 / 100.0;
    double dirtyRealPriceComputed = METHOD_BOND_INFLATION.dirtyRealPriceFromCleanRealPrice(bond_110817, cleanRealPrice);
    double yieldComputed = METHOD_BOND_INFLATION.yieldRealFromDirtyRealPrice(bond_110817, dirtyRealPriceComputed);
    assertEquals("Inflation Capital Indexed bond: yield ", yieldExpected, yieldComputed, 1.0E-8);
    double accruedExpected = 2164.26;
    double accruedRealExpected = accruedExpected / m1 / indexRatioExpected;
    double accruedReal = bond_110817.getAccruedInterest();
    assertEquals("Inflation Capital Indexed bond: accrued", accruedRealExpected, accruedReal / NOTIONAL_TIPS_1, 1.0E-8);
    double netAmountExpected = 1139944.26; // For 1m; uses the rounding rules.
    double netAmount2 = indexRatioExpected * m1 * cleanRealPrice + accruedExpected;
    assertEquals("Inflation Capital Indexed bond: net amount", netAmountExpected, netAmount2, 1.0E-2);
    double netAmount = METHOD_BOND_INFLATION.netAmount(bond_110817, market, cleanRealPrice);
    assertEquals("Inflation Capital Indexed bond: net amount", netAmountExpected, netAmount * m1 / NOTIONAL_TIPS_1, 2.0E+0); // The difference is due to rounding.
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {

    long startTime, endTime;
    final int nbTest = 10000;

    final double[] yield = new double[] {-0.01, 0.00, 0.01, 0.02, 0.03};

    double[] dirtyRealPrice = new double[yield.length];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopyield = 0; loopyield < yield.length; loopyield++) {
        dirtyRealPrice[loopyield] = METHOD_BOND_INFLATION.dirtyRealPriceFromYieldReal(BOND_SECURITY_TIPS_1, yield[loopyield]);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " price from yield for inflation bonds (TIPS): " + (endTime - startTime) + " ms");

    double[] yieldComputed = new double[yield.length];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopyield = 0; loopyield < yield.length; loopyield++) {
        yieldComputed[loopyield] = METHOD_BOND_INFLATION.yieldRealFromDirtyRealPrice(BOND_SECURITY_TIPS_1, dirtyRealPrice[loopyield]);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " yield from price for inflation bonds (TIPS): " + (endTime - startTime) + " ms");
  }

}

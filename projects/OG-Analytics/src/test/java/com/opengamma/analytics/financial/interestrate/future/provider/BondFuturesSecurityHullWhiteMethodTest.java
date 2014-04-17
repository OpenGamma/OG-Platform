/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceHullWhiteIssuerCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.issuer.MarketQuoteCurveSensitivityHullWhiteIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.MarketQuoteHullWhiteIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityHullWhiteIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Tests related to the bond future figures computed with the Hull-White one factor model for the delivery option.
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesSecurityHullWhiteMethodTest {

  private final static IssuerProviderDiscount ISSUER_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  private final static String[] ISSUER_NAMES = IssuerProviderDiscountDataSets.getIssuerNames();

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency USD = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String US_GOVT = ISSUER_NAMES[0];
  //  private static final Pair<String, Currency> ISSUER_CCY = Pairs.of(US_GOVT, USD);
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final int NB_BOND = 7;
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5) };
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
    DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31) };
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175 };
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292 };
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(USD, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, US_GOVT);
    }
  }
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  private static final ZonedDateTime FIRST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime LAST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIRST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_NOTICE_DATE);
  private static final double LAST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_NOTICE_DATE);
  private static final double FIRST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_DELIVERY_DATE);
  private static final double LAST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_DELIVERY_DATE);
  private static final BondFixedSecurity[] BASKET_AT_DELIVERY = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] BASKET_AT_SPOT = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET_AT_DELIVERY[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE);
      BASKET_AT_SPOT[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE);
    }
  }

  private static final BondFuturesSecurity BOND_FUTURE_SEC = new BondFuturesSecurity(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME, NOTIONAL,
      BASKET_AT_DELIVERY, BASKET_AT_SPOT, CONVERSION_FACTOR);
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteDataSets.createHullWhiteParameters();
  private static final HullWhiteIssuerProviderDiscount MULTICURVES_HW_ISSUER = new HullWhiteIssuerProviderDiscount(ISSUER_MULTICURVES, PARAMETERS_HW);

  private static final MarketQuoteHullWhiteIssuerCalculator MQC = MarketQuoteHullWhiteIssuerCalculator.getInstance();
  private static final MarketQuoteCurveSensitivityHullWhiteIssuerCalculator MQCSC = MarketQuoteCurveSensitivityHullWhiteIssuerCalculator.getInstance();

  private static final SimpleParameterSensitivityIssuerCalculator<HullWhiteIssuerProviderInterface> SPS_HW_C = new SimpleParameterSensitivityIssuerCalculator<>(MQCSC);
  private static final double SHIFT = 1.0E-7;
  private static final SimpleParameterSensitivityHullWhiteIssuerDiscountInterpolatedFDCalculator SPS_HW_FDC = new SimpleParameterSensitivityHullWhiteIssuerDiscountInterpolatedFDCalculator(MQC, SHIFT);

  private static final BondFuturesSecurityHullWhiteMethod METHOD_FUT_SEC_HW = BondFuturesSecurityHullWhiteMethod.getInstance();
  private static final BondFuturesSecurityHullWhiteNumericalIntegrationMethod METHOD_FUT_SEC_NI = BondFuturesSecurityHullWhiteNumericalIntegrationMethod.getInstance();
  private static final FuturesPriceHullWhiteIssuerCalculator FPHWIC = FuturesPriceHullWhiteIssuerCalculator.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_NI = 1.0E-6;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-6;

  @Test
  /**
   * Test price by explicit formula versus a numerical integration.
   */
  public void price() {
    final double priceExplicit = METHOD_FUT_SEC_HW.price(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    final double priceNumInteg = METHOD_FUT_SEC_NI.price(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    assertEquals("Bond future security Discounting Method: price from curves", priceExplicit, priceNumInteg, TOLERANCE_PRICE_NI);
  }

  @Test
  public void priceMethodVsCalculator() {
    final double priceMethod = METHOD_FUT_SEC_HW.price(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    final double priceCalculator = BOND_FUTURE_SEC.accept(MQC, MULTICURVES_HW_ISSUER);
    assertEquals("Bond future security Discounting Method: price from curves", priceCalculator, priceMethod, TOLERANCE_PRICE);
  }

  @Test
  public void price6() {
    final HullWhiteIssuerProviderDiscount hwIssuer6 = new HullWhiteIssuerProviderDiscount(IssuerProviderDiscountDataSets.createIssuerProvider6(), PARAMETERS_HW);
    final double priceMethod = METHOD_FUT_SEC_HW.price(BOND_FUTURE_SEC, hwIssuer6);
    final double priceExpected = 1.00; // Rates are at 6%
    assertEquals("Bond future security Discounting Method: price from curves", priceExpected, priceMethod, 5.0E-3);
  }

  @Test
  public void priceCurveSensitivity() {
    final SimpleParameterSensitivity pcsAD = SPS_HW_C.calculateSensitivity(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER, MULTICURVES_HW_ISSUER.getIssuerProvider().getAllNames());
    final SimpleParameterSensitivity pcsFD = SPS_HW_FDC.calculateSensitivity(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    AssertSensivityObjects.assertEquals("Bond future security Discounting Method: price from curves", pcsAD, pcsFD, TOLERANCE_PRICE_DELTA);
  }

  @Test
  public void priceCurveSensitivityMethodVsCalculator() {
    final MulticurveSensitivity pcsMethod = METHOD_FUT_SEC_HW.priceCurveSensitivity(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    final MulticurveSensitivity pcsCalculator = BOND_FUTURE_SEC.accept(MQCSC, MULTICURVES_HW_ISSUER);
    AssertSensivityObjects.assertEquals("Bond future security Discounting Method: price from curves", pcsCalculator, pcsMethod, TOLERANCE_PRICE_DELTA);
  }

  @Test
  /**
   * Test price and price curve sensitivity as one result using AD.
   */
  public void priceAD() {
    final double price = METHOD_FUT_SEC_HW.price(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    final MulticurveSensitivity pcs = METHOD_FUT_SEC_HW.priceCurveSensitivity(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    final Pair<Double, MulticurveSensitivity> priceAD = METHOD_FUT_SEC_HW.priceAD(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    assertEquals("Bond future security Discounting Method: price from curves", price, priceAD.getFirst(), TOLERANCE_PRICE);
    AssertSensivityObjects.assertEquals("Bond future security Discounting Method: price from curves", pcs, priceAD.getSecond(), TOLERANCE_PRICE_DELTA);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10000;
    @SuppressWarnings("unused")
    double priceFuture = 0.0;
    @SuppressWarnings("unused")
    MulticurveSensitivity pcs;
    @SuppressWarnings("unused")
    Pair<Double, MulticurveSensitivity> priceAD;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      priceFuture = METHOD_FUT_SEC_HW.price(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    }
    endTime = System.currentTimeMillis();
    System.out.println("BondFuturesSecurityHullWhiteMethodTest: " + nbTest + " price Bond Future Hull-White (Default number of points): " + (endTime - startTime) + " ms");
    // Performance note: HW price: 30-Dec-13: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 4100 ms for 10000 futures.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pcs = METHOD_FUT_SEC_HW.priceCurveSensitivity(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    }
    endTime = System.currentTimeMillis();
    System.out.println("BondFuturesSecurityHullWhiteMethodTest: " + nbTest + " price curve sensi Bond Future Hull-White (Default number of points): " + (endTime - startTime) + " ms");
    // Performance note: HW price: 30-Dec-13: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 4100 ms for 10000 futures.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      priceAD = METHOD_FUT_SEC_HW.priceAD(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER);
    }
    endTime = System.currentTimeMillis();
    System.out.println("BondFuturesSecurityHullWhiteMethodTest: " + nbTest + " price and price curve sensi Bond Future Hull-White (Default number of points): " + (endTime - startTime) + " ms");
    // Performance note: HW price: 30-Dec-13: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 4100 ms for 10000 futures.
  }

  @Test(enabled = false)
  /**
   * Tests of performance with different level of precision in the numerical procedure. "enabled = false" for the standard testing.
   */
  public void performanceNbPts() {
    long startTime, endTime;
    final int nbTest = 1000;

    final int[] nbPoint = new int[] {41, 61, 81, 101, 151, 201, 501 };
    final int nbRange = nbPoint.length;
    final double[] priceRange = new double[nbRange];

    for (int looprange = 0; looprange < nbRange; looprange++) {
      startTime = System.currentTimeMillis();
      for (int looptest = 0; looptest < nbTest; looptest++) {
        priceRange[looprange] = FPHWIC.visitBondFuturesSecurity(BOND_FUTURE_SEC, MULTICURVES_HW_ISSUER, nbPoint[looprange]);
      }
      endTime = System.currentTimeMillis();
      System.out.println("BondFuturesSecurityHullWhiteMethodTest: " + nbTest + " price Bond Future Hull-White: with " + nbPoint[looprange] + " points: " + (endTime - startTime) + " ms - price: " +
          priceRange[looprange]);
    }

  }

}

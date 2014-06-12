/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSmilePriceProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSmilePriceProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSmileProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
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

/**
 * Tests related to the pricing methods for bond future options with up-front premium payment.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureOptionPremiumSecurityBlackSurfaceMethodTest {

  private final static IssuerProviderDiscount ISSUER_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  private final static String ISSUER_NAME = IssuerProviderDiscountDataSets.getIssuerNames()[0]; // US GOVT

  private static final InterpolatedDoublesSurface BLACK_PARAMETERS = BlackDataSets.createBlackSurfaceExpiryTenor();

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final int SETTLEMENT_DAYS = 1;
  private static final int NB_BOND = 7;
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292};
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  private static final ZonedDateTime FIRST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime LAST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000;
  private static final double REF_PRICE = 0.0;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIRST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_NOTICE_DATE);
  private static final double LAST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_NOTICE_DATE);
  private static final double FIRST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_DELIVERY_DATE);
  private static final double LAST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_DELIVERY_DATE);

  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  private static final Currency USD = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8),
    Period.ofYears(5), Period.ofYears(5), Period.ofYears(5)};
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
    DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31)};
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175};
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(USD, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_NAME);
    }
  }
  private static final BondFixedSecurity[] BASKET = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] STANDARD = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE);
      STANDARD[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE);
    }
  }
  private static final BondFuture BOND_FUTURE_DERIV = new BondFuture(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME, NOTIONAL, BASKET,
      CONVERSION_FACTOR, REF_PRICE);
  private static final double OPTION_EXPIRY = 0.5;
  private static final double PRICE_FUTURE = 1.0325;
  private static final BlackBondFuturesSmileProvider BLACK_MULTICURVES = new BlackBondFuturesSmileProvider(ISSUER_MULTICURVES, BLACK_PARAMETERS);
  private static final BlackBondFuturesSmilePriceProvider BLACK_PRICE_MULTICURVES = new BlackBondFuturesSmilePriceProvider(BLACK_MULTICURVES, PRICE_FUTURE);

  private static final double STRIKE = 1.04;
  private static final BondFutureOptionPremiumSecurity BOND_FUTURE_OPTION_DERIV_CALL = new BondFutureOptionPremiumSecurity(BOND_FUTURE_DERIV, OPTION_EXPIRY, STRIKE, true);
  private static final BondFutureOptionPremiumSecurity BOND_FUTURE_OPTION_DERIV_PUT = new BondFutureOptionPremiumSecurity(BOND_FUTURE_DERIV, OPTION_EXPIRY, STRIKE, false);

  private static final BondFutureDiscountingMethod METHOD_FUTURES = BondFutureDiscountingMethod.getInstance();
  private static final BondFutureOptionPremiumSecurityBlackSmileMethod METHOD_BLACK_SEC = BondFutureOptionPremiumSecurityBlackSmileMethod.getInstance();

  private static final double EPS = 1e-15;
  private static final double TOLERANCE_PRICE_SENSI = 1.0E-6;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity1() {
    METHOD_BLACK_SEC.price(null, BLACK_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity2() {
    METHOD_BLACK_SEC.price(null, BLACK_PRICE_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity5() {
    METHOD_BLACK_SEC.priceCurveSensitivity(null, BLACK_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity6() {
    METHOD_BLACK_SEC.priceCurveSensitivity(null, BLACK_PRICE_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity7() {
    METHOD_BLACK_SEC.priceBlackSensitivity(null, BLACK_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity8() {
    METHOD_BLACK_SEC.priceBlackSensitivity(null, BLACK_PRICE_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity15() {
    METHOD_BLACK_SEC.priceDelta(null, BLACK_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity16() {
    METHOD_BLACK_SEC.priceDelta(null, BLACK_PRICE_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity9() {
    METHOD_BLACK_SEC.priceGamma(null, BLACK_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity10() {
    METHOD_BLACK_SEC.priceGamma(null, BLACK_PRICE_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity17() {
    METHOD_BLACK_SEC.priceVega(null, BLACK_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity18() {
    METHOD_BLACK_SEC.priceVega(null, BLACK_PRICE_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity11() {
    METHOD_BLACK_SEC.impliedVolatility(null, BLACK_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity12() {
    METHOD_BLACK_SEC.impliedVolatility(null, BLACK_PRICE_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity13() {
    METHOD_BLACK_SEC.underlyingFuturePrice(null, ISSUER_MULTICURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    METHOD_BLACK_SEC.price(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmileProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData2() {
    METHOD_BLACK_SEC.price(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmilePriceProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData7() {
    METHOD_BLACK_SEC.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmileProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData8() {
    METHOD_BLACK_SEC.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmilePriceProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData10() {
    METHOD_BLACK_SEC.priceBlackSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmileProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData11() {
    METHOD_BLACK_SEC.priceBlackSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmilePriceProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData21() {
    METHOD_BLACK_SEC.priceDelta(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmileProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData22() {
    METHOD_BLACK_SEC.priceDelta(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmilePriceProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData13() {
    METHOD_BLACK_SEC.priceGamma(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmileProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData14() {
    METHOD_BLACK_SEC.priceGamma(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmilePriceProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData24() {
    METHOD_BLACK_SEC.priceVega(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmileProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData25() {
    METHOD_BLACK_SEC.priceVega(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmilePriceProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData16() {
    METHOD_BLACK_SEC.impliedVolatility(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmileProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData17() {
    METHOD_BLACK_SEC.impliedVolatility(BOND_FUTURE_OPTION_DERIV_CALL, (BlackBondFuturesSmilePriceProviderInterface) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData19() {
    METHOD_BLACK_SEC.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, (IssuerProviderInterface) null);
  }

  @Test
  public void priceFromFuturePrice() {
    final double callBlackPrice = BlackFormulaRepository.price(PRICE_FUTURE, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE), true);
    assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price from future price", callBlackPrice, METHOD_BLACK_SEC.price(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_PRICE_MULTICURVES),
        EPS);
    final double putBlackPrice = BlackFormulaRepository.price(PRICE_FUTURE, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE), false);
    assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price from future price", putBlackPrice, METHOD_BLACK_SEC.price(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_PRICE_MULTICURVES), EPS);
    assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price from future price", PRICE_FUTURE - STRIKE, callBlackPrice - putBlackPrice, EPS);
  }

  @Test
  public void priceFromCurves() {
    final double priceFuture = METHOD_BLACK_SEC.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, ISSUER_MULTICURVES);
    assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price", priceFuture, METHOD_BLACK_SEC.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_PUT, ISSUER_MULTICURVES), EPS);
    final double callBlackPrice = BlackFormulaRepository.price(priceFuture, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE), true);
    assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price", callBlackPrice, METHOD_BLACK_SEC.price(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_MULTICURVES), EPS);
    final double putBlackPrice = BlackFormulaRepository.price(priceFuture, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE), false);
    assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price", putBlackPrice, METHOD_BLACK_SEC.price(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_MULTICURVES), EPS);
  }

  @Test
  /**
   * Tests the curve sensitivity when the future price is computed from the curves.
   */
  public void priceCurveSensitivityFromCurves() {
    final MulticurveSensitivity pcsCallComputed = METHOD_BLACK_SEC.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_MULTICURVES).cleaned();
    final MulticurveSensitivity pcsPutComputed = METHOD_BLACK_SEC.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_MULTICURVES);
    final MulticurveSensitivity pcsFuture = METHOD_FUTURES.priceCurveSensitivity(BOND_FUTURE_DERIV, ISSUER_MULTICURVES).cleaned();
    final MulticurveSensitivity pcsCallPut = pcsCallComputed.plus(pcsPutComputed.multipliedBy(-1.0)).cleaned();
    AssertSensitivityObjects.assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price curve sensitivity - put/call parity", pcsFuture, pcsCallPut, TOLERANCE_PRICE_SENSI);
    final double delta = METHOD_BLACK_SEC.priceDelta(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_MULTICURVES);
    final MulticurveSensitivity pcsCallExpected = pcsFuture.multipliedBy(delta);
    AssertSensitivityObjects.assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price curve sensitivity", pcsCallExpected, pcsCallComputed, TOLERANCE_PRICE_SENSI);
  }

  @Test
  /**
   * Tests the curve sensitivity when the future price is directly provided.
   */
  public void priceCurveSensitivityFromFuturesPrice() {
    final MulticurveSensitivity pcsCallComputed = METHOD_BLACK_SEC.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_PRICE_MULTICURVES).cleaned();
    final MulticurveSensitivity pcsCallNoFut = METHOD_BLACK_SEC.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_MULTICURVES).cleaned();
    AssertSensitivityObjects.assertDoesNotEqual("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price curve sensitivity", pcsCallComputed, pcsCallNoFut, TOLERANCE_PRICE_SENSI);
    final MulticurveSensitivity pcsPutComputed = METHOD_BLACK_SEC.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_PRICE_MULTICURVES);
    final MulticurveSensitivity pcsFuture = METHOD_FUTURES.priceCurveSensitivity(BOND_FUTURE_DERIV, ISSUER_MULTICURVES).cleaned();
    final MulticurveSensitivity pcsCallPut = pcsCallComputed.plus(pcsPutComputed.multipliedBy(-1.0)).cleaned();
    AssertSensitivityObjects.assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price curve sensitivity - put/call parity", pcsFuture, pcsCallPut, TOLERANCE_PRICE_SENSI);
    final double delta = METHOD_BLACK_SEC.priceDelta(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_PRICE_MULTICURVES);
    final MulticurveSensitivity pcsCallExpected = pcsFuture.multipliedBy(delta);
    AssertSensitivityObjects.assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price curve sensitivity", pcsCallExpected, pcsCallComputed, TOLERANCE_PRICE_SENSI);
  }

  @Test
  public void testGreeksFromFuturePrice() {
    final double callBlackDelta = BlackFormulaRepository.delta(PRICE_FUTURE, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE), true);
    assertEquals(callBlackDelta, METHOD_BLACK_SEC.priceDelta(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_PRICE_MULTICURVES), EPS);
    final double putBlackDelta = BlackFormulaRepository.delta(PRICE_FUTURE, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE), false);
    assertEquals(putBlackDelta, METHOD_BLACK_SEC.priceDelta(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_PRICE_MULTICURVES), EPS);
    final double blackVega = BlackFormulaRepository.vega(PRICE_FUTURE, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE));
    assertEquals(blackVega, METHOD_BLACK_SEC.priceVega(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_PRICE_MULTICURVES), EPS);
    assertEquals(blackVega, METHOD_BLACK_SEC.priceVega(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_PRICE_MULTICURVES), EPS);
    final double blackGamma = BlackFormulaRepository.gamma(PRICE_FUTURE, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE));
    assertEquals(blackGamma, METHOD_BLACK_SEC.priceGamma(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_PRICE_MULTICURVES), EPS);
    assertEquals(blackGamma, METHOD_BLACK_SEC.priceGamma(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_PRICE_MULTICURVES), EPS);
  }

  @Test
  public void testGreeks() {
    final double priceFuture = METHOD_BLACK_SEC.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, ISSUER_MULTICURVES);
    final double callBlackDelta = BlackFormulaRepository.delta(priceFuture, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE), true);
    assertEquals(callBlackDelta, METHOD_BLACK_SEC.priceDelta(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_MULTICURVES), EPS);
    final double putBlackDelta = BlackFormulaRepository.delta(priceFuture, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE), false);
    assertEquals(putBlackDelta, METHOD_BLACK_SEC.priceDelta(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_MULTICURVES), EPS);
    final double blackVega = BlackFormulaRepository.vega(priceFuture, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE));
    assertEquals(blackVega, METHOD_BLACK_SEC.priceVega(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_MULTICURVES), EPS);
    assertEquals(blackVega, METHOD_BLACK_SEC.priceVega(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_MULTICURVES), EPS);
    final double blackGamma = BlackFormulaRepository.gamma(priceFuture, STRIKE, OPTION_EXPIRY, BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE));
    assertEquals(blackGamma, METHOD_BLACK_SEC.priceGamma(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_MULTICURVES), EPS);
    assertEquals(blackGamma, METHOD_BLACK_SEC.priceGamma(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_MULTICURVES), EPS);
  }

  @Test
  public void impliedVolatility() {
    final double impliedVolatility = BLACK_MULTICURVES.getVolatility(OPTION_EXPIRY, STRIKE);
    assertEquals(impliedVolatility, METHOD_BLACK_SEC.impliedVolatility(BOND_FUTURE_OPTION_DERIV_CALL, BLACK_MULTICURVES), EPS);
    assertEquals(impliedVolatility, METHOD_BLACK_SEC.impliedVolatility(BOND_FUTURE_OPTION_DERIV_PUT, BLACK_MULTICURVES), EPS);
  }

}

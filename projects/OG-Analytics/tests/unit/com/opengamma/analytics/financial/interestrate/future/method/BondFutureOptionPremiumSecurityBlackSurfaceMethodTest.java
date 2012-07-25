/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import static org.testng.Assert.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class BondFutureOptionPremiumSecurityBlackSurfaceMethodTest {
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final int SETTLEMENT_DAYS = 1;
  private static final int NB_BOND = 7;
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292 };
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  private static final ZonedDateTime FIRST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime LAST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000;
  private static final double REF_PRICE = 0.0;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIRST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_NOTICE_DATE);
  private static final double LAST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_NOTICE_DATE);
  private static final double FIRST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_DELIVERY_DATE);
  private static final double LAST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_DELIVERY_DATE);
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME };
  private static final YieldCurveWithBlackCubeBundle DATA = TestsDataSetsBlack.createCubesBondFutureOption();
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5) };
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
      DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31) };
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175 };
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
    }
  }
  private static final BondFixedSecurity[] BASKET = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] STANDARD = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE, CURVES_NAME);
      STANDARD[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
  }
  private static final BondFuture BOND_FUTURE_DERIV = new BondFuture(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME, NOTIONAL,
      BASKET, CONVERSION_FACTOR, REF_PRICE);
  private static final double OPTION_EXPIRY = 0.5;
  private static final double PRICE_FUTURE = 1.0325;
  private static final double STRIKE = 1.04;
  private static final BondFutureOptionPremiumSecurity BOND_FUTURE_OPTION_DERIV_CALL = new BondFutureOptionPremiumSecurity(BOND_FUTURE_DERIV, OPTION_EXPIRY, STRIKE, true);
  private static final BondFutureOptionPremiumSecurity BOND_FUTURE_OPTION_DERIV_PUT = new BondFutureOptionPremiumSecurity(BOND_FUTURE_DERIV, OPTION_EXPIRY, STRIKE, false);
  private static final BondFutureOptionPremiumSecurityBlackSurfaceMethod METHOD = BondFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity1() {
    METHOD.optionPriceFromFuturePrice(null, DATA, PRICE_FUTURE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity2() {
    METHOD.optionPriceFromFuturePrice(null, (YieldCurveBundle) DATA, PRICE_FUTURE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity3() {
    METHOD.optionPrice(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity4() {
    METHOD.optionPrice(null, (YieldCurveBundle) DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity5() {
    METHOD.priceCurveSensitivity(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity6() {
    METHOD.priceCurveSensitivity(null, (YieldCurveBundle) DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity7() {
    METHOD.priceBlackSensitivity(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity8() {
    METHOD.priceBlackSensitivity(null, (YieldCurveBundle) DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity9() {
    METHOD.optionPriceGamma(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity10() {
    METHOD.optionPriceGamma(null, (YieldCurveBundle) DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity11() {
    METHOD.impliedVolatility(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity12() {
    METHOD.impliedVolatility(null, (YieldCurveBundle) DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity13() {
    METHOD.underlyingFuturePrice(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity14() {
    METHOD.underlyingFuturePrice(null, (YieldCurveBundle) DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity15() {
    METHOD.optionPriceDelta(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity16() {
    METHOD.optionPriceDelta(null, (YieldCurveBundle) DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity17() {
    METHOD.optionPriceVega(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity18() {
    METHOD.optionPriceVega(null, (YieldCurveBundle) DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    METHOD.optionPriceFromFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, null, PRICE_FUTURE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData2() {
    METHOD.optionPriceFromFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null, PRICE_FUTURE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData3() {
    METHOD.optionPrice(BOND_FUTURE_OPTION_DERIV_CALL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData4() {
    METHOD.optionPrice(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData5() {
    METHOD.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData6() {
    METHOD.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData7() {
    METHOD.priceBlackSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData8() {
    METHOD.priceBlackSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData9() {
    METHOD.optionPriceGamma(BOND_FUTURE_OPTION_DERIV_CALL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData10() {
    METHOD.optionPriceGamma(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData11() {
    METHOD.impliedVolatility(BOND_FUTURE_OPTION_DERIV_CALL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData12() {
    METHOD.impliedVolatility(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData13() {
    METHOD.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData14() {
    METHOD.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData15() {
    METHOD.optionPriceDelta(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData16() {
    METHOD.optionPriceDelta(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData17() {
    METHOD.optionPriceVega(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData18() {
    METHOD.optionPriceVega(BOND_FUTURE_OPTION_DERIV_CALL, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDataType1() {
    METHOD.optionPriceFromFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, TestsDataSetsBlack.createCurvesBond(), PRICE_FUTURE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDataType2() {
    METHOD.optionPrice(BOND_FUTURE_OPTION_DERIV_CALL, TestsDataSetsBlack.createCurvesBond());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDataType3() {
    METHOD.priceCurveSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, TestsDataSetsBlack.createCurvesBond());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDataType4() {
    METHOD.priceBlackSensitivity(BOND_FUTURE_OPTION_DERIV_CALL, TestsDataSetsBlack.createCurvesBond());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDataType5() {
    METHOD.optionPriceGamma(BOND_FUTURE_OPTION_DERIV_CALL, TestsDataSetsBlack.createCurvesBond());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDataType6() {
    METHOD.impliedVolatility(BOND_FUTURE_OPTION_DERIV_CALL, TestsDataSetsBlack.createCurvesBond());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDataType7() {
    METHOD.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, TestsDataSetsBlack.createCurvesBond());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDataType8() {
    METHOD.optionPriceDelta(BOND_FUTURE_OPTION_DERIV_CALL, TestsDataSetsBlack.createCurvesBond());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDataType9() {
    METHOD.optionPriceVega(BOND_FUTURE_OPTION_DERIV_CALL, TestsDataSetsBlack.createCurvesBond());
  }

  @Test
  public void testBlackPriceFromFuturePrice() {
    final double callBlackPrice = BlackFormulaRepository.price(PRICE_FUTURE, STRIKE, OPTION_EXPIRY, DATA.getVolatility(OPTION_EXPIRY, STRIKE), true);
    assertEquals(callBlackPrice, METHOD.optionPriceFromFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, DATA, PRICE_FUTURE), EPS);
    final double putBlackPrice = BlackFormulaRepository.price(PRICE_FUTURE, STRIKE, OPTION_EXPIRY, DATA.getVolatility(OPTION_EXPIRY, STRIKE), false);
    assertEquals(putBlackPrice, METHOD.optionPriceFromFuturePrice(BOND_FUTURE_OPTION_DERIV_PUT, DATA, PRICE_FUTURE), EPS);
    assertEquals(PRICE_FUTURE - STRIKE, callBlackPrice - putBlackPrice, EPS);
  }

  @Test
  public void testBlackPrice() {
    final double priceFuture = METHOD.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, DATA);
    assertEquals(priceFuture, METHOD.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_PUT, DATA), EPS);
    final double callBlackPrice = BlackFormulaRepository.price(priceFuture, STRIKE, OPTION_EXPIRY, DATA.getVolatility(OPTION_EXPIRY, STRIKE), true);
    assertEquals(callBlackPrice, METHOD.optionPrice(BOND_FUTURE_OPTION_DERIV_CALL, DATA), EPS);
    final double putBlackPrice = BlackFormulaRepository.price(priceFuture, STRIKE, OPTION_EXPIRY, DATA.getVolatility(OPTION_EXPIRY, STRIKE), false);
    assertEquals(putBlackPrice, METHOD.optionPrice(BOND_FUTURE_OPTION_DERIV_PUT, DATA), EPS);
  }

  @Test
  public void testGreeks() {
    final double priceFuture = METHOD.underlyingFuturePrice(BOND_FUTURE_OPTION_DERIV_CALL, DATA);
    final double callBlackDelta = BlackFormulaRepository.delta(priceFuture, STRIKE, OPTION_EXPIRY, DATA.getVolatility(OPTION_EXPIRY, STRIKE), true);
    assertEquals(callBlackDelta, METHOD.optionPriceDelta(BOND_FUTURE_OPTION_DERIV_CALL, DATA), EPS);
    final double putBlackDelta = BlackFormulaRepository.delta(priceFuture, STRIKE, OPTION_EXPIRY, DATA.getVolatility(OPTION_EXPIRY, STRIKE), false);
    assertEquals(putBlackDelta, METHOD.optionPriceDelta(BOND_FUTURE_OPTION_DERIV_PUT, DATA), EPS);
    final double blackVega = BlackFormulaRepository.vega(priceFuture, STRIKE, OPTION_EXPIRY, DATA.getVolatility(OPTION_EXPIRY, STRIKE));
    assertEquals(blackVega, METHOD.optionPriceVega(BOND_FUTURE_OPTION_DERIV_CALL, DATA), EPS);
    assertEquals(blackVega, METHOD.optionPriceVega(BOND_FUTURE_OPTION_DERIV_PUT, DATA), EPS);
    final double blackGamma = BlackFormulaRepository.gamma(priceFuture, STRIKE, OPTION_EXPIRY, DATA.getVolatility(OPTION_EXPIRY, STRIKE));
    assertEquals(blackGamma, METHOD.optionPriceGamma(BOND_FUTURE_OPTION_DERIV_CALL, DATA), EPS);
    assertEquals(blackGamma, METHOD.optionPriceGamma(BOND_FUTURE_OPTION_DERIV_PUT, DATA), EPS);
  }

  @Test
  public void testImpliedVolatility() {
    final double impliedVolatility = DATA.getVolatility(OPTION_EXPIRY, STRIKE);
    assertEquals(impliedVolatility, METHOD.impliedVolatility(BOND_FUTURE_OPTION_DERIV_CALL, DATA), EPS);
    assertEquals(impliedVolatility, METHOD.impliedVolatility(BOND_FUTURE_OPTION_DERIV_PUT, DATA), EPS);
  }

}

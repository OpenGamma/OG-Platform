/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

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
import com.opengamma.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.financial.interestrate.future.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.financial.interestrate.future.InterestRateFutureSecurity;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests the method for interest rate future option with SABR volatility parameter surfaces.
 */
public class InterestRateFutureOptionPremiumTransactionSABRMethodTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtil.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "EDU2";
  private static final InterestRateFutureSecurityDefinition EDU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2010, 8, 18);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final InterestRateFutureSecurity EDU2 = EDU2_DEFINITION.toDerivative(REFERENCE_DATE, CURVES);
  // Option 
  private static final ZonedDateTime EXPIRATION_DATE = DateUtil.getUTCDate(2011, 9, 16);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final double EXPIRATION_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
  private static final double STRIKE = 0.9850;
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionPremiumSecurity OPTION_EDU2 = new InterestRateFutureOptionPremiumSecurity(EDU2, EXPIRATION_TIME, STRIKE, IS_CALL);
  // Transaction
  private static final int QUANTITY = -123;
  private static final double TRADE_PRICE = 0.0050;

  private static final InterestRateFutureOptionPremiumTransactionSABRMethod METHOD = new InterestRateFutureOptionPremiumTransactionSABRMethod();
  private static final InterestRateFutureOptionPremiumSecuritySABRMethod METHOD_SECURITY = new InterestRateFutureOptionPremiumSecuritySABRMethod();

  private static final YieldCurveBundle CURVES_BUNDLE = TestsDataSets.createCurves1();
  private static final SABRInterestRateParameter SABR_PARAMETER = TestsDataSets.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES_BUNDLE);
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();

  @Test
  /**
   * Test the present value from the quoted option price.
   */
  public void presentValueFromOptionPrice() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double priceQuoted = 0.01;
    // Premium in the past
    InterestRateFutureOptionPremiumTransaction transactionNoPremium = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, 0.0, 0.0);
    double pvNoPremium = METHOD.presentValueFromPrice(transactionNoPremium, curves, priceQuoted);
    double pvNoPremiumExpected = priceQuoted * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    assertEquals("Future option: present value from quoted price", pvNoPremiumExpected, pvNoPremium);
    assertEquals("Future option: present value from quoted price", pvNoPremiumExpected, METHOD.presentValueFromPrice(transactionNoPremium, sabrBundle, priceQuoted));
    // Premium today
    double premium = -TRADE_PRICE * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    InterestRateFutureOptionPremiumTransaction transactionPremiumToday = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, 0.0, TRADE_PRICE);
    double pvPremiumToday = METHOD.presentValueFromPrice(transactionPremiumToday, curves, priceQuoted);
    double pvPremiumTodayExpected = pvNoPremiumExpected + premium;
    assertEquals("Future option: present value from quoted price", pvPremiumTodayExpected, pvPremiumToday);
    // Premium in the future
    double premiumTime = 4.0 / 365.0;
    InterestRateFutureOptionPremiumTransaction transactionPremiumFuture = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, premiumTime, TRADE_PRICE);
    double df = curves.getCurve(DISCOUNTING_CURVE_NAME).getDiscountFactor(premiumTime);
    double pvPremiumFuture = METHOD.presentValueFromPrice(transactionPremiumFuture, curves, priceQuoted);
    double pvPremiumFutureExpected = pvNoPremiumExpected + premium * df;
    assertEquals("Future option: present value from quoted price", pvPremiumFutureExpected, pvPremiumFuture);
  }

  @Test
  /**
   * Test the present value from the future price.
   */
  public void presentValueFromFuturePrice() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double priceFuture = 0.9905;
    // Premium in the past
    InterestRateFutureOptionPremiumTransaction transactionNoPremium = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, 0.0, 0.0);
    double pvNoPremium = METHOD.presentValueFromFuturePrice(transactionNoPremium, sabrBundle, priceFuture);
    double priceSecurity = METHOD_SECURITY.optionPriceFromFuturePrice(OPTION_EDU2, sabrBundle, priceFuture);
    double pvNoPremiumExpected = priceSecurity * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    assertEquals("Future option: present value from future price", pvNoPremiumExpected, pvNoPremium);
    assertEquals("Future option: present value from future price", pvNoPremiumExpected, METHOD.presentValueFromPrice(transactionNoPremium, sabrBundle, priceSecurity));
    // Premium today
    double premium = -TRADE_PRICE * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    InterestRateFutureOptionPremiumTransaction transactionPremiumToday = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, 0.0, TRADE_PRICE);
    double pvPremiumToday = METHOD.presentValueFromFuturePrice(transactionPremiumToday, sabrBundle, priceFuture);
    double pvPremiumTodayExpected = pvNoPremiumExpected + premium;
    assertEquals("Future option: present value from future price", pvPremiumTodayExpected, pvPremiumToday);
    // Premium in the future
    double premiumTime = 4.0 / 365.0;
    InterestRateFutureOptionPremiumTransaction transactionPremiumFuture = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, premiumTime, TRADE_PRICE);
    double df = curves.getCurve(DISCOUNTING_CURVE_NAME).getDiscountFactor(premiumTime);
    double pvPremiumFuture = METHOD.presentValueFromFuturePrice(transactionPremiumFuture, sabrBundle, priceFuture);
    double pvPremiumFutureExpected = pvNoPremiumExpected + premium * df;
    assertEquals("Future option: present value from future price", pvPremiumFutureExpected, pvPremiumFuture);
  }

  @Test
  /**
   * Test the present value from the future price.
   */
  public void presentValue() {
    InterestRateFutureSecurityDiscountingMethod methodFuture = new InterestRateFutureSecurityDiscountingMethod();
    double priceFuture = methodFuture.price(EDU2, CURVES_BUNDLE);
    // Premium in the past
    InterestRateFutureOptionPremiumTransaction transactionNoPremium = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, 0.0, 0.0);
    double pvNoPremium = METHOD.presentValue(transactionNoPremium, SABR_BUNDLE);
    double pvNoPremiumExpected = METHOD.presentValueFromFuturePrice(transactionNoPremium, SABR_BUNDLE, priceFuture);
    assertEquals("Future option: present value", pvNoPremiumExpected, pvNoPremium);
    // Premium today
    double premium = -TRADE_PRICE * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    InterestRateFutureOptionPremiumTransaction transactionPremiumToday = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, 0.0, TRADE_PRICE);
    double pvPremiumToday = METHOD.presentValue(transactionPremiumToday, SABR_BUNDLE);
    double pvPremiumTodayExpected = pvNoPremiumExpected + premium;
    assertEquals("Future option: present value", pvPremiumTodayExpected, pvPremiumToday);
    // Premium in the future
    double premiumTime = 4.0 / 365.0;
    InterestRateFutureOptionPremiumTransaction transactionPremiumFuture = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, premiumTime, TRADE_PRICE);
    double df = CURVES_BUNDLE.getCurve(DISCOUNTING_CURVE_NAME).getDiscountFactor(premiumTime);
    double pvPremiumFuture = METHOD.presentValue(transactionPremiumFuture, SABR_BUNDLE);
    double pvPremiumFutureExpected = pvNoPremiumExpected + premium * df;
    assertEquals("Future option: present value", pvPremiumFutureExpected, pvPremiumFuture);
  }

  @Test
  /**
   * Test the present value from the future price.
   */
  public void presentValueMethodVsCalculator() {
    // Premium in the past
    InterestRateFutureOptionPremiumTransaction transactionNoPremium = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, 0.0, 0.0);
    double pvNoPremiumMethod = METHOD.presentValue(transactionNoPremium, SABR_BUNDLE);
    double pvNoPremiumCalculator = PVC.visit(transactionNoPremium, SABR_BUNDLE);
    assertEquals("Future option: present value from quoted price", pvNoPremiumMethod, pvNoPremiumCalculator);
    // Premium today
    InterestRateFutureOptionPremiumTransaction transactionPremiumToday = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, 0.0, TRADE_PRICE);
    double pvPremiumTodayMethod = METHOD.presentValue(transactionPremiumToday, SABR_BUNDLE);
    double pvPremiumTodayCalculator = PVC.visit(transactionPremiumToday, SABR_BUNDLE);
    assertEquals("Future option: present value from quoted price", pvPremiumTodayMethod, pvPremiumTodayCalculator);
    // Premium in the future
    double premiumTime = 4.0 / 365.0;
    InterestRateFutureOptionPremiumTransaction transactionPremiumFuture = new InterestRateFutureOptionPremiumTransaction(OPTION_EDU2, QUANTITY, premiumTime, TRADE_PRICE);
    double pvPremiumMethod = METHOD.presentValue(transactionPremiumFuture, SABR_BUNDLE);
    double pvPremiumCalculator = PVC.visit(transactionPremiumFuture, SABR_BUNDLE);
    assertEquals("Future option: present value from quoted price", pvPremiumMethod, pvPremiumCalculator);
  }

}

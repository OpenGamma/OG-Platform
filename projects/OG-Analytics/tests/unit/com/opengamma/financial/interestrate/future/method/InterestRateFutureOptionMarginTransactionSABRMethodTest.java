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
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests the method for interest rate future option with SABR volatility parameter surfaces.
 */
public class InterestRateFutureOptionMarginTransactionSABRMethodTest {
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
  private static final InterestRateFutureOptionMarginSecurity OPTION_EDU2 = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE, IS_CALL);
  // Transaction
  private static final int QUANTITY = -123;
  private static final double TRADE_PRICE = 0.0050;

  private static final InterestRateFutureOptionMarginTransactionSABRMethod METHOD = new InterestRateFutureOptionMarginTransactionSABRMethod();
  private static final InterestRateFutureOptionMarginSecuritySABRMethod METHOD_SECURITY = new InterestRateFutureOptionMarginSecuritySABRMethod();

  private static final YieldCurveBundle CURVES_BUNDLE = TestsDataSets.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSets.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES_BUNDLE);
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();

  @Test
  /**
   * Test the present value from the quoted option price.
   */
  public void presentValueFromOptionPrice() {
    final double priceQuoted = 0.01;
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
    final double pv = METHOD.presentValueFromPrice(transactionNoPremium, priceQuoted).getAmount();
    final double pvExpected = (priceQuoted - TRADE_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    assertEquals("Future option: present value from quoted price", pvExpected, pv);
  }

  @Test
  /**
   * Test the present value from the future price.
   */
  public void presentValueFromFuturePrice() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double priceFuture = 0.9905;
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
    final double pv = METHOD.presentValueFromFuturePrice(transactionNoPremium, sabrBundle, priceFuture).getAmount();
    final double priceSecurity = METHOD_SECURITY.optionPriceFromFuturePrice(OPTION_EDU2, sabrBundle, priceFuture);
    final double pvExpected = (priceSecurity - TRADE_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    assertEquals("Future option: present value from future price", pvExpected, pv, 1.0E-2);
  }

  @Test
  /**
   * Test the present value from the future price.
   */
  public void presentValue() {
    final InterestRateFutureSecurityDiscountingMethod methodFuture = InterestRateFutureSecurityDiscountingMethod.getInstance();
    final double priceFuture = methodFuture.price(EDU2, CURVES_BUNDLE);
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
    final double pvNoPremium = METHOD.presentValue(transactionNoPremium, SABR_BUNDLE).getAmount();
    final double pvNoPremiumExpected = METHOD.presentValueFromFuturePrice(transactionNoPremium, SABR_BUNDLE, priceFuture).getAmount();
    assertEquals("Future option: present value", pvNoPremiumExpected, pvNoPremium);
  }

  @Test
  /**
   * Test the present value from the method and from the calculator.
   */
  public void presentValueMethodVsCalculator() {
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, 0.0);
    final double pvNoPremiumMethod = METHOD.presentValue(transactionNoPremium, SABR_BUNDLE).getAmount();
    final double pvNoPremiumCalculator = PVC.visit(transactionNoPremium, SABR_BUNDLE);
    assertEquals("Future option: present value: Method vs Calculator", pvNoPremiumMethod, pvNoPremiumCalculator);
  }

}

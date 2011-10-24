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
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.future.calculator.PresentValueFromFuturePriceCalculator;
import com.opengamma.financial.interestrate.future.definition.BondFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.BondFutureTransaction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the bond future transaction figures computed by discounting.
 */
public class BondFutureTransactionDiscountingMethodTest {
  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final int NB_BOND = 7;
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5)};
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
      DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31)};
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175};
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292};
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
    }
  }
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  private static final ZonedDateTime FIRST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime LAST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIRST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_NOTICE_DATE);
  private static final double LAST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_NOTICE_DATE);
  private static final double FIRST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_DELIVERY_DATE);
  private static final double LAST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_DELIVERY_DATE);
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurvesBond1();
  private static final BondFixedSecurity[] BASKET = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] STANDARD = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE, CURVES_NAME);
      STANDARD[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
  }
  private static final BondFutureSecurity BOND_FUTURE_SECURITY = new BondFutureSecurity(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME, NOTIONAL,
      BASKET, CONVERSION_FACTOR);
  private static final BondFutureTransactionDiscountingMethod METHOD_FUTURE_TRANSACTION = BondFutureTransactionDiscountingMethod.getInstance();
  private static final BondFutureSecurityDiscountingMethod METHOD_FUTURE_SECURITY = BondFutureSecurityDiscountingMethod.getInstance();
  // Transaction
  private static final int QUANTITY = 4321;
  private static final double REFERENCE_PRICE = 1.0987;
  private static final BondFutureTransaction FUTURE_TRANSACTION = new BondFutureTransaction(BOND_FUTURE_SECURITY, QUANTITY, REFERENCE_PRICE);

  @Test
  /**
   * Tests the present value method for bond futures transactions.
   */
  public void presentValue() {
    final CurrencyAmount pvComputed = METHOD_FUTURE_TRANSACTION.presentValue(FUTURE_TRANSACTION, CURVES);
    final double priceFuture = METHOD_FUTURE_SECURITY.price(BOND_FUTURE_SECURITY, CURVES);
    final double pvExpected = (priceFuture - REFERENCE_PRICE) * NOTIONAL * QUANTITY;
    assertEquals("Bond future transaction Discounting Method: present value currency", CUR, pvComputed.getCurrency());
    assertEquals("Bond future transaction Discounting Method: present value amount", pvExpected, pvComputed.getAmount(), 1.0E-2);
    final InterestRateDerivative derivative = FUTURE_TRANSACTION;
    final CurrencyAmount pvComputed2 = METHOD_FUTURE_TRANSACTION.presentValue(derivative, CURVES);
    assertEquals("Bond future transaction Discounting Method: present value", pvComputed, pvComputed2);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double presentValueCalculator = calculator.visit(FUTURE_TRANSACTION, CURVES);
    assertEquals("IR future transaction Method: present value from price", pvComputed.getAmount(), presentValueCalculator);
  }

  @Test
  /**
   * Tests the present value method for bond futures transactions.
   */
  public void presentValueFromPrice() {
    final double quotedPrice = 1.05;
    final double presentValueMethod = METHOD_FUTURE_TRANSACTION.presentValueFromPrice(FUTURE_TRANSACTION, quotedPrice);
    assertEquals("Bond future transaction Method: present value from price", (quotedPrice - REFERENCE_PRICE) * QUANTITY * NOTIONAL, presentValueMethod);
    final PresentValueFromFuturePriceCalculator calculator = PresentValueFromFuturePriceCalculator.getInstance();
    final double presentValueCalculator = calculator.visit(FUTURE_TRANSACTION, quotedPrice);
    assertEquals("Bond future transaction Method: present value from price", presentValueMethod, presentValueCalculator);
  }

  @Test
  /**
   * Tests the present value curve sensitivity method for bond futures transactions.
   */
  public void presentValueCurveSensitivity() {
    final InterestRateCurveSensitivity pvcsComputed = METHOD_FUTURE_TRANSACTION.presentValueCurveSensitivity(FUTURE_TRANSACTION, CURVES);
    final InterestRateCurveSensitivity pcsSecurity = METHOD_FUTURE_SECURITY.priceCurveSensitivity(BOND_FUTURE_SECURITY, CURVES);
    final InterestRateCurveSensitivity pvcsExpected = pcsSecurity.multiply(QUANTITY * NOTIONAL);
    assertEquals("Bond future transaction Discounting Method: present value curve sensitivity", pvcsExpected, pvcsComputed);
    final PresentValueCurveSensitivityCalculator calculator = PresentValueCurveSensitivityCalculator.getInstance();
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(calculator.visit(FUTURE_TRANSACTION, CURVES));
    assertEquals("Bond future transaction Discounting Method: present value curve sensitivity", pvcsComputed, pvcsCalculator);
  }

}

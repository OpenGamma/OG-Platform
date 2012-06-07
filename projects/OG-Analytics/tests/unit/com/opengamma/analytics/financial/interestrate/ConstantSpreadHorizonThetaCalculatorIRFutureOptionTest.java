/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * 
 */
public class ConstantSpreadHorizonThetaCalculatorIRFutureOptionTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 13);
  private static final ConstantSpreadHorizonThetaCalculator CALC_SGLSHIFT = new ConstantSpreadHorizonThetaCalculator(REFERENCE_DATE, 1);

  // The following builds up an Interest Rate Future Option Definition, and the market data the Black model requires for it
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future option mid-curve 1Y
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final double STRIKE = 0.9805;
  private static final InterestRateFutureDefinition ERU2 = new InterestRateFutureDefinition(LAST_TRADING_DATE, STRIKE, LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, 1, NAME);
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_ERU2 = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE, IS_CALL);
  // Transaction
  private static final int QUANTITY = -123;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 5, 12);
  private static final double TRADE_PRICE = 0.0050;
  private static final InterestRateFutureOptionMarginTransactionDefinition OPTION_TRANSACTION = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2, QUANTITY, TRADE_DATE, TRADE_PRICE);

  // Market Data 
  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesEUR();
  private static final String[] CURVE_NAMES = TestsDataSetsBlack.curvesEURNames();
  private static final InterpolatedDoublesSurface BLACK_PARAMETER = TestsDataSetsBlack.createBlackSurface();
  private static final YieldCurveWithBlackCubeBundle BLACK_BUNDLE = new YieldCurveWithBlackCubeBundle(BLACK_PARAMETER, CURVES);

  @Test
  /**
   * Test sensitivity to reference price - theoretically 0
   */
  public void thetaIRFO_referencePrice() {
    final double refZero = 0.0;
    MultipleCurrencyAmount theta1_0 = CALC_SGLSHIFT.getTheta(OPTION_TRANSACTION, REFERENCE_DATE, CURVE_NAMES, BLACK_BUNDLE, refZero);
    final double refOne = 0.1;
    MultipleCurrencyAmount theta1_1 = CALC_SGLSHIFT.getTheta(OPTION_TRANSACTION, REFERENCE_DATE, CURVE_NAMES, BLACK_BUNDLE, refOne);

    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final InterestRateFutureOptionMarginTransaction derivative = OPTION_TRANSACTION.toDerivative(REFERENCE_DATE, refOne, CURVE_NAMES);
    final double pvToday = derivative.accept(pvCalculator, BLACK_BUNDLE);

    assertEquals("InterestRateFutureOption Theta - sensitive to reference rate: ", 0.0, (theta1_0.getAmount(CUR) - theta1_1.getAmount(CUR)) / pvToday, 1e-15);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  /**
   * Test to ensure that one cannot ask the calculator for the value between now and now
   */
  public void thetaIRFO_ZeroShift() {
    new ConstantSpreadHorizonThetaCalculator(REFERENCE_DATE, 0);

  }

  @Test
  /**
   * Useful visualisation to check behaviour as we shift further out
   */
  public void thetaIRFO_DifferentShifts() {
    final int nSteps = 10;
    final double[] thetas = new double[nSteps];
    for (int i = 1; i <= nSteps; i++) {
      ConstantSpreadHorizonThetaCalculator calc = new ConstantSpreadHorizonThetaCalculator(REFERENCE_DATE, i * 10);
      MultipleCurrencyAmount theta = calc.getTheta(OPTION_TRANSACTION, REFERENCE_DATE, CURVE_NAMES, BLACK_BUNDLE, TRADE_PRICE);
      thetas[i - 1] = theta.getAmount(CUR);
      //      System.out.println(" , " + thetas[i - 1]);
    }
  }

  @Test
  /**
   * We expect the FutureOption to provide theta one day before expiry
   */
  public void thetaIRFO_EveOfExpiry() {
    ZonedDateTime eveOfExpiry = EXPIRATION_DATE.minusDays(1);
    ConstantSpreadHorizonThetaCalculator calc = new ConstantSpreadHorizonThetaCalculator(eveOfExpiry, 1);
    MultipleCurrencyAmount theta = calc.getTheta(OPTION_TRANSACTION, eveOfExpiry, CURVE_NAMES, BLACK_BUNDLE, TRADE_PRICE);
    assertTrue("InterestRateFutureOption Theta - expected to be non-zero on the day before expiry: ", 0.0 > theta.getAmount(CUR));
  }

  @Test
  /**
   * We currently model the theta of margined future options to lose all their value as time shifts across expiry
   * TODO - Review this choice, and behaviour of TodayPaymentCalculator
   */
  public void thetaIRFO_AcrossExpiry() {
    ConstantSpreadHorizonThetaCalculator calc = new ConstantSpreadHorizonThetaCalculator(EXPIRATION_DATE, 1);
    MultipleCurrencyAmount theta = calc.getTheta(OPTION_TRANSACTION, EXPIRATION_DATE, CURVE_NAMES, BLACK_BUNDLE, TRADE_PRICE);
    InterestRateFutureOptionMarginTransaction derivAtExpiry = OPTION_TRANSACTION.toDerivative(EXPIRATION_DATE, TRADE_PRICE, CURVE_NAMES);
    final double valueAtExpiry = derivAtExpiry.accept(PresentValueBlackCalculator.getInstance(), BLACK_BUNDLE);
    assertEquals("InterestRateFutureOption Theta - Across Expiry: ", -1 * valueAtExpiry, theta.getAmount(CUR), 0);
  }
}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ConstantSpreadHorizonThetaCalculatorIRFutureOptionTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 13);
  private static final ConstantSpreadHorizonThetaCalculator CALC = ConstantSpreadHorizonThetaCalculator.getInstance();// TODO: Change the calculator to a provider

  // The following builds up an Interest Rate Future Option Definition, and the market data the Black model requires for it
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future option mid-curve 1Y
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final double STRIKE = 0.9805;
  private static final InterestRateFutureSecurityDefinition ERU2 = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
  // InterestRateFutureTransactionDefinition(LAST_TRADING_DATE, STRIKE, LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, 1, NAME);
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
  private static final InterpolatedDoublesSurface BLACK_PARAMETER = TestsDataSetsBlack.createBlackSurfaceExpiryTenor();
  private static final YieldCurveWithBlackCubeBundle BLACK_BUNDLE = new YieldCurveWithBlackCubeBundle(BLACK_PARAMETER, CURVES);

  // constants
  private static final double PRICE_ZERO = 0.0;
  private static final double PRICE_ONE = 0.1;
  private static final int ONE_DAY_FWD = 1;

  @Test(enabled = false)
  // TODO: Change the calculator to a provider
  /**
   * Test sensitivity to reference price - theoretically 0
   */
  public void thetaIRFOReferencePrice() {
    final MultipleCurrencyAmount theta1_0 = CALC.getTheta(OPTION_TRANSACTION, REFERENCE_DATE, CURVE_NAMES, BLACK_BUNDLE, PRICE_ZERO, ONE_DAY_FWD);
    final MultipleCurrencyAmount theta1_1 = CALC.getTheta(OPTION_TRANSACTION, REFERENCE_DATE, CURVE_NAMES, BLACK_BUNDLE, PRICE_ONE, ONE_DAY_FWD);

    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final InterestRateFutureOptionMarginTransaction derivative = OPTION_TRANSACTION.toDerivative(REFERENCE_DATE, PRICE_ONE, CURVE_NAMES);
    final double pvToday = derivative.accept(pvCalculator, BLACK_BUNDLE);

    assertEquals("InterestRateFutureOption Theta - sensitive to reference rate: ", 0.0, (theta1_0.getAmount(CUR) - theta1_1.getAmount(CUR)) / pvToday, 1e-15);
  }

  @Test(enabled = false)
  // TODO: Change the calculator to a provider (expectedExceptions = IllegalArgumentException.class)
  /**
   * Was a useful visualisation to check behaviour as we shift further out.
   * Now confirms that getTheta will not handle shifts other than 1 and -1.
   */
  public void thetaIRFODifferentShifts() { // TODO Ensure we can go backwards
    final int nSteps = 10;
    for (int i = -1; i <= nSteps; i++) {
      final MultipleCurrencyAmount theta = CALC.getTheta(OPTION_TRANSACTION, REFERENCE_DATE, CURVE_NAMES, BLACK_BUNDLE, TRADE_PRICE, i);
      System.out.println(i + " , " + theta.getAmount(CUR));
    }
  }

  @Test(enabled = false)
  // TODO: Change the calculator to a provider
  /**
   * We expect the FutureOption to provide theta one day before expiry
   */
  public void thetaIRFOEveOfExpiry() {
    final ZonedDateTime eveOfExpiry = EXPIRATION_DATE.minusDays(1);
    final MultipleCurrencyAmount theta = CALC.getTheta(OPTION_TRANSACTION, eveOfExpiry, CURVE_NAMES, BLACK_BUNDLE, TRADE_PRICE, ONE_DAY_FWD);
    assertTrue("InterestRateFutureOption Theta - expected to be non-zero on the day before expiry: ", 0.0 > theta.getAmount(CUR));
  }

  @Test(enabled = false)
  // TODO: Change the calculator to a provider
  /**
   * We currently model the theta of margined future options to lose all their value as time shifts across expiry
   * TODO - Review this choice, and behaviour of TodayPaymentCalculator
   */
  public void thetaIRFOAcrossExpiry() {
    final MultipleCurrencyAmount theta = CALC.getTheta(OPTION_TRANSACTION, EXPIRATION_DATE, CURVE_NAMES, BLACK_BUNDLE, TRADE_PRICE, ONE_DAY_FWD);
    final InterestRateFutureOptionMarginTransaction derivAtExpiry = OPTION_TRANSACTION.toDerivative(EXPIRATION_DATE, TRADE_PRICE, CURVE_NAMES);
    final double valueAtExpiry = derivAtExpiry.accept(PresentValueBlackCalculator.getInstance(), BLACK_BUNDLE);
    assertEquals("InterestRateFutureOption Theta - Across Expiry: ", -1 * valueAtExpiry, theta.getAmount(CUR), 0);
  }
}

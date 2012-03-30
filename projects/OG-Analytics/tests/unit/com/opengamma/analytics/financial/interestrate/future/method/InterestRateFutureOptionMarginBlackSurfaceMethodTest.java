package com.opengamma.analytics.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.surface.SurfaceValue;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

public class InterestRateFutureOptionMarginBlackSurfaceMethodTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final IborIndex EURIBOR3M_INDEX = IndexIborTestsMaster.getInstance().getIndex("EURIBOR3M", CALENDAR);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M_INDEX.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0;
  private static final String NAME = "ERU2";
  private static final double STRIKE = 0.9850;
  private static final InterestRateFutureDefinition ERU2_DEFINITION = new InterestRateFutureDefinition(LAST_TRADING_DATE, STRIKE, LAST_TRADING_DATE, EURIBOR3M_INDEX, REFERENCE_PRICE, NOTIONAL,
      FUTURE_FACTOR, 1, NAME);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String[] CURVE_NAMES = TestsDataSetsBlack.curvesEURNames();
  private static final InterestRateFuture ERU2 = ERU2_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE, CURVE_NAMES);
  // Option 
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final boolean IS_CALL = true;

  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_ERU2_DEFINITION = new InterestRateFutureOptionMarginSecurityDefinition(ERU2_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);
  private static final InterestRateFutureOptionMarginSecurity OPTION_ERU2 = OPTION_ERU2_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  // Transaction
  private static final int QUANTITY = -123;
  private static final double TRADE_PRICE = 0.0050;
  private static final ZonedDateTime TRADE_DATE_1 = DateUtils.getUTCDate(2010, 8, 17, 13, 00);
  private static final ZonedDateTime TRADE_DATE_2 = DateUtils.getUTCDate(2010, 8, 18, 9, 30);
  private static final double MARGIN_PRICE = 0.0025; // Settle price for 17-Aug
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_1_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2_DEFINITION, QUANTITY,
      TRADE_DATE_1, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION_1 = TRANSACTION_1_DEFINITION.toDerivative(REFERENCE_DATE, MARGIN_PRICE, CURVE_NAMES);
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_2_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2_DEFINITION, QUANTITY,
      TRADE_DATE_2, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION_2 = TRANSACTION_2_DEFINITION.toDerivative(REFERENCE_DATE, MARGIN_PRICE, CURVE_NAMES);

  private static final InterestRateFutureDiscountingMethod METHOD_FUTURES = InterestRateFutureDiscountingMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod METHOD_SECURITY_OPTION_BLACK = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod METHOD_TRANSACTION_OPTION_BLACK = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();
  private static final PresentValueBlackCalculator PVC_BLACK = PresentValueBlackCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackCalculator PVCSC_BLACK = PresentValueCurveSensitivityBlackCalculator.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesEUR();
  private static final InterpolatedDoublesSurface BLACK_PARAMETER = TestsDataSetsBlack.createBlackSurface();
  private static final YieldCurveWithBlackCubeBundle BLACK_BUNDLE = new YieldCurveWithBlackCubeBundle(BLACK_PARAMETER, CURVES);

  private static final double VOL_SHIFT = 1.0E-6;
  private static final double DELTA_SHIFT = 1.0E-6;
  //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;

  @Test
  /**
   * Test the option price from the future price. Standard option.
   */
  public void price() {
    final double rateStrike = 1.0 - STRIKE;
    final double expiry = OPTION_ERU2.getExpirationTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, expiry, !IS_CALL);
    final double priceFuture = METHOD_FUTURES.price(ERU2, BLACK_BUNDLE);
    final double forward = 1 - priceFuture;
    final double volatility = BLACK_BUNDLE.getVolatility(expiry, rateStrike);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double priceExpected = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    final double priceComputed = METHOD_SECURITY_OPTION_BLACK.optionPrice(OPTION_ERU2, BLACK_BUNDLE);
    assertEquals("Future option with Black volatilities: option security price", priceExpected, priceComputed);
  }

  @Test
  /**
   * Test the option transaction present value.
   */
  public void presentValue() {
    final double priceOption = METHOD_SECURITY_OPTION_BLACK.optionPrice(OPTION_ERU2, BLACK_BUNDLE);
    final double presentValue1Expected = (priceOption - MARGIN_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    final CurrencyAmount presentValue1Computed = METHOD_TRANSACTION_OPTION_BLACK.presentValue(TRANSACTION_1, BLACK_BUNDLE);
    assertEquals("Future option with Black volatilities: option transaction pv", presentValue1Expected, presentValue1Computed.getAmount(), TOLERANCE_PRICE);
    final double presentValue2Expected = (priceOption - TRADE_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    final CurrencyAmount presentValue2Computed = METHOD_TRANSACTION_OPTION_BLACK.presentValue(TRANSACTION_2, BLACK_BUNDLE);
    assertEquals("Future option with Black volatilities: option transaction pv", presentValue2Expected, presentValue2Computed.getAmount(), TOLERANCE_PRICE);
    final double presentValue1Calculator = PVC_BLACK.visit(TRANSACTION_1, BLACK_BUNDLE);
    assertEquals("Future option with Black volatilities: option transaction pv", presentValue1Computed.getAmount(), presentValue1Calculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the present value curves sensitivity computed from the curves
   */
  public void presentValueCurveSensitivity() {
    final InterestRateCurveSensitivity pvcsMethod = METHOD_TRANSACTION_OPTION_BLACK.presentValueCurveSensitivity(TRANSACTION_1, BLACK_BUNDLE);
    pvcsMethod.cleaned();
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    String[] curvesBumpedForward = new String[] {CURVE_NAMES[0], bumpedCurveName};
    InterestRateFutureOptionMarginTransaction transactionBumped = TRANSACTION_1_DEFINITION.toDerivative(REFERENCE_DATE, TRADE_PRICE, curvesBumpedForward);
    final double[] nodeTimesForward = new double[] {ERU2.getFixingPeriodStartTime(), ERU2.getFixingPeriodEndTime()};
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(transactionBumped, BLACK_BUNDLE, CURVE_NAMES[1], bumpedCurveName, nodeTimesForward, DELTA_SHIFT,
        METHOD_TRANSACTION_OPTION_BLACK);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvcsMethod.getSensitivities().get(CURVE_NAMES[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity future pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], TOLERANCE_DELTA);
    }
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(PVCSC_BLACK.visit(TRANSACTION_1, BLACK_BUNDLE));
    assertTrue("Future option with Black volatilities: option transaction curve sensi", InterestRateCurveSensitivity.compare(pvcsMethod, pvcsCalculator, TOLERANCE_DELTA));
  }

  @Test
  /**
   * Test the option price Black sensitivity
   */
  public void priceBlackSensitivity() {
    final InterpolatedDoublesSurface blackParameterPlus = TestsDataSetsBlack.createBlackSurfaceShift(VOL_SHIFT);
    final InterpolatedDoublesSurface blackParameterMinus = TestsDataSetsBlack.createBlackSurfaceShift(-VOL_SHIFT);
    final YieldCurveWithBlackCubeBundle blackBundlePlus = new YieldCurveWithBlackCubeBundle(blackParameterPlus, CURVES);
    final YieldCurveWithBlackCubeBundle blackBundleMinus = new YieldCurveWithBlackCubeBundle(blackParameterMinus, CURVES);
    final double pricePlus = METHOD_SECURITY_OPTION_BLACK.optionPrice(OPTION_ERU2, blackBundlePlus);
    final double priceMinus = METHOD_SECURITY_OPTION_BLACK.optionPrice(OPTION_ERU2, blackBundleMinus);
    final double priceSensiExpected = (pricePlus - priceMinus) / (2 * VOL_SHIFT);
    final SurfaceValue priceSensiComputed = METHOD_SECURITY_OPTION_BLACK.priceBlackSensitivity(OPTION_ERU2, BLACK_BUNDLE);
    final DoublesPair point = new DoublesPair(OPTION_ERU2.getExpirationTime(), STRIKE);
    assertEquals("Future option with Black volatilities: option security vol sensi", priceSensiExpected, priceSensiComputed.getMap().get(point), TOLERANCE_DELTA);
    assertEquals("Future option with Black volatilities: option security vol sensi", 1, priceSensiComputed.getMap().size());
  }

  @Test
  /**
   * Test the option price Black sensitivity
   */
  public void presentValueBlackSensitivity() {
    final SurfaceValue pvbsSecurity = METHOD_SECURITY_OPTION_BLACK.priceBlackSensitivity(OPTION_ERU2, BLACK_BUNDLE);
    final SurfaceValue pvbsTransactionComputed = METHOD_TRANSACTION_OPTION_BLACK.presentValueBlackSensitivity(TRANSACTION_1, BLACK_BUNDLE);
    final SurfaceValue pvbsTransactionExpected = SurfaceValue.multiplyBy(pvbsSecurity, QUANTITY * NOTIONAL * FUTURE_FACTOR);
    assertTrue("Future option with Black volatilities: option security vol sensi", SurfaceValue.compare(pvbsTransactionComputed, pvbsTransactionExpected, TOLERANCE_DELTA));
  }

}

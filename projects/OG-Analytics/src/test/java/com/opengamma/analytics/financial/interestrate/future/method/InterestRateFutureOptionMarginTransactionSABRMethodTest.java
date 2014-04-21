/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the method for interest rate future option with SABR volatility parameter surfaces.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionMarginTransactionSABRMethodTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "EDU2";
  private static final double STRIKE = 0.9850;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAMES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };
  // Option
  private static final InterestRateFutureSecurityDefinition EDU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
  private static final InterestRateFutureSecurity EDU2 = EDU2_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAMES);
  // Option
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final double EXPIRATION_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurity OPTION_EDU2 = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE, IS_CALL);
  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_EDU2_DEFINITION = new InterestRateFutureOptionMarginSecurityDefinition(EDU2_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);
  // Transaction
  private static final int QUANTITY = -123;
  private static final double TRADE_PRICE = 0.0050;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_EDU2_DEFINITION, QUANTITY,
      TRADE_DATE, TRADE_PRICE);

  private static final InterestRateFutureOptionMarginTransactionSABRMethod METHOD = InterestRateFutureOptionMarginTransactionSABRMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecuritySABRMethod METHOD_SECURITY = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();

  private static final YieldCurveBundle CURVES_BUNDLE = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR1();
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
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
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
    final double pvNoPremiumCalculator = transactionNoPremium.accept(PVC, SABR_BUNDLE);
    assertEquals("Future option: present value: Method vs Calculator", pvNoPremiumMethod, pvNoPremiumCalculator);
  }

  @Test
  /**
   * Test the present value curves sensitivity computed from the curves
   */
  public void presentValueCurveSensitivity() {
    final InterestRateCurveSensitivity pvsFuture = METHOD.presentValueCurveSensitivity(TRANSACTION, SABR_BUNDLE);
    pvsFuture.cleaned();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] curvesBumpedForward = new String[] {DISCOUNTING_CURVE_NAME, bumpedCurveName };
    final InterestRateFutureOptionMarginTransaction transactionBumped = TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE, TRADE_PRICE, curvesBumpedForward);
    final double[] nodeTimesForward = new double[] {EDU2.getFixingPeriodStartTime(), EDU2.getFixingPeriodEndTime() };
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(transactionBumped, SABR_BUNDLE, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvsFuture.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity future pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Tests that the method return the same result as the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final PresentValueCurveSensitivitySABRCalculator calculator = PresentValueCurveSensitivitySABRCalculator.getInstance();
    final Map<String, List<DoublesPair>> sensiCalculator = TRANSACTION.accept(calculator, SABR_BUNDLE);
    final InterestRateCurveSensitivity sensiMethod = METHOD.presentValueCurveSensitivity(TRANSACTION, SABR_BUNDLE);
    assertEquals("Future option curve sensitivity: method comparison with present value calculator", sensiCalculator, sensiMethod.getSensitivities());
    final InterestRateFutureOptionMarginSecuritySABRMethod methodSecurity = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();
    final InterestRateCurveSensitivity sensiSecurity = methodSecurity.priceCurveSensitivity(OPTION_EDU2, SABR_BUNDLE);
    final InterestRateCurveSensitivity sensiFromSecurity = sensiSecurity.multipliedBy(QUANTITY * NOTIONAL * FUTURE_FACTOR);
    for (int looppt = 0; looppt < sensiMethod.getSensitivities().get(FORWARD_CURVE_NAME).size(); looppt++) {
      assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getSensitivities().get(FORWARD_CURVE_NAME).get(looppt).first, sensiFromSecurity
          .getSensitivities().get(FORWARD_CURVE_NAME).get(looppt).first, 1.0E-10);
      assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getSensitivities().get(FORWARD_CURVE_NAME).get(looppt).second, sensiFromSecurity
          .getSensitivities().get(FORWARD_CURVE_NAME).get(looppt).second, 1.0E-2);
    }
  }

  @Test
  public void presentValueSABRSensitivity() {
    final PresentValueSABRSensitivityDataBundle pvcs = METHOD.presentValueSABRSensitivity(TRANSACTION, SABR_BUNDLE);
    // SABR sensitivity vs finite difference
    final double pv = METHOD.presentValue(TRANSACTION, SABR_BUNDLE).getAmount();
    final double shift = 0.000001;
    final double delay = EDU2.getTradingLastTime() - OPTION_EDU2.getExpirationTime();
    final DoublesPair expectedExpiryDelay = DoublesPair.of(OPTION_EDU2.getExpirationTime(), delay);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shift);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, CURVES_BUNDLE);
    final double pvAlphaBumped = METHOD.presentValue(TRANSACTION, sabrBundleAlphaBumped).getAmount();
    final double expectedAlphaSensi = (pvAlphaBumped - pv) / shift;
    assertEquals("Number of alpha sensitivity", pvcs.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvcs.getAlpha().getMap().keySet().contains(expectedExpiryDelay), true);
    assertEquals("Alpha sensitivity value", pvcs.getAlpha().getMap().get(expectedExpiryDelay), expectedAlphaSensi, 1.0E+1);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped(shift);
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, CURVES_BUNDLE);
    final double pvRhoBumped = METHOD.presentValue(TRANSACTION, sabrBundleRhoBumped).getAmount();
    final double expectedRhoSensi = (pvRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvcs.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvcs.getRho().getMap().keySet().contains(expectedExpiryDelay), true);
    assertEquals("Rho sensitivity value", pvcs.getRho().getMap().get(expectedExpiryDelay), expectedRhoSensi, 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped(shift);
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, CURVES_BUNDLE);
    final double pvNuBumped = METHOD.presentValue(TRANSACTION, sabrBundleNuBumped).getAmount();
    final double expectedNuSensi = (pvNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvcs.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvcs.getNu().getMap().keySet().contains(expectedExpiryDelay), true);
    assertEquals("Nu sensitivity value", pvcs.getNu().getMap().get(expectedExpiryDelay), expectedNuSensi, 1.0E+0);
  }

  @Test
  /**
   * Tests that the method return the same result as the calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivitySABRCalculator calculator = PresentValueSABRSensitivitySABRCalculator.getInstance();
    final PresentValueSABRSensitivityDataBundle sensiCalculator = TRANSACTION.accept(calculator, SABR_BUNDLE);
    final PresentValueSABRSensitivityDataBundle sensiMethod = METHOD.presentValueSABRSensitivity(TRANSACTION, SABR_BUNDLE);
    assertEquals("Future option curve sensitivity: method comparison with present value calculator", sensiCalculator, sensiMethod);
    final InterestRateFutureOptionMarginSecuritySABRMethod methodSecurity = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();
    PresentValueSABRSensitivityDataBundle sensiSecurity = methodSecurity.priceSABRSensitivity(OPTION_EDU2, SABR_BUNDLE);
    sensiSecurity = sensiSecurity.multiplyBy(QUANTITY * NOTIONAL * FUTURE_FACTOR);
    assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getAlpha(), sensiSecurity.getAlpha());
    assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getRho(), sensiSecurity.getRho());
    assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getNu(), sensiSecurity.getNu());
  }

}

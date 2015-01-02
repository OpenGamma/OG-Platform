/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.InterpolatorTestUtil;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class STIRFuturesOptionNormalExpSimpleMoneynessMethodE2ETest {

  private static final Interpolator1D SQUARE_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.SQUARE_LINEAR,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D TIME_SQUARE_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.TIME_SQUARE,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(TIME_SQUARE_FLAT, SQUARE_FLAT);

  private static final double[] EXPIRY;
  private static final double[] SIMPLEMONEY;
  private static final double[] VOL = new double[] {
      1.0623, 1.0623, 1.0623, 1.0623, 1.0623, 0.9517, 0.8098, 0.6903, 0.6519, 0.6872, 0.7490, 0.8161, 0.8823,
      1.0623, 1.0623, 1.0623, 1.0623, 1.0623, 0.9517, 0.8098, 0.6903, 0.6519, 0.6872, 0.7490, 0.8161, 0.8823,
      1.0623, 1.0623, 1.0623, 1.0623, 1.0623, 0.9517, 0.8098, 0.6903, 0.6519, 0.6872, 0.7490, 0.8161, 0.8823,
      1.1414, 1.0815, 1.0316, 0.9926, 0.9638, 0.8791, 0.7843, 0.7094, 0.6817, 0.6948, 0.7252, 0.7617, 0.8002,
      1.1278, 1.0412, 0.9654, 0.9021, 0.8511, 0.8108, 0.7794, 0.7551, 0.7369, 0.7240, 0.7160, 0.7128, 0.7144,
      0.9697, 0.9412, 0.9130, 0.8854, 0.8585, 0.8327, 0.8084, 0.7861, 0.7664, 0.7502, 0.7383, 0.7318, 0.7317,
      0.9611, 0.9265, 0.8938, 0.8630, 0.8347, 0.8089, 0.7859, 0.7659, 0.7489, 0.7351, 0.7242, 0.7161, 0.7105,
      0.9523, 0.9116, 0.8741, 0.8401, 0.8101, 0.7843, 0.7626, 0.7451, 0.7310, 0.7197, 0.7098, 0.7000, 0.6886
  };
  private static final double[] EXPIRY_SET = new double[] {7.0 / 365.0, 14.0 / 365.0, 21.0 / 365.0, 30.0 / 365.0,
      60.0 / 365.0, 90.0 / 365.0, 120.0 / 365.0, 180.0 / 365.0 };
  private static final double[] MONEYNESS_SET = new double[] {-8.0E-3, -7.0E-3, -6.0E-3, -5.0E-3, -4.0E-3, -3.0E-3,
      -2.0E-3, -1.0E-3, 0.0, 1.0E-3, 2.0E-3, 3.0E-3, 4.0E-3 };
  private static final int NUM_EXPIRY = EXPIRY_SET.length;
  private static final int NUM_MONEY = MONEYNESS_SET.length;
  static {
    int nTotal = NUM_EXPIRY * NUM_MONEY;
    EXPIRY = new double[nTotal];
    SIMPLEMONEY = new double[nTotal];
    for (int i = 0; i < NUM_EXPIRY; ++i) {
      for (int j = 0; j < NUM_MONEY; ++j) {
        EXPIRY[i * NUM_MONEY + j] = EXPIRY_SET[i];
        SIMPLEMONEY[i * NUM_MONEY + j] = MONEYNESS_SET[j];
      }
    }
  }

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  final private static InterpolatedDoublesSurface VOL_SURFACE_SIMPLEMONEY = InterpolatedDoublesSurface.from(EXPIRY,
      SIMPLEMONEY, VOL, INTERPOLATOR_2D);
  final private static NormalSTIRFuturesExpSimpleMoneynessProviderDiscount NORMAL_MULTICURVES = new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(
      MULTICURVES, VOL_SURFACE_SIMPLEMONEY, EURIBOR3M);

  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE,
      -EURIBOR3M.getSpotLag(), TARGET);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final double STRIKE = 0.9850;
  private static final InterestRateFutureSecurityDefinition ERU2_DEFINITION = new InterestRateFutureSecurityDefinition(
      LAST_TRADING_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, TARGET);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18, 10, 0);
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_ERU2_DEFINITION = new InterestRateFutureOptionMarginSecurityDefinition(
      ERU2_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);
  private static final InterestRateFutureOptionMarginSecurity OPTION_ERU2 = OPTION_ERU2_DEFINITION
      .toDerivative(REFERENCE_DATE);

  private static final InterestRateFutureOptionMarginSecurityNormalSmileMethod METHOD_SECURITY_OPTION_NORMAL = InterestRateFutureOptionMarginSecurityNormalSmileMethod
      .getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = InterestRateFutureSecurityDiscountingMethod
      .getInstance();
  private static final NormalPriceFunction NORMAL_FUNCTION = new NormalPriceFunction();

  @Test
  public void blackTest() {
    double expirationTime = 1.4959;
    double volatility = 0.9;
    double futurePrice = 98.5 * 0.01;
    double strike = 99.5 * 0.01;
    double forwardInterestRate = (1.0 - futurePrice);
    double strikeInterestRate = (1.0 - strike);

    System.out.println(BlackFormulaRepository.price(futurePrice, strike, expirationTime, volatility, IS_CALL));
    System.out.println(BlackFormulaRepository.price(forwardInterestRate, strikeInterestRate, expirationTime,
        volatility, !IS_CALL));
    System.out.println();

  }

  @Test
  public void test() {
    double computed = METHOD_SECURITY_OPTION_NORMAL.price(OPTION_ERU2, NORMAL_MULTICURVES);

    InterestRateFutureSecurity underlyingFuture = ERU2_DEFINITION.toDerivative(REFERENCE_DATE);
    double priceFuture = METHOD_FUTURE.price(underlyingFuture, MULTICURVES); // 1 - forward
    double expirationTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRATION_DATE);
    EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE, expirationTime, IS_CALL);
    double volatility = VOL_SURFACE_SIMPLEMONEY.getZValue(expirationTime, STRIKE - priceFuture);
    final NormalFunctionData normalPoint = new NormalFunctionData(priceFuture, 1.0, volatility);
    double expected = NORMAL_FUNCTION.getPriceFunction(option).evaluate(normalPoint);
    System.out.println(expected + "\t" + computed);
  }

  @Test
  public void decompositionTest() {
    double expirationTime = 1.4959;
    double volatility = 0.9;
    double futurePrice = 101.5 * 0.01;
    double strike = 99.5 * 0.01;
    double forwardInterestRate = (1.0 - futurePrice);
    double strikeInterestRate = (1.0 - strike);
    //        TimeCalculator.getTimeBetween(LocalDate.of(2014, 3, 17), LocalDate.of(2015, 9, 14));
    EuropeanVanillaOption option = new EuropeanVanillaOption(strikeInterestRate, expirationTime, true);
    final NormalFunctionData normalPoint = new NormalFunctionData(forwardInterestRate, 1.0, volatility);
    normalPoint.getForward();
    double computed = NORMAL_FUNCTION.getPriceFunction(option).evaluate(normalPoint);
    System.out.println(computed);

    System.out.println();
    ProbabilityDistribution<Double> normal = new NormalDistribution(0, 1);
    double sigmaRootT = volatility * Math.sqrt(expirationTime);
    double d = (forwardInterestRate - strikeInterestRate) / sigmaRootT;
    double call = normal.getCDF(d) * (forwardInterestRate - strikeInterestRate) + sigmaRootT *
        normal.getPDF(d);
    double put = normal.getCDF(-d) * (strikeInterestRate - forwardInterestRate) + sigmaRootT *
        normal.getPDF(d);
    System.out.println(call);
    System.out.println(put);

    System.out.println();
    double dP = (futurePrice - strike) / sigmaRootT;
    double callP = normal.getCDF(dP) * (futurePrice - strike) + sigmaRootT *
        normal.getPDF(dP);
    double putP = normal.getCDF(-dP) * (strike - futurePrice) + sigmaRootT *
        normal.getPDF(dP);
    System.out.println(callP);
    System.out.println(putP);

    System.out.println();
    System.out.println(BlackFormulaRepository.price(0.410, 0.500, expirationTime, volatility, false));
    System.out.println(BlackFormulaRepository.price(100.0 - 0.410, 100.0 - 0.500, expirationTime, volatility, true));
  }

  /**
   * Print volatility surface
   */
  @Test(enabled = false)
  public void volatilitySurfacePrintTest() {
    int nSample = 100;
    double minExpiry = EXPIRY_SET[0] * 0.8;
    double maxExpiry = EXPIRY_SET[NUM_EXPIRY - 1] * 1.2;
    double intervalExpiry = (maxExpiry - minExpiry) / (nSample - 1.0);

    double minMoney = MONEYNESS_SET[0] * 1.2;
    double maxMoney = MONEYNESS_SET[NUM_MONEY - 1] * 1.2;
    double intervalMoney = (maxMoney - minMoney) / (nSample - 1.0);

    for (int j = 0; j < nSample; ++j) {
      double moneyness = minMoney + intervalMoney * j;
      System.out.print("\t" + moneyness);
    }
    System.out.print("\n");
    for (int i = 0; i < nSample; ++i) {
      double expiry = minExpiry + intervalExpiry * i;
      System.out.print(expiry);
      for (int j = 0; j < nSample; ++j) {
        double moneyness = minMoney + intervalMoney * j;
        System.out.print("\t" + VOL_SURFACE_SIMPLEMONEY.getZValue(expiry, moneyness));
      }
      System.out.print("\n");
    }
  }

  /**
   * Check surface is correctly interpolated. 
   */
  @Test
  public void Interpolation2DTest() {
    double tol = 1.0e-10;

    double[] expiry = new double[] {22.0 / 365.0, 22.0 / 365.0, 22.0 / 365.0, 22.0 / 365.0, 22.0 / 365.0, 57.0 / 365.0,
        57.0 / 365.0, 57.0 / 365.0, 57.0 / 365.0, 57.0 / 365.0 };
    double[] moneyness = new double[] {-0.001086366, -0.000180979, 0.0, 0.000723589, 0.00162734, -0.001540065,
        -0.000633857, 0.0, 0.00027153, 0.001176098 };
    double[] vol = new double[] {0.716515, 0.641929, 0.637017, 0.662312, 0.747397, 0.703106, 0.677655, 0.663821,
        0.659139, 0.649632 };
    InterpolatedDoublesSurface surface = InterpolatedDoublesSurface.from(expiry, moneyness, vol, INTERPOLATOR_2D);

    double keyMoneyness = 0.001;
    double computed1 = surface.getZValue(22.0 / 365.0, keyMoneyness);
    double ratio1 = (keyMoneyness - 0.000723589) / (0.00162734 - 0.000723589);
    double exp1 = Math.sqrt(0.662312 * 0.662312 * (1.0 - ratio1) + 0.747397 * 0.747397 * ratio1);
    InterpolatorTestUtil.assertRelative("Interpolation2DTest, moneyness", exp1, computed1, tol);

    double keyExpiry = 40.0;
    double computed2 = surface.getZValue(keyExpiry / 365.0, 0.0);
    double ratio2 = (keyExpiry - 22.0) / (57.0 - 22.0);
    double exp2 = Math.sqrt((0.637017 * 0.637017 * 22.0 * (1.0 - ratio2) + 0.663821 * 0.663821 * 57.0 * ratio2) /
        keyExpiry);
    InterpolatorTestUtil.assertRelative("Interpolation2DTest, time", exp2, computed2, tol);

    expiry = new double[] {22.0 / 365.0, 57.0 / 365.0, 22.0 / 365.0, 57.0 / 365.0, 22.0 / 365.0,
        57.0 / 365.0, 22.0 / 365.0, 57.0 / 365.0, 22.0 / 365.0, 57.0 / 365.0 };
    moneyness = new double[] {-0.001086366, -0.001540065, -0.000180979, -0.000633857, 0.0, 0.0, 0.000723589,
        0.00027153, 0.00162734, 0.001176098 };
    vol = new double[] {0.716515, 0.703106, 0.641929, 0.677655, 0.637017, 0.663821, 0.662312, 0.659139,
        0.747397, 0.649632 };
    surface = InterpolatedDoublesSurface.from(expiry, moneyness, vol, INTERPOLATOR_2D);
    computed1 = surface.getZValue(22.0 / 365.0, keyMoneyness);
    InterpolatorTestUtil.assertRelative("Interpolation2DTest, moneyness", exp1, computed1, tol);
    computed2 = surface.getZValue(keyExpiry / 365.0, 0.0);
    InterpolatorTestUtil.assertRelative("Interpolation2DTest, time", exp2, computed2, tol);
  }
}

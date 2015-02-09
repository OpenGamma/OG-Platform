/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveEUR;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.DeltaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.GammaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionDeltaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionGammaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionThetaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionVegaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueCurveSensitivityNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.ThetaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.VegaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.InterpolatorTestUtil;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * E2E test for STIR futures option using volatility surface with simple moneyness. 
 */
public class STIRFuturesOptionNormalExpSimpleMoneynessMethodE2ETest {
  private static final STIRFuturesOptionNormalExpSimpleMoneynessExamplesData DATA = new STIRFuturesOptionNormalExpSimpleMoneynessExamplesData();

  /* Interpolators */
  private static final Interpolator1D SQUARE_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.SQUARE_LINEAR,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D TIME_SQUARE_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.TIME_SQUARE,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  /* Interpolation is done along y direction first */
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(TIME_SQUARE_FLAT, SQUARE_FLAT);

  /* Volatility surface */
  private static final double[] EXPIRY = DATA.getExpiry();
  private static final double[] SIMPLEMONEY = DATA.getSimpleMoneyness();
  private static final double[] VOL = DATA.getVolatility();

  /* Curve */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR =
      StandardDataSetsMulticurveEUR.getCurvesEurOisE3();
  private static final MulticurveProviderDiscount MULTICURVES = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  final private static InterpolatedDoublesSurface VOL_SURFACE_SIMPLEMONEY = InterpolatedDoublesSurface.from(EXPIRY,
      SIMPLEMONEY, VOL, INTERPOLATOR_2D);
  final private static NormalSTIRFuturesExpSimpleMoneynessProviderDiscount NORMAL_MULTICURVES = new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(
      MULTICURVES, VOL_SURFACE_SIMPLEMONEY, EURIBOR3M);

  /* Option */
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

  /* Transaction */
  private static final int QUANTITY = 12;
  private static final double TRADE_PRICE = 0.0050;
  private static final ZonedDateTime TRADE_DATE_1 = DateUtils.getUTCDate(2010, 8, 17, 13, 00);
  private static final double MARGIN_PRICE = 0.0025; // Settle price for 17-Aug
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_1_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(
      OPTION_ERU2_DEFINITION, QUANTITY,
      TRADE_DATE_1, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION_1 = TRANSACTION_1_DEFINITION.toDerivative(
      REFERENCE_DATE, MARGIN_PRICE);

  /* Calculator */
  private static final PresentValueNormalSTIRFuturesCalculator PVNFC =
      PresentValueNormalSTIRFuturesCalculator.getInstance();
  private static final PresentValueCurveSensitivityNormalSTIRFuturesCalculator PVCSNFC =
      PresentValueCurveSensitivityNormalSTIRFuturesCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<NormalSTIRFuturesProviderInterface> PSSFC =
      new ParameterSensitivityParameterCalculator<>(PVCSNFC);
  private static final MarketQuoteSensitivityBlockCalculator<NormalSTIRFuturesProviderInterface> MQSBC =
      new MarketQuoteSensitivityBlockCalculator<>(PSSFC);
  private static final PV01CurveParametersCalculator<NormalSTIRFuturesProviderInterface> PV01CPC = new PV01CurveParametersCalculator<>(
      PVCSNFC);
  private static final PositionDeltaNormalSTIRFutureOptionCalculator PDNFOC = PositionDeltaNormalSTIRFutureOptionCalculator
      .getInstance();
  private static final PositionGammaNormalSTIRFutureOptionCalculator PGNFOC = PositionGammaNormalSTIRFutureOptionCalculator
      .getInstance();
  private static final PositionThetaNormalSTIRFutureOptionCalculator PTNFOC = PositionThetaNormalSTIRFutureOptionCalculator
      .getInstance();
  private static final PositionVegaNormalSTIRFutureOptionCalculator PVNFOC = PositionVegaNormalSTIRFutureOptionCalculator
      .getInstance();
  private static final DeltaNormalSTIRFutureOptionCalculator DNFOC = DeltaNormalSTIRFutureOptionCalculator
      .getInstance();
  private static final GammaNormalSTIRFutureOptionCalculator GNFOC = GammaNormalSTIRFutureOptionCalculator
      .getInstance();
  private static final ThetaNormalSTIRFutureOptionCalculator TNFOC = ThetaNormalSTIRFutureOptionCalculator
      .getInstance();
  private static final VegaNormalSTIRFutureOptionCalculator VNFOC = VegaNormalSTIRFutureOptionCalculator.getInstance();

  private static final double BP1 = 1.0E-4;


  /**
   * E2E test for PV and all the risk measures.
   */
  @Test
  public void E2ETest() {
    double tol = 1.0e-10;
    MultipleCurrencyAmount pv = TRANSACTION_1.accept(PVNFC, NORMAL_MULTICURVES);
    assertRelative("E2ETest, pv", 861850.4996823109, pv.getAmount(EUR), tol);

    MultipleCurrencyParameterSensitivity bucketedPv01 = MQSBC.fromInstrument(TRANSACTION_1, NORMAL_MULTICURVES, BLOCK)
        .multipliedBy(BP1);
    // market quote sensitivity, thus nonzero values for discounting curve
     double[] deltaDsc = new double [] {-6.699625499005507E-5, -6.699601771230529E-5, 1.1097176565222359E-10,
        -3.668758024917107E-9, -0.0025658905357422443, -0.004555359103668548, -0.006086354603993385,
        0.17904224681155592, -0.3696466428416493, 0.026504268860874675, -7.840654172613281E-14, 9.633192325368143E-14,
        3.080721153798044E-13, 2.4989987634531895E-13, 1.479886424366408E-13, 1.6993761653354066E-13 };
    double[] deltaFwd = new double[] {-0.07843097744832121, 0.0016386932025324356, 7.200414013419107E-4,
        0.016153849016546293, 212.57700563629456, -361.1909217716853, -2.2771335549327615E-11, 2.63862064877223E-12,
        7.0640044230169935E-12, 1.4665415911199027E-12, 6.039106742059584E-12, 4.3654232078987675E-12,
        3.962055990473498E-12, 4.319049695483522E-12, 3.0470051651095408E-12, 2.605778031824316E-12,
        1.6592159214426629E-12 };
     LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVES.getName(EUR), EUR), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVES.getName(EURIBOR3M), EUR), new DoubleMatrix1D(deltaFwd));
     MultipleCurrencyParameterSensitivity expectedbucket = new MultipleCurrencyParameterSensitivity(sensitivity);
    AssertSensitivityObjects.assertEquals("E2ETest, bucketed pv01", expectedbucket, bucketedPv01, tol);

    ReferenceAmount<Pair<String, Currency>> pv01 = TRANSACTION_1.accept(PV01CPC, NORMAL_MULTICURVES);
    // parameter sensitivity, thus null for discounting curve
    assertRelative("E2ETest, pv01", -149.20874491438397,
        pv01.getMap().get(Pairs.of(MULTICURVES.getName(EURIBOR3M), EUR)), tol);

    Double positionDelta = TRANSACTION_1.accept(PDNFOC, NORMAL_MULTICURVES);
    Double positionGamma = TRANSACTION_1.accept(PGNFOC, NORMAL_MULTICURVES);
    Double positionTheta = TRANSACTION_1.accept(PTNFOC, NORMAL_MULTICURVES);
    Double positionVega = TRANSACTION_1.accept(PVNFOC, NORMAL_MULTICURVES);
    assertRelative("E2ETest, positionDelta", 1514540.7118335292, positionDelta, tol);
    assertRelative("E2ETest, positionGamma", 1672748.1927204835, positionGamma, tol);
    assertRelative("E2ETest, positionTheta", -396583.47181617195, positionTheta, tol);
    assertRelative("E2ETest, positionVega", 1243371.6048490028, positionVega, tol);

    Double delta = TRANSACTION_1.accept(DNFOC, NORMAL_MULTICURVES);
    Double gamma = TRANSACTION_1.accept(GNFOC, NORMAL_MULTICURVES);
    Double theta = TRANSACTION_1.accept(TNFOC, NORMAL_MULTICURVES);
    Double vega = TRANSACTION_1.accept(VNFOC, NORMAL_MULTICURVES);
    assertRelative("E2ETest, delta", 0.5048469039445097, delta, tol);
    assertRelative("E2ETest, gamma", 0.5575827309068279, gamma, tol);
    assertRelative("E2ETest, theta", -0.13219449060539065, theta, tol);
    assertRelative("E2ETest, vega", 0.4144572016163343, vega, tol);
  }

  /**
   * Check data points are correctly stored.
   */
  @Test
  public void nodePointTest() {
    double tol = 1.0e-14;
    /* sample points randomly chosen */
    assertRelative("nodePointTest", 1.0623, VOL_SURFACE_SIMPLEMONEY.getZValue(21.0 / 365.0, -0.005), tol);
    assertRelative("nodePointTest", 0.7794, VOL_SURFACE_SIMPLEMONEY.getZValue(60.0 / 365.0, -0.002), tol);
    assertRelative("nodePointTest", 0.8938, VOL_SURFACE_SIMPLEMONEY.getZValue(120.0 / 365.0, -0.006), tol);
    assertRelative("nodePointTest", 0.6519, VOL_SURFACE_SIMPLEMONEY.getZValue(14.0 / 365.0, 0.0), tol);
    assertRelative("nodePointTest", 0.7383, VOL_SURFACE_SIMPLEMONEY.getZValue(90.0 / 365.0, 0.002), tol);
    assertRelative("nodePointTest", 0.7, VOL_SURFACE_SIMPLEMONEY.getZValue(180.0 / 365.0, 0.003), tol);
  }

  /**
   * Check flat extrapolation on volatility.
   */
  @Test
  public void extrapolationTest() {
    double tol = 1.0e-14;
    /* sample points randomly chosen */
    assertRelative("extrapolationTest", 1.0623, VOL_SURFACE_SIMPLEMONEY.getZValue(3.0 / 365.0, -0.01), tol);
    assertRelative("extrapolationTest", 1.1414, VOL_SURFACE_SIMPLEMONEY.getZValue(30.0 / 365.0, -0.015), tol);
    assertRelative("extrapolationTest", 0.9523, VOL_SURFACE_SIMPLEMONEY.getZValue(220.0 / 365.0, -0.012), tol);
    assertRelative("extrapolationTest", 0.7626, VOL_SURFACE_SIMPLEMONEY.getZValue(240.0 / 365.0, -0.002), tol);
    assertRelative("extrapolationTest", 0.6886, VOL_SURFACE_SIMPLEMONEY.getZValue(200.0 / 365.0, 0.007), tol);
    assertRelative("extrapolationTest", 0.7144, VOL_SURFACE_SIMPLEMONEY.getZValue(60.0 / 365.0, 0.006), tol);
    assertRelative("extrapolationTest", 0.8823, VOL_SURFACE_SIMPLEMONEY.getZValue(2.0 / 365.0, 0.008), tol);
    assertRelative("extrapolationTest", 1.0623, VOL_SURFACE_SIMPLEMONEY.getZValue(1.0 / 365.0, -0.004), tol);
  }

  /**
   * Check non-uniformly distributed date points are correctly interpolated. 
   */
  @Test
  public void InterpolatorTest() {
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
    assertRelative("Interpolation2DTest, moneyness", exp1, computed1, tol);
    computed2 = surface.getZValue(keyExpiry / 365.0, 0.0);
    assertRelative("Interpolation2DTest, time", exp2, computed2, tol);
  }

  /**
   * Print volatility surface.
   */
  @Test(enabled = false)
  public void volatilitySurfacePrintTest() {
    int nSample = 100;
    double minExpiry = EXPIRY[0] * 0.8;
    double maxExpiry = EXPIRY[EXPIRY.length - 1] * 1.2;
    double intervalExpiry = (maxExpiry - minExpiry) / (nSample - 1.0);

    double minMoney = SIMPLEMONEY[0] * 1.2;
    double maxMoney = SIMPLEMONEY[SIMPLEMONEY.length - 1] * 1.2;
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


  private void assertRelative(String message, double expected, double obtained, double relativeTol) {
    double ref = Math.max(Math.abs(expected), 1.0);
    assertEquals(message, expected, obtained, ref * relativeTol);
  }
}

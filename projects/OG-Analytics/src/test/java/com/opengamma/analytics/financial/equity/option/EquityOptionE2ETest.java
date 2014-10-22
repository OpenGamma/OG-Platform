/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import static com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackScholesRhoCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotDeltaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotGammaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackThetaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackVegaCalculator;
import com.opengamma.analytics.financial.equity.EqyOptBjerksundStenslandGreekCalculator;
import com.opengamma.analytics.financial.equity.EqyOptBjerksundStenslandPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayDateUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class EquityOptionE2ETest {
  // Calculators for European options
  private static final EquityOptionBlackPresentValueCalculator PV_EUROPEAN = EquityOptionBlackPresentValueCalculator
      .getInstance();
  private static final EquityOptionBlackScholesRhoCalculator RHO_EUROPEAN = EquityOptionBlackScholesRhoCalculator
      .getInstance();
  private static final EquityOptionBlackSpotDeltaCalculator DELTA_EUROPEAN = EquityOptionBlackSpotDeltaCalculator
      .getInstance();
  private static final EquityOptionBlackSpotGammaCalculator GAMMA_EUROPEAN = EquityOptionBlackSpotGammaCalculator
      .getInstance();
  private static final EquityOptionBlackThetaCalculator THETA_EUROPEAN = EquityOptionBlackThetaCalculator.getInstance();
  private static final EquityOptionBlackVegaCalculator VEGA_EUROPEAN = EquityOptionBlackVegaCalculator.getInstance();

  // Calculators for American options
  private static final EqyOptBjerksundStenslandPresentValueCalculator PV_AMERICAN = EqyOptBjerksundStenslandPresentValueCalculator
      .getInstance();
  private static final EqyOptBjerksundStenslandGreekCalculator GREEKS_AMERICAN = EqyOptBjerksundStenslandGreekCalculator
      .getInstance();

  // trade
  private static final Currency USD = Currency.USD;
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final ZoneId ZID = ZoneId.of("EST");
  private static final ZonedDateTime TRADE_DATE = ZonedDateTime.of(2014, 3, 26, 13, 46, 0, 0, ZID);
  private static final double NOTIONAL = 325;

  // yield curve
  private static final double[] SINGLE_CURVE_TIME = new double[] {0.002739726, 0.093150685, 0.257534247, 0.515068493,
      1.005479452, 2.009416873, 3.005479452, 4.005479452, 5.005479452, 6.006684632, 7.010958904, 8.008219178,
      9.005479452, 10.00668463, 12.00547945, 15.00547945, 20.00547945 };
  private static final double[] SINGLE_CURVE_RATE = new double[] {0.001774301, 0.000980829, 0.000940143, 0.001061566,
      0.001767578, 0.005373189, 0.009795971, 0.013499667, 0.016397755, 0.018647803, 0.020528999, 0.022002859,
      0.023322553, 0.024538027, 0.026482704, 0.028498622, 0.030369559 };
  private static final String SINGLE_CURVE_NAME = "Single Curve";
  private static final Interpolator1D YIELD_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final InterpolatedDoublesCurve INTERPOLATED_CURVE = InterpolatedDoublesCurve.from(SINGLE_CURVE_TIME,
      SINGLE_CURVE_RATE, YIELD_INTERPOLATOR);
  private static final YieldAndDiscountCurve SINGLE_CURVE = new YieldCurve(SINGLE_CURVE_NAME, INTERPOLATED_CURVE);

  // tools for vol surface
  private static final BjerksundStenslandModel AMERICAN_MODEL = new BjerksundStenslandModel();
  private final static CombinedInterpolatorExtrapolator EXPIRY_INTERPOLATOR = getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final CombinedInterpolatorExtrapolator STRIKE_INTERPOLATOR = getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

  // market data
  private static final double SPOT = 56.0;
  private static final ZonedDateTime[] DIVIDEND_DATES = new ZonedDateTime[] {
      ZonedDateTime.of(2014, 4, 2, 0, 0, 0, 0, ZID), ZonedDateTime.of(2014, 7, 1, 0, 0, 0, 0, ZID) };
  private static final double[] ALPHA = new double[] {0.38, 0.4 };
  private static final double[] BETA = new double[] {0.0, 0.0 };
  private static final double POINT_VALUE = 100.0;
  private static final ZonedDateTime[] EXPIRY_DATES = new ZonedDateTime[] {
      ZonedDateTime.of(2014, 3, 28, 15, 0, 0, 0, ZID), ZonedDateTime.of(2014, 4, 11, 15, 0, 0, 0, ZID),
      ZonedDateTime.of(2014, 4, 25, 15, 0, 0, 0, ZID), ZonedDateTime.of(2014, 5, 9, 15, 0, 0, 0, ZID),
      ZonedDateTime.of(2014, 5, 23, 15, 0, 0, 0, ZID), ZonedDateTime.of(2014, 6, 20, 15, 0, 0, 0, ZID) };
  private static final double[][] STRIKES = new double[][] {
      {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
      {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
      {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
      {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
      {52.5, 55, 57.5, 60 }, {52.5, 55, 57.5, 60 } };
  private static final double[][] CALL_PRICES = new double[][] {
      {5.1, 4.1, 3.08, 2.07, 1.13, 0.41, 0.105, 0.05, 0.025, 0.02 },
      {5.275, 4.275, 3.34, 2.51, 1.702, 1.021, 0.64, 0.435, 0.345, 0.31 },
      {5.575, 4.61, 3.645, 2.805, 2.085, 1.505, 1.125, 0.825, 0.655, 0.535 },
      {5.65, 4.78, 3.925, 3.15, 2.475, 1.87, 1.455, 1.12, 0.89, 0.725 },
      {4.55, 2.81, 1.615, 0.92 }, {5.05, 3.425, 2.085, 1.215 } };
  private static final double[][] PUT_PRICES = new double[][] {
      {0.025, 0.03, 0.03, 0.025, 0.04, 0.2, 1.03, 2.01, 3.02, 4.075 },
      {0.295, 0.315, 0.355, 0.48, 0.65, 0.88, 1.63, 2.57, 3.675, 4.895 },
      {0.36, 0.475, 0.64, 0.855, 1.035, 1.205, 1.915, 2.925, 4.05, 5.15 },
      {0.49, 0.64, 0.79, 1, 1.34, 1.615, 2.21, 3.285, 4.325, 5.3 },
      {0.795, 1.4, 2.92, 5.315 }, {1.315, 2.055, 3.46, 5.41 } };

  private static final double TOL = 1.0e-12;

  @Test
  public void dividendCallOnlyTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.AMERICAN;
    int nExpiries = EXPIRY_DATES.length;
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = CALL_PRICES[i].length;
      isCalls[i] = new boolean[nOptions];
      Arrays.fill(isCalls[i], true);
    }
    boolean isAmerican = exerciseType == ExerciseDecisionType.AMERICAN ? true : false;
    double[] timeToExpiries = toDateToDouble(EXPIRY_DATES);
    double[] tau = toDateToDouble(DIVIDEND_DATES);
    AffineDividends dividend = new AffineDividends(tau, ALPHA, BETA);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, CALL_PRICES, isCalls,
        forwardCurve, isAmerican);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // Option 1
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 53.5; // ITM 
    EquityOptionDefinition targetOption1Dfn = new EquityOptionDefinition(true, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, POINT_VALUE, SettlementType.PHYSICAL);
    EquityOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract1 = targetOption1.accept(PV_AMERICAN, data);
    Double pv1 = targetOption1.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks1 = targetOption1.accept(GREEKS_AMERICAN, data);
    double delta1 = greeks1.get(Greek.DELTA);
    double gamma1 = greeks1.get(Greek.GAMMA);
    double theta1 = greeks1.get(Greek.THETA);
    double rho1 = greeks1.get(Greek.RHO);
    double vega1 = greeks1.get(Greek.VEGA);
    double positionDelta1 = greeks1.get(Greek.DELTA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionGamma1 = greeks1.get(Greek.GAMMA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionTheta1 = greeks1.get(Greek.THETA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionRho1 = greeks1.get(Greek.RHO) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionVega1 = greeks1.get(Greek.VEGA) * targetOption1.getUnitAmount() * NOTIONAL;
    assertEqualsRelative("pv per contract", 291.56750963236584, pvPerContract1, TOL);
    assertEqualsRelative("pv", 94759.4406305189, pv1, TOL);
    assertEqualsRelative("delta", 0.7433580421991999, delta1, TOL);
    assertEqualsRelative("gamma", 0.09387569587899836, gamma1, TOL);
    assertEqualsRelative("theta", -0.028143893730161934, theta1, TOL);
    assertEqualsRelative("rho", 0.011158414048770745, rho1, TOL);
    assertEqualsRelative("vega", 0.03669291093468003, vega1, TOL);
    assertEqualsRelative("position delta", 24159.136371473996, positionDelta1, TOL);
    assertEqualsRelative("position gamma", 3050.960116067447, positionGamma1, TOL);
    assertEqualsRelative("position theta", -914.6765462302628, positionTheta1, TOL);
    assertEqualsRelative("position rho", 362.6484565850492, positionRho1, TOL);
    assertEqualsRelative("position vega", 1192.519605377101, positionVega1, TOL);

    // Option 2
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[1]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 57.5; // OTM
    EquityOptionDefinition targetOption2Dfn = new EquityOptionDefinition(true, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, POINT_VALUE, SettlementType.PHYSICAL);
    EquityOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract2 = targetOption2.accept(PV_AMERICAN, data);
    Double pv2 = targetOption2.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks2 = targetOption2.accept(GREEKS_AMERICAN, data);
    double delta2 = greeks2.get(Greek.DELTA);
    double gamma2 = greeks2.get(Greek.GAMMA);
    double theta2 = greeks2.get(Greek.THETA);
    double rho2 = greeks2.get(Greek.RHO);
    double vega2 = greeks2.get(Greek.VEGA);
    double positionDelta2 = greeks2.get(Greek.DELTA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionGamma2 = greeks2.get(Greek.GAMMA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionTheta2 = greeks2.get(Greek.THETA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionRho2 = greeks2.get(Greek.RHO) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionVega2 = greeks2.get(Greek.VEGA) * targetOption2.getUnitAmount() * NOTIONAL;
    assertEqualsRelative("pv per contract", 188.76756455195363, pvPerContract2, TOL);
    assertEqualsRelative("pv", 61349.45847938493, pv2, TOL);
    assertEqualsRelative("delta", 0.41601610970443836, delta2, TOL);
    assertEqualsRelative("gamma", 0.058690007627269086, gamma2, TOL);
    assertEqualsRelative("theta", -0.01619733480905359, theta2, TOL);
    assertEqualsRelative("rho", 0.0386020497360748, rho2, TOL);
    assertEqualsRelative("vega", 0.09652116218314116, vega2, TOL);
    assertEqualsRelative("position delta", 13520.523565394247, positionDelta2, TOL);
    assertEqualsRelative("position gamma", 1907.4252478862454, positionGamma2, TOL);
    assertEqualsRelative("position theta", -526.4133812942416, positionTheta2, TOL);
    assertEqualsRelative("position rho", 1254.566616422431, positionRho2, TOL);
    assertEqualsRelative("position vega", 3136.9377709520877, positionVega2, TOL);
 }

  @Test
  public void dividendPutOnlyTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.AMERICAN;
    int nExpiries = EXPIRY_DATES.length;
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = PUT_PRICES[i].length;
      isCalls[i] = new boolean[nOptions];
      Arrays.fill(isCalls[i], false);
    }
    boolean isAmerican = exerciseType == ExerciseDecisionType.AMERICAN ? true : false;
    double[] timeToExpiries = toDateToDouble(EXPIRY_DATES);
    double[] tau = toDateToDouble(DIVIDEND_DATES);
    AffineDividends dividend = new AffineDividends(tau, ALPHA, BETA);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, PUT_PRICES, isCalls,
        forwardCurve, isAmerican);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // Option 1
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 53.5; // OTM
    EquityOptionDefinition targetOption1Dfn = new EquityOptionDefinition(false, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract1 = targetOption1.accept(PV_AMERICAN, data);
    Double pv1 = targetOption1.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks1 = targetOption1.accept(GREEKS_AMERICAN, data);
    double delta1 = greeks1.get(Greek.DELTA);
    double gamma1 = greeks1.get(Greek.GAMMA);
    double theta1 = greeks1.get(Greek.THETA);
    double rho1 = greeks1.get(Greek.RHO);
    double vega1 = greeks1.get(Greek.VEGA);
    double positionDelta1 = greeks1.get(Greek.DELTA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionGamma1 = greeks1.get(Greek.GAMMA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionTheta1 = greeks1.get(Greek.THETA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionRho1 = greeks1.get(Greek.RHO) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionVega1 = greeks1.get(Greek.VEGA) * targetOption1.getUnitAmount() * NOTIONAL;
    assertEqualsRelative("pv per contract", 40.86299068016362, pvPerContract1, TOL);
    assertEqualsRelative("pv", 13280.471971053177, pv1, TOL);
    assertEqualsRelative("delta", -0.22632187721042948, delta1, TOL);
    assertEqualsRelative("gamma", 0.09894526790792406, gamma1, TOL);
    assertEqualsRelative("theta", -0.0337728538179612, theta1, TOL);
    assertEqualsRelative("rho", -0.005734862479160847, rho1, TOL);
    assertEqualsRelative("vega", 0.03517593490085753, vega1, TOL);
    assertEqualsRelative("position delta", -7355.461009338958, positionDelta1, TOL);
    assertEqualsRelative("position gamma", 3215.721207007532, positionGamma1, TOL);
    assertEqualsRelative("position theta", -1097.617749083739, positionTheta1, TOL);
    assertEqualsRelative("position rho", -186.38303057272753, positionRho1, TOL);
    assertEqualsRelative("position vega", 1143.2178842778699, positionVega1, TOL);

    // Option 2
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[1]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 58.5; // ITM
    EquityOptionDefinition targetOption2Dfn = new EquityOptionDefinition(false, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract2 = targetOption2.accept(PV_AMERICAN, data);
    Double pv2 = targetOption2.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks2 = targetOption2.accept(GREEKS_AMERICAN, data);
    double delta2 = greeks2.get(Greek.DELTA);
    double gamma2 = greeks2.get(Greek.GAMMA);
    double theta2 = greeks2.get(Greek.THETA);
    double rho2 = greeks2.get(Greek.RHO);
    double vega2 = greeks2.get(Greek.VEGA);
    double positionDelta2 = greeks2.get(Greek.DELTA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionGamma2 = greeks2.get(Greek.GAMMA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionTheta2 = greeks2.get(Greek.THETA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionRho2 = greeks2.get(Greek.RHO) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionVega2 = greeks2.get(Greek.VEGA) * targetOption2.getUnitAmount() * NOTIONAL;
    assertEqualsRelative("pv per contract", 389.5706476110104, pvPerContract2, TOL);
    assertEqualsRelative("pv", 126610.46047357837, pv2, TOL);
    assertEqualsRelative("delta", -0.680130005903282, delta2, TOL);
    assertEqualsRelative("gamma", 0.06622597074777498, gamma2, TOL);
    assertEqualsRelative("theta", -0.01656306237658076, theta2, TOL);
    assertEqualsRelative("rho", -0.08281575479676825, rho2, TOL);
    assertEqualsRelative("vega", 0.08778111393062614, vega2, TOL);
    assertEqualsRelative("position delta", -22104.225191856665, positionDelta2, TOL);
    assertEqualsRelative("position gamma", 2152.344049302687, positionGamma2, TOL);
    assertEqualsRelative("position theta", -538.2995272388747, positionTheta2, TOL);
    assertEqualsRelative("position rho", -2691.512030894968, positionRho2, TOL);
    assertEqualsRelative("position vega", 2852.8862027453497, positionVega2, TOL);
  }

  /**
   * Vol surface is constructed by using OTM options
   */
  @Test
  public void dividendCallPutOTMTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.AMERICAN;
    int nExpiries = EXPIRY_DATES.length;
    double[][] optionPrices = new double[nExpiries][];
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = STRIKES[i].length;
      optionPrices[i] = new double[nOptions];
      isCalls[i] = new boolean[nOptions];
      for (int j = 0; j < nOptions; ++j) {
        if (STRIKES[i][j] < SPOT) {
          optionPrices[i][j] = PUT_PRICES[i][j];
          isCalls[i][j] = false;
        } else {
          optionPrices[i][j] = CALL_PRICES[i][j];
          isCalls[i][j] = true;
        }
      }
    }
    boolean isAmerican = exerciseType == ExerciseDecisionType.AMERICAN ? true : false;
    double[] timeToExpiries = toDateToDouble(EXPIRY_DATES);
    double[] tau = toDateToDouble(DIVIDEND_DATES);
    AffineDividends dividend = new AffineDividends(tau, ALPHA, BETA);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, optionPrices, isCalls,
        forwardCurve, isAmerican);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // ITM call option
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 53.5; // ITM 
    EquityOptionDefinition targetOption1Dfn = new EquityOptionDefinition(true, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, POINT_VALUE, SettlementType.PHYSICAL);
    EquityOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract1 = targetOption1.accept(PV_AMERICAN, data);
    Double pv1 = targetOption1.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks1 = targetOption1.accept(GREEKS_AMERICAN, data);
    double delta1 = greeks1.get(Greek.DELTA);
    double gamma1 = greeks1.get(Greek.GAMMA);
    double theta1 = greeks1.get(Greek.THETA);
    double rho1 = greeks1.get(Greek.RHO);
    double vega1 = greeks1.get(Greek.VEGA);
    double positionDelta1 = greeks1.get(Greek.DELTA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionGamma1 = greeks1.get(Greek.GAMMA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionTheta1 = greeks1.get(Greek.THETA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionRho1 = greeks1.get(Greek.RHO) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionVega1 = greeks1.get(Greek.VEGA) * targetOption1.getUnitAmount() * NOTIONAL;
    assertEqualsRelative("pv per contract", 263.49044651502817, pvPerContract1, TOL);
    assertEqualsRelative("pv", 85634.39511738415, pv1, TOL);
    assertEqualsRelative("delta", 0.8249842622485184, delta1, TOL);
    assertEqualsRelative("gamma", 0.12405698831215517, gamma1, TOL);
    assertEqualsRelative("theta", -0.013847882501060185, theta1, TOL);
    assertEqualsRelative("rho", 0.009035557820894862, rho1, TOL);
    assertEqualsRelative("vega", 0.028178878648973716, vega1, TOL);
    assertEqualsRelative("position delta", 26811.98852307685, positionDelta1, TOL);
    assertEqualsRelative("position gamma", 4031.852120145043, positionGamma1, TOL);
    assertEqualsRelative("position theta", -450.056181284456, positionTheta1, TOL);
    assertEqualsRelative("position rho", 293.655629179083, positionRho1, TOL);
    assertEqualsRelative("position vega", 915.8135560916459, positionVega1, TOL);

    // ITM put option
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[1]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 58.5; // ITM
    EquityOptionDefinition targetOption2Dfn = new EquityOptionDefinition(false, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract2 = targetOption2.accept(PV_AMERICAN, data);
    Double pv2 = targetOption2.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks2 = targetOption2.accept(GREEKS_AMERICAN, data);
    double delta2 = greeks2.get(Greek.DELTA);
    double gamma2 = greeks2.get(Greek.GAMMA);
    double theta2 = greeks2.get(Greek.THETA);
    double rho2 = greeks2.get(Greek.RHO);
    double vega2 = greeks2.get(Greek.VEGA);
    double positionDelta2 = greeks2.get(Greek.DELTA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionGamma2 = greeks2.get(Greek.GAMMA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionTheta2 = greeks2.get(Greek.THETA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionRho2 = greeks2.get(Greek.RHO) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionVega2 = greeks2.get(Greek.VEGA) * targetOption2.getUnitAmount() * NOTIONAL;
    assertEqualsRelative("pv per contract", 449.8314803750468, pvPerContract2, TOL);
    assertEqualsRelative("pv", 146195.23112189022, pv2, TOL);
    assertEqualsRelative("delta", -0.6292338338039938, delta2, TOL);
    assertEqualsRelative("gamma", 0.053545033644796146, gamma2, TOL);
    assertEqualsRelative("theta", -0.02136148161220006, theta2, TOL);
    assertEqualsRelative("rho", -0.07838217763747224, rho2, TOL);
    assertEqualsRelative("vega", 0.09297744638761118, vega2, TOL);
    assertEqualsRelative("position delta", -20450.099598629797, positionDelta2, TOL);
    assertEqualsRelative("position gamma", 1740.2135934558748, positionGamma2, TOL);
    assertEqualsRelative("position theta", -694.248152396502, positionTheta2, TOL);
    assertEqualsRelative("position rho", -2547.420773217848, positionRho2, TOL);
    assertEqualsRelative("position vega", 3021.7670075973633, positionVega2, TOL);
  }

  @Test(enabled = false)
  public void europeanTest() {
    ExerciseDecisionType exerciseType = ExerciseDecisionType.EUROPEAN;
    boolean isAmerican = exerciseType == ExerciseDecisionType.AMERICAN ? true : false;
    double[][] euCallPrices = new double[][] {
        {5.1, 4.1, 3.08, 2.07, 1.13, 0.31, 0.105, 0.05, 0.025, 0.02 },
        {5.175, 4.175, 3.34, 2.51, 1.702, 1.021, 0.64, 0.435, 0.345, 0.31 },
        {5.575, 4.61, 3.645, 2.805, 2.085, 1.505, 1.125, 0.825, 0.655, 0.535 },
        {5.65, 4.78, 3.925, 3.15, 2.475, 1.87, 1.455, 1.12, 0.89, 0.725 },
        {4.55, 2.81, 1.615, 0.92 }, {5.05, 3.425, 2.085, 1.215 } };
    double[][] euPutPrices = new double[][] {
        {0.025, 0.03, 0.03, 0.025, 0.04, 0.305, 1.03, 2.01, 3.02, 4.075 },
        {0.525, 0.545, 0.715, 0.88, 1.08, 1.39, 2.02, 2.81, 3.715, 4.695 },
        {0.95, 0.985, 1.02, 1.175, 1.455, 1.875, 2.495, 3.195, 4.03, 4.91 },
        {1.03, 1.15, 1.3, 1.52, 1.84, 2.245, 2.83, 3.485, 4.255, 5.1 },
        {1.425, 2.19, 3.49, 5.295 }, {1.915, 2.785, 3.95, 5.58 } };

    double[] timeToExpiries = toDateToDouble(EXPIRY_DATES);
    double[] tau = toDateToDouble(DIVIDEND_DATES);
    AffineDividends dividend = new AffineDividends(tau, ALPHA, BETA);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);

    int nExpiries = EXPIRY_DATES.length;
    double[][] optionPrices = new double[nExpiries][];
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = STRIKES[i].length;
      optionPrices[i] = new double[nOptions];
      isCalls[i] = new boolean[nOptions];
      for (int j = 0; j < nOptions; ++j) {
        if (STRIKES[i][j] < SPOT) {
          optionPrices[i][j] = euPutPrices[i][j];
          isCalls[i][j] = false;
        } else {
          optionPrices[i][j] = euCallPrices[i][j];
          isCalls[i][j] = true;
        }
      }
    }

    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, optionPrices, isCalls,
        forwardCurve, isAmerican);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    //    int nStrikes = 100;
    //    int nTimes = 100;
    //    double[] str = new double[nStrikes];
    //    double[] tm = new double[nTimes];
    //    double minStr = 50.0;
    //    double maxStr = 61.0;
    //    for (int i = 0; i < nStrikes; ++i) {
    //      str[i] = minStr + (maxStr - minStr) / (nStrikes - 1) * i;
    //    }
    //    double minTm = 0.001;
    //    double maxTm = 0.25;
    //    for (int j = 0; j < nTimes; ++j) {
    //      tm[j] = minTm + (maxTm - minTm) / (nTimes - 1) * j;
    //    }
    //
    //    for (int j = 0; j < nTimes; ++j) {
    //      System.out.print("\t" + tm[j]);
    //    }
    //    System.out.println();
    //    for (int i = 0; i < nStrikes; ++i) {
    //      System.out.print(str[i]);
    //      for (int j = 0; j < nTimes; ++j) {
    //        System.out.print("\t" + volSurface.getVolatility(tm[j], str[i]));
    //      }
    //      System.out.println();
    //    }
    //
    //    System.out.println("\n\n");
    //    //    for (int j = 0; j < timeToExpiries.length; ++j) {
    //    //      System.out.print("\t" + timeToExpiries[j]);
    //    //    }
    //    System.out.println();
    //    for (int i = 0; i < nStrikes; ++i) {
    //      System.out.print(str[i]);
    //      for (int j = 0; j < timeToExpiries.length; ++j) {
    //        System.out.print("\t" + volSurface.getVolatility(timeToExpiries[j], str[i]));
    //      }
    //      System.out.println();
    //    }
    //    System.out.println();
    //    for (int j = 0; j < timeToExpiries.length; ++j) {
    //      System.out.println(forwardCurve.getForward(timeToExpiries[j]));
    //    }

    // ITM call option
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 53.5; // ITM 
    EquityOptionDefinition targetOption1Dfn = new EquityOptionDefinition(true, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, POINT_VALUE, SettlementType.PHYSICAL);
    EquityOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);

    Double pvPerContract1 = targetOption1.accept(PV_EUROPEAN, data);
    Double pv1 = targetOption1.accept(PV_EUROPEAN, data) * NOTIONAL;
    Double delta1 = targetOption1.accept(DELTA_EUROPEAN, data);
    Double gamma1 = targetOption1.accept(GAMMA_EUROPEAN, data);
    Double theta1 = targetOption1.accept(THETA_EUROPEAN, data);
    Double rho1 = targetOption1.accept(RHO_EUROPEAN, data);
    Double vega1 = targetOption1.accept(VEGA_EUROPEAN, data);
    double positionDelta1 = delta1 * targetOption1.getUnitAmount() * NOTIONAL;
    double positionGamma1 = gamma1 * targetOption1.getUnitAmount() * NOTIONAL;
    double positionTheta1 = theta1 * targetOption1.getUnitAmount() * NOTIONAL;
    double positionRho1 = rho1 * targetOption1.getUnitAmount() * NOTIONAL;
    double positionVega1 = vega1 * targetOption1.getUnitAmount() * NOTIONAL;

    System.out.println(pvPerContract1);
    System.out.println(pv1);
    System.out.println(delta1);
    System.out.println(gamma1);
    System.out.println(theta1);
    System.out.println(rho1);
    System.out.println(vega1);
    System.out.println(positionDelta1);
    System.out.println(positionGamma1);
    System.out.println(positionTheta1);
    System.out.println(positionRho1);
    System.out.println(positionVega1);

    assertEqualsRelative("pv per contract", 291.9372446579417, pvPerContract1, TOL);
    assertEqualsRelative("pv", 94879.60451383104, pv1, TOL);
    assertEqualsRelative("delta", 0.7049649450883742, delta1, TOL);
    assertEqualsRelative("gamma", 0.08035708404397142, gamma1, TOL);
    assertEqualsRelative("theta", 16.418932753910394, theta1, TOL);
    assertEqualsRelative("rho", 0.009035557820894862, rho1, TOL);
    assertEqualsRelative("vega", 0.028178878648973716, vega1, TOL);
    assertEqualsRelative("position delta", 26811.98852307685, positionDelta1, TOL);
    assertEqualsRelative("position gamma", 4031.852120145043, positionGamma1, TOL);
    assertEqualsRelative("position theta", -450.056181284456, positionTheta1, TOL);
    assertEqualsRelative("position rho", 293.655629179083, positionRho1, TOL);
    assertEqualsRelative("position vega", 915.8135560916459, positionVega1, TOL);

    // ITM put option
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[1]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 58.5; // ITM
    EquityOptionDefinition targetOption2Dfn = new EquityOptionDefinition(false, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);

    Double pvPerContract2 = targetOption2.accept(PV_EUROPEAN, data);
    Double pv2 = targetOption2.accept(PV_EUROPEAN, data) * NOTIONAL;
    Double delta2 = targetOption2.accept(DELTA_EUROPEAN, data);
    Double gamma2 = targetOption2.accept(GAMMA_EUROPEAN, data);
    Double theta2 = targetOption2.accept(THETA_EUROPEAN, data);
    Double rho2 = targetOption2.accept(RHO_EUROPEAN, data);
    Double vega2 = targetOption2.accept(VEGA_EUROPEAN, data);
    double positionDelta2 = delta2 * targetOption2.getUnitAmount() * NOTIONAL;
    double positionGamma2 = gamma2 * targetOption2.getUnitAmount() * NOTIONAL;
    double positionTheta2 = theta2 * targetOption2.getUnitAmount() * NOTIONAL;
    double positionRho2 = rho2 * targetOption2.getUnitAmount() * NOTIONAL;
    double positionVega2 = vega2 * targetOption2.getUnitAmount() * NOTIONAL;

    System.out.println(pvPerContract2);
    System.out.println(pv2);
    System.out.println(delta2);
    System.out.println(gamma2);
    System.out.println(theta2);
    System.out.println(rho2);
    System.out.println(vega2);
    System.out.println(positionDelta2);
    System.out.println(positionGamma2);
    System.out.println(positionTheta2);
    System.out.println(positionRho2);
    System.out.println(positionVega2);

    assertEqualsRelative("pv per contract", 449.8314803750468, pvPerContract2, TOL);
    assertEqualsRelative("pv", 146195.23112189022, pv2, TOL);
    assertEqualsRelative("delta", -0.6292338338039938, delta2, TOL);
    assertEqualsRelative("gamma", 0.053545033644796146, gamma2, TOL);
    assertEqualsRelative("theta", -0.02136148161220006, theta2, TOL);
    assertEqualsRelative("rho", -0.07838217763747224, rho2, TOL);
    assertEqualsRelative("vega", 0.09297744638761118, vega2, TOL);
    assertEqualsRelative("position delta", -20450.099598629797, positionDelta2, TOL);
    assertEqualsRelative("position gamma", 1740.2135934558748, positionGamma2, TOL);
    assertEqualsRelative("position theta", -694.248152396502, positionTheta2, TOL);
    assertEqualsRelative("position rho", -2547.420773217848, positionRho2, TOL);
    assertEqualsRelative("position vega", 3021.7670075973633, positionVega2, TOL);

  }

  @Test
  public void noDividendTest() {

  }

  private BlackVolatilitySurfaceStrike createSurface(double spot, double[] timeToExpiries, double[][] strikes,
      double[][] marketPrices, boolean[][] isCalls, ForwardCurveAffineDividends forwardCurve, boolean isAmerican) {
    int nExpiry = timeToExpiries.length;
    ArrayList<Double> expiryList = new ArrayList<>();
    ArrayList<Double> strikeList = new ArrayList<>();
    ArrayList<Double> impliedVolList = new ArrayList<>();
    for (int i = 0; i < nExpiry; ++i) {
      double expiry = timeToExpiries[i];
      int nStrikes = strikes[i].length;
      for (int j = 0; j < nStrikes; ++j) {
        if (!Double.isNaN(marketPrices[i][j])) {
          double strike = strikes[i][j];
          expiryList.add(expiry);
          strikeList.add(strike);
          double interestRate = SINGLE_CURVE.getInterestRate(expiry);
          double costOfCarry = Math.log(forwardCurve.getForward(expiry) / spot) / expiry;
          boolean isCall = isCalls[i][j];
          double impliedVol;
          if (isAmerican) {
            impliedVol = AMERICAN_MODEL.impliedVolatility(marketPrices[i][j], spot, strike, interestRate,
                costOfCarry, expiry, isCall);
          } else {
            double df = SINGLE_CURVE.getDiscountFactor(expiry);
            double fwdPrice = marketPrices[i][j] / df;
            double fwd = forwardCurve.getForward(expiry);
            impliedVol = BlackFormulaRepository.impliedVolatility(fwdPrice, fwd, strike, expiry, isCall);
          }
          impliedVolList.add(impliedVol);
        }
      }
    }

    InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(expiryList, strikeList, impliedVolList,
        new GridInterpolator2D(EXPIRY_INTERPOLATOR, STRIKE_INTERPOLATOR));
    return new BlackVolatilitySurfaceStrike(surface);
  }

  private double[] toDateToDouble(ZonedDateTime[] targetDates) {
    int nDates = targetDates.length;
    double[] res = new double[nDates];
    for (int i = 0; i < nDates; ++i) {
      res[i] = TimeCalculator.getTimeBetween(TRADE_DATE, targetDates[i], DayCounts.ACT_365);
    }
    return res;
  }

  private void assertEqualsRelative(String message, double expected, double result, double relativeTol) {
    double tol = Math.max(1.0, Math.abs(expected)) * relativeTol;
    assertEquals(message, expected, result, tol);
  }

}

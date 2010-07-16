/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.interestrate.DoubleCurveFinder;
import com.opengamma.financial.interestrate.DoubleCurveJacobian;
import com.opengamma.financial.interestrate.InterestRateCalculator;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CubicSplineInterpolatorWithSensitivities1D;
import com.opengamma.math.interpolation.Extrapolator1D;
import com.opengamma.math.interpolation.ExtrapolatorMethod;
import com.opengamma.math.interpolation.FlatExtrapolator;
import com.opengamma.math.interpolation.FlatExtrapolatorWithSensitivities;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.InterpolationResultWithSensitivities;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineDataBundle;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineWithSensitivitiesDataBundle;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.interpolation.LinearExtrapolator;
import com.opengamma.math.interpolation.LinearExtrapolatorWithSensitivity;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.FiniteDifferenceJacobianCalculator;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class MultiInstrumentDoubleCurveBootstrapTest {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveBootStrapTest.class);
  private static final int HOTSPOT_WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);

  private static final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> EXTRAPOLATOR;
  private static final Interpolator1D<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> EXTRAPOLATOR_WITH_SENSITIVITY;

  private static List<InterestRateDerivative> INSTRUMENTS;
  private static double[] MARKET_VALUES;
  private static YieldAndDiscountCurve FUNDING_CURVE;
  private static YieldAndDiscountCurve FORWARD_CURVE;

  private static final double[] FWD_NODE_TIMES;
  private static final double[] FUND_NODE_TIMES;
  private static final double EPS = 1e-8;
  private static final int STEPS = 100;
  private static final DoubleMatrix1D X0;

  private static final InterestRateCalculator RATE_CALCULATOR = new InterestRateCalculator();

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> DOUBLE_CURVE_FINDER;
  private static final JacobianCalculator DOUBLE_CURVE_JACOBIAN;

  protected static final Function1D<Double, Double> DUMMY_FWD_CURVE = new Function1D<Double, Double>() {

    private static final double a = -0.0325;
    private static final double b = 0.021;
    private static final double c = 0.52;
    private static final double d = 0.055;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  protected static final Function1D<Double, Double> DUMMY_FUND_CURVE = new Function1D<Double, Double>() {

    private static final double a = -0.03;
    private static final double b = 0.02;
    private static final double c = 0.5;
    private static final double d = 0.05;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  static {

    INSTRUMENTS = new ArrayList<InterestRateDerivative>();

    final double[] liborMaturities = new double[] {1. / 12., 2. / 12., 3. / 12.};// note using 1m and 2m LIBOR tenors for what should be the 3m-libor curve is probably wrong
    final double[] fraMaturities = new double[] {0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0};
    final double[] cashMaturities = new double[] {1 / 365.25, 7 / 365.25, 1.1 / 12.0, 3.1 / 12., 6 / 12., 1.0};
    final int[] swapSemiannualGrid = new int[] {4, 6, 8, 10, 12, 14, 16, 18, 20, 30, 40, 50, 60};

    final double[] remainingFwdNodes = new double[] {3.0, 5.0, 7.0, 10.0, 20.0, 30.01};
    final double[] remainingFundNodes = new double[] {2.0, 3.0, 5.0, 7.0, 10.0, 20.0, 30.01};

    final int nFwdNodes = liborMaturities.length + fraMaturities.length + remainingFwdNodes.length;
    final int nFundNodes = cashMaturities.length + remainingFundNodes.length;

    FWD_NODE_TIMES = new double[nFwdNodes];
    FUND_NODE_TIMES = new double[nFundNodes];

    int fwdIndex = 0;
    int fundIndex = 0;

    InterestRateDerivative ird;

    for (final double t : liborMaturities) {
      ird = new Libor(t);
      INSTRUMENTS.add(ird);
      FWD_NODE_TIMES[fwdIndex++] = t;
    }
    for (final double t : fraMaturities) {
      ird = new ForwardRateAgreement(t - 0.25, t);
      INSTRUMENTS.add(ird);
      FWD_NODE_TIMES[fwdIndex++] = t;
    }

    for (final double t : cashMaturities) {
      ird = new Cash(t);
      INSTRUMENTS.add(ird);
      FUND_NODE_TIMES[fundIndex++] = t;
    }

    for (final int element : swapSemiannualGrid) {
      final Swap swap = setupSwap(element);
      INSTRUMENTS.add(swap);
    }

    if (INSTRUMENTS.size() != (nFwdNodes + nFundNodes)) {
      throw new IllegalArgumentException("number of instruments not equal to number of nodes");
    }

    for (final double t : remainingFwdNodes) {
      FWD_NODE_TIMES[fwdIndex++] = t;
    }

    for (final double t : remainingFundNodes) {
      FUND_NODE_TIMES[fundIndex++] = t;
    }

    Arrays.sort(FWD_NODE_TIMES);
    Arrays.sort(FUND_NODE_TIMES);

    final int n = INSTRUMENTS.size();

    // set up curves to obtain "market" prices
    final double[] fwdYields = new double[FWD_NODE_TIMES.length];
    final double[] fundYields = new double[FUND_NODE_TIMES.length];

    for (int i = 0; i < FWD_NODE_TIMES.length; i++) {
      fwdYields[i] = DUMMY_FWD_CURVE.evaluate(FWD_NODE_TIMES[i]);
    }

    for (int i = 0; i < FUND_NODE_TIMES.length; i++) {
      fundYields[i] = DUMMY_FUND_CURVE.evaluate(FUND_NODE_TIMES[i]);
    }

    final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> cubicInterpolator = new NaturalCubicSplineInterpolator1D();
    final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> cubicInterpolatorWithSense = new CubicSplineInterpolatorWithSensitivities1D();
    final ExtrapolatorMethod<Interpolator1DCubicSplineDataBundle, InterpolationResult> linear_em = new LinearExtrapolator<Interpolator1DCubicSplineDataBundle, InterpolationResult>();
    final ExtrapolatorMethod<Interpolator1DCubicSplineDataBundle, InterpolationResult> flat_em = new FlatExtrapolator<Interpolator1DCubicSplineDataBundle, InterpolationResult>();
    final ExtrapolatorMethod<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> linear_em_sense = new LinearExtrapolatorWithSensitivity<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities>();
    final ExtrapolatorMethod<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> flat_em_sense = new FlatExtrapolatorWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities>();
    EXTRAPOLATOR = new Extrapolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult>(linear_em, flat_em, cubicInterpolator);
    EXTRAPOLATOR_WITH_SENSITIVITY = new Extrapolator1D<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities>(linear_em_sense, flat_em_sense,
        cubicInterpolatorWithSense);

    FORWARD_CURVE = makeYieldCurve(fwdYields, FWD_NODE_TIMES, EXTRAPOLATOR);
    FUNDING_CURVE = makeYieldCurve(fundYields, FUND_NODE_TIMES, EXTRAPOLATOR);

    // now get market prices
    MARKET_VALUES = new double[n];

    for (int i = 0; i < n; i++) {
      MARKET_VALUES[i] = RATE_CALCULATOR.getRate(FORWARD_CURVE, FUNDING_CURVE, INSTRUMENTS.get(i));
    }

    final double[] rates = new double[n];
    for (int i = 0; i < fwdYields.length; i++) {
      rates[i] = fwdYields[i] + 0.03;
    }

    for (int i = 0; i < fundYields.length; i++) {
      rates[i + fwdYields.length] = 0.05;// fundYields[i] + 0.03;
    }

    X0 = new DoubleMatrix1D(rates);

    DOUBLE_CURVE_FINDER = new DoubleCurveFinder(INSTRUMENTS, MARKET_VALUES, FWD_NODE_TIMES, FUND_NODE_TIMES, null, null, EXTRAPOLATOR, EXTRAPOLATOR);
    DOUBLE_CURVE_JACOBIAN = new DoubleCurveJacobian<Interpolator1DCubicSplineWithSensitivitiesDataBundle>(INSTRUMENTS, FWD_NODE_TIMES, FUND_NODE_TIMES, EXTRAPOLATOR_WITH_SENSITIVITY,
        EXTRAPOLATOR_WITH_SENSITIVITY);

  }

  @Test
  public void testNewton() {

    final VectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS/* , DOUBLE_CURVE_JACOBIAN */);
    doHotSpot(rootFinder, "default Newton, double curve", DOUBLE_CURVE_FINDER);

  }

  // @Test
  // public void testBroyden() {
  // final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS, DOUBLE_CURVE_JACOBIAN);
  //
  // doHotSpot(rootFinder, "default Newton, double curve", DOUBLE_CURVE_FINDER);
  // }
  //
  // @Test
  // public void ShermanMorrison() {
  // final VectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS, DOUBLE_CURVE_JACOBIAN);
  // doHotSpot(rootFinder, "default Newton, double curve", DOUBLE_CURVE_FINDER);
  // }

  //
  @SuppressWarnings("unchecked")
  @Test
  public void testJacobian() {
    final JacobianCalculator jacobianFD = new FiniteDifferenceJacobianCalculator(1e-8);
    final DoubleMatrix2D jacExact = DOUBLE_CURVE_JACOBIAN.evaluate(X0, DOUBLE_CURVE_FINDER);
    final DoubleMatrix2D jacFD = jacobianFD.evaluate(X0, DOUBLE_CURVE_FINDER);
    // System.out.println("exact: " + jacExact.toString());
    // System.out.println("FD: " + jacFD.toString());

    assertMatrixEquals(jacExact, jacFD, 1e-6);
  }

  private void doHotSpot(final VectorRootFinder rootFinder, final String name, final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor) {
    for (int i = 0; i < HOTSPOT_WARMUP_CYCLES; i++) {
      doTest(rootFinder, (DoubleCurveFinder) functor);
    }
    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on " + name, BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        doTest(rootFinder, (DoubleCurveFinder) functor);
      }
      timer.finished();
    }
  }

  private void doTest(final VectorRootFinder rootFinder, final DoubleCurveFinder functor) {
    final double[] yieldCurveNodes = rootFinder.getRoot(functor, X0).getData();
    final double[] fwdYields = Arrays.copyOfRange(yieldCurveNodes, 0, FWD_NODE_TIMES.length);
    final YieldAndDiscountCurve fwdCurve = makeYieldCurve(fwdYields, FWD_NODE_TIMES, EXTRAPOLATOR);
    final double[] fundYields = Arrays.copyOfRange(yieldCurveNodes, FWD_NODE_TIMES.length, yieldCurveNodes.length);
    final YieldAndDiscountCurve fundCurve = makeYieldCurve(fundYields, FUND_NODE_TIMES, EXTRAPOLATOR);
    for (int i = 0; i < MARKET_VALUES.length; i++) {
      assertEquals(MARKET_VALUES[i], RATE_CALCULATOR.getRate(fwdCurve, fundCurve, INSTRUMENTS.get(i)), EPS);
    }
  }

  private static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times, final Interpolator1D<? extends Interpolator1DCubicSplineDataBundle, InterpolationResult> interpolator) {
    final int n = yields.length;
    if (n != times.length) {
      throw new IllegalArgumentException("rates and times different lengths");
    }
    return new InterpolatedYieldCurve(times, yields, interpolator);
  }

  private static Swap setupSwap(final int payments) {
    final double[] fixed = new double[payments];
    final double[] floating = new double[2 * payments];
    final double[] deltaStart = new double[2 * payments];
    final double[] deltaEnd = new double[2 * payments];
    for (int i = 0; i < payments; i++) {
      floating[2 * i + 1] = fixed[i] = 0.5 * (1 + i) + 0.02 * (RANDOM.nextDouble() - 0.5);
    }
    for (int i = 0; i < 2 * payments; i++) {
      if (i % 2 == 0) {
        floating[i] = 0.25 * (1 + i) + 0.02 * (RANDOM.nextDouble() - 0.5);
      }
      deltaStart[i] = 0.02 * (i == 0 ? RANDOM.nextDouble() : (RANDOM.nextDouble() - 0.5));
      deltaEnd[i] = 0.02 * (RANDOM.nextDouble() - 0.5);
    }
    return new Swap(fixed, floating, deltaStart, deltaEnd);
  }

  private void assertMatrixEquals(final DoubleMatrix2D m1, final DoubleMatrix2D m2, final double eps) {
    final int m = m1.getNumberOfRows();
    final int n = m1.getNumberOfColumns();
    assertEquals(m2.getNumberOfRows(), m);
    assertEquals(m2.getNumberOfColumns(), n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < m; j++) {
        assertEquals(m1.getEntry(i, j), m2.getEntry(i, j), eps);
      }
    }
  }
}

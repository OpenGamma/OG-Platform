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

import com.opengamma.financial.interestrate.swap.DoubleCurveFinder;
import com.opengamma.financial.interestrate.swap.DoubleCurveJacobian;
import com.opengamma.financial.interestrate.swap.SingleCurveFinder;
import com.opengamma.financial.interestrate.swap.SingleCurveJacobian;
import com.opengamma.financial.interestrate.swap.SwapRateCalculator;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CubicSplineInterpolatorWithSensitivities1D;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineDataBundle;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineWithSensitivitiesDataBundle;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.FiniteDifferenceJacobianCalculator;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.math.rootfinding.newton.ShermanMorrisonVectorRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class YieldCurveBootStrapTest {
  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveBootStrapTest.class);
  private static final int HOTSPOT_WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  protected static final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> CUBIC = new NaturalCubicSplineInterpolator1D();
  protected static final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> CUBIC_WITH_SENSITIVITY = new CubicSplineInterpolatorWithSensitivities1D();
  private static final Interpolator1DWithSensitivities<Interpolator1DCubicSplineDataBundle> CUBIC_WITH_FD_SENSITIVITY = new Interpolator1DWithSensitivities<Interpolator1DCubicSplineDataBundle>(CUBIC);
  protected static final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> LINEAR = new LinearInterpolator1D();
  protected static List<Swap> SWAPS;
  protected static double[] SWAP_VALUES;
  protected static final double[] TIME_GRID;
  protected static final double[] FWD_NODE_TIMES;
  protected static final double[] FUND_NODE_TIMES;
  protected static final double SPOT_RATE;
  protected static final double EPS = 1e-8;
  protected static final int STEPS = 100;
  protected static final DoubleMatrix1D X0;
  protected static YieldAndDiscountCurve FUNDING_CURVE;
  protected static YieldAndDiscountCurve FORWARD_CURVE;
  private static final SwapRateCalculator SWAP_RATE_CALCULATOR = new SwapRateCalculator();
  protected static final Function1D<DoubleMatrix1D, DoubleMatrix1D> SINGLE_CURVE_FINDER;
  protected static final Function1D<DoubleMatrix1D, DoubleMatrix1D> DOUBLE_CURVE_FINDER;
  protected static final JacobianCalculator SINGLE_CURVE_JACOBIAN;
  protected static final JacobianCalculator DOUBLE_CURVE_JACOBIAN;

  static {
    final int[] payments = new int[] {1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60};
    SPOT_RATE = 0.005;
    FWD_NODE_TIMES = new double[] {0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 31.0};
    final double[] fwdYields = new double[] {0.01, 0.02, 0.035, 0.06, 0.055, 0.05, 0.045};
    FUND_NODE_TIMES = new double[] {1.0, 2.0, 5.0, 10.0, 20.0, 31.0};
    final double[] fundYields = new double[] {0.021, 0.036, 0.06, 0.054, 0.049, 0.044};

    FORWARD_CURVE = makeYieldCurve(fwdYields, FWD_NODE_TIMES, CUBIC);
    FUNDING_CURVE = makeYieldCurve(fundYields, FUND_NODE_TIMES, CUBIC);

    final int n = payments.length;
    TIME_GRID = new double[n];
    SWAP_VALUES = new double[n];
    SWAPS = new ArrayList<Swap>();
    final double[] rates = new double[n];
    Swap swap;
    int nFloat;
    for (int i = 0; i < n; i++) {
      swap = setupSwap(payments[i]);
      SWAPS.add(swap);
      nFloat = swap.getNumberOfFloatingPayments();
      TIME_GRID[i] = Math.max(swap.getFixedPaymentTimes()[swap.getNumberOfFixedPayments() - 1], swap.getFloatingPaymentTimes()[nFloat - 1] + swap.getDeltaEnd()[nFloat - 1]);
      SWAP_VALUES[i] = SWAP_RATE_CALCULATOR.getRate(FORWARD_CURVE, FUNDING_CURVE, swap);
      rates[i] = 0.05;
    }
    X0 = new DoubleMatrix1D(rates);

    SINGLE_CURVE_FINDER = new SingleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, TIME_GRID, CUBIC);
    DOUBLE_CURVE_FINDER = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, FWD_NODE_TIMES, FUND_NODE_TIMES, null, null, CUBIC, CUBIC);
    SINGLE_CURVE_JACOBIAN = new SingleCurveJacobian(SWAPS, SPOT_RATE, TIME_GRID, CUBIC_WITH_SENSITIVITY);
    DOUBLE_CURVE_JACOBIAN = new DoubleCurveJacobian(SWAPS, SPOT_RATE, FWD_NODE_TIMES, FUND_NODE_TIMES, CUBIC_WITH_SENSITIVITY, CUBIC_WITH_SENSITIVITY);
  }

  @Test
  public void doNothing() {

  }

  @SuppressWarnings("unchecked")
  @Test
  public void testNewton() {

    VectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder, "default Newton, finite difference", SINGLE_CURVE_FINDER);
    rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doHotSpot(rootFinder, "default Newton, single curve", SINGLE_CURVE_FINDER);

    rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder, "default Newton, double curve, finite difference", DOUBLE_CURVE_FINDER);
    rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS, DOUBLE_CURVE_JACOBIAN);
    doHotSpot(rootFinder, "default Newton, double curve", DOUBLE_CURVE_FINDER);
  }

  @Test
  public void testShermanMorrison() {
    VectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder, "Sherman Morrison, finite difference", SINGLE_CURVE_FINDER);
    rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doHotSpot(rootFinder, "Sherman Morrison, single curve", SINGLE_CURVE_FINDER);

    rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder, "Sherman Morrisonn, double curve, finite difference", DOUBLE_CURVE_FINDER);
    rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS, DOUBLE_CURVE_JACOBIAN);
    doHotSpot(rootFinder, "Sherman Morrison, double curve", DOUBLE_CURVE_FINDER);
  }

  @Test
  public void testBroyden() {
    VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder, "Broyden, finite difference", SINGLE_CURVE_FINDER);
    rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doHotSpot(rootFinder, "Broyden, single curve", SINGLE_CURVE_FINDER);

    rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder, "Broyden, double curve, finite difference", DOUBLE_CURVE_FINDER);
    rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS, DOUBLE_CURVE_JACOBIAN);
    doHotSpot(rootFinder, "Broyden, double curve", DOUBLE_CURVE_FINDER);
  }

  private void doHotSpot(final VectorRootFinder rootFinder, String name, Function1D<DoubleMatrix1D, DoubleMatrix1D> functor) {
    for (int i = 0; i < HOTSPOT_WARMUP_CYCLES; i++) {
      doTest(rootFinder, functor);
    }
    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on " + name, BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        doTest(rootFinder, functor);
      }
      timer.finished();
    }
  }

  private void doTest(final VectorRootFinder rootFinder, Function1D<DoubleMatrix1D, DoubleMatrix1D> functor) {
    if (functor.getClass().equals(SingleCurveFinder.class)) {
      doTestForSingleCurve(rootFinder, (SingleCurveFinder) functor);
    } else if (functor.getClass().equals(DoubleCurveFinder.class)) {
      doTestForDoubleCurve(rootFinder, (DoubleCurveFinder) functor);
    } else {
      throw new IllegalArgumentException("functor must be a SingleCurveFinder or DoubleCurveFinder");
    }
  }

  private void doTestForSingleCurve(final VectorRootFinder rootFinder, SingleCurveFinder functor) {
    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final YieldAndDiscountCurve curve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);
    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAP_RATE_CALCULATOR.getRate(curve, curve, SWAPS.get(i)), 1e-3);// TODO change this back to EPS
    }
  }

  private void doTestForDoubleCurve(final VectorRootFinder rootFinder, DoubleCurveFinder functor) {
    final double[] yieldCurveNodes = rootFinder.getRoot(functor, X0).getData();
    final double[] fwdYields = Arrays.copyOfRange(yieldCurveNodes, 0, FWD_NODE_TIMES.length);
    final YieldAndDiscountCurve fwdCurve = makeYieldCurve(fwdYields, FWD_NODE_TIMES, CUBIC);
    final double[] fundYields = Arrays.copyOfRange(yieldCurveNodes, FWD_NODE_TIMES.length, yieldCurveNodes.length);
    final YieldAndDiscountCurve fundCurve = makeYieldCurve(fundYields, FUND_NODE_TIMES, CUBIC);
    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAP_RATE_CALCULATOR.getRate(fwdCurve, fundCurve, SWAPS.get(i)), EPS);
    }
  }

  @Test
  public void testTickingSwapRates() {
    final NormalDistribution normDist = new NormalDistribution(0, 1.0, RANDOM);
    final VectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS);
    final double[] swapRates = Arrays.copyOf(SWAP_VALUES, SWAP_VALUES.length);
    DoubleMatrix1D yieldCurveNodes = X0;
    YieldAndDiscountCurve curve;
    final double sigma = 0.03;
    for (int t = 0; t < 100; t++) {
      for (int i = 0; i < SWAP_VALUES.length; i++) {
        swapRates[i] *= Math.exp(-0.5 * sigma * sigma + sigma * normDist.nextRandom());
      }
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new SingleCurveFinder(SWAPS, swapRates, SPOT_RATE, TIME_GRID, CUBIC);
      yieldCurveNodes = rootFinder.getRoot(functor, yieldCurveNodes);
      curve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);
      for (int i = 0; i < swapRates.length; i++) {
        assertEquals(swapRates[i], SWAP_RATE_CALCULATOR.getRate(curve, curve, SWAPS.get(i)), EPS);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  // TODO move this into SingleCurveJacobianTest
  public void testSingleCurveJacobian() {

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new SingleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, TIME_GRID, CUBIC);
    final JacobianCalculator jacobianExact = new SingleCurveJacobian(SWAPS, SPOT_RATE, TIME_GRID, CUBIC_WITH_SENSITIVITY);
    final JacobianCalculator jacobianFD = new FiniteDifferenceJacobianCalculator(1e-6);
    final DoubleMatrix2D jacExact = jacobianExact.evaluate(X0, func);
    final DoubleMatrix2D jacFD = jacobianFD.evaluate(X0, func);
    assertMatrixEquals(jacExact, jacFD, 1e-8);
    System.out.println(jacExact.toString());
    System.out.println(jacFD.toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDoubleCurveJacobian() {
    final double[] fwdTimes = new double[] {0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 31.0};
    final double[] fundTimes = new double[] {1.0, 2.0, 5.0, 10.0, 20.0, 31.0};
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, fwdTimes, fundTimes, null, null, CUBIC, CUBIC);
    final JacobianCalculator jacobianExact = new DoubleCurveJacobian(SWAPS, SPOT_RATE, fwdTimes, fundTimes, CUBIC_WITH_SENSITIVITY, CUBIC_WITH_SENSITIVITY);
    final JacobianCalculator jacobianFD = new FiniteDifferenceJacobianCalculator(1e-8);
    final DoubleMatrix2D jacExact = jacobianExact.evaluate(X0, func);
    final DoubleMatrix2D jacFD = jacobianFD.evaluate(X0, func);
    // assertMatrixEquals(jacExact, jacFD, 1e-3);
    System.out.println(jacExact.toString());
    System.out.println(jacFD.toString());
  }

  @Test
  public void testForwardCurveOnly() {
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, TIME_GRID, null, null, FUNDING_CURVE, CUBIC, null);
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final YieldAndDiscountCurve fwdCurve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);
    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAP_RATE_CALCULATOR.getRate(fwdCurve, FUNDING_CURVE, SWAPS.get(i)), EPS);
    }
  }

  @Test
  public void testFundingCurveOnly() {
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, null, TIME_GRID, FORWARD_CURVE, null, null, CUBIC);
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final YieldAndDiscountCurve fundCurve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);
    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAP_RATE_CALCULATOR.getRate(FORWARD_CURVE, fundCurve, SWAPS.get(i)), EPS);
    }
  }

  @Test
  public void testFindingTwoCurves() {
    final double[] fwdTimes = new double[] {0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 31.0};
    final double[] fundTimes = new double[] {1.0, 2.0, 5.0, 10.0, 20.0, 31.0};
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, fwdTimes, fundTimes, null, null, CUBIC, CUBIC);
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    final double[] yieldCurveNodes = rootFinder.getRoot(functor, X0).getData();
    final double[] fwdYields = Arrays.copyOfRange(yieldCurveNodes, 0, fwdTimes.length);
    final YieldAndDiscountCurve fwdCurve = makeYieldCurve(fwdYields, fwdTimes, CUBIC);
    final double[] fundYields = Arrays.copyOfRange(yieldCurveNodes, fwdTimes.length, yieldCurveNodes.length);
    final YieldAndDiscountCurve fundCurve = makeYieldCurve(fundYields, fundTimes, CUBIC);
    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAP_RATE_CALCULATOR.getRate(fwdCurve, fundCurve, SWAPS.get(i)), EPS);
    }
  }

  private static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times, final Interpolator1D<? extends Interpolator1DCubicSplineDataBundle, InterpolationResult> interpolator) {
    final int n = yields.length;
    if (n != times.length) {
      throw new IllegalArgumentException("rates and times different lengths");
    }
    double[] t = new double[n + 1];
    double[] y = new double[n + 1];
    t[0] = 0;
    y[0] = SPOT_RATE;
    for (int i = 0; i < n; i++) {
      t[i + 1] = times[i];
      y[i + 1] = yields[i];
    }
    return new InterpolatedYieldCurve(t, y, interpolator);
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

  private void assertMatrixEquals(DoubleMatrix2D m1, DoubleMatrix2D m2, double eps) {
    int m = m1.getNumberOfRows();
    int n = m1.getNumberOfColumns();
    assertEquals(m2.getNumberOfRows(), m);
    assertEquals(m2.getNumberOfColumns(), n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < m; j++) {
        assertEquals(m1.getEntry(i, j), m2.getEntry(i, j), eps);
      }
    }
  }
}

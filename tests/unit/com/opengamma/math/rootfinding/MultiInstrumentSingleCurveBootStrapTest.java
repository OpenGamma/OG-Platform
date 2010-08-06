/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.libor.definition.Libor;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearExtrapolator1D;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DCubicSplineDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.FlatExtrapolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.LinearExtrapolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.NaturalCubicSplineInterpolator1DNodeSensitivityCalculator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.FiniteDifferenceJacobianCalculator;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.math.rootfinding.newton.ShermanMorrisonVectorRootFinder;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class MultiInstrumentSingleCurveBootStrapTest {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveBootStrapTest.class);
  private static final int HOTSPOT_WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);

  private static final Interpolator1D<Interpolator1DCubicSplineDataBundle> EXTRAPOLATOR;
  private static final Interpolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> EXTRAPOLATOR_WITH_SENSITIVITY;
  private static final List<InterestRateDerivative> INSTRUMENTS;
  private static final double[] MARKET_VALUES;
  private static final String CURVE_NAME = "Libor_3m_GBP";
  private static final YieldAndDiscountCurve CURVE;

  private static final double[] NODE_TIMES;

  private static final double EPS = 1e-8;
  private static final int STEPS = 100;
  private static final DoubleMatrix1D X0;

  private static final InterestRateDerivativeVisitor<Double> CALCULATOR = PresentValueCalculator.getInstance();
  private static final InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> SENSITIVITY_CALCULATOR = PresentValueSensitivityCalculator.getInstance();
  private static final ParRateCalculator RATE_CALCULATOR = ParRateCalculator.getInstance();
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> SINGLE_CURVE_FINDER;
  private static final JacobianCalculator SINGLE_CURVE_JACOBIAN;

  private static final Function1D<Double, Double> DUMMY_CURVE = new Function1D<Double, Double>() {

    // private static final double A = -0.0325;
    // private static final double B = 0.021;
    // private static final double C = 0.52;
    // private static final double D = 0.055;

    private static final double A = 0;
    private static final double B = 0.004148649;
    private static final double C = 0.056397936;
    private static final double D = 0.004457019;
    private static final double E = 0.000429628;

    @Override
    public Double evaluate(final Double x) {
      return (A + B * x) * Math.exp(-C * x) + E * x + D;
    }
  };

  static {
    final NaturalCubicSplineInterpolator1D cubicInterpolator = new NaturalCubicSplineInterpolator1D();
    final NaturalCubicSplineInterpolator1DNodeSensitivityCalculator cubicSensitivityCalculator = new NaturalCubicSplineInterpolator1DNodeSensitivityCalculator();
    final LinearExtrapolator1D<Interpolator1DCubicSplineDataBundle> linearExtrapolator = new LinearExtrapolator1D<Interpolator1DCubicSplineDataBundle>(cubicInterpolator);
    final LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> linearExtrapolatorSensitivityCalculator = new LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle>(
        cubicSensitivityCalculator);
    final FlatExtrapolator1D<Interpolator1DCubicSplineDataBundle> flatExtrapolator = new FlatExtrapolator1D<Interpolator1DCubicSplineDataBundle>();
    final FlatExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> flatExtrapolatorSensitivityCalculator = new FlatExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle>();
    EXTRAPOLATOR = new CombinedInterpolatorExtrapolator<Interpolator1DCubicSplineDataBundle>(cubicInterpolator, linearExtrapolator, flatExtrapolator);
    EXTRAPOLATOR_WITH_SENSITIVITY = new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle>(cubicSensitivityCalculator,
        linearExtrapolatorSensitivityCalculator, flatExtrapolatorSensitivityCalculator);
    INSTRUMENTS = new ArrayList<InterestRateDerivative>();

    final double[] liborMaturities = new double[] {1. / 12, 2. / 12, 3. / 12}; //
    final double[] fraMaturities = new double[] {0.5, 0.75};
    final double[] cashMaturities = new double[] {1. / 365, 1. / 52, 2. / 52.};
    final double[] swapMaturities = new double[] {1.00, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556, 35.00833333, 50.01388889};

    final int nNodes = liborMaturities.length + fraMaturities.length + cashMaturities.length + swapMaturities.length;

    NODE_TIMES = new double[nNodes];
    int index = 0;
    for (final double t : liborMaturities) {
      NODE_TIMES[index++] = t;
    }
    for (final double t : fraMaturities) {
      NODE_TIMES[index++] = t;
    }
    for (final double t : cashMaturities) {
      NODE_TIMES[index++] = t;
    }
    for (final double t : swapMaturities) {
      NODE_TIMES[index++] = t;
    }

    // set up curve to obtain "market" prices
    final double[] yields = new double[nNodes];

    for (int i = 0; i < nNodes; i++) {
      yields[i] = DUMMY_CURVE.evaluate(NODE_TIMES[i]);
    }

    // now get market prices
    MARKET_VALUES = new double[nNodes];
    final double[] rates = new double[nNodes];
    CURVE = makeYieldCurve(yields, NODE_TIMES, EXTRAPOLATOR);
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(CURVE_NAME, CURVE);

    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.05;
    }
    X0 = new DoubleMatrix1D(rates);

    InterestRateDerivative ird;
    index = 0;
    double rate;
    for (final double t : liborMaturities) {
      ird = new Libor(t, 0.0, CURVE_NAME);
      rate = RATE_CALCULATOR.getValue(ird, bundle);
      ird = new Libor(t, rate, CURVE_NAME);
      INSTRUMENTS.add(ird);
      MARKET_VALUES[index++] = rate;
    }
    for (final double t : fraMaturities) {
      ird = new ForwardRateAgreement(t - 0.25, t, 0.0, CURVE_NAME, CURVE_NAME);
      rate = RATE_CALCULATOR.getValue(ird, bundle);
      System.out.println("Strike: " + rate);
      ird = new ForwardRateAgreement(t - 0.25, t, rate, CURVE_NAME, CURVE_NAME);
      INSTRUMENTS.add(ird);
      MARKET_VALUES[index++] = rate;
    }

    for (final double t : cashMaturities) {
      ird = new Cash(t, 0.0, CURVE_NAME);
      rate = RATE_CALCULATOR.getValue(ird, bundle);
      ird = new Cash(t, rate, CURVE_NAME);
      INSTRUMENTS.add(ird);
      MARKET_VALUES[index++] = rate;
    }

    for (final double t : swapMaturities) {
      ird = setupSwap(t, 0.0, CURVE_NAME, CURVE_NAME);
      rate = RATE_CALCULATOR.getValue(ird, bundle);
      ird = setupSwap(t, rate, CURVE_NAME, CURVE_NAME);
      INSTRUMENTS.add(ird);
      MARKET_VALUES[index++] = rate;
    }

    if (INSTRUMENTS.size() != (nNodes)) {
      throw new IllegalArgumentException("number of instruments not equal to number of nodes");
    }

    Arrays.sort(NODE_TIMES);

    final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> unknownCurveInterpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
    final LinkedHashMap<String, double[]> unknownCurveNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> unknownCurveNodeSensitivityCalculators = new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();
    unknownCurveInterpolators.put(CURVE_NAME, EXTRAPOLATOR);
    unknownCurveNodes.put(CURVE_NAME, NODE_TIMES);
    unknownCurveNodeSensitivityCalculators.put(CURVE_NAME, EXTRAPOLATOR_WITH_SENSITIVITY);
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(INSTRUMENTS, null, unknownCurveNodes, unknownCurveInterpolators, unknownCurveNodeSensitivityCalculators);
    SINGLE_CURVE_FINDER = new MultipleYieldCurveFinderFunction(data, CALCULATOR);
    SINGLE_CURVE_JACOBIAN = new MultipleYieldCurveFinderJacobian(INSTRUMENTS, unknownCurveNodes, unknownCurveInterpolators, unknownCurveNodeSensitivityCalculators, null, SENSITIVITY_CALCULATOR);
  }

  @Test
  public void testNewton() {
    final VectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doHotSpot(rootFinder, "default Newton, single curve", SINGLE_CURVE_FINDER);
  }

  @Test
  public void testBroyden() {
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doHotSpot(rootFinder, "Broyden, single curve", SINGLE_CURVE_FINDER);
  }

  @Test
  public void testShermanMorrison() {
    final VectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doHotSpot(rootFinder, "Broyden, single curve", SINGLE_CURVE_FINDER);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSingleJacobian() {
    final JacobianCalculator jacobianFD = new FiniteDifferenceJacobianCalculator(1e-8);
    final DoubleMatrix2D jacExact = SINGLE_CURVE_JACOBIAN.evaluate(X0, SINGLE_CURVE_FINDER);
    final DoubleMatrix2D jacFD = jacobianFD.evaluate(X0, SINGLE_CURVE_FINDER);
    assertMatrixEquals(jacExact, jacFD, 1e-5);
  }

  private void doHotSpot(final VectorRootFinder rootFinder, final String name, final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor) {
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

  private void doTest(final VectorRootFinder rootFinder, final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor) {
    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final YieldAndDiscountCurve curve = makeYieldCurve(yieldCurveNodes.getData(), NODE_TIMES, EXTRAPOLATOR);
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(CURVE_NAME, curve);
    for (int i = 0; i < MARKET_VALUES.length; i++) {
      assertEquals(MARKET_VALUES[i], RATE_CALCULATOR.getValue(INSTRUMENTS.get(i), bundle), EPS);
    }
  }

  private static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedYieldCurve(times, yields, interpolator);
  }

  private static FixedFloatSwap setupSwap(final double time, final double swapRate, final String fundCurveName, final String liborCurveName) {
    final int index = (int) Math.round(2 * time);
    return setupSwap(index, swapRate, fundCurveName, liborCurveName);
  }

  private static FixedFloatSwap setupSwap(final int payments, final double swapRate, final String fundCurveName, final String liborCurveName) {
    final double[] fixed = new double[payments];

    final double[] floating = new double[2 * payments];
    final double[] deltaStart = new double[2 * payments];
    final double[] deltaEnd = new double[2 * payments];
    final double sigma = 0.0 / 365.0;
    for (int i = 0; i < payments; i++) {
      fixed[i] = 0.5 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      floating[2 * i + 1] = fixed[i];
    }
    for (int i = 0; i < 2 * payments; i++) {
      if (i % 2 == 0) {
        floating[i] = 0.25 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      }
      deltaStart[i] = sigma * (i == 0 ? RANDOM.nextDouble() : (RANDOM.nextDouble() - 0.5));
      deltaEnd[i] = sigma * (RANDOM.nextDouble() - 0.5);
    }
    final ConstantCouponAnnuity fixedLeg = new ConstantCouponAnnuity(fixed, swapRate, fundCurveName);
    final VariableAnnuity floatingLeg = new VariableAnnuity(floating, 1.0, deltaStart, deltaEnd, fundCurveName, liborCurveName);
    return new FixedFloatSwap(fixedLeg, floatingLeg);
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

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

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
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
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
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
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.FlatExtrapolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.LinearExtrapolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.NaturalCubicSplineInterpolator1DNodeSensitivityCalculator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.math.rootfinding.newton.ShermanMorrisonVectorRootFinder;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class BasisSwapYieldCurveFinderTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final CombinedInterpolatorExtrapolator<Interpolator1DCubicSplineDataBundle> INTERPOLATOR;
  private static final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> SENSITIVITY_CALCULATOR;
  private static List<InterestRateDerivative> INSTRUMENTS;
  private static YieldAndDiscountCurve TREASURY_CURVE;
  private static YieldAndDiscountCurve LIBOR_CURVE;

  private static String TREASURY_CURVE_NAME = "Treasury";
  private static String LIBOR_CURVE_NAME = "Libor_3m_USD";

  private static final double[] LIBOR_NODE_TIMES;
  private static final double[] TREASURY_NODE_TIMES;
  private static final double EPS = 1e-8;
  private static final int STEPS = 100;
  private static final DoubleMatrix1D X0;

  private static final InterestRateDerivativeVisitor<Double> CALCULATOR = PresentValueCalculator.getInstance();
  private static final InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> PV_SENSITIVITY_CALCULATOR = PresentValueSensitivityCalculator.getInstance();

  private static final ParRateCalculator RATE_CALCULATOR = ParRateCalculator.getInstance();

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> DOUBLE_CURVE_FINDER;
  private static final JacobianCalculator DOUBLE_CURVE_JACOBIAN;

  protected static final Function1D<Double, Double> DUMMY_TREAURY_CURVE = new Function1D<Double, Double>() {

    private static final double a = -0.0325;
    private static final double b = 0.021;
    private static final double c = 0.52;
    private static final double d = 0.055;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  protected static final Function1D<Double, Double> DUMMY_SPEAD_CURVE = new Function1D<Double, Double>() {

    private static final double a = 0.0025;
    private static final double b = 0.0021;
    private static final double c = 0.2;
    private static final double d = 0.0;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  static {

    INSTRUMENTS = new ArrayList<InterestRateDerivative>();

    final NaturalCubicSplineInterpolator1D cubicInterpolator = new NaturalCubicSplineInterpolator1D();
    final NaturalCubicSplineInterpolator1DNodeSensitivityCalculator cubicSensitivityCalculator = new NaturalCubicSplineInterpolator1DNodeSensitivityCalculator();
    final LinearExtrapolator1D<Interpolator1DCubicSplineDataBundle> linearExtrapolator = new LinearExtrapolator1D<Interpolator1DCubicSplineDataBundle>(cubicInterpolator);
    final LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> linearExtrapolatorSensitivityCalculator = new LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle>(
        cubicSensitivityCalculator);
    final FlatExtrapolator1D<Interpolator1DCubicSplineDataBundle> flatExtrapolator = new FlatExtrapolator1D<Interpolator1DCubicSplineDataBundle>();
    final FlatExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> flatExtrapolatorSensitivityCalculator = new FlatExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle>();
    INTERPOLATOR = new CombinedInterpolatorExtrapolator<Interpolator1DCubicSplineDataBundle>(cubicInterpolator, linearExtrapolator, flatExtrapolator);
    SENSITIVITY_CALCULATOR = new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle>(cubicSensitivityCalculator, linearExtrapolatorSensitivityCalculator,
        flatExtrapolatorSensitivityCalculator);

    final double[] liborMaturities = new double[] {1. / 12, 2. / 12, 3. / 12}; // 
    final double[] fraMaturities = new double[] {0.5, 0.75};
    final double[] cashMaturities = new double[] {1. / 365, 1. / 52., 2. / 52., 1. / 12, 3. / 12, 6. / 12};
    final double[] swapMaturities = new double[] {1.00, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556, 35.00833333, 50.01388889};
    final double[] basisSwapMaturities = new double[] {1, 2, 5, 10, 20, 30, 50};

    final int nLiborNodes = liborMaturities.length + fraMaturities.length + swapMaturities.length;
    final int nTreasuryNodes = cashMaturities.length + basisSwapMaturities.length;

    LIBOR_NODE_TIMES = new double[nLiborNodes];
    TREASURY_NODE_TIMES = new double[nTreasuryNodes];

    // set up curves to obtain "market" prices
    final double[] liborYields = new double[LIBOR_NODE_TIMES.length];
    final double[] treasuryYields = new double[TREASURY_NODE_TIMES.length];
    int liborIndex = 0;
    int fundIndex = 0;
    for (final double t : liborMaturities) {
      LIBOR_NODE_TIMES[liborIndex++] = t;
    }
    for (final double t : fraMaturities) {
      LIBOR_NODE_TIMES[liborIndex++] = t;
    }
    for (final double t : cashMaturities) {
      TREASURY_NODE_TIMES[fundIndex++] = t;
    }
    for (final double t : swapMaturities) {
      LIBOR_NODE_TIMES[liborIndex++] = t;
    }
    for (final double t : basisSwapMaturities) {
      TREASURY_NODE_TIMES[fundIndex++] = t;
    }

    for (int i = 0; i < TREASURY_NODE_TIMES.length; i++) {
      treasuryYields[i] = DUMMY_TREAURY_CURVE.evaluate(TREASURY_NODE_TIMES[i]);
    }

    for (int i = 0; i < LIBOR_NODE_TIMES.length; i++) {
      liborYields[i] = DUMMY_TREAURY_CURVE.evaluate(LIBOR_NODE_TIMES[i]) + DUMMY_SPEAD_CURVE.evaluate(LIBOR_NODE_TIMES[i]);
    }

    LIBOR_CURVE = new InterpolatedYieldCurve(LIBOR_NODE_TIMES, liborYields, INTERPOLATOR);
    TREASURY_CURVE = new InterpolatedYieldCurve(TREASURY_NODE_TIMES, treasuryYields, INTERPOLATOR);

    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(LIBOR_CURVE_NAME, LIBOR_CURVE);
    bundle.setCurve(TREASURY_CURVE_NAME, TREASURY_CURVE);

    InterestRateDerivative ird;

    for (final double t : liborMaturities) {
      ird = new Libor(t, 0.0, LIBOR_CURVE_NAME);
      final double rate = RATE_CALCULATOR.getValue(ird, bundle);
      ird = new Libor(t, rate, LIBOR_CURVE_NAME);
      INSTRUMENTS.add(ird);
    }
    for (final double t : fraMaturities) {
      ird = new ForwardRateAgreement(t - 0.25, t, 0.0, TREASURY_CURVE_NAME, LIBOR_CURVE_NAME);
      final double rate = RATE_CALCULATOR.getValue(ird, bundle);
      ird = new ForwardRateAgreement(t - 0.25, t, rate, TREASURY_CURVE_NAME, LIBOR_CURVE_NAME);
      INSTRUMENTS.add(ird);
    }

    for (final double t : cashMaturities) {
      ird = new Cash(t, 0.0, TREASURY_CURVE_NAME);
      final double rate = RATE_CALCULATOR.getValue(ird, bundle);
      ird = new Cash(t, rate, TREASURY_CURVE_NAME);
      INSTRUMENTS.add(ird);
    }

    for (final double t : swapMaturities) {
      ird = setupSwap(t, 0.0, TREASURY_CURVE_NAME, LIBOR_CURVE_NAME);
      final double rate = RATE_CALCULATOR.getValue(ird, bundle);
      ird = setupSwap(t, rate, TREASURY_CURVE_NAME, LIBOR_CURVE_NAME);
      INSTRUMENTS.add(ird);
    }
    for (final double t : basisSwapMaturities) {
      ird = setupBasisSwap(t, 0.0, TREASURY_CURVE_NAME, TREASURY_CURVE_NAME, LIBOR_CURVE_NAME);
      final double rate = RATE_CALCULATOR.getValue(ird, bundle);
      ird = setupBasisSwap(t, rate, TREASURY_CURVE_NAME, TREASURY_CURVE_NAME, LIBOR_CURVE_NAME);
      INSTRUMENTS.add(ird);
    }

    if (INSTRUMENTS.size() != (nLiborNodes + nTreasuryNodes)) {
      throw new IllegalArgumentException("number of instruments not equal to number of nodes");
    }

    Arrays.sort(LIBOR_NODE_TIMES);
    Arrays.sort(TREASURY_NODE_TIMES);

    final int n = INSTRUMENTS.size();
    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      rates[i] = 0.05;
    }
    X0 = new DoubleMatrix1D(rates);

    final LinkedHashMap<String, Interpolator1D> unknownCurves = new LinkedHashMap<String, Interpolator1D>();
    final LinkedHashMap<String, double[]> unknownCurvesNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator> unknownCurveNodeSensitivityCalculators = new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator>();
    unknownCurves.put(TREASURY_CURVE_NAME, INTERPOLATOR);
    unknownCurvesNodes.put(TREASURY_CURVE_NAME, TREASURY_NODE_TIMES);
    unknownCurveNodeSensitivityCalculators.put(TREASURY_CURVE_NAME, SENSITIVITY_CALCULATOR);
    unknownCurves.put(LIBOR_CURVE_NAME, INTERPOLATOR);
    unknownCurvesNodes.put(LIBOR_CURVE_NAME, LIBOR_NODE_TIMES);
    unknownCurveNodeSensitivityCalculators.put(LIBOR_CURVE_NAME, SENSITIVITY_CALCULATOR);
    DOUBLE_CURVE_FINDER = new MultipleYieldCurveFinderFunction(INSTRUMENTS, unknownCurvesNodes, unknownCurves, unknownCurveNodeSensitivityCalculators, null, CALCULATOR);
    DOUBLE_CURVE_JACOBIAN = new MultipleYieldCurveFinderJacobian(INSTRUMENTS, unknownCurvesNodes, unknownCurves, unknownCurveNodeSensitivityCalculators, null, PV_SENSITIVITY_CALCULATOR);
  }

  @Test
  public void testNewton() {
    final VectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS, DOUBLE_CURVE_JACOBIAN);
    doTest(rootFinder, DOUBLE_CURVE_FINDER);
  }

  @Test
  public void testBroyden() {
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS, DOUBLE_CURVE_JACOBIAN);
    doTest(rootFinder, DOUBLE_CURVE_FINDER);
  }

  @Test
  public void testShermanMorrison() {
    final VectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS, DOUBLE_CURVE_JACOBIAN);
    doTest(rootFinder, DOUBLE_CURVE_FINDER);
  }

  private void doTest(final VectorRootFinder rootFinder, final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor) {
    final double[] yieldCurveNodes = rootFinder.getRoot(functor, X0).getData();
    final double[] fundYields = Arrays.copyOfRange(yieldCurveNodes, 0, TREASURY_NODE_TIMES.length);
    final YieldAndDiscountCurve fundCurve = new InterpolatedYieldCurve(TREASURY_NODE_TIMES, fundYields, INTERPOLATOR);
    final double[] liborYields = Arrays.copyOfRange(yieldCurveNodes, TREASURY_NODE_TIMES.length, yieldCurveNodes.length);
    final YieldAndDiscountCurve liborCurve = new InterpolatedYieldCurve(LIBOR_NODE_TIMES, liborYields, INTERPOLATOR);

    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(TREASURY_CURVE_NAME, fundCurve);
    bundle.setCurve(LIBOR_CURVE_NAME, liborCurve);

    for (final InterestRateDerivative ird : INSTRUMENTS) {
      assertEquals(0.0, CALCULATOR.getValue(ird, bundle), EPS);
    }

    for (final double t : TREASURY_NODE_TIMES) {
      assertEquals(TREASURY_CURVE.getInterestRate(t), fundCurve.getInterestRate(t), EPS);
    }
    for (final double t : LIBOR_NODE_TIMES) {
      assertEquals(LIBOR_CURVE.getInterestRate(t), liborCurve.getInterestRate(t), EPS);
    }
  }

  private static BasisSwap setupBasisSwap(final double time, final double spread, final String fundCurveName, final String payCurveName, final String revieveCurveName) {
    final int index = (int) Math.round(4 * time);
    final double[] paymentTimes = new double[index];
    final double[] deltas = new double[index];
    final double[] spreads = new double[index];
    final double[] yearFracs = new double[index];
    for (int i = 0; i < index; i++) {
      paymentTimes[i] = 0.25 * (i + 1);
      spreads[i] = spread;
      yearFracs[i] = 0.25;
    }
    final VariableAnnuity payLeg = new VariableAnnuity(paymentTimes, 1.0, deltas, deltas, yearFracs, spreads, fundCurveName, payCurveName);
    final VariableAnnuity receiveLeg = new VariableAnnuity(paymentTimes, fundCurveName, revieveCurveName);
    return new BasisSwap(payLeg, receiveLeg);
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

}

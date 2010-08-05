/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
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
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.FlatExtrapolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.LinearExtrapolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.NaturalCubicSplineInterpolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.temp.InterpolationResult;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.math.rootfinding.newton.ShermanMorrisonVectorRootFinder;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class MarketDataImpliedYieldCurveTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> EXTRAPOLATOR;
  private static final Interpolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> EXTRAPOLATOR_WITH_SENSITIVITY;
  private static final List<InterestRateDerivative> INSTRUMENTS;

  private static final InterestRateDerivativeVisitor<Double> CALCULATOR = PresentValueCalculator.getInstance();
  private static final InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> SENSITIVITY_CALCULATOR = PresentValueSensitivityCalculator.getInstance();

  private static final double[] NODE_TIMES;
  private static final double EPS = 1e-8;
  private static final int STEPS = 100;
  private static final DoubleMatrix1D X0;
  private static final String CURVE_NAME = "Market Data Curve";

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> SINGLE_CURVE_FINDER;
  private static final JacobianCalculator SINGLE_CURVE_JACOBIAN;

  static {
    INSTRUMENTS = new ArrayList<InterestRateDerivative>();

    final double[] liborMaturities = new double[] {0.019164956, 0.038329911, 0.084873374, 0.169746749, 0.251882272, 0.336755647, 0.41889117, 0.503764545, 0.588637919, 0.665297741, 0.750171116,
        0.832306639, 0.917180014, 0.999315537};
    // 
    final double[] liborRates = new double[] {0.0506375, 0.05075, 0.0513, 0.0518625, 0.0523625, 0.0526125, 0.052925, 0.053175, 0.053375, 0.0535188, 0.0536375, 0.0537563, 0.0538438, 0.0539438};
    final double[] fraMaturities = new double[] {1.437371663, 1.686516085, 1.938398357};
    final double[] fraRates = new double[] {0.0566, 0.05705, 0.0572};
    final double[] swapMaturities = new double[] {/* 2.001368925, */3.000684463, 4, 4.999315537, 7.000684463, 10.00136893, 15.00068446, 20, 24.99931554, 30.00136893, 35.00068446, 50.00136893};
    final double[] swapRates = new double[] {/* 0.05412, */0.054135, 0.054295, 0.05457, 0.055075, 0.055715, 0.05652, 0.056865, 0.05695, 0.056925, 0.056885, 0.056725};

    final int nNodes = liborMaturities.length + fraMaturities.length + swapMaturities.length;
    if (nNodes != (liborRates.length + fraRates.length + swapRates.length)) {
      throw new IllegalArgumentException("maturities and rates different length");
    }

    NODE_TIMES = new double[nNodes];
    // MARKET_VALUES = new double[nNodes];
    int index = 0;

    InterestRateDerivative ird;

    for (int i = 0; i < liborMaturities.length; i++) {
      final double t = liborMaturities[i];
      final double r = liborRates[i];
      ird = new Libor(t, r, CURVE_NAME);
      INSTRUMENTS.add(ird);
      NODE_TIMES[index++] = t;
    }
    for (int i = 0; i < fraMaturities.length; i++) {
      final double t = fraMaturities[i];
      final double r = fraRates[i];
      ird = new ForwardRateAgreement(t - 0.25, t, r, CURVE_NAME, CURVE_NAME);
      INSTRUMENTS.add(ird);
      NODE_TIMES[index++] = t;
    }

    for (int i = 0; i < swapMaturities.length; i++) {
      final double t = swapMaturities[i];
      final double r = swapRates[i];
      ird = setupSwap(t, r, CURVE_NAME, CURVE_NAME);
      INSTRUMENTS.add(ird);
      NODE_TIMES[index++] = t;
    }

    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.05;
    }

    X0 = new DoubleMatrix1D(rates);

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

    final LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators = new LinkedHashMap<String, Interpolator1D>();
    final LinkedHashMap<String, double[]> unknownCurveNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator> unknownCurveNodeSensitivityCalculators = new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator>();
    unknownCurveInterpolators.put(CURVE_NAME, EXTRAPOLATOR);
    unknownCurveNodes.put(CURVE_NAME, NODE_TIMES);
    unknownCurveNodeSensitivityCalculators.put(CURVE_NAME, EXTRAPOLATOR_WITH_SENSITIVITY);
    SINGLE_CURVE_FINDER = new MultipleYieldCurveFinderFunction(INSTRUMENTS, unknownCurveNodes, unknownCurveInterpolators, unknownCurveNodeSensitivityCalculators, null, CALCULATOR);
    SINGLE_CURVE_JACOBIAN = new MultipleYieldCurveFinderJacobian(INSTRUMENTS, unknownCurveNodes, unknownCurveInterpolators, unknownCurveNodeSensitivityCalculators, null, SENSITIVITY_CALCULATOR);
  }

  @Test
  public void testNewton() {
    final VectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doTest(rootFinder, SINGLE_CURVE_FINDER);
  }

  @Test
  public void testBroyden() {
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doTest(rootFinder, SINGLE_CURVE_FINDER);
  }

  @Test
  public void testShermanMorrison() {
    final VectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doTest(rootFinder, SINGLE_CURVE_FINDER);
  }

  private void doTest(final VectorRootFinder rootFinder, final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor) {
    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(NODE_TIMES, yieldCurveNodes.getData(), EXTRAPOLATOR);
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(CURVE_NAME, curve);
    for (final InterestRateDerivative ird : INSTRUMENTS) {
      assertEquals(0.0, CALCULATOR.getValue(ird, bundle), EPS);
    }
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

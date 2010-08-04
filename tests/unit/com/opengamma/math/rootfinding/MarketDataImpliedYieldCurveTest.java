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
import com.opengamma.financial.interestrate.ParRateCalculator;
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
import com.opengamma.math.interpolation.CubicSplineInterpolatorWithSensitivities1D;
import com.opengamma.math.interpolation.Extrapolator1D;
import com.opengamma.math.interpolation.ExtrapolatorMethod;
import com.opengamma.math.interpolation.FixedNodeInterpolator1D;
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
  private static final Interpolator1D<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> EXTRAPOLATOR_WITH_SENSITIVITY;
  private static final List<InterestRateDerivative> INSTRUMENTS;

  private static final InterestRateDerivativeVisitor<Double> CALCULATOR = PresentValueCalculator.getInstance();
  private static final InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> SENSITIVITY_CALCULATOR = PresentValueSensitivityCalculator.getInstance();
  private static final ParRateCalculator RATE_CALCULATOR = ParRateCalculator.getInstance();

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
      double t = liborMaturities[i];
      double r = liborRates[i];
      ird = new Libor(t, r, CURVE_NAME);
      INSTRUMENTS.add(ird);
      NODE_TIMES[index++] = t;
    }
    for (int i = 0; i < fraMaturities.length; i++) {
      double t = fraMaturities[i];
      double r = fraRates[i];
      ird = new ForwardRateAgreement(t - 0.25, t, r, CURVE_NAME, CURVE_NAME);
      INSTRUMENTS.add(ird);
      NODE_TIMES[index++] = t;
    }

    for (int i = 0; i < swapMaturities.length; i++) {
      double t = swapMaturities[i];
      double r = swapRates[i];
      ird = setupSwap(t, r, CURVE_NAME, CURVE_NAME);
      INSTRUMENTS.add(ird);
      NODE_TIMES[index++] = t;
    }

    double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.05;
    }

    X0 = new DoubleMatrix1D(rates);

    final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> cubicInterpolator = new NaturalCubicSplineInterpolator1D();
    final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> cubicInterpolatorWithSense = new CubicSplineInterpolatorWithSensitivities1D();
    final ExtrapolatorMethod<Interpolator1DCubicSplineDataBundle, InterpolationResult> linear_em = new LinearExtrapolator<Interpolator1DCubicSplineDataBundle, InterpolationResult>();
    final ExtrapolatorMethod<Interpolator1DCubicSplineDataBundle, InterpolationResult> flat_em = new FlatExtrapolator<Interpolator1DCubicSplineDataBundle, InterpolationResult>();
    final ExtrapolatorMethod<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> linear_em_sense = new LinearExtrapolatorWithSensitivity<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities>();
    final ExtrapolatorMethod<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> flat_em_sense = new FlatExtrapolatorWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities>();
    EXTRAPOLATOR = new Extrapolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult>(linear_em, flat_em, cubicInterpolator);
    EXTRAPOLATOR_WITH_SENSITIVITY = new Extrapolator1D<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities>(linear_em_sense, flat_em_sense,
        cubicInterpolatorWithSense);

    LinkedHashMap<String, FixedNodeInterpolator1D> unknownCurves = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    FixedNodeInterpolator1D fnInterpolator = new FixedNodeInterpolator1D(NODE_TIMES, EXTRAPOLATOR);
    unknownCurves.put(CURVE_NAME, fnInterpolator);
    SINGLE_CURVE_FINDER = new MultipleYieldCurveFinderFunction(INSTRUMENTS, unknownCurves, null, CALCULATOR);

    unknownCurves = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    fnInterpolator = new FixedNodeInterpolator1D(NODE_TIMES, EXTRAPOLATOR_WITH_SENSITIVITY);
    unknownCurves.put(CURVE_NAME, fnInterpolator);
    SINGLE_CURVE_JACOBIAN = new MultipleYieldCurveFinderJacobian(INSTRUMENTS, unknownCurves, null, SENSITIVITY_CALCULATOR);

  }

  @Test
  public void testNewton() {
    VectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doTest(rootFinder, SINGLE_CURVE_FINDER);
  }

  @Test
  public void testBroyden() {
    VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doTest(rootFinder, SINGLE_CURVE_FINDER);
  }

  @Test
  public void testShermanMorrison() {
    VectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doTest(rootFinder, SINGLE_CURVE_FINDER);
  }

  private void doTest(final VectorRootFinder rootFinder, Function1D<DoubleMatrix1D, DoubleMatrix1D> functor) {
    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(NODE_TIMES, yieldCurveNodes.getData(), EXTRAPOLATOR);
    // System.out.println("times: " + (new DoubleMatrix1D(NODE_TIMES)).toString());
    // System.out.println("yields: " + yieldCurveNodes.toString());
    YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(CURVE_NAME, curve);
    for (InterestRateDerivative ird : INSTRUMENTS) {
      assertEquals(0.0, CALCULATOR.getValue(ird, bundle), EPS);
    }
  }

  private static FixedFloatSwap setupSwap(final double time, final double swapRate, final String fundCurveName, final String liborCurveName) {
    int index = (int) Math.round(2 * time);
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
    ConstantCouponAnnuity fixedLeg = new ConstantCouponAnnuity(fixed, swapRate, fundCurveName);
    VariableAnnuity floatingLeg = new VariableAnnuity(floating, 1.0, deltaStart, deltaEnd, fundCurveName, liborCurveName);
    return new FixedFloatSwap(fixedLeg, floatingLeg);
  }
}

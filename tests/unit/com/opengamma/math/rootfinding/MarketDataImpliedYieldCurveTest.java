/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.SingleCurveFinder;
import com.opengamma.financial.interestrate.SingleCurveJacobian;
import com.opengamma.financial.interestrate.SwapRateCalculator;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CubicSplineInterpolatorWithSensitivities1D;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineDataBundle;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineWithSensitivitiesDataBundle;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.math.rootfinding.newton.ShermanMorrisonVectorRootFinder;

/**
 * 
 */
public class MarketDataImpliedYieldCurveTest {
  private static final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> CUBIC = new NaturalCubicSplineInterpolator1D();
  private static final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> CUBIC_WITH_SENSITIVITY = new CubicSplineInterpolatorWithSensitivities1D();
  private static final List<InterestRateDerivative> INSTRUMENTS;
  private static final double[] MARKET_VALUES;
  private static final SwapRateCalculator RATE_CALCULATOR = new SwapRateCalculator();

  private static final double[] NODE_TIMES;
  private static final double SPOT_RATE;
  private static final double EPS = 1e-8;
  private static final int STEPS = 100;
  private static final DoubleMatrix1D X0;

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
    MARKET_VALUES = new double[nNodes];
    int index = 0;

    InterestRateDerivative ird;

    for (int i = 0; i < liborMaturities.length; i++) {
      double t = liborMaturities[i];
      double r = liborRates[i];
      ird = new Libor(t);
      INSTRUMENTS.add(ird);
      NODE_TIMES[index] = t;
      MARKET_VALUES[index++] = r;
    }
    for (int i = 0; i < fraMaturities.length; i++) {
      double t = fraMaturities[i];
      double r = fraRates[i];
      ird = new ForwardRateAgreement(t - 0.25, t);
      INSTRUMENTS.add(ird);
      NODE_TIMES[index] = t;
      MARKET_VALUES[index++] = r;
    }

    for (int i = 0; i < swapMaturities.length; i++) {
      double t = swapMaturities[i];
      double r = swapRates[i];
      ird = setupSwap(t);
      INSTRUMENTS.add(ird);
      NODE_TIMES[index] = t;
      MARKET_VALUES[index++] = r;
    }

    double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.05;
    }

    X0 = new DoubleMatrix1D(rates);
    SPOT_RATE = liborRates[0];

    SINGLE_CURVE_FINDER = new SingleCurveFinder(INSTRUMENTS, MARKET_VALUES, SPOT_RATE, NODE_TIMES, CUBIC);
    SINGLE_CURVE_JACOBIAN = new SingleCurveJacobian<Interpolator1DCubicSplineWithSensitivitiesDataBundle>(INSTRUMENTS, SPOT_RATE, NODE_TIMES, CUBIC_WITH_SENSITIVITY);

  }

  @Test
  public void testNewton() {
    VectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doTest(rootFinder, (SingleCurveFinder) SINGLE_CURVE_FINDER);
  }

  @Test
  public void testBroyden() {
    VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doTest(rootFinder, (SingleCurveFinder) SINGLE_CURVE_FINDER);
  }

  @Test
  public void testShermanMorrison() {
    VectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS, SINGLE_CURVE_JACOBIAN);
    doTest(rootFinder, (SingleCurveFinder) SINGLE_CURVE_FINDER);
  }

  private void doTest(final VectorRootFinder rootFinder, final SingleCurveFinder functor) {
    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final YieldAndDiscountCurve curve = makeYieldCurve(yieldCurveNodes.getData(), NODE_TIMES, CUBIC);
    // System.out.println("times: " + (new DoubleMatrix1D(NODE_TIMES)).toString());
    // System.out.println("yields: " + yieldCurveNodes.toString());
    for (int i = 0; i < MARKET_VALUES.length; i++) {
      assertEquals(MARKET_VALUES[i], RATE_CALCULATOR.getRate(curve, curve, INSTRUMENTS.get(i)), EPS);
    }
  }

  private static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times, final Interpolator1D<? extends Interpolator1DCubicSplineDataBundle, InterpolationResult> interpolator) {
    final int n = yields.length;
    if (n != times.length) {
      throw new IllegalArgumentException("rates and times different lengths");
    }
    final double[] t = new double[n + 1];
    final double[] y = new double[n + 1];
    t[0] = 0;
    y[0] = SPOT_RATE;
    for (int i = 0; i < n; i++) {
      t[i + 1] = times[i];
      y[i + 1] = yields[i];
    }
    return new InterpolatedYieldCurve(t, y, interpolator);
  }

  private static Swap setupSwap(final double t) {
    int payments = (int) Math.round(2 * t);
    final double[] fixed = new double[payments];
    final double[] floating = new double[2 * payments];
    final double[] deltaStart = new double[2 * payments];
    final double[] deltaEnd = new double[2 * payments];
    for (int i = 0; i < payments - 1; i++) {
      fixed[i] = 0.5 * (1 + i);
      floating[2 * i + 1] = fixed[i];
    }
    fixed[payments - 1] = t;
    floating[2 * payments - 1] = t;
    for (int i = 0; i < 2 * payments; i++) {
      if (i % 2 == 0) {
        floating[i] = 0.25 * (1 + i);
      }
      deltaStart[i] = 0.0;
      deltaEnd[i] = 0.0;
    }
    return new Swap(fixed, floating, deltaStart, deltaEnd);
  }

}

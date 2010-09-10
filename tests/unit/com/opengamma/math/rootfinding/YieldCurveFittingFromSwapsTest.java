/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.math.rootfinding.newton.ShermanMorrisonVectorRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class YieldCurveFittingFromSwapsTest extends YieldCurveFittingSetup {
  private double[] _swapValues;

  public YieldCurveFittingFromSwapsTest() {
    _logger = LoggerFactory.getLogger(YieldCurveBootStrapTest.class);
    _hotspotWarmupCycles = 0;
    _benchmarkCycles = 1;
    _interolatorName = Interpolator1DFactory.NATURAL_CUBIC_SPLINE;
    _marketValueCalculator = PAR_RATE_DIFFERENCE_CALCULATOR;
    _marketValueSensitivityCalculator = PAR_RATE_SENSITIVITY_CALCULATOR;
    setupExtrapolator();
    setupSingleCurveInstruments();
    setupSingleCurveFinder();
    //setupDoubleCurveInstruments();
    //setupDoubleCurveFinder();
  }

  @Before
  public void setUp() {

  }

  @Override
  protected void setupSingleCurveInstruments() {

    _curve1Knots = new double[] {0.5, 1.00, 1.5, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556};
    final double[] yields = {0.01, 0.015, 0.02, 0.03, 0.04, 0.05, 0.052, 0.049, 0.045, 0.044, 0.043, 0.041, 0.04};

    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    _curve1 = makeYieldCurve(yields, _curve1Knots, EXTRAPOLATOR);
    curveBundle.setCurve(_curve1Name, _curve1);

    final int n = _curve1Knots.length;

    InterestRateDerivative instrument;
    _swapValues = new double[n];
    for (int i = 0; i < n; i++) {
      instrument = setupSwap(_curve1Knots[i], _curve1Name, _curve1Name);
      _swapValues[i] = PAR_RATE_CALCULATOR.getValue(instrument, curveBundle);
      instrument = setParSwapRate((FixedFloatSwap) instrument, _swapValues[i]);
      SINGLE_CURVE_INSTRUMENTS.add(instrument);
    }

    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      rates[i] = 0.05;
    }

    _startPosition = new DoubleMatrix1D(rates);

  }

  @Override
  protected void setupDoubleCurveInstruments() {
    _curve1Knots = new double[] {1, 2, 5, 10, 20, 31};
    _curve2Knots = new double[] {0.5, 1, 2, 5, 10, 20, 31};
    _curve1Yields = new double[] {0.021, 0.036, 0.06, 0.054, 0.049, 0.044};
    _curve2Yields = new double[] {0.01, 0.02, 0.035, 0.06, 0.055, 0.05, 0.045};

    final int[] payments = new int[] {1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60};

    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    _curve1 = makeYieldCurve(_curve1Yields, _curve1Knots, EXTRAPOLATOR);
    _curve2 = makeYieldCurve(_curve2Yields, _curve2Knots, EXTRAPOLATOR);
    curveBundle.setCurve(_curve1Name, _curve1);
    curveBundle.setCurve(_curve2Name, _curve2);

    InterestRateDerivative instrument;
    double swapRate;
    final int n = payments.length;
    for (int i = 0; i < n; i++) {
      instrument = setupSwap(payments[i], _curve1Name, _curve2Name);
      swapRate = PAR_RATE_CALCULATOR.getValue(instrument, curveBundle);
      instrument = setParSwapRate((FixedFloatSwap) instrument, swapRate);
      DOUBLE_CURVE_INSTRUMENTS.add(instrument);
    }

    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      rates[i] = 0.05;
    }

    _startPosition = new DoubleMatrix1D(rates);

  }

  @Test
  public void testNewton() {
    final NewtonVectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS);
    testRootFindingMethods(rootFinder, "Newton");
  }

  @Test
  public void testShermanMorrison() {
    final NewtonVectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
    testRootFindingMethods(rootFinder, "ShermanMorrison");
  }

  @Test
  public void testBroyden() {
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    testRootFindingMethods(rootFinder, "Broyden");
  }

  @Test
  public void testTickingForwardRates() {
    final NormalDistribution normDist = new NormalDistribution(0, 1.0, RANDOM);
    final double sigma = 0.3;
    final double dt = 1. / 12.;
    final double rootdt = Math.sqrt(dt);

    final double tol = 1e-8;
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(tol, tol, STEPS);
    final int n = _curve1Knots.length;
    final double[] yields = new double[n];
    final double[] forwards = new double[n];
    DoubleMatrix1D yieldCurveNodes = _startPosition;

    for (int i = 0; i < n; i++) {
      forwards[i] = 0.01 + _curve1Knots[i] * 0.002;//start with upward sloping forward curve 
    }

    for (int t = 0; t < 120; t++) {
      final List<InterestRateDerivative> instruments = new ArrayList<InterestRateDerivative>();
      double sum = 0;
      for (int i = 0; i < n; i++) {
        forwards[i] *= Math.exp(sigma * rootdt * normDist.nextRandom() - sigma * sigma / 2.0 * dt);
        sum += forwards[i] * (_curve1Knots[i] - (i == 0 ? 0.0 : _curve1Knots[i - 1]));
        yields[i] = sum / _curve1Knots[i];
      }

      final YieldCurveBundle curveBundle = new YieldCurveBundle();
      final YieldAndDiscountCurve curve = makeYieldCurve(yields, _curve1Knots, EXTRAPOLATOR);
      curveBundle.setCurve(_curve1Name, curve);

      InterestRateDerivative instrument;
      final double[] swapRates = new double[n];
      for (int i = 0; i < n; i++) {
        instrument = setupSwap(_curve1Knots[i], _curve1Name, _curve1Name);
        swapRates[i] = PAR_RATE_CALCULATOR.getValue(instrument, curveBundle);
        instrument = setParSwapRate((FixedFloatSwap) instrument, swapRates[i]);
        instruments.add(instrument);
      }
      final MultipleYieldCurveFinderDataBundle data = getSingleYieldCurveFinderDataBundle(instruments, EXTRAPOLATOR, EXTRAPOLATOR_WITH_SENSITIVITY);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new MultipleYieldCurveFinderFunction(data, _marketValueCalculator);

      yieldCurveNodes = rootFinder.getRoot(functor, yieldCurveNodes);
      for (int i = 0; i < n; i++) {
        assertEquals(yields[i], yieldCurveNodes.getEntry(i), 1e-4);
      }
    }
  }

}

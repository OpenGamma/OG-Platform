/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DWithSecondDerivativeModel;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class YieldCurveBootStrapTest {
  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveBootStrapTest.class);
  private static final int HOTSPOT_WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Interpolator1D<? extends Interpolator1DWithSecondDerivativeModel> INTERPOLATOR = new NaturalCubicSplineInterpolator1D();
  //private static final Interpolator1D<Interpolator1DModel> INTERPOLATOR = new LinearInterpolator1D();
  protected List<ToySwap> _swaps;
  protected double[] _swapValues;
  private final double[] _timeGrid;
  final DoubleMatrix1D _x0;

  public YieldCurveBootStrapTest() {
    final int[] payments = new int[] {1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60};
    _swapValues = new double[] {0.03, 0.035, 0.04, 0.043, 0.045, 0.045, 0.04, 0.035, 0.033, 0.034, 0.036, 0.039, 0.042};
    final int n = payments.length;
    _timeGrid = new double[n];
    _swaps = new ArrayList<ToySwap>();
    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      _swaps.add(setupSwap(payments[i]));
      _timeGrid[i] = _swaps.get(i).getLastPaymentTime();
      rates[i] = 0.05;
    }

    _x0 = new DoubleMatrix1D(rates);
  }

  @Test
  public void doNothing() {

  }

  @Test
  public void testNewton() {
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder();
    doTest(rootFinder);
  }

  @Test
  public void TestShermanMorrison() {
    final VectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder();
    doTest(rootFinder);
  }

  @Test
  public void TestBroyden() {
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder();
    doTest(rootFinder);
  }

  private void doTest(final VectorRootFinder rootFinder) {

    for (int i = 0; i < HOTSPOT_WARMUP_CYCLES; i++) {
      final DoubleMatrix1D x1 = rootFinder.getRoot(ROOT, _x0);
    }

    final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on newton", BENCHMARK_CYCLES);
    for (int i = 0; i < BENCHMARK_CYCLES; i++) {
      final DoubleMatrix1D x1 = rootFinder.getRoot(ROOT, _x0);
    }
    timer.finished();

  }

  private final Function1D<DoubleMatrix1D, DoubleMatrix1D> ROOT = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final Map<Double, Double> data = new HashMap<Double, Double>();
      data.put(0.0, 0.03);
      for (int i = 0; i < _timeGrid.length; i++) {
        data.put(_timeGrid[i], x.getEntry(i));
      }
      final YieldAndDiscountCurve curve = new InterpolatedDiscountCurve(data, INTERPOLATOR);

      final double[] res = new double[_swapValues.length];
      for (int i = 0; i < _swapValues.length; i++) {
        res[i] = _swapValues[i] - _swaps.get(i).getSwapRate(curve);
      }
      return new DoubleMatrix1D(res);
    }

  };

  private ToySwap setupSwap(final int payments) {
    final double[] fixed = new double[payments];
    final double[] floating = new double[2 * payments];
    final LinkedList<Pair<Double, Double>> liborSetResetTimes = new LinkedList<Pair<Double, Double>>();

    for (int i = 0; i < payments; i++) {
      floating[2 * i + 1] = fixed[i] = 0.5 * (1 + i) + 0.02 * (RANDOM.nextDouble() - 0.5);
    }

    for (int i = 0; i < 2 * payments; i++) {
      if (i % 2 == 0) {
        floating[i] = 0.25 * (1 + i) + 0.02 * (RANDOM.nextDouble() - 0.5);
      }
      final Pair<Double, Double> temp = new DoublesPair(0.25 * i + 0.01 * RANDOM.nextDouble(), 0.25 * (i + 1) + 0.01 * RANDOM.nextDouble());
      liborSetResetTimes.add(temp);
    }

    return new ToySwap(fixed, floating, liborSetResetTimes);
  }

  private class ToySwap {
    double[] _fixedPaymentTimes;
    double[] _floatPaymentTimes;
    LinkedList<Pair<Double, Double>> _liborSetResetTimes;

    public ToySwap(final double[] fixedPaymentDates, final double[] floatingPaymentDates, final LinkedList<Pair<Double, Double>> liborSetResetTimes) {
      _fixedPaymentTimes = fixedPaymentDates;
      if (floatingPaymentDates.length != liborSetResetTimes.size()) {
        throw new IllegalArgumentException("list of floatingPaymentDates not the same length as liborSetResetTimes");
      }
      _floatPaymentTimes = floatingPaymentDates;
      _liborSetResetTimes = liborSetResetTimes;
    }

    public double getSwapRate(final YieldAndDiscountCurve curve) {
      double fixed = _fixedPaymentTimes[0] * curve.getDiscountFactor(_fixedPaymentTimes[0]);
      for (int i = 1; i < _fixedPaymentTimes.length; i++) {
        fixed += (_fixedPaymentTimes[i] - _fixedPaymentTimes[i - 1]) * curve.getDiscountFactor(_fixedPaymentTimes[i]);
      }
      double floating = 0.0;
      double libor, ta, tb;
      for (int i = 0; i < _floatPaymentTimes.length; i++) {
        ta = _liborSetResetTimes.get(i).getFirst();
        tb = _liborSetResetTimes.get(i).getSecond();
        libor = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) - 1.0;
        floating += libor * curve.getDiscountFactor(_floatPaymentTimes[i]);
      }

      return floating / fixed;
    }

    public double getLastPaymentTime() {
      return Math.max(_fixedPaymentTimes[_fixedPaymentTimes.length - 1], _liborSetResetTimes.getLast().getSecond());
    }

  }

}

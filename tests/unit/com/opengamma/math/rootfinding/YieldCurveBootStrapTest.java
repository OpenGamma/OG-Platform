/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DModel;
import com.opengamma.math.interpolation.Interpolator1DWithSecondDerivativeModel;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
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
  protected static final Interpolator1D<? extends Interpolator1DWithSecondDerivativeModel> CUBIC = new NaturalCubicSplineInterpolator1D();
  protected static final Interpolator1D<Interpolator1DModel> LINEAR = new LinearInterpolator1D();
  protected static List<ToySwap> SWAPS;
  protected static double[] SWAP_VALUES;
  protected static final double[] TIME_GRID;
  protected static final double SPOT_RATE;
  protected static final double EPS = 1e-8;
  protected static final int STEPS = 100;
  protected static final DoubleMatrix1D X0;

  static {
    final int[] payments = new int[] { 1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60 };
    SWAP_VALUES = new double[] { 0.03, 0.035, 0.04, 0.043, 0.045, 0.045, 0.04, 0.035, 0.033, 0.034, 0.036, 0.039, 0.042 };
    SPOT_RATE = 0.03;
    final int n = payments.length;
    TIME_GRID = new double[n];
    SWAPS = new ArrayList<ToySwap>();
    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      SWAPS.add(setupSwap(payments[i]));
      TIME_GRID[i] = SWAPS.get(i).getLastPaymentTime();
      rates[i] = 0.05;
    }
    X0 = new DoubleMatrix1D(rates);
  }

  @Test
  public void doNothing() {

  }

  @Test
  public void testNewton() {
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder);
  }

  @Test
  public void testShermanMorrison() {
    final VectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder);
  }

  @Test
  public void testBroyden() {
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder);
  }

  private void doHotSpot(final VectorRootFinder rootFinder) {
    for (int i = 0; i < HOTSPOT_WARMUP_CYCLES; i++) {
      doTest(rootFinder);
    }

    final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on newton", BENCHMARK_CYCLES);
    for (int i = 0; i < BENCHMARK_CYCLES; i++) {
      doTest(rootFinder);
    }
    timer.finished();

  }

  private void doTest(final VectorRootFinder rootFinder) {
    Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new RootFunctor(SWAPS, SWAP_VALUES, SPOT_RATE, TIME_GRID,
        CUBIC);
    DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
    data.put(0.0, SPOT_RATE);
    for (int i = 0; i < TIME_GRID.length; i++) {
      data.put(TIME_GRID[i], yieldCurveNodes.getEntry(i));
    }
    final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(data, CUBIC);
    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAPS.get(i).getSwapRate(curve), EPS);
    }
  }

  @Test
  public void testTickingSwapRates() {
    NormalDistribution normDist = new NormalDistribution(0, 1.0, RANDOM);
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    double[] swapRates = SWAP_VALUES.clone();
    DoubleMatrix1D yieldCurveNodes = X0;
    double sigma = 0.03;

    for (int t = 0; t < 100; t++) {
      for (int i = 0; i < SWAP_VALUES.length; i++) {
        swapRates[i] *= Math.exp(-0.5 * sigma * sigma + sigma * normDist.nextRandom());
      }
      Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new RootFunctor(SWAPS, swapRates, SPOT_RATE, TIME_GRID,
          CUBIC);
      yieldCurveNodes = rootFinder.getRoot(functor, yieldCurveNodes);

      final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
      data.put(0.0, SPOT_RATE);
      for (int i = 0; i < TIME_GRID.length; i++) {
        data.put(TIME_GRID[i], yieldCurveNodes.getEntry(i));
      }
      final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(data, CUBIC);
      for (int i = 0; i < swapRates.length; i++) {
        assertEquals(swapRates[i], SWAPS.get(i).getSwapRate(curve), EPS);
      }

    }

  }

  private class RootFunctor extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

    protected List<ToySwap> _swaps;
    protected double[] _swapValues;
    protected double _spotRate;
    protected final double[] _timeGrid;
    protected final Interpolator1D<? extends Interpolator1DModel> _interpolator;
    int n;

    public RootFunctor(List<ToySwap> swaps, double[] swapValues, double spotRate, double[] timeGrid,
        Interpolator1D<? extends Interpolator1DModel> cubic) {
      _swaps = swaps;
      _swapValues = swapValues;
      _spotRate = spotRate;
      _timeGrid = timeGrid;
      _interpolator = cubic;
      n = _swaps.size();

    }

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
      final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
      data.put(0.0, _spotRate);
      for (int i = 0; i < _timeGrid.length; i++) {
        data.put(_timeGrid[i], x.getEntry(i));
      }
      final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(data, _interpolator);
      final double[] res = new double[n];
      for (int i = 0; i < n; i++) {
        res[i] = _swapValues[i] - _swaps.get(i).getSwapRate(curve);
      }
      return new DoubleMatrix1D(res);
    }
  }

  private static ToySwap setupSwap(final int payments) {
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
      final Pair<Double, Double> temp = new DoublesPair(0.25 * i + 0.01 * RANDOM.nextDouble(), 0.25 * (i + 1) + 0.01
          * RANDOM.nextDouble());
      liborSetResetTimes.add(temp);
    }

    return new YieldCurveBootStrapTest().new ToySwap(fixed, floating, liborSetResetTimes);
  }

  private class ToySwap {
    double[] _fixedPaymentTimes;
    double[] _floatPaymentTimes;
    LinkedList<Pair<Double, Double>> _liborSetResetTimes;

    public ToySwap(final double[] fixedPaymentDates, final double[] floatingPaymentDates,
        final LinkedList<Pair<Double, Double>> liborSetResetTimes) {
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

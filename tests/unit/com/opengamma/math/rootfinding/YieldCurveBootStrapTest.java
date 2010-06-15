/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
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
import com.opengamma.util.ArgumentChecker;
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
  protected static YieldAndDiscountCurve FUNDING_CURVE;
  protected static YieldAndDiscountCurve FORWARD_CURVE;

  static {
    final int[] payments = new int[] { 1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60 };
    SPOT_RATE = 0.005;
    double[] fwdTimes = new double[] { 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 31.0 };
    double[] fwdYields = new double[] { 0.01, 0.02, 0.035, 0.06, 0.055, 0.05, 0.045 };
    double[] fundTimes = new double[] { 1.0, 2.0, 5.0, 10.0, 20.0, 31.0 };
    double[] fundYields = new double[] { 0.021, 0.036, 0.06, 0.054, 0.049, 0.044 };

    FORWARD_CURVE = makeYieldCurve(fwdYields, fwdTimes, CUBIC);
    FUNDING_CURVE = makeYieldCurve(fundYields, fundTimes, CUBIC);

    //    FUNDING_CURVE = new ToyYieldCurve(-0.04, 0.03, 0.3, 0.05);
    //    FORWARD_CURVE = new ToyYieldCurve(-0.03, 0.025, 0.25, 0.05);

    //  SWAP_VALUES = new double[] { 0.03, 0.035, 0.04, 0.043, 0.045, 0.045, 0.04, 0.035, 0.033, 0.034, 0.036, 0.039, 0.042 };

    final int n = payments.length;
    TIME_GRID = new double[n];
    SWAP_VALUES = new double[n];
    SWAPS = new ArrayList<ToySwap>();
    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      SWAPS.add(setupSwap(payments[i]));
      TIME_GRID[i] = SWAPS.get(i).getLastPaymentTime();
      SWAP_VALUES[i] = SWAPS.get(i).getSwapRate(FORWARD_CURVE, FUNDING_CURVE);
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

    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on newton", BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        doTest(rootFinder);
      }
      timer.finished();
    }

  }

  private void doTest(final VectorRootFinder rootFinder) {
    Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new SingleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE,
        TIME_GRID, CUBIC);
    DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final YieldAndDiscountCurve curve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);

    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAPS.get(i).getSwapRate(curve, curve), EPS);
    }
  }

  @Test
  public void testTickingSwapRates() {
    NormalDistribution normDist = new NormalDistribution(0, 1.0, RANDOM);
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS, EPS, STEPS);
    double[] swapRates = SWAP_VALUES.clone();
    DoubleMatrix1D yieldCurveNodes = X0;
    YieldAndDiscountCurve curve;
    double sigma = 0.03;

    for (int t = 0; t < 100; t++) {
      for (int i = 0; i < SWAP_VALUES.length; i++) {
        swapRates[i] *= Math.exp(-0.5 * sigma * sigma + sigma * normDist.nextRandom());
      }
      Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new SingleCurveFinder(SWAPS, swapRates, SPOT_RATE,
          TIME_GRID, CUBIC);
      yieldCurveNodes = rootFinder.getRoot(functor, yieldCurveNodes);
      curve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);

      for (int i = 0; i < swapRates.length; i++) {
        assertEquals(swapRates[i], SWAPS.get(i).getSwapRate(curve, curve), EPS);
      }

    }
  }

  @Test
  public void testForwardCurveOnly() {
    Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE,
        TIME_GRID, null, null, FUNDING_CURVE, CUBIC, null);
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);

    YieldAndDiscountCurve fwdCurve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);

    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAPS.get(i).getSwapRate(fwdCurve, FUNDING_CURVE), EPS);
    }

  }

  @Test
  public void testFundingCurveOnly() {
    Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, null,
        TIME_GRID, FORWARD_CURVE, null, null, CUBIC);
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);

    DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);

    YieldAndDiscountCurve fundCurve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);

    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAPS.get(i).getSwapRate(FORWARD_CURVE, fundCurve), EPS);
    }
  }

  @Test
  public void testFindingTwoCurves() {
    double[] fwdTimes = new double[] { 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 31.0 };
    double[] fundTimes = new double[] { 1.0, 2.0, 5.0, 10.0, 20.0, 31.0 };
    Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, fwdTimes,
        fundTimes, null, null, CUBIC, CUBIC);
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    double[] yieldCurveNodes = rootFinder.getRoot(functor, X0).getData();

    double[] fwdYields = Arrays.copyOfRange(yieldCurveNodes, 0, fwdTimes.length);
    YieldAndDiscountCurve fwdCurve = makeYieldCurve(fwdYields, fwdTimes, CUBIC);
    double[] fundYields = Arrays.copyOfRange(yieldCurveNodes, fwdTimes.length, yieldCurveNodes.length);
    YieldAndDiscountCurve fundCurve = makeYieldCurve(fundYields, fundTimes, CUBIC);

    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAPS.get(i).getSwapRate(fwdCurve, fundCurve), EPS);
    }

  }

  private static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times,
      final Interpolator1D<? extends Interpolator1DWithSecondDerivativeModel> interpolator) {
    int n = yields.length;
    if (n != times.length) {
      throw new IllegalArgumentException("rates and times different lengths");
    }
    final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
    data.put(0.0, SPOT_RATE);
    for (int i = 0; i < n; i++) {
      data.put(times[i], yields[i]);
    }
    return new InterpolatedYieldCurve(data, interpolator);
  }

  public void checkConverged(DoubleMatrix1D yieldCurveNodes, double[] fwdTimes, double[] fundTimes) {

    final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
    data.put(0.0, SPOT_RATE);
    for (int i = 0; i < TIME_GRID.length; i++) {
      data.put(TIME_GRID[i], yieldCurveNodes.getEntry(i));
    }

  }

  private class SingleCurveFinder extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

    protected List<ToySwap> _swaps;
    protected double[] _swapValues;
    protected double _spotRate;
    protected final double[] _timeGrid;
    protected final Interpolator1D<? extends Interpolator1DModel> _interpolator;
    int n;

    public SingleCurveFinder(List<ToySwap> swaps, double[] swapValues, double spotRate, double[] timeGrid,
        Interpolator1D<? extends Interpolator1DModel> interpolator) {
      _swaps = swaps;
      _swapValues = swapValues;
      _spotRate = spotRate;
      _timeGrid = timeGrid;
      _interpolator = interpolator;
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
        res[i] = _swaps.get(i).getSwapRate(curve, curve) - _swapValues[i];
      }
      return new DoubleMatrix1D(res);
    }
  }

  private class DoubleCurveFinder extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

    protected List<ToySwap> _swaps;
    protected double[] _swapValues;
    protected double _spotRate;
    protected final double[] _forwardTimeGrid;
    protected final double[] _fundingTimeGrid;
    protected YieldAndDiscountCurve _fwdCurve;
    protected YieldAndDiscountCurve _fundCurve;

    protected final Interpolator1D<? extends Interpolator1DModel> _forwardInterpolator;
    protected final Interpolator1D<? extends Interpolator1DModel> _fundingInterpolator;
    int nSwaps, nFwdNodes, nFundNodes;

    /**
     * 
     * @param swaps
     * @param swapValues
     * @param spotRate
     * @param forwardTimeGrid
     * @param fundingTimeGrid
     * @param forwardCurve
     * @param fundCurve
     * @param forwardInterpolator
     * @param fundingInterpolator
     */
    public DoubleCurveFinder(List<ToySwap> swaps, double[] swapValues, double spotRate, double[] forwardTimeGrid,
        double[] fundingTimeGrid, YieldAndDiscountCurve forwardCurve, YieldAndDiscountCurve fundCurve,
        Interpolator1D<? extends Interpolator1DModel> forwardInterpolator,
        Interpolator1D<? extends Interpolator1DModel> fundingInterpolator) {
      _swaps = swaps;
      _swapValues = swapValues;
      _spotRate = spotRate;
      _forwardTimeGrid = forwardTimeGrid;
      _fundingTimeGrid = fundingTimeGrid;
      _forwardInterpolator = forwardInterpolator;
      _fundingInterpolator = fundingInterpolator;
      nSwaps = _swaps.size();
      nFwdNodes = (forwardTimeGrid == null ? 0 : forwardTimeGrid.length);
      nFundNodes = (fundingTimeGrid == null ? 0 : fundingTimeGrid.length);
      _fwdCurve = forwardCurve;
      _fundCurve = fundCurve;

      if (nSwaps != nFwdNodes + nFundNodes) {
        throw new IllegalArgumentException("total number of nodes does not much number of instruments");
      }

    }

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D x) {

      if (nFwdNodes == 0) {
        if (_fwdCurve == null) {
          _fwdCurve = new ConstantYieldCurve(_spotRate);
        }
      } else {
        final TreeMap<Double, Double> fwdData = new TreeMap<Double, Double>();
        fwdData.put(0.0, _spotRate);
        for (int i = 0; i < nFwdNodes; i++) {
          fwdData.put(_forwardTimeGrid[i], x.getEntry(i));
        }
        _fwdCurve = new InterpolatedYieldCurve(fwdData, _forwardInterpolator);
      }

      if (nFundNodes == 0) {
        if (_fundCurve == null) {
          _fundCurve = new ConstantYieldCurve(_spotRate);
        }
      } else {
        final TreeMap<Double, Double> fundData = new TreeMap<Double, Double>();
        fundData.put(0.0, _spotRate);
        for (int i = 0; i < nFundNodes; i++) {
          fundData.put(_fundingTimeGrid[i], x.getEntry(i + nFwdNodes));
        }
        _fundCurve = new InterpolatedYieldCurve(fundData, _fundingInterpolator);
      }

      final double[] res = new double[nSwaps];
      for (int i = 0; i < nSwaps; i++) {
        res[i] = _swaps.get(i).getSwapRate(_fwdCurve, _fundCurve) - _swapValues[i];
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

  private static class ToyYieldCurve extends YieldAndDiscountCurve {

    final double _a;
    final double _b;
    final double _c;
    final double _d;

    public ToyYieldCurve(final double a, final double b, final double c, final double d) {
      ArgumentChecker.notNegative(a + d, "a+d");
      ArgumentChecker.notNegative(d, "d");
      ArgumentChecker.notNegative(c, "c");
      ArgumentChecker.isTrue(b >= -c * d, "b>-d*c");

      _a = a;
      _b = b;
      _c = c;
      _d = d;

    }

    /* (non-Javadoc)
     * @see com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve#getDiscountFactor(java.lang.Double)
     */
    @Override
    public double getDiscountFactor(Double t) {
      double rate = getInterestRate(t);
      return Math.exp(-rate * t);
    }

    /* (non-Javadoc)
     * @see com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve#getInterestRate(java.lang.Double)
     */
    @Override
    public double getInterestRate(Double t) {
      return (_a + _b * t) * Math.exp(-_c * t) + _d;
    }

    /* (non-Javadoc)
     * @see com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve#getMaturities()
     */
    @Override
    public Set<Double> getMaturities() {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve#withMultipleShifts(java.util.Map)
     */
    @Override
    public YieldAndDiscountCurve withMultipleShifts(Map<Double, Double> shifts) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve#withParallelShift(java.lang.Double)
     */
    @Override
    public YieldAndDiscountCurve withParallelShift(Double shift) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve#withSingleShift(java.lang.Double, java.lang.Double)
     */
    @Override
    public YieldAndDiscountCurve withSingleShift(Double t, Double shift) {
      // TODO Auto-generated method stub
      return null;
    }

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

    /**
     * 
     * @param forwardCurve The Curve used to calculate forward LIBOR rates
     * @param fundingCurve The curve used to calculate discount factors 
     * @return
     */
    public double getSwapRate(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve) {
      double fixed = _fixedPaymentTimes[0] * fundingCurve.getDiscountFactor(_fixedPaymentTimes[0]);
      for (int i = 1; i < _fixedPaymentTimes.length; i++) {
        fixed += (_fixedPaymentTimes[i] - _fixedPaymentTimes[i - 1])
            * fundingCurve.getDiscountFactor(_fixedPaymentTimes[i]);
      }
      double floating = 0.0;
      double libor, ta, tb;
      for (int i = 0; i < _floatPaymentTimes.length; i++) {
        ta = _liborSetResetTimes.get(i).getFirst();
        tb = _liborSetResetTimes.get(i).getSecond();
        libor = forwardCurve.getDiscountFactor(ta) / forwardCurve.getDiscountFactor(tb) - 1.0;
        floating += libor * fundingCurve.getDiscountFactor(_floatPaymentTimes[i]);
      }

      return floating / fixed;
    }

    public double getLastPaymentTime() {
      return Math.max(_fixedPaymentTimes[_fixedPaymentTimes.length - 1], _liborSetResetTimes.getLast().getSecond());
    }

  }

}

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
import java.util.TreeMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CubicSplineInterpolatorWithSensitivities1D;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineWthSensitivitiesModel;
import com.opengamma.math.interpolation.Interpolator1DModel;
import com.opengamma.math.interpolation.Interpolator1DWithSecondDerivativeModel;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
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
  protected static final Interpolator1D<? extends Interpolator1DWithSecondDerivativeModel, InterpolationResult> CUBIC = new NaturalCubicSplineInterpolator1D();
  protected static final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWthSensitivitiesModel> CUBIC_WITH_SENSE = new CubicSplineInterpolatorWithSensitivities1D();
  protected static final Interpolator1D<Interpolator1DModel, InterpolationResult> LINEAR = new LinearInterpolator1D();
  protected static List<InterestRateDerivative> SWAPS;
  protected static double[] SWAP_VALUES;
  protected static final double[] TIME_GRID;
  protected static final double SPOT_RATE;
  protected static final double EPS = 1e-8;
  protected static final int STEPS = 100;
  protected static final DoubleMatrix1D X0;
  protected static YieldAndDiscountCurve FUNDING_CURVE;
  protected static YieldAndDiscountCurve FORWARD_CURVE;

  static {

    final int[] payments = new int[] {1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60};
    SPOT_RATE = 0.005;
    final double[] fwdTimes = new double[] {0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 31.0};
    final double[] fwdYields = new double[] {0.01, 0.02, 0.035, 0.06, 0.055, 0.05, 0.045};
    final double[] fundTimes = new double[] {1.0, 2.0, 5.0, 10.0, 20.0, 31.0};
    final double[] fundYields = new double[] {0.021, 0.036, 0.06, 0.054, 0.049, 0.044};

    FORWARD_CURVE = makeYieldCurve(fwdYields, fwdTimes, CUBIC);
    FUNDING_CURVE = makeYieldCurve(fundYields, fundTimes, CUBIC);

    final int n = payments.length;
    TIME_GRID = new double[n];
    SWAP_VALUES = new double[n];
    SWAPS = new ArrayList<InterestRateDerivative>();
    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      SWAPS.add(setupSwap(payments[i]));
      TIME_GRID[i] = SWAPS.get(i).getLastUsedTime();
      SWAP_VALUES[i] = SWAPS.get(i).getRate(FORWARD_CURVE, FUNDING_CURVE);
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

  /*  @Test
    public void testNewtonWithJacobian() {
      final NewtonRootFinderImpl rootFinder = new NewtonVectorRootFinder(EPS, EPS, STEPS);
      doHotSpotWithJacobian(rootFinder);
    }

    @Test
    public void testShermanMorrisonWithJacobian() {
      final NewtonRootFinderImpl rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
      doHotSpotWithJacobian(rootFinder);
    }

    @Test
    public void testBroydenWithJacobian() {
      final NewtonRootFinderImpl rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
      doHotSpotWithJacobian(rootFinder);
    }*/

  private void doHotSpot(final VectorRootFinder rootFinder) {
    for (int i = 0; i < HOTSPOT_WARMUP_CYCLES; i++) {
      doTest(rootFinder);
    }

    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on " + rootFinder.toString(), BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        doTest(rootFinder);
      }
      timer.finished();
    }

  }

  /*  private void doHotSpotWithJacobian(final NewtonRootFinderImpl rootFinder) {
      for (int i = 0; i < HOTSPOT_WARMUP_CYCLES; i++) {
        doTestWithJacobian(rootFinder);
      }

      if (BENCHMARK_CYCLES > 0) {
        final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on " + rootFinder.toString() + " with Jacobian", BENCHMARK_CYCLES);
        for (int i = 0; i < BENCHMARK_CYCLES; i++) {
          doTestWithJacobian(rootFinder);
        }
        timer.finished();
      }

    }*/

  private void doTest(final VectorRootFinder rootFinder) {

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new SingleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, TIME_GRID, CUBIC);
    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);
    final YieldAndDiscountCurve curve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);

    for (int i = 0; i < SWAP_VALUES.length; i++) {
      assertEquals(SWAP_VALUES[i], SWAPS.get(i).getRate(curve, curve), EPS);
    }
  }

  /*  private void doTestWithJacobian(final NewtonRootFinderImpl rootFinder) {

      final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new SingleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, TIME_GRID, CUBIC);
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = new SingleCurveJacobian(SWAPS, SPOT_RATE, TIME_GRID, CUBIC_WITH_SENSE);
      final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(func, jac, X0);
      final YieldAndDiscountCurve curve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);

      for (int i = 0; i < SWAP_VALUES.length; i++) {
        assertEquals(SWAP_VALUES[i], SWAPS.get(i).getRate(curve, curve), EPS);
      }
    }*/

  @Test
  public void testTickingSwapRates() {

    final NormalDistribution normDist = new NormalDistribution(0, 1.0, RANDOM);
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS, EPS, STEPS);
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
        assertEquals(swapRates[i], SWAPS.get(i).getRate(curve, curve), EPS);
      }

    }
  }

  /*  @Test
    public void testSingleCurveJacobian() {
      // double[] timeGrid = new double[] { 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5,
      // 4.0, 4.5, 5.0, 10, 20, 31 };
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new SingleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, TIME_GRID, CUBIC);
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianExact = new SingleCurveJacobian(SWAPS, SPOT_RATE, TIME_GRID, CUBIC_WITH_SENSE);
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFD = new JacobianCalculator(func);
      final DoubleMatrix2D jacExact = jacobianExact.evaluate(X0);
      final DoubleMatrix2D jacFD = jacobianFD.evaluate(X0);
      System.out.println(jacExact.toString());
      System.out.println(jacFD.toString());
    }

    @Test
    public void testDoubleCurveJacobian() {
      final double[] fwdTimes = new double[] {0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 31.0};
      final double[] fundTimes = new double[] {1.0, 2.0, 5.0, 10.0, 20.0, 31.0};

      final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, fwdTimes, fundTimes, null, null, CUBIC, CUBIC);
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianExact = new DoubleCurveJacobian(SWAPS, SPOT_RATE, fwdTimes, fundTimes, CUBIC_WITH_SENSE, CUBIC_WITH_SENSE);
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFD = new JacobianCalculator(func);
      final DoubleMatrix2D jacExact = jacobianExact.evaluate(X0);
      final DoubleMatrix2D jacFD = jacobianFD.evaluate(X0);
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
        assertEquals(SWAP_VALUES[i], SWAPS.get(i).getRate(fwdCurve, FUNDING_CURVE), EPS);
      }

    }

    @Test
    public void testFundingCurveOnly() {
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new DoubleCurveFinder(SWAPS, SWAP_VALUES, SPOT_RATE, null, TIME_GRID, FORWARD_CURVE, null, null, CUBIC);
      final VectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);

      final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(functor, X0);

      final YieldAndDiscountCurve fundCurve = makeYieldCurve(yieldCurveNodes.getData(), TIME_GRID, CUBIC);

      for (int i = 0; i < SWAP_VALUES.length; i++) {
        assertEquals(SWAP_VALUES[i], SWAPS.get(i).getRate(FORWARD_CURVE, fundCurve), EPS);
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
        assertEquals(SWAP_VALUES[i], SWAPS.get(i).getRate(fwdCurve, fundCurve), EPS);
      }

    }*/

  private static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times,
      final Interpolator1D<? extends Interpolator1DWithSecondDerivativeModel, InterpolationResult> interpolator) {
    final int n = yields.length;
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

  public void checkConverged(final DoubleMatrix1D yieldCurveNodes, final double[] fwdTimes, final double[] fundTimes) {

    final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
    data.put(0.0, SPOT_RATE);
    for (int i = 0; i < TIME_GRID.length; i++) {
      data.put(TIME_GRID[i], yieldCurveNodes.getEntry(i));
    }

  }

  private class SingleCurveFinder extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

    protected List<InterestRateDerivative> _irds;
    protected double[] _marketRates;
    protected double _spotRate;
    protected final double[] _timeGrid;
    protected final Interpolator1D<? extends Interpolator1DModel, InterpolationResult> _interpolator;
    int n;

    public SingleCurveFinder(final List<InterestRateDerivative> irds, final double[] marketRates, final double spotRate, final double[] timeGrid,
        final Interpolator1D<? extends Interpolator1DModel, InterpolationResult> interpolator) {

      _irds = irds;
      _marketRates = marketRates;
      _spotRate = spotRate;
      _timeGrid = timeGrid;
      _interpolator = interpolator;
      n = _irds.size();

    }

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
      data.put(0.0, _spotRate);
      for (int i = 0; i < _timeGrid.length; i++) {
        data.put(_timeGrid[i], x.getEntry(i));
      }
      final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(data, _interpolator);
      final double[] res = new double[n];
      for (int i = 0; i < n; i++) {
        res[i] = _irds.get(i).getRate(curve, curve) - _marketRates[i];
      }
      return new DoubleMatrix1D(res);
    }
  }

  private class SingleCurveJacobian extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

    protected List<InterestRateDerivative> _irds;
    protected double _spotRate;
    protected final double[] _timeGrid;
    protected final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWthSensitivitiesModel> _interpolator;
    int _nRows, _nCols;

    public SingleCurveJacobian(final List<InterestRateDerivative> irds, final double spotRate, final double[] timeGrid,
        final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWthSensitivitiesModel> interpolator) {

      _irds = irds;
      _spotRate = spotRate;
      _timeGrid = timeGrid;
      _interpolator = interpolator;
      _nRows = _nCols = _irds.size();
    }

    @Override
    public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
      final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
      data.put(0.0, _spotRate);
      for (int i = 0; i < _timeGrid.length; i++) {
        data.put(_timeGrid[i], x.getEntry(i));
      }
      final InterpolatedYieldAndDiscountCurve curve = new InterpolatedYieldCurve(data, _interpolator);
      final Interpolator1DCubicSplineWthSensitivitiesModel model = (Interpolator1DCubicSplineWthSensitivitiesModel) curve.getModels().values().iterator().next();

      final double[][] res = new double[_nRows][_nCols];
      for (int i = 0; i < _nRows; i++) {
        final ToySwap swap = (ToySwap) _irds.get(i);
        final List<Pair<Double, Double>> fwdSense = swap.getForwardCurveSensitivities(curve, curve);
        final List<Pair<Double, Double>> fundSense = swap.getFundingCurveSensitivities(curve, curve);
        final int n = fwdSense.size() + fundSense.size();
        final double[][] sense = new double[n][];
        int k = 0;
        for (final Pair<Double, Double> timeAndDF : fwdSense) {
          sense[k++] = _interpolator.interpolate(model, timeAndDF.getFirst()).getSensitivities();
        }
        for (final Pair<Double, Double> timeAndDF : fundSense) {
          sense[k++] = _interpolator.interpolate(model, timeAndDF.getFirst()).getSensitivities();
        }
        for (int j = 0; j < _nCols; j++) {
          double temp = 0.0;
          k = 0;
          for (final Pair<Double, Double> timeAndDF : fwdSense) {
            temp += timeAndDF.getSecond() * sense[k++][j + 1];
          }
          for (final Pair<Double, Double> timeAndDF : fundSense) {
            temp += timeAndDF.getSecond() * sense[k++][j + 1];
          }
          res[i][j] = temp;
        }

      }
      return new DoubleMatrix2D(res);
    }

  }

  private class DoubleCurveJacobian extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

    protected List<InterestRateDerivative> _irds;
    protected double _spotRate;
    protected final double[] _fwdTimeGrid;
    protected final double[] _fundTimeGrid;
    protected final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWthSensitivitiesModel> _fwdInterpolator;
    protected final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWthSensitivitiesModel> _fundInterpolator;
    int nSwaps, _nFwdNodes, _nFundNodes;

    public DoubleCurveJacobian(final List<InterestRateDerivative> irds, final double spotRate, final double[] forwardTimeGrid, final double[] fundingTimeGrid,
        final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWthSensitivitiesModel> fwdInterpolator,
        final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWthSensitivitiesModel> fundingInterpolator) {

      _irds = irds;
      _spotRate = spotRate;
      _fwdTimeGrid = forwardTimeGrid;
      _fundTimeGrid = fundingTimeGrid;
      _fwdInterpolator = fwdInterpolator;
      _fundInterpolator = fundingInterpolator;
      nSwaps = _irds.size();
      _nFwdNodes = (forwardTimeGrid == null ? 0 : forwardTimeGrid.length);
      _nFundNodes = (fundingTimeGrid == null ? 0 : fundingTimeGrid.length);

      if (nSwaps != _nFwdNodes + _nFundNodes) {
        throw new IllegalArgumentException("total number of nodes does not much number of instruments");
      }

    }

    @Override
    public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {

      final TreeMap<Double, Double> fwdData = new TreeMap<Double, Double>();
      fwdData.put(0.0, _spotRate);
      for (int i = 0; i < _nFwdNodes; i++) {
        fwdData.put(_fwdTimeGrid[i], x.getEntry(i));
      }
      final InterpolatedYieldAndDiscountCurve fwdCurve = new InterpolatedYieldCurve(fwdData, _fwdInterpolator);

      final TreeMap<Double, Double> fundData = new TreeMap<Double, Double>();
      fundData.put(0.0, _spotRate);
      for (int i = 0; i < _nFundNodes; i++) {
        fundData.put(_fundTimeGrid[i], x.getEntry(i + _nFwdNodes));
      }
      final InterpolatedYieldAndDiscountCurve fundCurve = new InterpolatedYieldCurve(fundData, _fundInterpolator);

      final Interpolator1DCubicSplineWthSensitivitiesModel fwdModel = (Interpolator1DCubicSplineWthSensitivitiesModel) fwdCurve.getModels().values().iterator().next();
      final Interpolator1DCubicSplineWthSensitivitiesModel fundModel = (Interpolator1DCubicSplineWthSensitivitiesModel) fundCurve.getModels().values().iterator().next();

      final int n = _nFundNodes + _nFwdNodes;
      final double[][] res = new double[n][n];
      for (int i = 0; i < n; i++) {
        final ToySwap swap = (ToySwap) _irds.get(i);
        final List<Pair<Double, Double>> fwdSense = swap.getForwardCurveSensitivities(fwdCurve, fundCurve);
        final List<Pair<Double, Double>> fundSense = swap.getFundingCurveSensitivities(fwdCurve, fundCurve);

        double[][] sense = new double[fwdSense.size()][];
        int k = 0;
        for (final Pair<Double, Double> timeAndDF : fwdSense) {
          sense[k++] = _fwdInterpolator.interpolate(fwdModel, timeAndDF.getFirst()).getSensitivities();
        }
        for (int j = 0; j < _nFwdNodes; j++) {
          double temp = 0.0;
          k = 0;
          for (final Pair<Double, Double> timeAndDF : fwdSense) {
            temp += timeAndDF.getSecond() * sense[k++][j + 1];
          }
          res[i][j] = temp;
        }

        sense = new double[fundSense.size()][];
        k = 0;
        for (final Pair<Double, Double> timeAndDF : fundSense) {
          sense[k++] = _fundInterpolator.interpolate(fundModel, timeAndDF.getFirst()).getSensitivities();
        }
        for (int j = 0; j < _nFundNodes; j++) {
          double temp = 0.0;
          k = 0;
          for (final Pair<Double, Double> timeAndDF : fundSense) {
            temp += timeAndDF.getSecond() * sense[k++][j + 1];
          }
          res[i][j + _nFwdNodes] = temp;
        }

      }
      return new DoubleMatrix2D(res);
    }

  }

  private class DoubleCurveFinder extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

    protected List<InterestRateDerivative> _swaps;
    protected double[] _swapValues;
    protected double _spotRate;
    protected final double[] _forwardTimeGrid;
    protected final double[] _fundingTimeGrid;
    protected YieldAndDiscountCurve _fwdCurve;
    protected YieldAndDiscountCurve _fundCurve;

    protected final Interpolator1D<? extends Interpolator1DModel, InterpolationResult> _forwardInterpolator;
    protected final Interpolator1D<? extends Interpolator1DModel, InterpolationResult> _fundingInterpolator;
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
    public DoubleCurveFinder(final List<InterestRateDerivative> swaps, final double[] swapValues, final double spotRate, final double[] forwardTimeGrid, final double[] fundingTimeGrid,
        final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundCurve, final Interpolator1D<? extends Interpolator1DModel, InterpolationResult> forwardInterpolator,
        final Interpolator1D<? extends Interpolator1DModel, InterpolationResult> fundingInterpolator) {
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
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {

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
        res[i] = _swaps.get(i).getRate(_fwdCurve, _fundCurve) - _swapValues[i];
      }
      return new DoubleMatrix1D(res);
    }
  }

  private static ToySwap setupSwap(final int payments) {
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

    return new YieldCurveBootStrapTest().new ToySwap(fixed, floating, deltaStart, deltaEnd);
  }

  private interface InterestRateDerivative {

    abstract public double getRate(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve);

    abstract public double getLastUsedTime();
  }

  private class SimpleSwap implements InterestRateDerivative {
    final double[] _fixedPaymentTimes;
    final int _nPayments;

    public SimpleSwap(final double[] fixedPaymentTimes) {
      ArgumentChecker.notEmpty(fixedPaymentTimes, "fixedPaymentTimes must not be null or zero length");
      _fixedPaymentTimes = fixedPaymentTimes;
      _nPayments = _fixedPaymentTimes.length;
    }

    @Override
    public double getLastUsedTime() {
      return _fixedPaymentTimes[_nPayments - 1];
    }

    /**
     * Note the forward curve is never used here 
     */
    @Override
    public double getRate(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve) {
      double fixed = _fixedPaymentTimes[0] * fundingCurve.getDiscountFactor(_fixedPaymentTimes[0]);
      for (int i = 1; i < _nPayments; i++) {
        fixed += (_fixedPaymentTimes[i] - _fixedPaymentTimes[i - 1]) * fundingCurve.getDiscountFactor(_fixedPaymentTimes[i]);
      }
      final double floating = 1 - fundingCurve.getDiscountFactor(_fixedPaymentTimes[_nPayments - 1]);
      return floating / fixed;
    }
  }

  private class ToySwap implements InterestRateDerivative {
    double[] _fixedPaymentTimes;
    double[] _floatPaymentTimes;
    double[] _deltaStart;
    double[] _deltaEnd;
    double[] _fixedYearFractions;
    double[] _floatYearFractions;
    double[] _liborYearFractions;
    int _nFix;
    int _nFloat;

    public ToySwap(final double[] fixedPaymentTimes, final double[] floatingPaymentTimes, final double[] fwdStartOffsets, final double[] fwdEndOffsets) {
      ArgumentChecker.notEmpty(fixedPaymentTimes, "fixedPaymentTime must not be null or zero length");
      ArgumentChecker.notEmpty(floatingPaymentTimes, "flaotingPaymentTimes must not be null or zero length");
      ArgumentChecker.notEmpty(fwdStartOffsets, "fwdStartOffsets must not be null or zero length");
      ArgumentChecker.notEmpty(fwdEndOffsets, "fwdEndOffsets must not be null or zero length");

      _nFix = fixedPaymentTimes.length;
      _nFloat = floatingPaymentTimes.length;

      if (fwdStartOffsets.length != _nFloat || fwdEndOffsets.length != _nFloat) {
        throw new IllegalArgumentException("list of floatingPaymentTimes not the same length as Offsets");
      }

      _fixedPaymentTimes = fixedPaymentTimes;
      _floatPaymentTimes = floatingPaymentTimes;
      _deltaStart = fwdStartOffsets;
      _deltaEnd = fwdEndOffsets;

      setupDefaultYearfractions();

    }

    private void setupDefaultYearfractions() {
      _fixedYearFractions = new double[_nFix];
      _fixedYearFractions[0] = _fixedPaymentTimes[0];
      for (int i = 1; i < _nFix; i++) {
        _fixedYearFractions[i] = _fixedPaymentTimes[i] - _fixedPaymentTimes[i - 1];
      }

      _floatYearFractions = new double[_nFloat];
      _floatYearFractions[0] = _floatPaymentTimes[0];
      for (int i = 1; i < _nFloat; i++) {
        _floatYearFractions[i] = _floatPaymentTimes[i] - _floatPaymentTimes[i - 1];
      }
      _liborYearFractions = Arrays.copyOf(_floatYearFractions, _nFloat);
    }

    /**
     * 
     * @param forwardCurve The Curve used to calculate forward LIBOR rates
     * @param fundingCurve The curve used to calculate discount factors 
     * @return
     */
    public double getRate(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve) {
      final double annuity = getAnnuity(fundingCurve);
      final double floating = getFloatLeg(forwardCurve, fundingCurve);
      return floating / annuity;
    }

    public double getLastUsedTime() {
      return Math.max(_fixedPaymentTimes[_nFix - 1], _floatPaymentTimes[_nFloat - 1] + _deltaEnd[_nFloat - 1]);
    }

    private double getAnnuity(final YieldAndDiscountCurve fundingCurve) {
      double fixed = 0.0;
      for (int i = 0; i < _nFix; i++) {
        fixed += _fixedYearFractions[i] * fundingCurve.getDiscountFactor(_fixedPaymentTimes[i]);
      }
      return fixed;
    }

    private double getFloatLeg(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve) {
      double floating = 0.0;
      final double[] libors = getLibors(forwardCurve);

      for (int i = 0; i < _nFloat; i++) {
        floating += _floatYearFractions[i] * libors[i] * fundingCurve.getDiscountFactor(_floatPaymentTimes[i]);
      }
      return floating;
    }

    private double[] getLibors(final YieldAndDiscountCurve forwardCurve) {
      final double[] libors = new double[_nFloat];
      double ta, tb;

      for (int i = 0; i < _nFloat; i++) {
        ta = (i == 0 ? 0.0 : _floatPaymentTimes[i - 1]) + _deltaStart[i];
        tb = _floatPaymentTimes[i] + _deltaEnd[i];
        libors[i] = (forwardCurve.getDiscountFactor(ta) / forwardCurve.getDiscountFactor(tb) - 1.0) / _liborYearFractions[i];
      }
      return libors;
    }

    public LinkedList<Pair<Double, Double>> getFundingCurveSensitivities(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve) {

      final double[] libors = getLibors(forwardCurve);
      final double annuity = getAnnuity(fundingCurve);
      final double floating = getFloatLeg(forwardCurve, fundingCurve);

      final double floatOverAnnSq = -floating / annuity / annuity;
      final LinkedList<Pair<Double, Double>> results = new LinkedList<Pair<Double, Double>>();
      Pair<Double, Double> temp;

      for (int i = 0; i < _nFix; i++) {
        temp = new DoublesPair(_fixedPaymentTimes[i], -_fixedPaymentTimes[i] * fundingCurve.getDiscountFactor(_fixedPaymentTimes[i]) * _fixedYearFractions[i] * floatOverAnnSq);
        results.add(temp);
      }
      for (int i = 0; i < _nFloat; i++) {
        temp = new DoublesPair(_floatPaymentTimes[i], -_floatPaymentTimes[i] * fundingCurve.getDiscountFactor(_floatPaymentTimes[i]) * libors[i] * _floatYearFractions[i] / annuity);
        results.add(temp);
      }
      return results;

    }

    public LinkedList<Pair<Double, Double>> getForwardCurveSensitivities(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve) {

      final double annuity = getAnnuity(fundingCurve);

      final LinkedList<Pair<Double, Double>> results = new LinkedList<Pair<Double, Double>>();

      double ta, tb;
      double temp1;
      double modDF, dfta, dftb;
      Pair<Double, Double> temp;

      for (int i = 0; i < _nFloat; i++) {
        ta = (i == 0 ? 0.0 : _floatPaymentTimes[i - 1]) + _deltaStart[i];
        tb = _floatPaymentTimes[i] + _deltaEnd[i];
        modDF = _floatYearFractions[i] / _liborYearFractions[i] * fundingCurve.getDiscountFactor(_floatPaymentTimes[i]);
        dfta = forwardCurve.getDiscountFactor(ta);
        dftb = forwardCurve.getDiscountFactor(tb);
        temp1 = modDF * dfta / dftb / annuity;

        temp = new DoublesPair(ta, -ta * temp1);
        results.add(temp);

        temp = new DoublesPair(tb, tb * temp1);
        results.add(temp);
      }
      return results;
    }

  }

}

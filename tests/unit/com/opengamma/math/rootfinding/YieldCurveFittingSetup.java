/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.ParRateDifferenceCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public abstract class YieldCurveFittingSetup {
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  protected static final InterestRateDerivativeVisitor<Double> PAR_RATE_CALCULATOR = ParRateCalculator.getInstance();
  protected static final InterestRateDerivativeVisitor<Double> PAR_RATE_DIFFERENCE_CALCULATOR = ParRateDifferenceCalculator
      .getInstance();
  protected static final InterestRateDerivativeVisitor<Map<String, List<DoublesPair>>> PAR_RATE_SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator
      .getInstance();
  protected static final InterestRateDerivativeVisitor<Double> PV_CALCULATOR = PresentValueCalculator.getInstance();
  protected static final InterestRateDerivativeVisitor<Map<String, List<DoublesPair>>> PV_SENSITIVITY_CALCULATOR = PresentValueSensitivityCalculator
      .getInstance();

  protected static final double EPS = 1e-8;
  protected static final int STEPS = 100;

  protected Logger _logger = null;
  protected int _hotspotWarmupCycles;
  protected int _benchmarkCycles;

  protected InterestRateDerivativeVisitor<Double> _marketValueCalculator = null;
  protected InterestRateDerivativeVisitor<Map<String, List<DoublesPair>>> _marketValueSensitivityCalculator = null;

  protected String _interolatorName = null;
  protected Interpolator1D<? extends Interpolator1DDataBundle> EXTRAPOLATOR;
  protected Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> EXTRAPOLATOR_WITH_SENSITIVITY;
  protected Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> EXTRAPOLATOR_WITH_FD_SENSITIVITY;

  protected List<InterestRateDerivative> SINGLE_CURVE_INSTRUMENTS;
  protected List<InterestRateDerivative> DOUBLE_CURVE_INSTRUMENTS;
  protected double[] _marketRates;
  protected final double[] _knotPoints = null;

  protected DoubleMatrix1D _startPosition = null;

  MultipleYieldCurveFinderDataBundle _yieldFinderData;
  protected Function1D<DoubleMatrix1D, DoubleMatrix1D> SINGLE_CURVE_FINDER;
  protected Function1D<DoubleMatrix1D, DoubleMatrix1D> DOUBLE_CURVE_FINDER;
  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> SINGLE_CURVE_JACOBIAN;
  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> DOUBLE_CURVE_JACOBIAN;
  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> SINGLE_CURVE_JACOBIAN_WITH_FD_INTERPOLATOR_SENSITIVITY;
  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> DOUBLE_CURVE_JACOBIAN_WITH_FD_INTERPOLATOR_SENSITIVITY;
  protected final String _curve1Name = "Curve 1";
  protected final String _curve2Name = "Curve 2";
  protected YieldAndDiscountCurve _curve1;
  protected YieldAndDiscountCurve _curve2;
  protected double[] _curve1Knots = null;
  protected double[] _curve2Knots = null;
  protected double[] _curve1Yields = null;
  protected double[] _curve2Yields = null;

  protected void setupExtrapolator() {
    SINGLE_CURVE_INSTRUMENTS = new ArrayList<InterestRateDerivative>();
    DOUBLE_CURVE_INSTRUMENTS = new ArrayList<InterestRateDerivative>();
    EXTRAPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(_interolatorName, LINEAR_EXTRAPOLATOR,
        FLAT_EXTRAPOLATOR);
    EXTRAPOLATOR_WITH_SENSITIVITY = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
        .getSensitivityCalculator(_interolatorName, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, false);
    EXTRAPOLATOR_WITH_FD_SENSITIVITY = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
        .getSensitivityCalculator(_interolatorName, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, true);

  }

  protected abstract void setupSingleCurveInstruments();

  protected abstract void setupDoubleCurveInstruments();

  protected MultipleYieldCurveFinderDataBundle getSingleYieldCurveFinderDataBundle(
      List<InterestRateDerivative> instruments, final Interpolator1D<? extends Interpolator1DDataBundle> extrapolator,
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> extrapolatorWithSense) {

    LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> unknownCurveInterpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
    LinkedHashMap<String, double[]> unknownCurveNodes = new LinkedHashMap<String, double[]>();
    LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> unknownCurveNodeSensitivityCalculators = new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();

    unknownCurveInterpolators.put(_curve1Name, extrapolator);
    unknownCurveNodes.put(_curve1Name, _curve1Knots);
    unknownCurveNodeSensitivityCalculators.put(_curve1Name, extrapolatorWithSense);
    return new MultipleYieldCurveFinderDataBundle(instruments, null, unknownCurveNodes, unknownCurveInterpolators,
        unknownCurveNodeSensitivityCalculators);
  }

  protected MultipleYieldCurveFinderDataBundle getDoubleYieldCurveFinderDataBundle(
      List<InterestRateDerivative> instruments, final Interpolator1D<? extends Interpolator1DDataBundle> extrapolator,
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> extrapolatorWithSense) {

    LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> unknownCurveInterpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
    LinkedHashMap<String, double[]> unknownCurveNodes = new LinkedHashMap<String, double[]>();
    LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> unknownCurveNodeSensitivityCalculators = new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();

    unknownCurveInterpolators.put(_curve1Name, extrapolator);
    unknownCurveInterpolators.put(_curve2Name, extrapolator);
    unknownCurveNodes.put(_curve1Name, _curve1Knots);
    unknownCurveNodes.put(_curve2Name, _curve2Knots);
    unknownCurveNodeSensitivityCalculators.put(_curve1Name, extrapolatorWithSense);
    unknownCurveNodeSensitivityCalculators.put(_curve2Name, extrapolatorWithSense);
    return new MultipleYieldCurveFinderDataBundle(instruments, null, unknownCurveNodes, unknownCurveInterpolators,
        unknownCurveNodeSensitivityCalculators);
  }

  protected void setupSingleCurveFinder() {

    MultipleYieldCurveFinderDataBundle data = getSingleYieldCurveFinderDataBundle(SINGLE_CURVE_INSTRUMENTS,
        EXTRAPOLATOR, EXTRAPOLATOR_WITH_SENSITIVITY);
    SINGLE_CURVE_FINDER = new MultipleYieldCurveFinderFunction(data, _marketValueCalculator);
    SINGLE_CURVE_JACOBIAN = new MultipleYieldCurveFinderJacobian(data, _marketValueSensitivityCalculator);

    data = getSingleYieldCurveFinderDataBundle(SINGLE_CURVE_INSTRUMENTS, EXTRAPOLATOR, EXTRAPOLATOR_WITH_FD_SENSITIVITY);
    SINGLE_CURVE_JACOBIAN_WITH_FD_INTERPOLATOR_SENSITIVITY = new MultipleYieldCurveFinderJacobian(data,
        _marketValueSensitivityCalculator);
  }

  protected void setupDoubleCurveFinder() {

    MultipleYieldCurveFinderDataBundle data = getDoubleYieldCurveFinderDataBundle(DOUBLE_CURVE_INSTRUMENTS,
        EXTRAPOLATOR, EXTRAPOLATOR_WITH_SENSITIVITY);
    DOUBLE_CURVE_FINDER = new MultipleYieldCurveFinderFunction(data, _marketValueCalculator);
    DOUBLE_CURVE_JACOBIAN = new MultipleYieldCurveFinderJacobian(data, _marketValueSensitivityCalculator);

    data = getDoubleYieldCurveFinderDataBundle(DOUBLE_CURVE_INSTRUMENTS, EXTRAPOLATOR, EXTRAPOLATOR_WITH_FD_SENSITIVITY);
    DOUBLE_CURVE_JACOBIAN_WITH_FD_INTERPOLATOR_SENSITIVITY = new MultipleYieldCurveFinderJacobian(data,
        _marketValueSensitivityCalculator);
  }

  public void testRootFindingMethods(NewtonVectorRootFinder rootFinder, String name) {
    final VectorFieldFirstOrderDifferentiator fd_jac_calculator = new VectorFieldFirstOrderDifferentiator();

    //   doHotSpot(rootFinder, name + ", single curve", SINGLE_CURVE_FINDER, SINGLE_CURVE_JACOBIAN);
    //    doHotSpot(rootFinder, name + ", single curve, finite difference", SINGLE_CURVE_FINDER, fd_jac_calculator
    //        .derivative(SINGLE_CURVE_FINDER));
    //    doHotSpot(rootFinder, name + ", single curve FD interpolator sensitivity", SINGLE_CURVE_FINDER,
    //        SINGLE_CURVE_JACOBIAN_WITH_FD_INTERPOLATOR_SENSITIVITY);

    //    doHotSpot(rootFinder, name + ", double curve", DOUBLE_CURVE_FINDER, DOUBLE_CURVE_JACOBIAN, true);
    //    doHotSpot(rootFinder, name + ", double curve, finite difference", DOUBLE_CURVE_FINDER, fd_jac_calculator
    //        .derivative(DOUBLE_CURVE_FINDER), true);
    //    doHotSpot(rootFinder, name + ", double curve FD interpolator sensitivity", DOUBLE_CURVE_FINDER,
    //        DOUBLE_CURVE_JACOBIAN_WITH_FD_INTERPOLATOR_SENSITIVITY, true);
  }

  private void doHotSpot(final NewtonVectorRootFinder rootFinder, final String name,
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor,
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFunction) {
    doHotSpot(rootFinder, name, functor, jacobianFunction, false);
  }

  private void doHotSpot(final NewtonVectorRootFinder rootFinder, final String name,
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> function,
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> JacobianFunction, final Boolean doubleCurveTest) {
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      doTest(rootFinder, function, JacobianFunction, doubleCurveTest);
    }
    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on " + name, _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        doTest(rootFinder, function, JacobianFunction, doubleCurveTest);
      }
      timer.finished();
    }
  }

  private void doTest(final NewtonVectorRootFinder rootFinder,
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> function,
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> JacobianFunction, final Boolean doubleCurveTest) {
    if (doubleCurveTest) {
      doTestForDoubleCurve(rootFinder, function, JacobianFunction);
    } else {
      doTestForSingleCurve(rootFinder, function, JacobianFunction);
    }
  }

  private void doTestForSingleCurve(final NewtonVectorRootFinder rootFinder,
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> f, final Function1D<DoubleMatrix1D, DoubleMatrix2D> j) {
    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(f, j, _startPosition);
    final DoubleMatrix1D modelMarketValueDiff = f.evaluate(yieldCurveNodes);

    for (int i = 0; i < modelMarketValueDiff.getNumberOfElements(); i++) {
      assertEquals(0.0, modelMarketValueDiff.getEntry(i), EPS);
    }
  }

  private void doTestForDoubleCurve(final NewtonVectorRootFinder rootFinder,
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> f, final Function1D<DoubleMatrix1D, DoubleMatrix2D> j) {

    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(f, j, _startPosition);
    final DoubleMatrix1D modelMarketValueDiff = f.evaluate(yieldCurveNodes);

    for (int i = 0; i < modelMarketValueDiff.getNumberOfElements(); i++) {
      assertEquals(0.0, modelMarketValueDiff.getEntry(i), EPS);
    }

    final double[] fundingYields = Arrays.copyOfRange(yieldCurveNodes.getData(), 0, _curve1Knots.length);
    final double[] liborYields = Arrays.copyOfRange(yieldCurveNodes.getData(), _curve1Knots.length, yieldCurveNodes
        .getNumberOfElements());

    //    for (int i = 0; i < FUNDING_CURVE_TIMES.length; i++) {
    //      assertEquals(FUNDING_YIELDS[i], fundingYields[i], EPS);
    //    }
    //    for (int i = 0; i < LIBOR_CURVE_TIMES.length; i++) {
    //      assertEquals(LIBOR_YIELDS[i], liborYields[i], EPS);
    //    }
    //    //
    //    final YieldAndDiscountCurve fundingCurve = makeYieldCurve(fundingYields, _curve1Knots, EXTRAPOLATOR);
    //    final YieldAndDiscountCurve liborCurve = makeYieldCurve(liborYields, _curve2Knots, EXTRAPOLATOR);
    //    final YieldCurveBundle bundle = new YieldCurveBundle();
    //    bundle.setCurve(_curve1Name, liborCurve);
    //    bundle.setCurve(_curve2Name, fundingCurve);
    //
    //    for (int i = 0; i < _marketRates.length; i++) {
    //      assertEquals(_marketRates[i], PAR_RATE_CALCULATOR.getValue(DOUBLE_CURVE_INSTRUMENTS.get(i), bundle), EPS);
    //    }
  }

  public void testSingleCurveJacobian(DoubleMatrix1D position) {
    final VectorFieldFirstOrderDifferentiator fdCal = new VectorFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFD = fdCal.derivative(SINGLE_CURVE_FINDER);
    final DoubleMatrix2D jacExact = SINGLE_CURVE_JACOBIAN.evaluate(position);
    final DoubleMatrix2D jacFDSensitivity = SINGLE_CURVE_JACOBIAN_WITH_FD_INTERPOLATOR_SENSITIVITY.evaluate(position);
    final DoubleMatrix2D jacFD = jacobianFD.evaluate(position);
    assertMatrixEquals(jacExact, jacFDSensitivity, 1e-6);
    assertMatrixEquals(jacExact, jacFD, 1e-6);
  }

  public void testDoubleCurveJacobian(DoubleMatrix1D position) {
    //    final VectorFieldFirstOrderDifferentiator fdCal = new VectorFieldFirstOrderDifferentiator();
    //    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFD = fdCal.derivative(DOUBLE_CURVE_FINDER);
    //    final DoubleMatrix2D jacExact = DOUBLE_CURVE_JACOBIAN.evaluate(position);
    //    final DoubleMatrix2D jacFDSensitivity = DOUBLE_CURVE_JACOBIAN_WITH_FD_INTERPOLATOR_SENSITIVITY.evaluate(position);
    //    final DoubleMatrix2D jacFD = jacobianFD.evaluate(position);
    //    assertMatrixEquals(jacExact, jacFDSensitivity, 1e-6);
    //    assertMatrixEquals(jacExact, jacFD, 1e-6);
  }

  protected static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    final int n = yields.length;
    if (n != times.length) {
      throw new IllegalArgumentException("rates and times different lengths");
    }
    return new InterpolatedYieldCurve(times, yields, interpolator);
  }

  protected static MultipleYieldCurveFinderDataBundle upDateInstruments(MultipleYieldCurveFinderDataBundle old,
      final List<InterestRateDerivative> instruments) {
    return new MultipleYieldCurveFinderDataBundle(instruments, old.getKnownCurves(), old.getUnknownCurveNodePoints(),
        old.getUnknownCurveInterpolators(), old.getUnknownCurveNodeSensitivityCalculators());
  }

  protected static FixedFloatSwap setParSwapRate(FixedFloatSwap swap, double rate) {
    VariableAnnuity floatingLeg = swap.getFloatingLeg();
    ConstantCouponAnnuity fixedLeg = swap.getFixedLeg();
    ConstantCouponAnnuity newLeg = new ConstantCouponAnnuity(fixedLeg.getPaymentTimes(), fixedLeg.getNotional(), rate,
        fixedLeg.getYearFractions(), fixedLeg.getFundingCurveName());
    return new FixedFloatSwap(newLeg, floatingLeg);
  }

  protected static FixedFloatSwap setupSwap(final double time, final String fundCurveName, final String liborCurveName) {
    final int index = (int) Math.round(2 * time);
    return setupSwap(index, fundCurveName, liborCurveName);
  }

  /**
   * 
   * @param payments
   * @param fundingCurveName
   * @param liborCurveName
   * @return
   */
  protected static FixedFloatSwap setupSwap(final int payments, final String fundingCurveName,
      final String liborCurveName) {
    final double[] fixed = new double[payments];
    final double[] floating = new double[2 * payments];
    final double[] indexFixing = new double[2 * payments];
    final double[] indexMaturity = new double[2 * payments];
    final double[] yearFrac = new double[2 * payments];

    final double sigma = 4.0 / 365.0;

    for (int i = 0; i < payments; i++) {
      floating[2 * i + 1] = fixed[i] = 0.5 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    for (int i = 0; i < 2 * payments; i++) {
      if (i % 2 == 0) {
        floating[i] = 0.25 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      }
      yearFrac[i] = 0.25 + sigma * (RANDOM.nextDouble() - 0.5);
      indexFixing[i] = 0.25 * i + sigma * (i == 0 ? RANDOM.nextDouble() / 2 : (RANDOM.nextDouble() - 0.5));
      indexMaturity[i] = 0.25 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    final ConstantCouponAnnuity fixedLeg = new ConstantCouponAnnuity(fixed, 0.0, fundingCurveName);
    final VariableAnnuity floatingLeg = new VariableAnnuity(floating, indexFixing, indexMaturity, yearFrac, 1.0,
        fundingCurveName, liborCurveName);
    return new FixedFloatSwap(fixedLeg, floatingLeg);
  }

  protected void assertMatrixEquals(final DoubleMatrix2D m1, final DoubleMatrix2D m2, final double eps) {
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

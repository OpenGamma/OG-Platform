/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.InterestRateDerivativeWithRate;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.definition.Libor;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.YieldCurveFittingTestDataBundle.TestType;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public abstract class YieldCurveFittingSetup {
  // CSOFF
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);

  protected static final double EPS = 1e-8;
  protected static final int STEPS = 100;

  protected Logger _logger = null;
  protected int _hotspotWarmupCycles;
  protected int _benchmarkCycles;

  protected YieldCurveFittingTestDataBundle getYieldCurveFittingTestDataBundle(final List<InterestRateDerivative> instruments, final YieldCurveBundle knownCurves, final List<String> curveNames,
      final List<double[]> curvesKnots, final Interpolator1D<? extends Interpolator1DDataBundle> extrapolator,
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> extrapolatorWithSense, final InterestRateDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator,
      final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator, final double[] marketRates, final DoubleMatrix1D startPosition,
      final List<double[]> curveYields) {

    Validate.notNull(curveNames);
    Validate.notNull(curvesKnots);
    Validate.notNull(instruments);
    Validate.notNull(extrapolator);
    Validate.notNull(extrapolatorWithSense);

    final int n = curveNames.size();
    Validate.isTrue(n == curvesKnots.size());
    int count = 0;
    for (int i = 0; i < n; i++) {
      Validate.notNull(curvesKnots.get(i));
      count += curvesKnots.get(i).length;
    }
    Validate.isTrue(count <= instruments.size(), "more nodes than instruments");

    final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> unknownCurveInterpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
    final LinkedHashMap<String, double[]> unknownCurveNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> unknownCurveNodeSensitivityCalculators = new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();

    for (int i = 0; i < n; i++) {
      unknownCurveInterpolators.put(curveNames.get(i), extrapolator);
      unknownCurveNodes.put(curveNames.get(i), curvesKnots.get(i));
      unknownCurveNodeSensitivityCalculators.put(curveNames.get(i), extrapolatorWithSense);
    }
    if (curveYields == null) {
      return new YieldCurveFittingTestDataBundle(instruments, knownCurves, unknownCurveNodes, unknownCurveInterpolators, unknownCurveNodeSensitivityCalculators, marketValueCalculator,
          marketValueSensitivityCalculator, marketRates, startPosition);
    }

    Validate.isTrue(curveYields.size() == n, "wrong number of true yields");
    final HashMap<String, double[]> yields = new HashMap<String, double[]>();
    for (int i = 0; i < n; i++) {
      yields.put(curveNames.get(i), curveYields.get(i));
    }
    return new YieldCurveFittingTestDataBundle(instruments, knownCurves, unknownCurveNodes, unknownCurveInterpolators, unknownCurveNodeSensitivityCalculators, marketValueCalculator,
        marketValueSensitivityCalculator, marketRates, startPosition, yields);
  }

  public void doHotSpot(final NewtonVectorRootFinder rootFinder, final YieldCurveFittingTestDataBundle data, final String name) {
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      doTestForCurveFinding(rootFinder, data);

    }
    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on " + name, _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {

        doTestForCurveFinding(rootFinder, data);

      }
      timer.finished();
    }
  }

  private void doTestForCurveFinding(final NewtonVectorRootFinder rootFinder, final YieldCurveFittingTestDataBundle data) {

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new MultipleYieldCurveFinderFunction(data, data.getMarketValueCalculator());
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = null;

    if (data.getTestType() == TestType.ANALYTIC_JACOBIAN) {
      jac = new MultipleYieldCurveFinderJacobian(data, data.getMarketValueSensitivityCalculator());
    } else if (data.getTestType() == TestType.FD_JACOBIAN) {
      final VectorFieldFirstOrderDifferentiator fdJacCalculator = new VectorFieldFirstOrderDifferentiator();
      jac = fdJacCalculator.derivative(func);
    } else {
      throw new IllegalArgumentException("unknown TestType " + data.getTestType());

    }

    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(func, jac, data.getStartPosition());
    final DoubleMatrix1D modelMarketValueDiff = func.evaluate(yieldCurveNodes);

    for (int i = 0; i < modelMarketValueDiff.getNumberOfElements(); i++) {
      assertEquals(0.0, modelMarketValueDiff.getEntry(i), EPS);
    }

    checkResult(yieldCurveNodes, data);
  }

  protected void checkResult(final DoubleMatrix1D yieldCurveNodes, final YieldCurveFittingTestDataBundle data) {
    final HashMap<String, double[]> yields = unpackYieldVector(data, yieldCurveNodes);

    final YieldCurveBundle bundle = new YieldCurveBundle();
    for (final String name : data.getCurveNames()) {
      final YieldAndDiscountCurve curve = makeYieldCurve(yields.get(name), data.getCurveNodePointsForCurve(name), data.getInterpolatorForCurve(name));
      bundle.setCurve(name, curve);
    }
    if (data.getKnownCurves() != null) {
      bundle.addAll(data.getKnownCurves());
    }

    // this is possibly a redundant test, especially if we are working in par-rate space (vs present value) as the very fact that
    // the root finder converged (and modelMarketValueDiff are within EPS of 0) means this will also pass
    for (int i = 0; i < data.getMarketRates().length; i++) {
      assertEquals(data.getMarketRates()[i], ParRateCalculator.getInstance().getValue(data.getDerivative(i), bundle), EPS);
    }

    // for (int i = 0; i < 40; i++) {
    // double t = 0.25 * i;
    // System.out.println(bundle.getCurve(data.getCurveNames().get(0)).getInterestRate(t));
    // }

    // this test cannot be performed when we don't know what the true yield curves are - i.e. we start from market data
    if (data.getCurveYields() != null) {
      for (final String name : data.getCurveNames()) {
        final double[] trueYields = data.getCurveYields().get(name);
        final double[] fittedYields = yields.get(name);
        for (int i = 0; i < trueYields.length; i++) {
          assertEquals(trueYields[i], fittedYields[i], EPS);
        }
      }
    }
  }

  protected HashMap<String, double[]> unpackYieldVector(final YieldCurveFittingTestDataBundle data, final DoubleMatrix1D yieldCurveNodes) {

    final HashMap<String, double[]> res = new HashMap<String, double[]>();
    int start = 0;
    int end = 0;
    for (final String name : data.getCurveNames()) {
      end += data.getCurveNodePointsForCurve(name).length;
      final double[] temp = Arrays.copyOfRange(yieldCurveNodes.getData(), start, end);
      res.put(name, temp);
      start = end;
    }

    return res;
  }

  public void testJacobian(final YieldCurveFittingTestDataBundle data) {
    final MultipleYieldCurveFinderFunction func = new MultipleYieldCurveFinderFunction(data, data.getMarketValueCalculator());
    final MultipleYieldCurveFinderJacobian jac = new MultipleYieldCurveFinderJacobian(data, data.getMarketValueSensitivityCalculator());
    final VectorFieldFirstOrderDifferentiator fdCal = new VectorFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFD = fdCal.derivative(func);
    final DoubleMatrix2D jacExact = jac.evaluate(data.getStartPosition());
    final DoubleMatrix2D jacFD = jacobianFD.evaluate(data.getStartPosition());

    // System.out.println("exact: " + jacExact);
    // System.out.println("FD: " + jacFD);
    assertMatrixEquals(jacExact, jacFD, 1e-5);

  }

  protected static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    final int n = yields.length;
    if (n != times.length) {
      throw new IllegalArgumentException("rates and times different lengths");
    }
    return new YieldCurve(InterpolatedDoublesCurve.from(times, yields, interpolator));
  }

  protected static MultipleYieldCurveFinderDataBundle updateInstruments(final MultipleYieldCurveFinderDataBundle old, final List<InterestRateDerivative> instruments) {
    return new MultipleYieldCurveFinderDataBundle(instruments, old.getKnownCurves(), old.getUnknownCurveNodePoints(), old.getUnknownCurveInterpolators(),
        old.getUnknownCurveNodeSensitivityCalculators());
  }

  protected static InterestRateDerivativeWithRate makeIRD(final String type, final double maturity, final String fundCurveName, final String indexCurveName, final double rate) {
    if ("cash".equals(type)) {
      return makeCash(maturity, fundCurveName, rate);
    } else if ("libor".equals(type)) {
      return makeLibor(maturity, indexCurveName, rate);
    } else if ("fra".equals(type)) {
      return makeFRA(maturity, fundCurveName, indexCurveName, rate);
    } else if ("future".equals(type)) {
      return makeFutrure(maturity, indexCurveName, rate);
    } else if ("swap".equals(type)) {
      return makeSwap(maturity, fundCurveName, indexCurveName, rate);
    } else if ("basisSwap".equals(type)) {
      return makeBasisSwap(maturity, fundCurveName, indexCurveName, rate);
    }
    throw new IllegalArgumentException("unknown IRD type " + type);
  }

  protected static InterestRateDerivativeWithRate makeCash(final double time, final String fundCurveName, final double rate) {
    return new Cash(time, rate, fundCurveName);
  }

  protected static InterestRateDerivativeWithRate makeLibor(final double time, final String indexCurveName, final double rate) {
    return new Libor(time, rate, indexCurveName);
  }

  protected static InterestRateDerivativeWithRate makeFRA(final double time, final String fundCurveName, final String indexCurveName, final double rate) {
    return new ForwardRateAgreement(time - 0.25, time, rate, fundCurveName, indexCurveName);
  }

  protected static InterestRateDerivativeWithRate makeFutrure(final double time, final String indexCurveName, final double rate) {
    return new InterestRateFuture(time, time + 0.25, 0.25, rate, indexCurveName);
  }

  protected static FixedFloatSwap makeSwap(final double time, final String fundCurveName, final String liborCurveName, final double rate) {
    final int index = (int) Math.round(2 * time);
    return makeSwap(index, fundCurveName, liborCurveName, rate);
  }

  protected static TenorSwap makeBasisSwap(final double time, final String fundCurveName, final String liborCurveName, final double rate) {

    final int index = (int) Math.round(4 * time);
    final double[] paymentTimes = new double[index];

    final double[] spreads = new double[index];
    final double[] indexFixing = new double[index];
    final double[] indexMaturity = new double[index];
    final double[] yearFracs = new double[index];
    for (int i = 0; i < index; i++) {
      indexFixing[i] = 0.25 * i;
      paymentTimes[i] = 0.25 * (i + 1);
      indexMaturity[i] = paymentTimes[i];
      spreads[i] = rate;
      yearFracs[i] = 0.25;
    }
    final ForwardLiborAnnuity payLeg = new ForwardLiborAnnuity(paymentTimes, 1.0, fundCurveName, fundCurveName);
    final ForwardLiborAnnuity receiveLeg = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, fundCurveName, liborCurveName);
    return new TenorSwap(payLeg, receiveLeg);
  }

  /**
   * 
   * @param payments
   * @param fundingCurveName
   * @param liborCurveName
   * @return
   */
  protected static FixedFloatSwap makeSwap(final int payments, final String fundingCurveName, final String liborCurveName, final double rate) {

    final double[] fixed = new double[payments];
    final double[] floating = new double[2 * payments];
    final double[] indexFixing = new double[2 * payments];
    final double[] indexMaturity = new double[2 * payments];
    final double[] yearFrac = new double[2 * payments];

    final double sigma = 0.0 / 365.0;

    for (int i = 0; i < payments; i++) {
      fixed[i] = 0.5 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      floating[2 * i + 1] = fixed[i];
    }
    for (int i = 0; i < 2 * payments; i++) {
      if (i % 2 == 0) {
        floating[i] = 0.25 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      }
      yearFrac[i] = 0.25 + sigma * (RANDOM.nextDouble() - 0.5);
      indexFixing[i] = 0.25 * i + sigma * (i == 0 ? RANDOM.nextDouble() / 2 : (RANDOM.nextDouble() - 0.5));
      indexMaturity[i] = 0.25 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    final FixedCouponAnnuity fixedLeg = new FixedCouponAnnuity(fixed, rate, fundingCurveName);

    final ForwardLiborAnnuity floatingLeg = new ForwardLiborAnnuity(floating, indexFixing, indexMaturity, yearFrac, 1.0, fundingCurveName, liborCurveName);
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

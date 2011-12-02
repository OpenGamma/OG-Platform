/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of
 * companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.YieldCurveFittingTestDataBundle.TestType;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.math.rootfinding.newton.ShermanMorrisonVectorRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class YieldCurveFittingFromSwapsTest extends YieldCurveFittingSetup {
  private static final Logger LOGGER = LoggerFactory.getLogger(YieldCurveFittingFromSwapsTest.class);
  private static final int WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected int getWarmupCycles() {
    return WARMUP_CYCLES;
  }

  @Override
  protected int getBenchmarkCycles() {
    return BENCHMARK_CYCLES;
  }

  @BeforeMethod
  public void setUp() {

  }

  protected YieldCurveFittingTestDataBundle getSingleCurveSetup() {

    final List<String> curveNames = new ArrayList<String>();
    final List<double[]> curveKnots = new ArrayList<double[]>();
    final List<double[]> yields = new ArrayList<double[]>();

    final String interpolatorName = Interpolator1DFactory.NATURAL_CUBIC_SPLINE;
    final int[] payments = new int[] { 1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60 };
    curveNames.add("single curve");
    curveKnots.add(new double[] { 0.5, 1.00, 1.5, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556 });
    yields.add(new double[] { 0.01, 0.015, 0.02, 0.03, 0.04, 0.05, 0.052, 0.049, 0.045, 0.044, 0.043, 0.041, 0.04 });

    final int n = payments.length;
    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      rates[i] = 0.05;
    }
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    // final InterestRateDerivativeVisitor<YieldCurveBundle, Double> calculator
    // = ParRateCalculator.getInstance();
    // final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String,
    // List<DoublesPair>>> sensitivityCalculator =
    // ParRateCurveSensitivityCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator = PresentValueCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = PresentValueCurveSensitivityCalculator.getInstance();

    return getSwapOnlySetup(payments, curveNames, null, curveKnots, yields, startPosition, interpolatorName, calculator, sensitivityCalculator);
  }

  protected YieldCurveFittingTestDataBundle getDoubleCurveSetup() {

    final List<String> curveNames = new ArrayList<String>();
    final List<double[]> curveKnots = new ArrayList<double[]>();
    final List<double[]> yields = new ArrayList<double[]>();

    final String interpolatorName = Interpolator1DFactory.NATURAL_CUBIC_SPLINE;
    final int[] payments = new int[] { 1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60 };
    curveNames.add("funding");
    curveNames.add("Libor");
    curveKnots.add(new double[] { 1, 2, 5, 10, 20, 31 });
    curveKnots.add(new double[] { 0.5, 1, 2, 5, 10, 20, 31 });
    yields.add(new double[] { 0.021, 0.036, 0.06, 0.054, 0.049, 0.044 });
    yields.add(new double[] { 0.01, 0.02, 0.035, 0.06, 0.055, 0.05, 0.045 });

    final double[] rates = new double[payments.length];
    int count = 0;
    for (final double[] trueYields : yields) {
      for (final double temp : trueYields) {
        rates[count++] = temp + 0.02;
      }
    }

    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator = ParRateCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = ParRateCurveSensitivityCalculator.getInstance();
    return getSwapOnlySetup(payments, curveNames, null, curveKnots, yields, startPosition, interpolatorName, calculator, sensitivityCalculator);
  }

  @Test
  public void testNewton() {

    final NewtonVectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, swaps only. Root finder: Newton");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve, swaps only. Root finder: Newton (FD Jacobian)");

    data = getDoubleCurveSetup();
    doHotSpot(rootFinder, data, "Double curve, swaps only. Root finder: Newton");

  }

  @Test
  public void testShermanMorrison() {

    final NewtonVectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, swaps only. Root finder: ShermanMorrison");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve, swaps only. Root finder:ShermanMorrison (FD Jacobian)");

    data = getDoubleCurveSetup();
    doHotSpot(rootFinder, data, "Double curve, swaps only. Root finder: ShermanMorrison");

  }

  @Test
  public void testBroyden() {

    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, swaps only. Root finder: Broyden");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve, swaps only. Root finder: Broyden (FD Jacobian)");

    data = getDoubleCurveSetup();
    doHotSpot(rootFinder, data, "Double curve, swaps only. Root finder: Broyden");
  }

  @Test
  public void testJacobian() {
    assertJacobian(getSingleCurveSetup());
    assertJacobian(getDoubleCurveSetup());
  }

  @Test
  public void testTickingForwardRates() {
    final YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    final NormalDistribution normDist = new NormalDistribution(0, 1.0, RANDOM);
    final double sigma = 0.3;
    final double dt = 1. / 12.;
    final double rootdt = Math.sqrt(dt);

    final double tol = 1e-8;
    final VectorRootFinder rootFinder = new BroydenVectorRootFinder(tol, tol, STEPS);

    final String curveName = data.getCurveNames().get(0);
    final double[] curveKnots = data.getCurveNodePointsForCurve(curveName);

    final int n = curveKnots.length;

    final double[] yields = new double[n];
    final double[] forwards = new double[n];
    DoubleMatrix1D yieldCurveNodes = data.getStartPosition();

    for (int i = 0; i < n; i++) {
      forwards[i] = 0.01 + curveKnots[i] * 0.002;// start with upward sloping
                                                 // forward curve
    }

    MultipleYieldCurveFinderDataBundle dataBundle = data;

    for (int t = 0; t < 120; t++) {
      final List<InstrumentDerivative> instruments = new ArrayList<InstrumentDerivative>();
      double sum = 0;
      for (int i = 0; i < n; i++) {
        forwards[i] *= Math.exp(sigma * rootdt * normDist.nextRandom() - sigma * sigma / 2.0 * dt);
        sum += forwards[i] * (curveKnots[i] - (i == 0 ? 0.0 : curveKnots[i - 1]));
        yields[i] = sum / curveKnots[i];
      }

      final YieldCurveBundle curveBundle = new YieldCurveBundle();

      final YieldAndDiscountCurve curve = makeYieldCurve(yields, curveKnots, data.getInterpolatorForCurve(curveName));
      curveBundle.setCurve(curveName, curve);

      InstrumentDerivative instrument;
      final double[] marketValue = new double[n];
      for (int i = 0; i < n; i++) {
        instrument = makeSwap(curveKnots[i], SimpleFrequency.QUARTERLY, curveName, curveName, 0, 1.0);
        instrument = REPLACE_RATE.visit(instrument, ParRateCalculator.getInstance().visit(instrument, curveBundle));
        instruments.add(instrument);
        marketValue[i] = data.getMarketValueCalculator().visit(instrument, curveBundle);
      }

      dataBundle = updateInstruments(dataBundle, instruments, marketValue);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> functor = new MultipleYieldCurveFinderFunction(dataBundle, data.getMarketValueCalculator());

      yieldCurveNodes = rootFinder.getRoot(functor, yieldCurveNodes);
      for (int i = 0; i < n; i++) {
        assertEquals(yields[i], yieldCurveNodes.getEntry(i), 1e-4);
      }
    }
  }

  @Test
  public void testForwardCurveOnly() {

    final List<String> curveNames = new ArrayList<String>();
    final List<String> knownCurves = new ArrayList<String>();
    final List<double[]> curveKnots = new ArrayList<double[]>();
    final List<double[]> yields = new ArrayList<double[]>();

    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    final int[] payments = new int[] { 1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60 };
    curveNames.add("funding");
    curveNames.add("Libor");
    knownCurves.add("funding");
    curveKnots.add(new double[] { 1, 5, 10, 20, 30 });
    curveKnots.add(new double[] { 0.5, 1.00, 1.5, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556 });
    yields.add(new double[] { 0.02, 0.04, 0.05, 0.05, 0.05 });
    yields.add(new double[] { 0.01, 0.015, 0.02, 0.03, 0.04, 0.05, 0.052, 0.049, 0.045, 0.044, 0.043, 0.041, 0.04 });

    final int n = payments.length;
    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      rates[i] = 0.05;
    }

    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator = PresentValueCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = PresentValueCurveSensitivityCalculator.getInstance();

    final YieldCurveFittingTestDataBundle data = getSwapOnlySetup(payments, curveNames, knownCurves, curveKnots, yields, startPosition, interpolatorName, calculator,
        sensitivityCalculator);

    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder, data, "Fit Forward curve only. Rootfinder: Broyden");
  }

  @Test
  public void testFundingCurveOnly() {

    final List<String> curveNames = new ArrayList<String>();
    final List<String> knownCurves = new ArrayList<String>();
    final List<double[]> curveKnots = new ArrayList<double[]>();
    final List<double[]> yields = new ArrayList<double[]>();

    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    final int[] payments = new int[] { 1, 2, 3, 4, 6, 8, 10, 14, 20, 30, 40, 50, 60 };
    curveNames.add("funding");
    curveNames.add("Libor");
    knownCurves.add("Libor");
    curveKnots.add(new double[] { 0.5, 1.00, 1.5, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556 });
    curveKnots.add(new double[] { 0.5, 1.00, 1.5, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556 });
    yields.add(new double[] { 0.01, 0.015, 0.02, 0.03, 0.04, 0.05, 0.052, 0.049, 0.045, 0.044, 0.043, 0.041, 0.04 });
    yields.add(new double[] { 0.015, 0.0155, 0.025, 0.032, 0.042, 0.052, 0.0521, 0.0491, 0.045, 0.044, 0.043, 0.041, 0.04 });

    final int n = payments.length;
    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      rates[i] = 0.05;
    }
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator = PresentValueCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = PresentValueCurveSensitivityCalculator.getInstance();

    final YieldCurveFittingTestDataBundle data = getSwapOnlySetup(payments, curveNames, knownCurves, curveKnots, yields, startPosition, interpolatorName, calculator,
        sensitivityCalculator);

    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    doHotSpot(rootFinder, data, "Fit Funding curve only. Rootfinder: Broyden");
  }

  protected YieldCurveFittingTestDataBundle getSwapOnlySetup(final int[] swapMaturities, final List<String> curveNames, final List<String> fixedCurveNames,
      final List<double[]> curveKnots, final List<double[]> yields, final DoubleMatrix1D startPosition, final String interpolator,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator) {

    final CombinedInterpolatorExtrapolator extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolator,
        LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);

    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    final int numCurves = curveNames.size();
    for (int i = 0; i < numCurves; i++) {
      final YieldAndDiscountCurve curve = makeYieldCurve(yields.get(i), curveKnots.get(i), extrapolator);
      curveBundle.setCurve(curveNames.get(i), curve);
    }

    final List<InstrumentDerivative> instruments = new ArrayList<InstrumentDerivative>();
    FixedFloatSwap instrument;
    final int n = swapMaturities.length;
    final double[] marketValues = new double[n];
    String curve1;
    String curve2;
    if (curveNames.size() == 1) {
      curve1 = curveNames.get(0);
      curve2 = curveNames.get(0);
    } else {
      curve1 = curveNames.get(0);
      curve2 = curveNames.get(1);
    }
    for (int i = 0; i < n; i++) {
      instrument = makeSwap(swapMaturities[i]/2.0, SimpleFrequency.QUARTERLY, curve1, curve2, 0, 1.0);
      instrument = REPLACE_RATE.visitFixedFloatSwap(instrument, ParRateCalculator.getInstance().visit(instrument, curveBundle));
      instruments.add(instrument);
      // if the calculator is Present Value this should be zero (by definition
      // of what par-rate is)
      marketValues[i] = marketValueCalculator.visit(instrument, curveBundle);
    }

    YieldCurveBundle knownCurves = null;
    if (fixedCurveNames != null && fixedCurveNames.size() > 0) {
      knownCurves = new YieldCurveBundle();
      for (final String name : fixedCurveNames) {
        knownCurves.setCurve(name, curveBundle.getCurve(name));
        // remove the know curves from the list to be fitted
        final int index = curveNames.indexOf(name);
        curveNames.remove(index);
        curveKnots.remove(index);
        yields.remove(index);
      }
    }

    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, knownCurves, curveNames, curveKnots, extrapolator,
        marketValueCalculator, marketValueSensitivityCalculator, marketValues, startPosition, yields, false);

    return data;
  }
}

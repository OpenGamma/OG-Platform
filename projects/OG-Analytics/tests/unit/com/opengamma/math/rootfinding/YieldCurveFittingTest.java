/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
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
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class YieldCurveFittingTest extends YieldCurveFittingSetup {
  private static final Logger LOGGER = LoggerFactory.getLogger(YieldCurveFittingTest.class);
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

  private static final Function1D<Double, Double> DUMMY_CURVE = new Function1D<Double, Double>() {

    private static final double A = 0;
    private static final double B = 0.004148649;
    private static final double C = 0.056397936;
    private static final double D = 0.004457019;
    private static final double E = 0.000429628;

    @Override
    public Double evaluate(final Double x) {
      return (A + B * x) * Math.exp(-C * x) + E * x + D;
    }
  };

  protected static final Function1D<Double, Double> DUMMY_SPEAD_CURVE = new Function1D<Double, Double>() {

    private static final double a = 0.0025;
    private static final double b = 0.0021;
    private static final double c = 0.2;
    private static final double d = 0.0;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  @Test
  public void testNewton() {
    final NewtonVectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, Libor, cash, FRA, swaps. Root finder: Newton");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve, Libor, cash, FRA, swaps. Root finder: Newton (FD Jacobian)");

    data = getDoubleCurveSetup();
    doHotSpot(rootFinder, data, "Double curve, Libor, cash, FRA, swaps, basis swaps. Root finder: Newton");
  }

  @Test
  public void testShermanMorrison() {
    final NewtonVectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, Libor, cash, FRA, swaps. Root finder: ShermanMorrison");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve, Libor, cash, FRA, swaps. Root finder:ShermanMorrison (FD Jacobian)");

    data = getDoubleCurveSetup();
    doHotSpot(rootFinder, data, "Double curve, Libor, cash, FRA, swaps, basis swaps. Root finder: ShermanMorrison");
  }

  @Test
  public void testBroyden() {
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve,Libor, cash, FRA, swaps. Root finder: Broyden");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve, Libor, cash, FRA, swaps. Root finder: Broyden (FD Jacobian)");

    data = getDoubleCurveSetup();
    doHotSpot(rootFinder, data, "Double curve, Libor, cash, FRA, swaps, basis swaps. Root finder: Broyden");
  }

  @Test
  public void testJacobian() {
    assertJacobian(getSingleCurveSetup());
    assertJacobian(getDoubleCurveSetup());
  }

  private YieldCurveFittingTestDataBundle getSingleCurveSetup() {

    final SimpleFrequency paymentFreq = SimpleFrequency.QUARTERLY;

    final List<String> curveNames = new ArrayList<String>();
    curveNames.add("single curve");
    final String interpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;

    final CombinedInterpolatorExtrapolator extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolator, LINEAR_EXTRAPOLATOR,
        FLAT_EXTRAPOLATOR);
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator = PresentValueCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = PresentValueCurveSensitivityCalculator.getInstance();
    // final InterestRateDerivativeVisitor<Double> calculator = ParRateDifferenceCalculator.getInstance();
    // final InterestRateDerivativeVisitor<Map<String, List<DoublesPair>>> sensitivityCalculator = ParRateCurveSensitivityCalculator.getInstance();

    final HashMap<String, double[]> maturities = new LinkedHashMap<String, double[]>();

    maturities.put("libor", new double[] {1. / 12, 2. / 12, 3. / 12 }); //
    maturities.put("fra", new double[] {0.5, 0.75 });
    maturities.put("cash", new double[] {1. / 365, 1. / 52, 2. / 52. });
    maturities.put("swap", new double[] {1.00, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556, 35.00833333, 50.01388889 });

    int nNodes = 0;
    for (final double[] temp : maturities.values()) {
      nNodes += temp.length;
    }

    double[] temp = new double[nNodes];
    int index = 0;
    for (final double[] times : maturities.values()) {
      for (final double t : times) {
        temp[index++] = t;
      }
    }
    Arrays.sort(temp);
    final List<double[]> curveKnots = new ArrayList<double[]>();
    curveKnots.add(temp);

    // set up curve to obtain "market" prices
    temp = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      temp[i] = DUMMY_CURVE.evaluate(curveKnots.get(0)[i]);
    }
    final List<double[]> yields = new ArrayList<double[]>();
    yields.add(temp);

    // now get market prices
    final double[] marketValues = new double[nNodes];

    final YieldAndDiscountCurve curve = makeYieldCurve(yields.get(0), curveKnots.get(0), extrapolator);
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(curveNames.get(0), curve);

    final List<InstrumentDerivative> instruments = new ArrayList<InstrumentDerivative>();
    InstrumentDerivative ird;
    index = 0;
    for (final String name : maturities.keySet()) {
      final double[] times = maturities.get(name);
      for (final double t : times) {
        ird = makeSingleCurrencyIRD(name, t, paymentFreq, curveNames.get(0), curveNames.get(0), 0.0, 1.0);
        ird = REPLACE_RATE.visit(ird, ParRateCalculator.getInstance().visit(ird, bundle));
        instruments.add(ird);
        marketValues[index] = calculator.visit(ird, bundle);
        index++;
      }
    }

    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.05;
    }
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, curveNames, curveKnots, extrapolator, calculator, sensitivityCalculator,
        marketValues, startPosition, yields, false);

    return data;
  }

  private YieldCurveFittingTestDataBundle getDoubleCurveSetup() {
    final SimpleFrequency paymentFreq = SimpleFrequency.QUARTERLY;

    final List<String> curveNames = new ArrayList<String>();
    curveNames.add("funding curve");
    curveNames.add("Libor curve");
    final String interpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;

    final CombinedInterpolatorExtrapolator extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolator, LINEAR_EXTRAPOLATOR,
        FLAT_EXTRAPOLATOR);

    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator = ParRateCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = ParRateCurveSensitivityCalculator.getInstance();

    final HashMap<String, double[]> fundingMaturities = new LinkedHashMap<String, double[]>();
    final HashMap<String, double[]> liborMaturities = new LinkedHashMap<String, double[]>();
    final HashMap<String, double[]> maturities = new LinkedHashMap<String, double[]>();

    fundingMaturities.put("cash", new double[] {1. / 365, 1. / 52, 2. / 52. });
    fundingMaturities.put("basisSwap", new double[] {1, 2, 5, 10, 20, 30, 50 });

    liborMaturities.put("libor", new double[] {1. / 12, 2. / 12, 3. / 12 }); //
    liborMaturities.put("fra", new double[] {0.5, 0.75 });
    liborMaturities.put("swap", new double[] {1.00, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556, 35.00833333, 50.01388889 });

    maturities.putAll(fundingMaturities);
    maturities.putAll(liborMaturities);

    final List<double[]> curveKnots = new ArrayList<double[]>();
    curveKnots.add(catMap(fundingMaturities));
    curveKnots.add(catMap(liborMaturities));

    // set up curve to obtain "market" prices
    final List<double[]> yields = new ArrayList<double[]>();
    double[] temp = new double[curveKnots.get(0).length];
    int index = 0;
    for (final double t : curveKnots.get(0)) {
      temp[index++] = DUMMY_CURVE.evaluate(t);
    }
    yields.add(temp);
    temp = new double[curveKnots.get(1).length];
    index = 0;
    for (final double t : curveKnots.get(1)) {
      temp[index++] = DUMMY_CURVE.evaluate(t) + DUMMY_SPEAD_CURVE.evaluate(t);
    }
    yields.add(temp);

    // now get market prices
    final int nNodes = curveKnots.get(0).length + curveKnots.get(1).length;
    final double[] marketValues = new double[nNodes];

    final YieldCurveBundle bundle = new YieldCurveBundle();
    if (curveKnots.get(0).length > 0) {
      bundle.setCurve(curveNames.get(0), makeYieldCurve(yields.get(0), curveKnots.get(0), extrapolator));
    }
    if (curveKnots.get(1).length > 0) {
      bundle.setCurve(curveNames.get(1), makeYieldCurve(yields.get(1), curveKnots.get(1), extrapolator));
    }

    final List<InstrumentDerivative> instruments = new ArrayList<InstrumentDerivative>();
    InstrumentDerivative ird;
    index = 0;
    for (final String name : maturities.keySet()) {
      for (final double t : maturities.get(name)) {
        ird = makeSingleCurrencyIRD(name, t, paymentFreq, curveNames.get(0), curveNames.get(1), 0.0, 1.0);
        marketValues[index] = ParRateCalculator.getInstance().visit(ird, bundle);
        instruments.add(REPLACE_RATE.visit(ird, marketValues[index]));
        index++;
      }
    }

    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.05;
    }
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, curveNames, curveKnots, extrapolator, calculator, sensitivityCalculator,
        marketValues, startPosition, yields, false);

    return data;
  }

}

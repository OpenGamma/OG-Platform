/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of
 * companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

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
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.YieldCurveFittingSetup;
import com.opengamma.math.rootfinding.YieldCurveFittingTestDataBundle;
import com.opengamma.math.rootfinding.YieldCurveFittingTestDataBundle.TestType;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InstrumentDoubleCurveSensitivityCalculatorTest extends YieldCurveFittingSetup {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentDoubleCurveSensitivityCalculatorTest.class);
  private static final int WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;
  private static final SimpleFrequency FRQ = SimpleFrequency.QUARTERLY;
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
  protected static final Function1D<Double, Double> DUMMY_SPREAD_CURVE = new Function1D<Double, Double>() {
    private static final double A = 0.0025;
    private static final double B = 0.0021;
    private static final double C = 0.2;

    @Override
    public Double evaluate(final Double x) {
      return (A + B * x) * Math.exp(-C * x);
    }
  };
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);
  private static final NewtonVectorRootFinder ROOT_FINDER = new BroydenVectorRootFinder(1e-12, 1e-12, 100, DecompositionFactory.SV_COMMONS);
  private static final List<String> CURVE_NAMES = Arrays.asList("Funding", "Libor 3m");
  private static final Map<String, Map<String, double[]>> MATURITIES = getDoubleCurveMaturities();
  private static final Map<String, Map<String, double[]>> MARKET_DATA = getDoubleCurveMarketData(MATURITIES);
  private static final YieldCurveFittingTestDataBundle PV_DATA = getDoubleCurveSetup(PresentValueCalculator.getInstance(), PresentValueCurveSensitivityCalculator.getInstance(), MATURITIES, MARKET_DATA,
      true);
  private static final YieldCurveFittingTestDataBundle PAR_RATE_DATA = getDoubleCurveSetup(ParRateCalculator.getInstance(), ParRateCurveSensitivityCalculator.getInstance(), MATURITIES, MARKET_DATA,
      false);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> PV_FUNCTION = new MultipleYieldCurveFinderFunction(PV_DATA, PV_DATA.getMarketValueCalculator());
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> PV_JACOBIAN_FUNCTION;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> PAR_RATE_FUNCTION = new MultipleYieldCurveFinderFunction(PAR_RATE_DATA, PAR_RATE_DATA.getMarketValueCalculator());
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> PAR_RATE_JACOBIAN_FUNCTION;
  private static final DoubleMatrix1D PV_YIELD_CURVE_NODES;
  private static final DoubleMatrix2D PV_JACOBIAN;
  private static final DoubleMatrix1D PAR_RATE_YIELD_CURVE_NODES;
  private static final DoubleMatrix2D PAR_RATE_JACOBIAN;
  private static final DoubleMatrix1D PV_COUPON_SENSITIVITY;
  private static final YieldCurveBundle PV_CURVES;
  private static final YieldCurveBundle PV_ALL_CURVES;
  private static final YieldCurveBundle PAR_RATE_CURVES;
  private static final YieldCurveBundle PAR_RATE_ALL_CURVES;
  private static final InstrumentSensitivityCalculator ISC = InstrumentSensitivityCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator PVCS = PresentValueCouponSensitivityCalculator.getInstance();
  private static final PresentValueNodeSensitivityCalculator PVNS = PresentValueNodeSensitivityCalculator.getDefaultInstance();

  static {
    if (PV_DATA.getTestType() == TestType.ANALYTIC_JACOBIAN) {
      PV_JACOBIAN_FUNCTION = new MultipleYieldCurveFinderJacobian(PV_DATA, PV_DATA.getMarketValueSensitivityCalculator());
    } else if (PV_DATA.getTestType() == TestType.FD_JACOBIAN) {
      final VectorFieldFirstOrderDifferentiator fdJacCalculator = new VectorFieldFirstOrderDifferentiator();
      PV_JACOBIAN_FUNCTION = fdJacCalculator.differentiate(PV_FUNCTION);
    } else {
      throw new IllegalArgumentException("unknown TestType " + PV_DATA.getTestType());
    }
    if (PAR_RATE_DATA.getTestType() == TestType.ANALYTIC_JACOBIAN) {
      PAR_RATE_JACOBIAN_FUNCTION = new MultipleYieldCurveFinderJacobian(PAR_RATE_DATA, PAR_RATE_DATA.getMarketValueSensitivityCalculator());
    } else if (PAR_RATE_DATA.getTestType() == TestType.FD_JACOBIAN) {
      final VectorFieldFirstOrderDifferentiator fdJacCalculator = new VectorFieldFirstOrderDifferentiator();
      PAR_RATE_JACOBIAN_FUNCTION = fdJacCalculator.differentiate(PAR_RATE_FUNCTION);
    } else {
      throw new IllegalArgumentException("unknown TestType " + PAR_RATE_DATA.getTestType());
    }
    PV_YIELD_CURVE_NODES = ROOT_FINDER.getRoot(PV_FUNCTION, PV_JACOBIAN_FUNCTION, PV_DATA.getStartPosition());
    PV_JACOBIAN = PV_JACOBIAN_FUNCTION.evaluate(PV_YIELD_CURVE_NODES);
    PAR_RATE_YIELD_CURVE_NODES = ROOT_FINDER.getRoot(PAR_RATE_FUNCTION, PAR_RATE_JACOBIAN_FUNCTION, PAR_RATE_DATA.getStartPosition());
    PAR_RATE_JACOBIAN = PAR_RATE_JACOBIAN_FUNCTION.evaluate(PAR_RATE_YIELD_CURVE_NODES);
    PAR_RATE_CURVES = getYieldCurveMap(PAR_RATE_DATA, PAR_RATE_YIELD_CURVE_NODES);
    PAR_RATE_ALL_CURVES = getAllCurves(PAR_RATE_DATA, PAR_RATE_CURVES);
    PV_CURVES = getYieldCurveMap(PV_DATA, PV_YIELD_CURVE_NODES);
    PV_ALL_CURVES = getAllCurves(PV_DATA, PV_CURVES);
    final double[] couponSensitivityArray = new double[PV_DATA.getNumInstruments()];
    for (int i = 0; i < PV_DATA.getNumInstruments(); i++) {
      couponSensitivityArray[i] = PVCS.visit(PV_DATA.getDerivative(i), PV_ALL_CURVES);
    }
    PV_COUPON_SENSITIVITY = new DoubleMatrix1D(couponSensitivityArray);
  }

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

  @Test
  public void testWithParRate() {
    for (int i = 0; i < PAR_RATE_DATA.getNumInstruments(); i++) {
      final DoubleMatrix1D bucketedDelta = ISC.calculateFromParRate(PAR_RATE_DATA.getDerivative(i), PAR_RATE_DATA.getKnownCurves(), PAR_RATE_CURVES, PAR_RATE_JACOBIAN, PVNS);
      final double sensitivity = PVCS.visit(PAR_RATE_DATA.getDerivative(i), PAR_RATE_ALL_CURVES);
      AssertJUnit.assertEquals(-sensitivity, bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < PAR_RATE_DATA.getNumInstruments(); j++) {
        if (j != i) {
          AssertJUnit.assertEquals(0.0, bucketedDelta.getEntry(j), 1e-8);
        }
      }
    }
  }

  @Test
  public void testWithPV() {
    // PresentValueCouponSensitivityCalculator is sensitivity to change in the
    // coupon rate for that instrument - what we calculate here is the
    // (hypothetical) change of PV of the instrument with a fixed coupon when
    // its par-rate changes - this is exactly the negative of the coupon
    // sensitivity
    for (int i = 0; i < PV_DATA.getNumInstruments(); i++) {
      final DoubleMatrix1D bucketedDelta = ISC.calculateFromPresentValue(PV_DATA.getDerivative(i), PV_DATA.getKnownCurves(), PV_CURVES, PV_COUPON_SENSITIVITY, PV_JACOBIAN, PVNS);
      AssertJUnit.assertEquals(-PV_COUPON_SENSITIVITY.getEntry(i), bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < PV_DATA.getNumInstruments(); j++) {
        if (j != i) {
          AssertJUnit.assertEquals(0.0, bucketedDelta.getEntry(j), 1e-8);
        }
      }
    }
  }
 
  @Test
  public void testBumpedData() {
    final double notional = 10394850;
    final double eps = 1e-4;

    final InstrumentDerivative libor = makeSingleCurrencyIRD("libor", 1.5, FRQ, CURVE_NAMES.get(0), CURVE_NAMES.get(1), 0.04, notional);
    testBumpedDataParRateMethod(libor, eps);
    testBumpedDataPVMethod(libor, eps);
    InstrumentDerivative swap = makeSingleCurrencyIRD("swap", 13,FRQ, CURVE_NAMES.get(0), CURVE_NAMES.get(1), 0.048, notional);
    testBumpedDataParRateMethod(swap, eps);
    testBumpedDataPVMethod(swap, eps);
    final InstrumentDerivative fra = makeSingleCurrencyIRD("fra", 0.6666, FRQ, CURVE_NAMES.get(0), CURVE_NAMES.get(1), 0.02, notional);
    testBumpedDataParRateMethod(fra, eps);
    testBumpedDataPVMethod(fra, eps);
    final InstrumentDerivative future = makeSingleCurrencyIRD("fra", 2, FRQ, CURVE_NAMES.get(0), CURVE_NAMES.get(1), 0.03, notional);
    testBumpedDataParRateMethod(future, eps);
    testBumpedDataPVMethod(future, eps);
    swap = makeSingleCurrencyIRD("swap", 19,FRQ, CURVE_NAMES.get(0), CURVE_NAMES.get(1), 0.05, notional);
    testBumpedDataParRateMethod(swap, eps);
    testBumpedDataPVMethod(swap, eps);
    final InstrumentDerivative basisSwap = makeSingleCurrencyIRD("basisSwap", 12,FRQ, CURVE_NAMES.get(0), CURVE_NAMES.get(1), 0.005, notional);
    testBumpedDataParRateMethod(basisSwap, eps);
    testBumpedDataPVMethod(basisSwap, eps);
  }

  private void testBumpedDataParRateMethod(final InstrumentDerivative ird, final double eps) {
    final DoubleMatrix1D sensitivities = ISC.calculateFromParRate(ird, null, PAR_RATE_CURVES, PAR_RATE_JACOBIAN, PVNS);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pv1 = calculator.visit(ird, PAR_RATE_ALL_CURVES);
    for (int i = 0; i < sensitivities.getNumberOfElements(); i++) {
      final int firstCurveSize = PAR_RATE_CURVES.getCurve(CURVE_NAMES.get(0)).getCurve().size();
      final String curveName = i < firstCurveSize ? CURVE_NAMES.get(0) : CURVE_NAMES.get(1);
      final int indexToBump = i < firstCurveSize ? i : i - firstCurveSize;
      final YieldCurveFittingTestDataBundle bumpedData = getDoubleCurveSetup(ParRateCalculator.getInstance(), ParRateCurveSensitivityCalculator.getInstance(), MATURITIES,
          getBumpedData(curveName, indexToBump, eps), false);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> f = new MultipleYieldCurveFinderFunction(bumpedData, bumpedData.getMarketValueCalculator());
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jf = new MultipleYieldCurveFinderJacobian(bumpedData, bumpedData.getMarketValueSensitivityCalculator());
      final DoubleMatrix1D bumpedNodes = ROOT_FINDER.getRoot(f, jf, bumpedData.getStartPosition());
      final YieldCurveBundle bumpedCurves = getYieldCurveMap(bumpedData, bumpedNodes);
      final YieldCurveBundle allBumpedCurves = getAllCurves(bumpedData, bumpedCurves);
      final double pv2 = calculator.visit(ird, allBumpedCurves);
      final double delta = pv2 - pv1;
      if (Math.abs(sensitivities.getEntry(i)) > 1e-3) {
        AssertJUnit.assertEquals(0, (delta - sensitivities.getEntry(i) * eps) / sensitivities.getEntry(i), eps);
      } else {
        AssertJUnit.assertEquals(0, sensitivities.getEntry(i), 1e-3);
        AssertJUnit.assertEquals(0, delta, 1e-3);
      }
    }
  }

  private void testBumpedDataPVMethod(final InstrumentDerivative ird, final double eps) {
    final DoubleMatrix1D sensitivities = ISC.calculateFromPresentValue(ird, null, PV_CURVES, PV_COUPON_SENSITIVITY, PV_JACOBIAN, PVNS);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pv1 = calculator.visit(ird, PV_ALL_CURVES);
    for (int i = 0; i < sensitivities.getNumberOfElements(); i++) {
      final int firstCurveSize = PV_CURVES.getCurve(CURVE_NAMES.get(0)).getCurve().size();
      final String curveName = i < firstCurveSize ? CURVE_NAMES.get(0) : CURVE_NAMES.get(1);
      final int indexToBump = i < firstCurveSize ? i : i - firstCurveSize;
      final YieldCurveFittingTestDataBundle bumpedData = getDoubleCurveSetup(PresentValueCalculator.getInstance(), PresentValueCurveSensitivityCalculator.getInstance(), MATURITIES,
          getBumpedData(curveName, indexToBump, eps), true);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> f = new MultipleYieldCurveFinderFunction(bumpedData, bumpedData.getMarketValueCalculator());
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jf = new MultipleYieldCurveFinderJacobian(bumpedData, bumpedData.getMarketValueSensitivityCalculator());
      final DoubleMatrix1D bumpedNodes = ROOT_FINDER.getRoot(f, jf, bumpedData.getStartPosition());
      final YieldCurveBundle bumpedCurves = getYieldCurveMap(bumpedData, bumpedNodes);
      final YieldCurveBundle allBumpedCurves = getAllCurves(bumpedData, bumpedCurves);
      final double pv2 = calculator.visit(ird, allBumpedCurves);
      final double delta = pv2 - pv1;
      if (Math.abs(sensitivities.getEntry(i)) > 1e-3) {
        AssertJUnit.assertEquals(0, (delta - sensitivities.getEntry(i) * eps) / sensitivities.getEntry(i), eps);
      } else {
        AssertJUnit.assertEquals(0, sensitivities.getEntry(i), 1e-3);
        AssertJUnit.assertEquals(0, delta, 1e-3);
      }
    }
  }

  private static YieldCurveBundle getAllCurves(final YieldCurveFittingTestDataBundle data, final YieldCurveBundle curves) {
    final YieldCurveBundle allCurves = new YieldCurveBundle(curves);
    if (data.getKnownCurves() != null) {
      allCurves.addAll(data.getKnownCurves());
    }
    return allCurves;
  }

  private static YieldCurveBundle getYieldCurveMap(final YieldCurveFittingTestDataBundle data, final DoubleMatrix1D yieldCurveNodes) {
    final HashMap<String, double[]> yields = unpackYieldVector(data, yieldCurveNodes);
    final LinkedHashMap<String, YieldAndDiscountCurve> curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    for (final String name : data.getCurveNames()) {
      final YieldAndDiscountCurve curve = makeYieldCurve(yields.get(name), data.getCurveNodePointsForCurve(name), data.getInterpolatorForCurve(name));
      curves.put(name, curve);
    }
    return new YieldCurveBundle(curves);
  }

  private static Map<String, Map<String, double[]>> getBumpedData(final String curveName, final int n, final double eps) {
    final Map<String, Map<String, double[]>> data = new LinkedHashMap<String, Map<String, double[]>>(getDoubleCurveMarketData(MATURITIES));
    int i = 0;
    for (final Map.Entry<String, double[]> entry : data.get(curveName).entrySet()) {
      final double[] value = entry.getValue();
      for (int j = 0; j < value.length; j++) {
        if (i == n) {
          value[j] += eps;
        }
        i++;
      }
    }
    return data;
  }

  private static Map<String, Map<String, double[]>> getDoubleCurveMaturities() {
    final Map<String, double[]> fundingMaturities = new LinkedHashMap<String, double[]>();
    final Map<String, double[]> liborMaturities = new LinkedHashMap<String, double[]>();
    final Map<String, Map<String, double[]>> maturities = new LinkedHashMap<String, Map<String, double[]>>();
    fundingMaturities.put("cash", new double[] {1. / 365, 1. / 52, 2. / 52. });
    fundingMaturities.put("basisSwap", new double[] {1, 2, 5, 10, 20, 30, 50 });
    liborMaturities.put("libor", new double[] {1. / 12, 2. / 12, 3. / 12 }); //
    liborMaturities.put("fra", new double[] {0.5, 0.75 });
    liborMaturities.put("swap", new double[] {1.00, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556, 35.00833333, 50.01388889 });
    maturities.put(CURVE_NAMES.get(0), fundingMaturities);
    maturities.put(CURVE_NAMES.get(1), liborMaturities);
    return maturities;
  }

  private static Map<String, Map<String, double[]>> getDoubleCurveMarketData(final Map<String, Map<String, double[]>> maturities) {
    final Map<String, Map<String, double[]>> marketData = new LinkedHashMap<String, Map<String, double[]>>();
    final List<double[]> curveKnots = new ArrayList<double[]>();
    curveKnots.add(getNodes(maturities.get(CURVE_NAMES.get(0))));
    curveKnots.add(getNodes(maturities.get(CURVE_NAMES.get(1))));
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
      temp[index++] = DUMMY_CURVE.evaluate(t) + DUMMY_SPREAD_CURVE.evaluate(t);
    }
    yields.add(temp);
    final YieldCurveBundle bundle = new YieldCurveBundle();
    if (curveKnots.get(0).length > 0) {
      bundle.setCurve(CURVE_NAMES.get(0), makeYieldCurve(yields.get(0), curveKnots.get(0), INTERPOLATOR));
    }
    if (curveKnots.get(1).length > 0) {
      bundle.setCurve(CURVE_NAMES.get(1), makeYieldCurve(yields.get(1), curveKnots.get(1), INTERPOLATOR));
    }
    InstrumentDerivative ird;
    for (final String curveName : maturities.keySet()) {
      final Map<String, double[]> dataForCurve = new LinkedHashMap<String, double[]>();
      for (final String instrumentType : maturities.get(curveName).keySet()) {
        final double[] marketValuesForCurve = new double[maturities.get(curveName).get(instrumentType).length];
        int i = 0;
        for (final double t : maturities.get(curveName).get(instrumentType)) {
          ird = makeSingleCurrencyIRD(instrumentType, t, FRQ, CURVE_NAMES.get(0), CURVE_NAMES.get(1), 0.0, 1);
          marketValuesForCurve[i++] = ParRateCalculator.getInstance().visit(ird, bundle);
        }
        dataForCurve.put(instrumentType, marketValuesForCurve);
      }
      marketData.put(curveName, dataForCurve);
    }
    return marketData;
  }

  private static YieldCurveFittingTestDataBundle getDoubleCurveSetup(final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator,
      final Map<String, Map<String, double[]>> maturities, final Map<String, Map<String, double[]>> marketValues, final boolean isPV) {
    final List<double[]> curveKnots = new ArrayList<double[]>();
    curveKnots.add(getNodes(maturities.get(CURVE_NAMES.get(0))));
    curveKnots.add(getNodes(maturities.get(CURVE_NAMES.get(1))));
    final int nNodes = curveKnots.get(0).length + curveKnots.get(1).length;
    final List<InstrumentDerivative> instruments = new ArrayList<InstrumentDerivative>();
    InstrumentDerivative ird;
    final double[] marketValuesArray = new double[nNodes];
    int index = 0;
    for (final String curveName : maturities.keySet()) {
      final Map<String, double[]> marketValuesForCurve = marketValues.get(curveName);
      for (final String instrumentType : marketValuesForCurve.keySet()) {
        final double[] marketValuesForInstrument = marketValuesForCurve.get(instrumentType);
        int i = 0;
        for (final double t : maturities.get(curveName).get(instrumentType)) {
          ird = makeSingleCurrencyIRD(instrumentType, t,FRQ, CURVE_NAMES.get(0), CURVE_NAMES.get(1), 0.0, 1);
          instruments.add(REPLACE_RATE.visit(ird, marketValuesForInstrument[i]));
          if (isPV) {
            marketValuesArray[index] = 0;
          } else {
            marketValuesArray[index] = marketValuesForInstrument[i];
          }
          i++;
          index++;
        }
      }
    }
    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.03;
    }
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);
    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, CURVE_NAMES, curveKnots, INTERPOLATOR, calculator,
        sensitivityCalculator, marketValuesArray, startPosition, null, false);
    return data;
  }

  private static double[] getNodes(final Map<String, double[]> map) {
    int nNodes = 0;
    for (final double[] temp : map.values()) {
      nNodes += temp.length;
    }

    final double[] temp = new double[nNodes];
    int index = 0;
    for (final double[] times : map.values()) {
      for (final double t : times) {
        temp[index++] = t;
      }
    }
    Arrays.sort(temp);
    return temp;
  }
}

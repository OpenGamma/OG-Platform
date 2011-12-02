/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of
 * companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
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
public class InstrumentSingleCurveSensitivityCalculatorTest extends YieldCurveFittingSetup {
  private static final String CURVE_NAME = "single curve";
  private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentSingleCurveSensitivityCalculatorTest.class);
  private static final int WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;
  private static final SimpleFrequency FRQ = SimpleFrequency.QUARTERLY;
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);
  private static final NewtonVectorRootFinder ROOT_FINDER = new BroydenVectorRootFinder(EPS, EPS, STEPS);
  private static final Map<String, double[]> MARKET_DATA = getSingleCurveMarketData();
  private static final Map<String, double[]> MATURITIES = getSingleCurveMaturities();
  private static final YieldCurveFittingTestDataBundle PV_DATA = getSingleCurveSetup(PresentValueCalculator.getInstance(), PresentValueCurveSensitivityCalculator.getInstance(), MATURITIES, MARKET_DATA,
      true);
  private static final YieldCurveFittingTestDataBundle PAR_RATE_DATA = getSingleCurveSetup(ParRateCalculator.getInstance(), ParRateCurveSensitivityCalculator.getInstance(), MATURITIES, MARKET_DATA,
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
      assertEquals(-sensitivity, bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < PAR_RATE_DATA.getNumInstruments(); j++) {
        if (j != i) {
          assertEquals(0.0, bucketedDelta.getEntry(j), 1e-8);
        }
      }
    }
  }

  @Test
  public void testwithPV() {
    // PresentValueCouponSensitivityCalculator is sensitivity to change in the
    // coupon rate for that instrument - what we calculate here is the
    // (hypothetical) change of PV of the instrument with a fixed coupon when
    // its par-rate changes - this is exactly the negative of the coupon
    // sensitivity
    for (int i = 0; i < PV_DATA.getNumInstruments(); i++) {
      final DoubleMatrix1D bucketedDelta = ISC.calculateFromPresentValue(PV_DATA.getDerivative(i), PV_DATA.getKnownCurves(), PV_CURVES, PV_COUPON_SENSITIVITY, PV_JACOBIAN, PVNS);
      assertEquals(-PV_COUPON_SENSITIVITY.getEntry(i), bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < PV_DATA.getNumInstruments(); j++) {
        if (j != i) {
          assertEquals(0.0, bucketedDelta.getEntry(j), 1e-8);
        }
      }
    }
  }

  @Test
  public void testBumpedData() {
    final double notional = 10394850;
    final double eps = 1e-3;
    final InstrumentDerivative libor = makeSingleCurrencyIRD("libor", 1.5, FRQ, CURVE_NAME, CURVE_NAME, 0.04, notional);
    testBumpedDataParRateMethod(libor, eps);
    testBumpedDataPVMethod(libor, eps);
    InstrumentDerivative swap = makeSingleCurrencyIRD("swap", 13,FRQ, CURVE_NAME, CURVE_NAME, 0.048, notional);
    testBumpedDataParRateMethod(swap, eps);
    testBumpedDataPVMethod(swap, eps);
    final InstrumentDerivative fra = makeSingleCurrencyIRD("fra", 0.6666, FRQ,CURVE_NAME, CURVE_NAME, 0.02, notional);
    testBumpedDataParRateMethod(fra, eps);
    testBumpedDataPVMethod(fra, eps);
    final InstrumentDerivative future = makeSingleCurrencyIRD("fra", 2,FRQ, CURVE_NAME, CURVE_NAME, 0.03, notional);
    testBumpedDataParRateMethod(future, eps);
    testBumpedDataPVMethod(future, eps);
    swap = makeSingleCurrencyIRD("swap", 19,FRQ, CURVE_NAME, CURVE_NAME, 0.05, notional);
    testBumpedDataParRateMethod(swap, eps);
    testBumpedDataPVMethod(swap, eps);
  }

  private void testBumpedDataParRateMethod(final InstrumentDerivative ird, final double eps) {
    final DoubleMatrix1D sensitivities = ISC.calculateFromParRate(ird, null, PAR_RATE_CURVES, PAR_RATE_JACOBIAN, PVNS);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pv1 = calculator.visit(ird, PAR_RATE_ALL_CURVES);
    for (int i = 0; i < sensitivities.getNumberOfElements(); i++) {
      final YieldCurveFittingTestDataBundle bumpedData = getSingleCurveSetup(ParRateCalculator.getInstance(), ParRateCurveSensitivityCalculator.getInstance(), MATURITIES, getBumpedData(i, eps), false);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> f = new MultipleYieldCurveFinderFunction(bumpedData, bumpedData.getMarketValueCalculator());
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jf = new MultipleYieldCurveFinderJacobian(bumpedData, bumpedData.getMarketValueSensitivityCalculator());
      final DoubleMatrix1D bumpedNodes = ROOT_FINDER.getRoot(f, jf, bumpedData.getStartPosition());
      final YieldCurveBundle bumpedCurves = getYieldCurveMap(bumpedData, bumpedNodes);
      final YieldCurveBundle allBumpedCurves = getAllCurves(bumpedData, bumpedCurves);
      final double pv2 = calculator.visit(ird, allBumpedCurves);
      final double delta = pv2 - pv1;
      if (Math.abs(sensitivities.getEntry(i)) > 1e-3) {
        assertEquals(0, (delta - sensitivities.getEntry(i) * eps) / sensitivities.getEntry(i), eps);
      } else {
        assertEquals(0, sensitivities.getEntry(i), 1e-4);
        assertEquals(0, delta, 1e-4);
      }
    }
  }

  private void testBumpedDataPVMethod(final InstrumentDerivative ird, final double eps) {
    final DoubleMatrix1D sensitivities = ISC.calculateFromPresentValue(ird, null, PV_CURVES, PV_COUPON_SENSITIVITY, PV_JACOBIAN, PVNS);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pv1 = calculator.visit(ird, PV_ALL_CURVES);
    for (int i = 0; i < sensitivities.getNumberOfElements(); i++) {
      final YieldCurveFittingTestDataBundle bumpedData = getSingleCurveSetup(PresentValueCalculator.getInstance(), PresentValueCurveSensitivityCalculator.getInstance(), MATURITIES, getBumpedData(i, eps),
          true);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> f = new MultipleYieldCurveFinderFunction(bumpedData, bumpedData.getMarketValueCalculator());
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jf = new MultipleYieldCurveFinderJacobian(bumpedData, bumpedData.getMarketValueSensitivityCalculator());
      final DoubleMatrix1D bumpedNodes = ROOT_FINDER.getRoot(f, jf, bumpedData.getStartPosition());
      final YieldCurveBundle bumpedCurves = getYieldCurveMap(bumpedData, bumpedNodes);
      final YieldCurveBundle allBumpedCurves = getAllCurves(bumpedData, bumpedCurves);
      final double pv2 = calculator.visit(ird, allBumpedCurves);
      final double delta = pv2 - pv1;
      if (Math.abs(sensitivities.getEntry(i)) > 1e-3) {
        assertEquals(0, (delta - sensitivities.getEntry(i) * eps) / sensitivities.getEntry(i), eps);
      } else {
        assertEquals(0, sensitivities.getEntry(i), 1e-4);
        assertEquals(0, delta, 1e-4);
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

  private static Map<String, double[]> getSingleCurveMaturities() {
    final LinkedHashMap<String, double[]> maturities = new LinkedHashMap<String, double[]>();
    maturities.put("libor", new double[] {0.25 });
    maturities.put("fra", new double[] {0.51, 0.74 });
    maturities.put("swap", new double[] {1, 2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30 });
    return maturities;
  }

  private static Map<String, double[]> getSingleCurveMarketData() {
    final LinkedHashMap<String, double[]> marketRates = new LinkedHashMap<String, double[]>();
    marketRates.put("libor", new double[] {0.02 }); //
    marketRates.put("fra", new double[] {0.0366, 0.04705 });
    marketRates.put("swap", new double[] {0.04285, 0.03953, 0.03986, 0.040965, 0.042035, 0.04314, 0.044, 0.046045, 0.048085, 0.048925, 0.049155, 0.049195 });
    return marketRates;
  }

  private static Map<String, double[]> getBumpedData(final int n, final double eps) {
    final Map<String, double[]> data = new LinkedHashMap<String, double[]>(getSingleCurveMarketData());
    int i = 0;
    for (final Map.Entry<String, double[]> entry : data.entrySet()) {
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

  private static YieldCurveFittingTestDataBundle getSingleCurveSetup(final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator,
      final Map<String, double[]> maturities, final Map<String, double[]> marketRates, final boolean isPV) {
    final List<String> curveNames = new ArrayList<String>();
    curveNames.add(CURVE_NAME);
    int nNodes = 0;
    for (final double[] temp : maturities.values()) {
      nNodes += temp.length;
    }
    final double[] temp = new double[nNodes];
    int index = 0;
    for (final double[] times : maturities.values()) {
      for (final double t : times) {
        temp[index++] = t;
      }
    }
    Arrays.sort(temp);
    final List<double[]> curveKnots = new ArrayList<double[]>();
    curveKnots.add(temp);
    // now get market prices
    final double[] marketValues = new double[nNodes];
    final List<InstrumentDerivative> instruments = new ArrayList<InstrumentDerivative>();
    InstrumentDerivative ird;
    index = 0;
    for (final String name : maturities.keySet()) {
      final double[] times = maturities.get(name);
      final double[] rates = marketRates.get(name);
      Validate.isTrue(times.length == rates.length);
      for (int i = 0; i < times.length; i++) {
        ird = makeSingleCurrencyIRD(name, times[i],FRQ, CURVE_NAME, CURVE_NAME, rates[i], 1);
        instruments.add(ird);
        if (isPV) {
          marketValues[index] = 0;
        } else {
          marketValues[index] = rates[i];
        }
        index++;
      }
    }    
    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.04;
    }
    rates[0] = 0.02;
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);
    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, curveNames, curveKnots, INTERPOLATOR,
        calculator, sensitivityCalculator, marketValues, startPosition, null, false);
    return data;
  }

}

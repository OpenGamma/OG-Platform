/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory;
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
public class InstrumentSensitivityCalculatorTest extends YieldCurveFittingSetup {

  @Test
  public void test() {
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    final YieldCurveFittingTestDataBundle data = getSingleCurveSetup(ParRateCurveSensitivityCalculator.getInstance());

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new MultipleYieldCurveFinderFunction(data, data.getMarketValueCalculator());
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = null;

    if (data.getTestType() == TestType.ANALYTIC_JACOBIAN) {
      jacFunc = new MultipleYieldCurveFinderJacobian(data, data.getMarketValueSensitivityCalculator());
    } else if (data.getTestType() == TestType.FD_JACOBIAN) {
      final VectorFieldFirstOrderDifferentiator fdJacCalculator = new VectorFieldFirstOrderDifferentiator();
      jacFunc = fdJacCalculator.derivative(func);
    } else {
      throw new IllegalArgumentException("unknown TestType " + data.getTestType());

    }

    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(func, jacFunc, data.getStartPosition());
    System.out.println(yieldCurveNodes + "\n");
    final DoubleMatrix2D jacobian = jacFunc.evaluate(yieldCurveNodes);

    final HashMap<String, double[]> yields = unpackYieldVector(data, yieldCurveNodes);

    final LinkedHashMap<String, YieldAndDiscountCurve> curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    for (final String name : data.getCurveNames()) {
      final YieldAndDiscountCurve curve = makeYieldCurve(yields.get(name), data.getCurveNodePointsForCurve(name), data.getInterpolatorForCurve(name));
      curves.put(name, curve);
    }

    final YieldCurveBundle allCurves = new YieldCurveBundle(curves);
    if (data.getKnownCurves() != null) {
      allCurves.addAll(data.getKnownCurves());
    }

    final InstrumentSensitivityCalculator isc = InstrumentSensitivityCalculator.getInstance();
    for (int i = 0; i < data.getNumInstruments(); i++) {
      final DoubleMatrix1D bucketedDelta = isc.calculateFromParRate(data.getDerivative(i), data.getKnownCurves(), curves, jacobian);
      final double sense = PresentValueCouponSensitivityCalculator.getInstance().visit(data.getDerivative(i), allCurves);
      // PresentValueCouponSensitivityCalculator is sensitivity to change in the coupon rate for that instrument - what we calculate here is the (hypothetical) change of PV of the
      // instrument with a fixed coupon when its par-rate change - this is exactly the negative of the coupon sensitivity
      assertEquals(-sense, bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < data.getNumInstruments(); j++) {
        if (j != i) {
          assertEquals(0.0, bucketedDelta.getEntry(j), 1e-7);
        }
      }
    }
  }

  @Test
  public void testwithPV() {
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    final YieldCurveFittingTestDataBundle data = getSingleCurveSetup(PresentValueSensitivityCalculator.getInstance());

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new MultipleYieldCurveFinderFunction(data, data.getMarketValueCalculator());
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = null;

    if (data.getTestType() == TestType.ANALYTIC_JACOBIAN) {
      jacFunc = new MultipleYieldCurveFinderJacobian(data, data.getMarketValueSensitivityCalculator());
    } else if (data.getTestType() == TestType.FD_JACOBIAN) {
      final VectorFieldFirstOrderDifferentiator fdJacCalculator = new VectorFieldFirstOrderDifferentiator();
      jacFunc = fdJacCalculator.derivative(func);
    } else {
      throw new IllegalArgumentException("unknown TestType " + data.getTestType());

    }

    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(func, jacFunc, data.getStartPosition());
    final DoubleMatrix2D jacobian = jacFunc.evaluate(yieldCurveNodes);

    final HashMap<String, double[]> yields = unpackYieldVector(data, yieldCurveNodes);

    final LinkedHashMap<String, YieldAndDiscountCurve> curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    for (final String name : data.getCurveNames()) {
      final YieldAndDiscountCurve curve = makeYieldCurve(yields.get(name), data.getCurveNodePointsForCurve(name), data.getInterpolatorForCurve(name));
      curves.put(name, curve);
    }

    final YieldCurveBundle allCurves = new YieldCurveBundle(curves);
    if (data.getKnownCurves() != null) {
      allCurves.addAll(data.getKnownCurves());
    }

    final InstrumentSensitivityCalculator isc = InstrumentSensitivityCalculator.getInstance();
    double[] couponSensitivity = new double[data.getNumInstruments()];
    for (int i = 0; i < data.getNumInstruments(); i++) {
      couponSensitivity[i] = PresentValueCouponSensitivityCalculator.getInstance().visit(data.getDerivative(i), allCurves);
      // PresentValueCouponSensitivityCalculator is sensitivity to change in the coupon rate for that instrument - what we calculate here is the (hypothetical) change of PV of the
      // instrument with a fixed coupon when its par-rate change - this is exactly the negative of the coupon sensitivity
    }
    for (int i = 0; i < data.getNumInstruments(); i++) {
      final DoubleMatrix1D bucketedDelta = isc.calculateFromPresentValue(data.getDerivative(i), data.getKnownCurves(), curves, new DoubleMatrix1D(couponSensitivity), jacobian);

      assertEquals(-couponSensitivity[i], bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < data.getNumInstruments(); j++) {
        if (j != i) {
          assertEquals(0.0, bucketedDelta.getEntry(j), 1e-7);
        }
      }
    }
  }

  private YieldCurveFittingTestDataBundle getSingleCurveSetup(final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator) {

    final List<String> curveNames = new ArrayList<String>();
    curveNames.add("single curve");
    final String interpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;

    final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolator, LINEAR_EXTRAPOLATOR,
        FLAT_EXTRAPOLATOR);
    final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> extrapolatorWithSense = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
        .getSensitivityCalculator(interpolator, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, false);

    final InterestRateDerivativeVisitor<YieldCurveBundle, Double> calculator = ParRateCalculator.getInstance();

    // final HashMap<String, double[]> maturities = new LinkedHashMap<String, double[]>();
    // maturities.put("libor", new double[] {0.019164956, 0.038329911, 0.084873374, 0.169746749, 0.251882272, 0.336755647, 0.41889117, 0.503764545, 0.588637919, 0.665297741, 0.750171116, 0.832306639,
    // 0.917180014, 0.999315537});
    // maturities.put("fra", new double[] {1.437371663, 1.686516085, 1.938398357});
    // // maturities.put("swap", new double[] {3.000684463, 4, 4.999315537, 7.000684463, 10.00136893, 15.00068446, 20, 24.99931554, 30.00136893, 35.00068446, 50.00136893});
    // maturities.put("swap", new double[] {1, 2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30});
    //
    // final HashMap<String, double[]> marketRates = new LinkedHashMap<String, double[]>();
    // marketRates.put("libor", new double[] {0.0506375, 0.05075, 0.0513, 0.0518625, 0.0523625, 0.0526125, 0.052925, 0.053175, 0.053375, 0.0535188, 0.0536375, 0.0537563, 0.0538438, 0.0539438}); //
    // marketRates.put("fra", new double[] {0.0566, 0.05705, 0.0572});
    // marketRates.put("swap", new double[] {0.04285, 0.03953, 0.03986, 0.040965, 0.042035, 0.04314, 0.044, 0.046045, 0.048085, 0.048925, 0.049155, 0.049195});

    final HashMap<String, double[]> maturities = new LinkedHashMap<String, double[]>();
    maturities.put("libor", new double[] {0.25});
    maturities.put("fra", new double[] {0.51, 0.74});

    maturities.put("swap", new double[] {1, 2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30});

    final HashMap<String, double[]> marketRates = new LinkedHashMap<String, double[]>();
    marketRates.put("libor", new double[] {0.02}); //
    marketRates.put("fra", new double[] {0.0366, 0.04705});
    marketRates.put("swap", new double[] {0.04285, 0.03953, 0.03986, 0.040965, 0.042035, 0.04314, 0.044, 0.046045, 0.048085, 0.048925, 0.049155, 0.049195});

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

    final List<InterestRateDerivative> instruments = new ArrayList<InterestRateDerivative>();
    InterestRateDerivative ird;
    index = 0;
    for (final String name : maturities.keySet()) {
      final double[] times = maturities.get(name);
      final double[] rates = marketRates.get(name);
      Validate.isTrue(times.length == rates.length);
      for (int i = 0; i < times.length; i++) {
        ird = makeIRD(name, times[i], curveNames.get(0), curveNames.get(0), rates[i]);
        instruments.add(ird);
        marketValues[index] = rates[i];
        index++;
      }
    }

    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.04;
    }
    rates[0] = 0.02;

    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, curveNames, curveKnots, extrapolator, extrapolatorWithSense, calculator, sensitivityCalculator,
        marketValues, startPosition, null);

    return data;

  }

}

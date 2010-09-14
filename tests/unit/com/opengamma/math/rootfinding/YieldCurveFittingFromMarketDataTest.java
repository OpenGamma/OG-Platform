/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import org.apache.commons.lang.Validate;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.ParRateDifferenceCalculator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory;
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
public class YieldCurveFittingFromMarketDataTest extends YieldCurveFittingSetup {

  public YieldCurveFittingFromMarketDataTest() {
    _logger = LoggerFactory.getLogger(YieldCurveFittingFromMarketDataTest.class);
    _hotspotWarmupCycles = 0;
    _benchmarkCycles = 1;
  }

  @Test
  public void testNewton() {
    NewtonVectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, market Data (Libor, FRA, swaps). Root finder: Newton");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve, market Data (Libor, FRA, swaps). Root finder: Newton (FD Jacobian)");

  }

  @Test
  public void testShermanMorrison() {
    NewtonVectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, market Data (Libor, FRA, swaps). Root finder: ShermanMorrison");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data,
        "Single curve, market Data (Libor, FRA, swaps). Root finder:ShermanMorrison (FD Jacobian)");

  }

  @Test
  public void testBroyden() {
    NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, market Data (Libor, FRA, swaps). Root finder: Broyden");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve,market Data (Libor, FRA, swaps). Root finder: Broyden (FD Jacobian)");

  }

  private YieldCurveFittingTestDataBundle getSingleCurveSetup() {

    List<String> curveNames = new ArrayList<String>();
    curveNames.add("single curve");
    String interpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;

    CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> extrapolator = CombinedInterpolatorExtrapolatorFactory
        .getInterpolator(interpolator, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);
    CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> extrapolatorWithSense = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
        .getSensitivityCalculator(interpolator, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, false);

    InterestRateDerivativeVisitor<Double> calculator = ParRateDifferenceCalculator.getInstance();
    InterestRateDerivativeVisitor<Map<String, List<DoublesPair>>> sensitivityCalculator = ParRateCurveSensitivityCalculator
        .getInstance();

    HashMap<String, double[]> maturities = new LinkedHashMap<String, double[]>();
    maturities.put("libor", new double[] {0.019164956, 0.038329911, 0.084873374, 0.169746749, 0.251882272, 0.336755647,
        0.41889117, 0.503764545, 0.588637919, 0.665297741, 0.750171116, 0.832306639, 0.917180014, 0.999315537}); //
    maturities.put("fra", new double[] {1.437371663, 1.686516085, 1.938398357});
    maturities.put("swap", new double[] {/* 2.001368925, */3.000684463, 4, 4.999315537, 7.000684463, 10.00136893,
        15.00068446, 20, 24.99931554, 30.00136893, 35.00068446, 50.00136893});

    HashMap<String, double[]> marketRates = new LinkedHashMap<String, double[]>();
    marketRates.put("libor", new double[] {0.0506375, 0.05075, 0.0513, 0.0518625, 0.0523625, 0.0526125, 0.052925,
        0.053175, 0.053375, 0.0535188, 0.0536375, 0.0537563, 0.0538438, 0.0539438}); //
    marketRates.put("fra", new double[] {0.0566, 0.05705, 0.0572});
    marketRates.put("swap", new double[] {/* 0.05412, */0.054135, 0.054295, 0.05457, 0.055075, 0.055715, 0.05652,
        0.056865, 0.05695, 0.056925, 0.056885, 0.056725});

    int nNodes = 0;
    for (double[] temp : maturities.values()) {
      nNodes += temp.length;
    }

    double[] temp = new double[nNodes];
    int index = 0;
    for (double[] times : maturities.values()) {
      for (final double t : times) {
        temp[index++] = t;
      }
    }
    Arrays.sort(temp);
    List<double[]> curveKnots = new ArrayList<double[]>();
    curveKnots.add(temp);

    // now get market prices
    double[] marketValues = new double[nNodes];

    List<InterestRateDerivative> instruments = new ArrayList<InterestRateDerivative>();
    InterestRateDerivative ird;
    index = 0;
    for (String name : maturities.keySet()) {
      double[] times = maturities.get(name);
      double[] rates = marketRates.get(name);
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
      rates[i] = 0.05;
    }
    DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, curveNames,
        curveKnots, extrapolator, extrapolatorWithSense, calculator, sensitivityCalculator, marketValues,
        startPosition, null);

    return data;

  }
}

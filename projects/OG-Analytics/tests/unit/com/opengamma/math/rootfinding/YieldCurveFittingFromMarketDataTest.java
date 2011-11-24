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

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
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
public class YieldCurveFittingFromMarketDataTest extends YieldCurveFittingSetup {
  private static final Logger LOGGER = LoggerFactory.getLogger(YieldCurveFittingFromMarketDataTest.class);
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

  @Test
  public void testNewton() {
    final NewtonVectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS);
    final YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, market Data (Libor, FRA, swaps). Root finder: Newton");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve, market Data (Libor, FRA, swaps). Root finder: Newton (FD Jacobian)");

  }

  @Test
  public void testShermanMorrison() {
    final NewtonVectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
    final YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, market Data (Libor, FRA, swaps). Root finder: ShermanMorrison");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data,
        "Single curve, market Data (Libor, FRA, swaps). Root finder:ShermanMorrison (FD Jacobian)");

  }

  @Test
  public void testBroyden() {
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    final YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(rootFinder, data, "Single curve, market Data (Libor, FRA, swaps). Root finder: Broyden");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Single curve,market Data (Libor, FRA, swaps). Root finder: Broyden (FD Jacobian)");

  }

  private YieldCurveFittingTestDataBundle getSingleCurveSetup() {

    final List<String> curveNames = new ArrayList<String>();
    curveNames.add("single curve");
    final String interpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;

    final CombinedInterpolatorExtrapolator extrapolator = CombinedInterpolatorExtrapolatorFactory
        .getInterpolator(interpolator, LINEAR_EXTRAPOLATOR,
            FLAT_EXTRAPOLATOR);
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator = ParRateCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = ParRateCurveSensitivityCalculator
        .getInstance();
    // final InterestRateDerivativeVisitor<YieldCurveBundle, Double> calculator = PresentValueCalculator.getInstance();
    // final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = PresentValueSensitivityCalculator.getInstance();

    final HashMap<String, double[]> maturities = new LinkedHashMap<String, double[]>();
    maturities.put("libor", new double[] {0.019164956, 0.038329911, 0.084873374, 0.169746749, 0.251882272, 0.336755647,
        0.41889117, 0.503764545, 0.588637919, 0.665297741, 0.750171116, 0.832306639,
        0.917180014, 0.999315537 }); //
    maturities.put("fra", new double[] {1.437371663, 1.686516085, 1.938398357 });
    maturities.put("swap", new double[] {/* 2.001368925, */3.000684463, 4, 4.999315537, 7.000684463, 10.00136893,
        15.00068446, 20, 24.99931554, 30.00136893, 35.00068446, 50.00136893 });
    maturities.put("swap", new double[] {1, 2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30 });

    final HashMap<String, double[]> marketRates = new LinkedHashMap<String, double[]>();
    marketRates.put("libor", new double[] {0.0506375, 0.05075, 0.0513, 0.0518625, 0.0523625, 0.0526125, 0.052925,
        0.053175, 0.053375, 0.0535188, 0.0536375, 0.0537563, 0.0538438, 0.0539438 }); //
    marketRates.put("fra", new double[] {0.0566, 0.05705, 0.0572 });
    // marketRates.put("swap", new double[] {/* 0.05412, */0.054135, 0.054295, 0.05457, 0.055075, 0.055715, 0.05652, 0.056865, 0.05695, 0.056925, 0.056885, 0.056725});
    marketRates.put("swap", new double[] {0.04285, 0.03953, 0.03986, 0.040965, 0.042035, 0.04314, 0.044, 0.046045,
        0.048085, 0.048925, 0.049155, 0.049195 });

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
        ird = makeSingleCurrencyIRD(name,  times[i], SimpleFrequency.QUARTERLY, curveNames.get(0), curveNames.get(0), rates[i], 1);
        instruments.add(ird);
        marketValues[index] = rates[i];
        index++;
      }
    }

    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.05;
    }
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, curveNames,
        curveKnots, extrapolator, calculator, sensitivityCalculator, marketValues, startPosition, null, false);

    return data;

  }
}

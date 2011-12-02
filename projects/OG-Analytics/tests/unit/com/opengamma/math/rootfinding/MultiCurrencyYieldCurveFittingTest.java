/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

import java.util.ArrayList;
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
import com.opengamma.financial.interestrate.YieldCurveBundle;
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
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class MultiCurrencyYieldCurveFittingTest extends YieldCurveFittingSetup {

  private static final Logger LOGGER = LoggerFactory.getLogger(MultiCurrencyYieldCurveFittingTest.class);
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

  private static final Currency DOMESTIC_CCY = Currency.USD;
  private static final Currency FOREIGN_CCY = Currency.JPY;
  private static final double spotFX = 1. / 76.335;
  private static final CurrencyAmount DOMESTIC_NOTIONAL = CurrencyAmount.of(DOMESTIC_CCY, 1e6);
  private static final CurrencyAmount FOREIGN_NOTIONAL = CurrencyAmount.of(FOREIGN_CCY, DOMESTIC_NOTIONAL.getAmount() / spotFX);

  protected static final Function1D<Double, Double> DOMESIC_DISCOUNT_CURVE = new Function1D<Double, Double>() {

    private static final double a = -0.0375;
    private static final double b = 0.0021;
    private static final double c = 0.2;
    private static final double d = 0.04;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  protected static final Function1D<Double, Double> DOMESIC_SPREAD_CURVE = new Function1D<Double, Double>() {

    private static final double a = 0.005;
    private static final double b = 0.002;
    private static final double c = 0.3;
    private static final double d = 0.001;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  protected static final Function1D<Double, Double> FOREIGN_DISCOUNT_CURVE = new Function1D<Double, Double>() {

    private static final double a = -0.024;
    private static final double b = 0.0015;
    private static final double c = 0.15;
    private static final double d = 0.025;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  protected static final Function1D<Double, Double> FOREIGN_SPREAD_CURVE = new Function1D<Double, Double>() {

    private static final double a = 0.007;
    private static final double b = 0.003;
    private static final double c = 0.35;
    private static final double d = 0.002;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  @Test
  public void testNewton() {
    final NewtonVectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getMultiCurveSetup();
    doHotSpot(rootFinder, data, "Multi curve, domestic OIS, CCS, domestic and foreign Libor,  FRA & swaps. Root finder: Newton");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Multi curve, domestic OIS, CCS, domestic and foreign Libor,  FRA & swaps. Root finder: Newton (FD Jacobian)");
  }
  
  @Test
  public void testShermanMorrison() {
    final NewtonVectorRootFinder rootFinder = new ShermanMorrisonVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getMultiCurveSetup();
    doHotSpot(rootFinder, data, "Multi curve, domestic OIS, CCS, domestic and foreign Libor,  FRA & swaps. Root finder:Sherman Morrison");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Multi curve, domestic OIS, CCS, domestic and foreign Libor,  FRA & swaps. Root finder: Sherman Morrison (FD Jacobian)");
  }

  @Test
  public void testBroyden() {
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(EPS, EPS, STEPS);
    YieldCurveFittingTestDataBundle data = getMultiCurveSetup();
    doHotSpot(rootFinder, data, "Multi curve, domestic OIS, CCS, domestic and foreign Libor,  FRA & swaps. Root finder: Broyden");
    data.setTestType(TestType.FD_JACOBIAN);
    doHotSpot(rootFinder, data, "Multi curve, domestic OIS, CCS, domestic and foreign Libor,  FRA & swaps. Root finder: Broyden (FD Jacobian)");
  }

  @Test
  public void testJacobian() {
    assertJacobian(getMultiCurveSetup());
  }

  private YieldCurveFittingTestDataBundle getMultiCurveSetup() {
    
    final SimpleFrequency paymentFreq = SimpleFrequency.QUARTERLY;

    final List<String> curveNames = new ArrayList<String>();
    curveNames.add("domestic funding curve");
    curveNames.add("domestic index curve");
    curveNames.add("foreign funding curve");
    curveNames.add("foreign index curve");
    final String interpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;

    final CombinedInterpolatorExtrapolator extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolator, LINEAR_EXTRAPOLATOR,
        FLAT_EXTRAPOLATOR);
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator = ParRateCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = ParRateCurveSensitivityCalculator.getInstance();

    final HashMap<String, double[]> domesticFundingMaturities = new LinkedHashMap<String, double[]>();
    final HashMap<String, double[]> domesticIndexMaturities = new LinkedHashMap<String, double[]>();
    final HashMap<String, double[]> foreignFundingMaturities = new LinkedHashMap<String, double[]>();
    final HashMap<String, double[]> foreignIndexMaturities = new LinkedHashMap<String, double[]>();
    final HashMap<String, double[]> foreignMaturities = new LinkedHashMap<String, double[]>();

    //domestic OIS instrument  - funding is assumed at OIS rate, so funding curve found from these along
    domesticFundingMaturities.put("OIS", new double[] { 1./365, 1./12 , 3./12, 6./12, 1.0, 2.0, 5.0, 10, 15.0, 20., 30.});
     
    //need instruments to find domestic index (3M Libor) curve
    domesticIndexMaturities.put("libor", new double[] {3. / 12 }); //3M spot Libor
    domesticIndexMaturities.put("fra", new double[] {0.5, 0.75 });
    domesticIndexMaturities.put("swap", new double[] {1.00, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556 });

    //foreign funding and index curves must be found together from Libor instruments (spot libor, FRAs & swaps) and Forex instruments (Forward FX and Cross Currency Swaps (CCS))
    foreignIndexMaturities.put("libor", new double[] {3. / 12 });
    foreignIndexMaturities.put("fra", new double[] {0.5, 0.75 });
    foreignIndexMaturities.put("swap", new double[] {2.0, 4.0, 7.0, 10., 15., 20., 25., 30. });
    foreignFundingMaturities.put("ForexFwd", new double[] {1. / 52, 2. / 52., 1. / 12, 3. / 12, 6. / 12, 1.0 });
    foreignFundingMaturities.put("CCS", new double[] {2., 3., 5., 7., 10., 15, 20., 30. });   
    foreignMaturities.putAll(foreignFundingMaturities);
    foreignMaturities.putAll(foreignIndexMaturities);

    final List<double[]> curveKnots = new ArrayList<double[]>();
    curveKnots.add(catMap(domesticFundingMaturities));
    curveKnots.add(catMap(domesticIndexMaturities));
    curveKnots.add(catMap(foreignFundingMaturities));
    curveKnots.add(catMap(foreignIndexMaturities));

    // set up curve to obtain "market" prices
    final List<double[]> yields = new ArrayList<double[]>();
    double[] temp = new double[curveKnots.get(0).length];
    int index = 0;
    for (final double t : curveKnots.get(0)) {
      temp[index++] = DOMESIC_DISCOUNT_CURVE.evaluate(t);
    }
    yields.add(temp);
    temp = new double[curveKnots.get(1).length];
    index=0;
    for (final double t : curveKnots.get(1)) {
      temp[index++] = DOMESIC_DISCOUNT_CURVE.evaluate(t) + DOMESIC_SPREAD_CURVE.evaluate(t);
    }
    yields.add(temp);
    temp = new double[curveKnots.get(2).length];
    index = 0;
    for (final double t : curveKnots.get(2)) {
      temp[index++] = FOREIGN_DISCOUNT_CURVE.evaluate(t);
    }
    yields.add(temp);
    temp = new double[curveKnots.get(3).length];
    index = 0;
    for (final double t : curveKnots.get(3)) {
      temp[index++] = FOREIGN_DISCOUNT_CURVE.evaluate(t) + FOREIGN_SPREAD_CURVE.evaluate(t);
    }
    yields.add(temp);

    // now get market prices
    final int nNodes = curveKnots.get(0).length + curveKnots.get(1).length + curveKnots.get(2).length + curveKnots.get(3).length;
    final double[] marketValues = new double[nNodes];

    final YieldCurveBundle bundle = new YieldCurveBundle();
  
    for (int i = 0; i < yields.size(); i++) {
      if (curveKnots.get(i).length > 0) {
        bundle.setCurve(curveNames.get(i), makeYieldCurve(yields.get(i), curveKnots.get(i), extrapolator));
      }
    }

    final List<InstrumentDerivative> instruments = new ArrayList<InstrumentDerivative>();
    InstrumentDerivative ird = null;
    index = 0;
    
    //the domestic funding curve instruments 
    for (final String name : domesticFundingMaturities.keySet()) {
      for (final double t : domesticFundingMaturities.get(name)) {
        ird = makeSingleCurrencyIRD(name, t, paymentFreq, curveNames.get(0), curveNames.get(0), 0.0, DOMESTIC_NOTIONAL.getAmount());
        marketValues[index] = ParRateCalculator.getInstance().visit(ird, bundle);
        instruments.add(REPLACE_RATE.visit(ird, marketValues[index]));
        index++;
      }
    }
    
    //the domestic Libor instruments 
    for (final String name : domesticIndexMaturities.keySet()) {
      for (final double t : domesticIndexMaturities.get(name)) {
        ird = makeSingleCurrencyIRD(name, t, paymentFreq, curveNames.get(0), curveNames.get(1), 0.0, DOMESTIC_NOTIONAL.getAmount());
        marketValues[index] = ParRateCalculator.getInstance().visit(ird, bundle);
        instruments.add(REPLACE_RATE.visit(ird, marketValues[index]));
        index++;
      }
    }
    //the foreign Libor instruments 
    for (final String name : foreignIndexMaturities.keySet()) {
      for (final double t : foreignIndexMaturities.get(name)) {
        ird = makeSingleCurrencyIRD(name, t, paymentFreq, curveNames.get(2), curveNames.get(3), 0.0, FOREIGN_NOTIONAL.getAmount());
        marketValues[index] = ParRateCalculator.getInstance().visit(ird, bundle);
        instruments.add(REPLACE_RATE.visit(ird, marketValues[index]));
        index++;
      }
    }
    //the Forex instruments 
    for (final String name : foreignFundingMaturities.keySet()) {
      for (final double t : foreignFundingMaturities.get(name)) {
        if ("ForexFwd".equals(name)) {
          ird = makeForexForward(DOMESTIC_NOTIONAL, FOREIGN_NOTIONAL.multipliedBy(-1.0), t, spotFX, curveNames.get(0), curveNames.get(2));
        } else if ("CCS".equals(name)) {
          ird = makeCrossCurrencySwap(DOMESTIC_NOTIONAL, FOREIGN_NOTIONAL, (int) t, SimpleFrequency.QUARTERLY, SimpleFrequency.QUARTERLY, curveNames.get(0), curveNames.get(1), curveNames.get(2),
              curveNames.get(3), 0.0);
        } else {
          throw new IllegalArgumentException("Unknown forex type " + name);
        }

        marketValues[index] = ParRateCalculator.getInstance().visit(ird, bundle);
        instruments.add(REPLACE_RATE.visit(ird, marketValues[index]));
       // marketValues[index] = 0.0; //for PV cal
        index++;
      }
    }

    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.05;
    }
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);


    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, curveNames, curveKnots, extrapolator, calculator,
        sensitivityCalculator, marketValues, startPosition, yields, false);

    return data;
  }

}

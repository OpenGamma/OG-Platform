/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.interestrate.SABRTermStructureParameters;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.volatility.VolatilityModel1D;
import com.opengamma.math.curve.AddCurveSpreadFunction;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.curve.SpreadDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.math.minimization.ConjugateDirectionVectorMinimizer;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.money.Currency;

/**
 * 
 */
@SuppressWarnings("unchecked")
public class CapletStrippingTest {

  private static Function1D<Double, Double> ALPHA = new Function1D<Double, Double>() {

    private double a = -0.01;
    private double b = 0.05;
    private double c = 0.3;
    private double d = 0.04;

    @Override
    public Double evaluate(Double t) {
      return (a + t * b) * Math.exp(-c * t) + d;
    }
  };

  private static Function1D<Double, Double> BETA = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(Double t) {
      return 0.5;
    }
  };

  private static Function1D<Double, Double> NU = new Function1D<Double, Double>() {

    private double a = 0.8;
    private double b = 0.0;
    private double c = 0.1;
    private double d = 0.4;

    @Override
    public Double evaluate(Double t) {
      return (a + t * b) * Math.exp(-c * t) + d;
    }
  };

  private static Function1D<Double, Double> RHO = new Function1D<Double, Double>() {

    private double a = -0.7;
    private double b = 0.0;
    private double c = 0.1;
    private double d = 0.0;

    @Override
    public Double evaluate(Double t) {
      return (a + t * b) * Math.exp(-c * t) + d;
    }
  };

  protected static final Function1D<Double, Double> DISCOUNT_CURVE = new Function1D<Double, Double>() {

    private static final double a = -0.0375;
    private static final double b = 0.0021;
    private static final double c = 0.2;
    private static final double d = 0.04;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  protected static final Function1D<Double, Double> SPREAD_CURVE = new Function1D<Double, Double>() {

    private static final double a = 0.005;
    private static final double b = 0.002;
    private static final double c = 0.3;
    private static final double d = 0.001;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  private static VolatilityModel1D VOL_MODEL = new SABRTermStructureParameters(FunctionalDoublesCurve.from(ALPHA), FunctionalDoublesCurve.from(BETA),
      FunctionalDoublesCurve.from(NU), FunctionalDoublesCurve.from(RHO));

  private static YieldCurveBundle YIELD_CURVES;
  private static List<CapFloor> CAPS;
  private static double[] MARKET_PRICES;
  private static double[] MARKET_VOLS;
  private static double[] MARKET_VEGAS;

  private static int[] CAP_MATURITIES = new int[] {1, 2, 3, 5, 10, 15, 20 };
  private static double[] NODES = new double[] {0., 1, 2, 3, 5, 10, 15, 20 };
  private static double[] STRIKES = new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07, 0.1 };
  private static LinkedHashMap<String, double[]> CURVE_NODES;
  private static LinkedHashMap<String, Interpolator1D> INTERPOLATORS;
  private static LinkedHashMap<String, ParameterLimitsTransform> TRANSFORMS;
  private static String[] NAMES = new String[] {"alpha", "beta", "nu", "rho" };

  static {
    YIELD_CURVES = new YieldCurveBundle();
    YIELD_CURVES.setCurve("funding", new YieldCurve(FunctionalDoublesCurve.from(DISCOUNT_CURVE)));
    YIELD_CURVES.setCurve("3m Libor", new YieldCurve(SpreadDoublesCurve.from(new AddCurveSpreadFunction(),
        FunctionalDoublesCurve.from(DISCOUNT_CURVE), FunctionalDoublesCurve.from(SPREAD_CURVE))));

    CAPS = new ArrayList<CapFloor>(CAP_MATURITIES.length * STRIKES.length);
    MARKET_PRICES = new double[CAP_MATURITIES.length * STRIKES.length];
    MARKET_VOLS = new double[CAP_MATURITIES.length * STRIKES.length];
    MARKET_VEGAS = new double[CAP_MATURITIES.length * STRIKES.length];

    CURVE_NODES = new LinkedHashMap<String, double[]>();
    INTERPOLATORS = new LinkedHashMap<String, Interpolator1D>();
    int count = 0;
    for (int i = 0; i < CAP_MATURITIES.length; i++) {
      for (String name : NAMES) {
        CURVE_NODES.put(name, NODES);
        INTERPOLATORS.put(name, new DoubleQuadraticInterpolator1D());
      }
      for (int j = 0; j < STRIKES.length; j++) {
        CapFloor cap = makeCap(CAP_MATURITIES[i], SimpleFrequency.QUARTERLY, "funding", "3m Libor", STRIKES[j]);
        CAPS.add(cap);
        CapFloorPricer pricer = new CapFloorPricer(cap, YIELD_CURVES);
        MARKET_PRICES[count] = pricer.price(VOL_MODEL);
        MARKET_VOLS[count] = pricer.impliedVol(VOL_MODEL);
        MARKET_VEGAS[count] = 0.001 * pricer.vega(VOL_MODEL);
        count++;
      }
    }

    TRANSFORMS = new LinkedHashMap<String, ParameterLimitsTransform>();
    TRANSFORMS.put(NAMES[0], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN)); //alpha > 0
    TRANSFORMS.put(NAMES[1], new DoubleRangeLimitTransform(0, 1)); //0<beta<1
    TRANSFORMS.put(NAMES[2], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN));
    TRANSFORMS.put(NAMES[3], new DoubleRangeLimitTransform(-1, 1)); //-0.95<rho<0.95
  }

  private void printInitialCurves() {

    int n = 100;
    for (int j = 0; j < n; j++) {
      double t = j * 20. / (n - 1);
      System.out.print(t + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < n; j++) {
        double t = j * 20. / (n - 1);
        double p = 0;
        switch (i) {
          case 0:
            p = ALPHA.evaluate(t);
            break;
          case 1:
            p = BETA.evaluate(t);
            break;
          case 2:
            p = NU.evaluate(t);
            break;
          case 3:
            p = RHO.evaluate(t);
            break;
        }
        System.out.print(p + "\t");
      }
      System.out.print("\n");
    }
  }

  @Test
  public void testStripping() {
    double[] start = new double[4 * NODES.length];
    //start from some realistic values, and transform these into the fitting parameters 
    Arrays.fill(start, 0, NODES.length, TRANSFORMS.get(NAMES[0]).transform(0.3));
    Arrays.fill(start, NODES.length, 2 * NODES.length, TRANSFORMS.get(NAMES[1]).transform(0.7));
    Arrays.fill(start, 2 * NODES.length, 3 * NODES.length, TRANSFORMS.get(NAMES[2]).transform(0.35));
    Arrays.fill(start, 3 * NODES.length, 4 * NODES.length, TRANSFORMS.get(NAMES[3]).transform(-0.2));

    CapletStrippingFunction func = new CapletStrippingFunction(CAPS, YIELD_CURVES, CURVE_NODES, INTERPOLATORS, TRANSFORMS, null);
    //   ConjugateDirectionVectorMinimizer minimiser = new ConjugateDirectionVectorMinimizer(new BrentMinimizer1D());

    //debug
    DoubleMatrix1D debug = func.evaluate(new DoubleMatrix1D(start));
    for (int i = 0; i < MARKET_PRICES.length; i++) {
      System.out.println(i + " " + MARKET_PRICES[i]);
    }
    System.out.println("debug: " + debug.toString());

    //    DoubleMatrix1D res = minimiser.minimize(func, new DoubleMatrix1D(start));
    //    System.out.println(res.toString());

    double[] sigma = new double[MARKET_PRICES.length];
    Arrays.fill(sigma, 0.001); //10bps

    NonLinearLeastSquare ls = new NonLinearLeastSquare();
    LeastSquareResults lsRes = ls.solve(new DoubleMatrix1D(MARKET_PRICES), new DoubleMatrix1D(MARKET_VEGAS), func, new DoubleMatrix1D(start));
    System.out.println("chi2: " + lsRes.getChiSq());
    DoubleMatrix1D res = lsRes.getParameters();
    double[][] fittedNodes = new double[4][NODES.length];

    for (int j = 0; j < NODES.length; j++) {
      System.out.print(NODES[j] + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < NODES.length; j++) {
        fittedNodes[i][j] = TRANSFORMS.get(NAMES[i]).inverseTransform(res.getEntry(i * NODES.length + j));
        System.out.print(fittedNodes[i][j] + "\t");
      }
      System.out.print("\n");
    }

    printInitialCurves();

  }

  private static CapFloor makeCap(final int years, final SimpleFrequency freq, final String discountCurve, final String indexCurve,
       final double strike) {
    int n = (int) (years * freq.getPeriodsPerYear()) - 1; //first caplet missing
    double tau = 1.0 / freq.getPeriodsPerYear();

    CapFloorIbor[] caplets = new CapFloorIbor[n];
    for (int i = 0; i < n; i++) {
      double fixingStart = (i + 1) * tau;
      double fixingEnd = (i + 2) * tau;
      caplets[i] = new CapFloorIbor(Currency.USD, fixingEnd, discountCurve, tau, 1.0, fixingStart, fixingStart, fixingEnd, tau, indexCurve, strike, true);
    }
    return new CapFloor(caplets);
  }
}

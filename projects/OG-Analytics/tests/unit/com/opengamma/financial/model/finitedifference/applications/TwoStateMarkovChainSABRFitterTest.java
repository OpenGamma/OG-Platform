/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.DupireLocalVolatilityCalculator;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class TwoStateMarkovChainSABRFitterTest {

  private static final Function1D<Double, Double> ALPHA;
  private static final double BETA = 1.0;
  private static final double RHO = -0.0;
  private static final Function1D<Double, Double> NU;
  private static final double T = 5.0;
  private static final double SPOT = 1.0;
  private static final ForwardCurve FORWARD_CURVE;
  private static final YieldCurve YIELD_CURVE;
  private static final double RATE = 0.0;
  private static final Function<Double, Double> SABR_VOL_FUNCTION;
  private static final List<double[]> EXPIRY_AND_STRIKES = new ArrayList<double[]>();
  private static final List<Pair<double[], Double>> SABR_VOLS;

  static {
    EXPIRY_AND_STRIKES.add(new double[] {1. / 12, 0.925 });
    EXPIRY_AND_STRIKES.add(new double[] {1. / 12, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {1. / 12, 1.075 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 0.95 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 1.05 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 1.1 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 0.8 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 1.1 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 0.7 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 1.2 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 1.4 });
    EXPIRY_AND_STRIKES.add(new double[] {3, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {4, 1.0 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1. / 12., 0.4 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1. / 12., 0.6 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1. / 12., 0.8 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1. / 12., 1.0 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1. / 12., 1.5 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1. / 12., 2.0 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1. / 12., 3.0 });
    //
    //    EXPIRY_AND_STRIKES.add(new double[] {1., 0.4 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1., 0.6 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1., 0.8 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1., 1.0 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1., 1.5 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1., 2.0 });
    //    EXPIRY_AND_STRIKES.add(new double[] {1., 3.0 });
    //
    //    EXPIRY_AND_STRIKES.add(new double[] {2., 0.4 });
    //    EXPIRY_AND_STRIKES.add(new double[] {2., 0.6 });
    //    EXPIRY_AND_STRIKES.add(new double[] {2., 0.8 });
    //    EXPIRY_AND_STRIKES.add(new double[] {2., 1.0 });
    //    EXPIRY_AND_STRIKES.add(new double[] {2., 1.5 });
    //    EXPIRY_AND_STRIKES.add(new double[] {2., 2.0 });
    //    EXPIRY_AND_STRIKES.add(new double[] {2., 3.0 });
    //
    //    EXPIRY_AND_STRIKES.add(new double[] {3., 0.4 });
    //    EXPIRY_AND_STRIKES.add(new double[] {3., 0.6 });
    //    EXPIRY_AND_STRIKES.add(new double[] {3., 0.8 });
    //    EXPIRY_AND_STRIKES.add(new double[] {3., 1.0 });
    //    EXPIRY_AND_STRIKES.add(new double[] {3., 1.5 });
    //    EXPIRY_AND_STRIKES.add(new double[] {3., 2.0 });
    //    EXPIRY_AND_STRIKES.add(new double[] {3., 3.0 });

    EXPIRY_AND_STRIKES.add(new double[] {5, 0.4 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.6 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.8 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 2.0 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 2.5 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 3.0 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 3.5 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 4.0 });

    Function1D<Double, Double> fwd = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double t) {
        return SPOT * Math.exp(t * RATE);
      }
    };

    FORWARD_CURVE = new ForwardCurve(fwd);
    YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));

    ALPHA = new Function1D<Double, Double>() {
      double a = 0.0;
      double b = 0.0;
      double c = 0.0;
      double d = 0.2;

      @Override
      public Double evaluate(Double t) {
        double atmVol = (a + b * t) * Math.exp(-c * t) + d;
        return atmVol * Math.pow(SPOT, 1 - BETA);
      }
    };

    NU = new Function1D<Double, Double>() {
      double a = 0.6;
      double b = 0.0;
      double c = 1.0;
      double d = 0.3;

      @Override
      public Double evaluate(Double t) {
        return (a + b * t) * Math.exp(-c * t) + d;
      }
    };

    SABR_VOL_FUNCTION = new Function<Double, Double>() {
      SABRHaganVolatilityFunction hagan = new SABRHaganVolatilityFunction();

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];
        EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        Function1D<SABRFormulaData, Double> func = hagan.getVolatilityFunction(option);
        SABRFormulaData data = new SABRFormulaData(FORWARD_CURVE.getForward(t), ALPHA.evaluate(t), BETA, NU.evaluate(t), RHO);
        return func.evaluate(data);
      }
    };

    SABR_VOLS = new ArrayList<Pair<double[], Double>>(EXPIRY_AND_STRIKES.size());
    for (int i = 0; i < EXPIRY_AND_STRIKES.size(); i++) {
      double[] tk = EXPIRY_AND_STRIKES.get(i);
      Pair<double[], Double> pair = new ObjectsPair<double[], Double>(tk, SABR_VOL_FUNCTION.evaluate(tk[0], tk[1]));
      SABR_VOLS.add(pair);
    }

  }

  @Test
  public void dumpSurfaceTest() {

    for (int i = 0; i < 101; i++) {
      double k = SPOT / 4.0 + 4.0 * SPOT * i / 100.;
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j < 101; j++) {
      double t = 0.0 + 5.0 * j / 100.;
      System.out.print(t);
      for (int i = 0; i < 101; i++) {
        double k = SPOT / 4.0 + 4.0 * SPOT * i / 100.;

        System.out.print("\t" + SABR_VOL_FUNCTION.evaluate(t, k));
      }
      System.out.print("\n");
    }
  }

  @Test
  public void test() {
    DoubleMatrix1D initialGuess = new DoubleMatrix1D(new double[] {0.2, 0.3, 0.2, 2.0, 0.95, 0.8 });
    TwoStateMarkovChainFitter fitter = new TwoStateMarkovChainFitter();
    LeastSquareResults res = fitter.fit(FORWARD_CURVE, SABR_VOLS, initialGuess);
    System.out.println("chi^2:" + res.getChiSq() + "\n params: " + res.getParameters().toString());
  }

  @Test
  public void localVolFitTest() {
    DoubleMatrix1D initialGuess = new DoubleMatrix1D(new double[] {0.2, 0.8, 0.2, 2.0, 0.6, 1.0 });
    TwoStateMarkovChainLocalVolFitter fitter = new TwoStateMarkovChainLocalVolFitter();
    fitter.fit(FORWARD_CURVE, new BlackVolatilitySurface(FunctionalDoublesSurface.from(SABR_VOL_FUNCTION)), SABR_VOLS, initialGuess);
    // System.out.println("chi^2:" + res.getChiSq() + "\n params: " + res.getParameters().toString());
  }

  @Test
  public void localVolTest() {
    DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();

    BlackVolatilitySurface volSurface = new BlackVolatilitySurface(FunctionalDoublesSurface.from(SABR_VOL_FUNCTION));
    LocalVolatilitySurface localVol = cal.getLocalVolatility(volSurface, SPOT, RATE);

    for (int i = 0; i < 101; i++) {
      double f = SPOT / 4.0 + 4.0 * SPOT * i / 100.;
      System.out.print("\t" + f);
    }
    System.out.print("\n");

    for (int j = 0; j < 101; j++) {
      double t = 0.0 + 5.0 * j / 100.;
      System.out.print(t);
      for (int i = 0; i < 101; i++) {
        double f = SPOT / 4.0 + 4.0 * SPOT * i / 100.;

        System.out.print("\t" + localVol.getVolatility(t, f));
      }
      System.out.print("\n");
    }

  }
}

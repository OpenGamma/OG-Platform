/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.ShiftedLogNormalTailExtrapolation;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.ShiftedLogNormalTailExtrapolationFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorMixedLogNormal;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABRWithRightExtrapolation;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRJohnsonVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRPaulotVolatilityFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class SimleInterpolationExtrapolationExamples {
  private static final ShiftedLogNormalTailExtrapolationFitter FITTER = new ShiftedLogNormalTailExtrapolationFitter();

  @Test(enabled = false)
  public void test() {
    double forward = 1.1;
    double[] strikes = new double[] {forward * 1.1, forward * 1.5 };
    double[] vols = new double[] {0.6, 0.7 };
    double expiry = 0.5;
    System.out.println(strikes[0] + "\t" + vols[0]);
    System.out.println(strikes[1] + "\t" + vols[1]);
    System.out.println();

    double[] res = FITTER.fitTwoVolatilities(forward, strikes, vols, expiry);

    for (int i = 0; i < 100; ++i) {
      double key = forward * (0.5 + 0.01 * i);
      double value = ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, key, expiry, res[0], res[1]);
      System.out.println(key + "\t" + value);
    }
  }

  @Test
      (enabled = false)
      public void comparisonTest() {
    double forward = 1.1;
    double[] strikes = new double[] {0.7 * forward, 0.85 * forward, forward, 1.15 * forward, 1.3 * forward };
    double[] vols = new double[] {0.3, 0.23, 0.18, 0.2, 0.21 };
    double expiry = 1.25;

    GeneralSmileInterpolator interp0 = new SmileInterpolatorSpline();
    GeneralSmileInterpolator interp1 = new SmileInterpolatorMixedLogNormal();
    GeneralSmileInterpolator interp2 = new SmileInterpolatorSABR();
    GeneralSmileInterpolator interp3 = new SmileInterpolatorSABRWithRightExtrapolation(forward * 3.0, 2.5);
    //    GeneralSmileInterpolator interp4 = new SmileInterpolatorSABR(new SABRJohnsonVolatilityFunction());
    GeneralSmileInterpolator interp5 = new SmileInterpolatorSABRWithRightExtrapolation(new SABRJohnsonVolatilityFunction(), forward * 3.0, 2.5);
    GeneralSmileInterpolator interp6 = new SmileInterpolatorSABR(new SABRPaulotVolatilityFunction());
    GeneralSmileInterpolator interp7 = new SmileInterpolatorSABRWithRightExtrapolation(new SABRPaulotVolatilityFunction(), forward * 3.0, 2.5);

    GeneralSmileInterpolator[] interps = new GeneralSmileInterpolator[] {
        interp0, interp1, interp2, interp3,
        //        interp4,
        interp5, interp6, interp7
    };
    int nInterps = interps.length;
    InterpolatedSmileFunction[] functions = new InterpolatedSmileFunction[nInterps];
    for (int i = 0; i < interps.length; ++i) {
      functions[i] = new InterpolatedSmileFunction(interps[i], forward, strikes, expiry, vols);
    }

    int nKeys = 200;
    for (int i = 0; i < nKeys; ++i) {
      double key = forward * (0.1 + i * 0.1);
      System.out.print(key + "\t");
      for (int j = 0; j < nInterps; ++j) {
        System.out.print(functions[j].getVolatility(key) + "\t");
      }
      System.out.println();
    }

    System.out.println("\n");
    for (int i = 0; i < strikes.length; ++i) {
      System.out.println(strikes[i] + "\t" + vols[i]);
    }

    System.out.println("\n");
    for (int i = 0; i < nKeys; ++i) {
      double key = forward * (0.6 + i * 0.8 / nKeys);
      System.out.print(key + "\t");
      for (int j = 0; j < nInterps; ++j) {
        System.out.print(functions[j].getVolatility(key) + "\t");
      }
      System.out.println();
    }
  }

  @Test(enabled = false)
  public void testt() {
    double strike = 1.5;
    double timeToExpiry = 2.0;
    boolean isCall = true;
    double forward = 4.1;

    double sigma = 0.2;
    double beta = 0.6;
    double alpha = Math.pow(sigma, 1 - beta);
    double rho = 1.;
    double nu = 0.4;
    double[] params = new double[] {alpha, beta, rho, nu };
    SABRFormulaData data = new SABRFormulaData(params);

    SABRHaganVolatilityFunction func = new SABRHaganVolatilityFunction();
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, timeToExpiry, isCall);

    double res = func.getVolatility(option, forward, data);
    double[] resAdj = func.getVolatilityAdjoint(option, forward, data);

    //    double eps = 1.e-7;
    //    for (int i = 0; i < 200; ++i) {
    //      double rhoTmp = -0.98 * (1. + i / 10000.);
    //
    //      //      double rhoTmpDw = rhoTmp - eps;
    //      double[] paramsTmp = new double[] {alpha, beta, rhoTmp, nu };
    //      //      double[] paramsTmpDw = new double[] {alpha, beta, rhoTmpDw, nu };
    //      SABRFormulaData dataTmp = new SABRFormulaData(paramsTmp);
    //      //      SABRFormulaData dataTmpDw = new SABRFormulaData(paramsTmpDw);
    //      //      double volTmp = func.getVolatility(option, forward, dataTmp);
    //      //      double volTmpDw = func.getVolatility(option, forward, dataTmpDw);
    //      //      System.out.println(rhoTmp + "\t" + (volTmp - volTmpDw) / eps);
    //
    //      double[] resAdjTmp = func.getVolatilityAdjoint(option, forward, dataTmp);
    //      System.out.println(rhoTmp + "\t" + resAdjTmp[0] + "\t" + resAdjTmp[1] + "\t" + resAdjTmp[2] + "\t" + resAdjTmp[3] + "\t" + resAdjTmp[4] + "\t" + resAdjTmp[5] + "\t" + resAdjTmp[6]);
    //    }

    System.out.println(res);
    System.out.println(new DoubleMatrix1D(resAdj));

    double[] volatilityD = new double[6];
    double[][] volatilityD2 = new double[2][2];
    double resAdj2 = func.getVolatilityAdjoint2(option, forward, data, volatilityD, volatilityD2);
    System.out.println(resAdj2);
    System.out.println(new DoubleMatrix1D(volatilityD));
    System.out.println(new DoubleMatrix2D(volatilityD2));

  }

  @Test(enabled = false)
  public void testtt() {
    double alpha = 0.19923806245090936, beta = 0.058222805067149275, rho = 1.0, nu = 0.4380688698912412;
    double[] params = new double[] {alpha, beta, rho, nu };
    SABRFormulaData data = new SABRFormulaData(params);

  }
}

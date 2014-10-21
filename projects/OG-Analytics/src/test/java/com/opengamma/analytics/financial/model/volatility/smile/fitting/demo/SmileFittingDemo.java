/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.demo;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.MixedLogNormalModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SVIModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SVIVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.BasisFunctionGenerator;
import com.opengamma.analytics.math.interpolation.BasisFunctionKnots;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.GeneralizedLeastSquare;
import com.opengamma.analytics.math.statistics.leastsquare.GeneralizedLeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;

/**
 * This uses Dec-2014 options on the S&P 500 index. The trade date is 20-Oct-2014 09:40 and the expiry is 19-Dec-2014 21:15  
 * (nominal expiry is 20-Dec-2014, which is a Saturday) 
 */
public class SmileFittingDemo {
  private static RandomEngine RANDOM = new MersenneTwister();
  private static final double fwd = 1879.52;
  private static final  double r = 0.00231;
  private static final  double t = 60.49514 / 365.;
  private static final  double[] strikes = new double[] {1700, 1750, 1800, 1850, 1900, 1950, 2000, 2050, 2150, 2250 };
  private static final  double[] iv = new double[] {0.25907, 0.23819, 0.21715, 0.19517, 0.17684, 0.15383, 0.13577, 0.12505, 0.1564, 0.17344 };
  private static VolatilityFunctionProvider<SABRFormulaData> SABR = new SABRHaganVolatilityFunction();
  private static double[] errors;
  private static final GeneralSmileInterpolator SABR_INTERPOLATOR = new SmileInterpolatorSABR();

  static {
    int n = iv.length;
    errors = new double[n];
    Arrays.fill(errors, 1e-3); // 10bps
  }

  /**
   * Fit the SABR model to market implied volatilities. The parameter beta is fixed at 1, so a three parameter fit is 
   * made. 
   */
  @Test(description = "Demo")
  public void globalSabrFitDemo() {
    // SABR starting parameters
    BitSet fixed = new BitSet();
    fixed.set(1);
    double atmVol = 0.18;
    double beta = 1.0;
    double rho = -0.9;
    double nu = 1.8;
    double alpha = atmVol * Math.pow(fwd, 1 - beta);

    DoubleMatrix1D start = new DoubleMatrix1D(alpha, beta, rho, nu);
    SmileModelFitter<SABRFormulaData>  sabrFitter = new SABRModelFitter(fwd, strikes, t, iv, errors, SABR);
    fitAndPrintSmile(sabrFitter, start, fixed);
  }

  /**
   * Fit the SVI model to market implied volatilities<p>
   * The model has 5 parameters $a,b,\rho,\nu$ and $m$, and the variance is given by
   * $\sigma^2 = a+b(\rho d + \sqrt{d^2+\nu^2})$ where $d= \ln\left(\frac{k}{f}\right)-m$
   * With $m=0$, the ATMF vol is given by $\sigma = \sqrt{a+b\nu}$<p>
   * Note: the solution is sensitive to the starting position  (many 'sensible' starting points give a local minimum)
   */
  @Test(description = "Demo")
  public void globalSVIFitDemo() {
    SVIVolatilityFunction model = new SVIVolatilityFunction();
    SVIModelFitter sviFitter = new SVIModelFitter(fwd, strikes, t, iv, errors, model);
    DoubleMatrix1D start = new DoubleMatrix1D(0.015, 0.1, -0.3, 0.3, 0.0 );
    BitSet fixed = new BitSet();
    fitAndPrintSmile(sviFitter, start, fixed);
  }


  private void fitAndPrintSmile(SmileModelFitter<? extends SmileModelData> fitter, DoubleMatrix1D start, BitSet fixed) {
    LeastSquareResultsWithTransform res = fitter.solve(start, fixed);
    System.out.println("chi-Square: "+res.getChiSq());
    VolatilityFunctionProvider<?> model = fitter.getModel();

    int nSamples = 100;
    System.out.println("Strike\tImplied Volatility");
    double[] sampleK = new double[nSamples];
    for (int i = 0; i < nSamples; i++) {
      sampleK[i] = 1500 + 1000. * i / (nSamples - 1.0);
    }
    SmileModelData data = fitter.toSmileModelData(res.getModelParameters());

    @SuppressWarnings("unchecked")
    Function1D<SmileModelData,double[]> volFunc = (Function1D<SmileModelData, double[]>) model.getVolatilityFunction(fwd, sampleK, t);
    double[] vols = volFunc.evaluate(data);
    for (int i = 0; i < nSamples; i++) {
      System.out.println(sampleK[i] + "\t" + vols[i]);
    }
  }

  @Test 
  void sabrInterpolationTest() {
    Function1D<Double, Double> func = SABR_INTERPOLATOR.getVolatilityFunction(fwd, strikes, t, iv);
    int nSamples = 100;
    System.out.println("Strike\tImplied Volatility");
    for (int i = 0; i < nSamples; i++) {
      double k = 1500 + 1000. * i / (nSamples - 1.0);
      double vol = func.evaluate(k);
      System.out.println(k+ "\t" + vol);
    }

  }


  @Test
  void mixedLogNormal() {
    int nNorms = 4;
    MixedLogNormalVolatilityFunction model = MixedLogNormalVolatilityFunction.getInstance();
    MixedLogNormalModelFitter fitter = new MixedLogNormalModelFitter(fwd, strikes, t, iv, errors, model, nNorms, true);
    DoubleMatrix1D start = new DoubleMatrix1D(getRandomStartValues(nNorms, true));
    LeastSquareResultsWithTransform res = fitter.solve(start);
    System.out.println(res);
    MixedLogNormalModelData data = new MixedLogNormalModelData(res.getModelParameters().getData());
    int nSamples = 100;
    System.out.println("Strike\tImplied Volatility");
    double[] sampleK = new double[nSamples];
    for (int i = 0; i < nSamples; i++) {
      sampleK[i] = 1500 + 1000. * i / (nSamples - 1.0);
    }

    Function1D<MixedLogNormalModelData, double[]> volFunc = model.getVolatilityFunction(fwd, sampleK, t);
    double[] vols = volFunc.evaluate(data);
    for (int i = 0; i < nSamples; i++) {
      System.out.println(sampleK[i] + "\t" + vols[i]);
    }
  }


  private double[] getRandomStartValues(int nNorms, boolean useShift) {
    final int n = useShift ? 3 * nNorms - 2 : 2 * nNorms - 1;
    final double[] res = new double[n];
    res[0] = 0.1 + 0.3 * RANDOM.nextDouble();
    for (int i = 1; i < nNorms; i++) {
      res[i] = 0.5 * RANDOM.nextDouble();
    }
    for (int i = nNorms; i < n; i++) {
      res[i] = 2 * Math.PI * RANDOM.nextDouble();
    }
    return res;
  }

  @Test
  void splineInterpolatorTest() {
    GeneralSmileInterpolator spline = new SmileInterpolatorSpline();
    Function1D<Double, Double> func = spline.getVolatilityFunction(fwd, strikes, t, iv);
    int nSamples = 100;
    System.out.println("Strike\tImplied Volatility");
    for (int i = 0; i < nSamples; i++) {
      double k = 1500 + 1000. * i / (nSamples - 1.0);
      double vol = func.evaluate(k);
      System.out.println(k+ "\t" + vol);
    }
  }

  @Test
  void pSplineTest() {
    BasisFunctionGenerator gen = new BasisFunctionGenerator();
    BasisFunctionKnots knots = BasisFunctionKnots.fromUniform(1500, 2500, 20, 3);
    List<Function1D<Double, Double>> set = gen.generateSet(knots);
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();

    int n = iv.length;
    double[] var = new double[n];
    for(int i=0;i<n;i++) {
      var[i] = iv[i]*iv[i];
    }

    double log10Lambda = 6;
    double lambda = Math.pow(10.0, log10Lambda);

    GeneralizedLeastSquareResults<Double> res = gls.solve(ArrayUtils.toObject(strikes), var, errors, set, lambda, 2);
    // System.out.println(res);
    Function1D<Double, Double> func = res.getFunction();
    int nSamples = 100;
    System.out.println("Strike\tImplied Volatility");
    for (int i = 0; i < nSamples; i++) {
      double k = 1500 + 1000. * i / (nSamples - 1.0);
      double vol = Math.sqrt(func.evaluate(k));
      System.out.println(k+ "\t" + vol);
    }
  }

}

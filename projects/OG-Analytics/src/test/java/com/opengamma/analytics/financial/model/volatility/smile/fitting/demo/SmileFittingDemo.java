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

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.HestonModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.MixedLogNormalModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SVIModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.BenaimDodgsonKainthExtrapolationFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.ShiftedLogNormalTailExtrapolation;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.ShiftedLogNormalTailExtrapolationFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.function.HestonVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SVIVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
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
  private static final double r = 0.00231;
  private static final double t = 60.49514 / 365.;
  private static final double[] strikes = new double[] {1700, 1750, 1800, 1850, 1900, 1950, 2000, 2050, 2150, 2250 };
  private static final double[] iv = new double[] {0.25907, 0.23819, 0.21715, 0.19517, 0.17684, 0.15383, 0.13577, 0.12505, 0.1564, 0.17344 };
  private static VolatilityFunctionProvider<SABRFormulaData> SABR = new SABRHaganVolatilityFunction();
  private static double[] errors;
  private static final GeneralSmileInterpolator SABR_INTERPOLATOR = new SmileInterpolatorSABR();

  private static final int NUM_SAMPLES = 101;
  private static final double LOWER_STRIKE = 1500.0;
  private static final double UPPER_STRIKE = 2500.0;

  static {
    int n = iv.length;
    errors = new double[n];
    Arrays.fill(errors, 1e-3); // 10bps
  }

  /**
   * Global fitters: These fit a smile model to market data in a least squares sense. Extrapolation just involves
   * using the calibrated parameters with the model for strikes outside the fitted range.
   */

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
    SmileModelFitter<SABRFormulaData> sabrFitter = new SABRModelFitter(fwd, strikes, t, iv, errors, SABR);
    fitAndPrintSmile(sabrFitter, start, fixed);
  }

  /**
   * Fit the SABR model to market implied volatilities. The parameter beta is fixed at 1, so a three parameter fit is
   * made. This differs from the example above in that outside the range of market strikes a shifted log-normal is
   * use to extrapolate the smile. 
   */
  @Test(description = "Demo")
  public void globalSabrFitWithExtrapolationDemo() {
    BitSet fixed = new BitSet();
    fixed.set(1);
    double atmVol = 0.18;
    double beta = 1.0;
    double rho = -0.9;
    double nu = 1.8;
    double alpha = atmVol * Math.pow(fwd, 1 - beta);

    DoubleMatrix1D start = new DoubleMatrix1D(alpha, beta, rho, nu);
    SmileModelFitter<SABRFormulaData> sabrFitter = new SABRModelFitter(fwd, strikes, t, iv, errors, SABR);
    fitAndPrintSmile(sabrFitter, start, fixed, strikes[0], strikes[strikes.length-1]);
  }


  /**
   * Again fit global SABR, but now use Benaim-Dodgson-Kainth extrapolation
   */
  @Test(description = "Demo")
  public void globalSabrFitWithBDKExtrapolationDemo() {
    BitSet fixed = new BitSet();
    fixed.set(1);
    double atmVol = 0.18;
    double beta = 1.0;
    double rho = -0.9;
    double nu = 1.8;
    double alpha = atmVol * Math.pow(fwd, 1 - beta);

    double muLow = 4.0;
    double muHigh = 0.6;

    DoubleMatrix1D start = new DoubleMatrix1D(alpha, beta, rho, nu);
    SABRModelFitter sabrFitter = new SABRModelFitter(fwd, strikes, t, iv, errors, SABR);
    fitAndPrintSmile(sabrFitter, start, fixed, strikes[0], strikes[strikes.length-1],muLow,muHigh);
  }


  /**
   * Fit the SVI model to market implied volatilities
   * <p>
   * The model has 5 parameters $a,b,\rho,\nu$ and $m$, and the variance is given by $\sigma^2 = a+b(\rho d + \sqrt{d^2+\nu^2})$ where $d= \ln\left(\frac{k}{f}\right)-m$ With $m=0$, the ATMF vol is
   * given by $\sigma = \sqrt{a+b\nu}$
   * <p>
   * Note: the solution is sensitive to the starting position (many 'sensible' starting points give a local minimum)
   */
  @Test(description = "Demo")
  public void globalSVIFitDemo() {
    SVIVolatilityFunction model = new SVIVolatilityFunction();
    SVIModelFitter sviFitter = new SVIModelFitter(fwd, strikes, t, iv, errors, model);
    DoubleMatrix1D start = new DoubleMatrix1D(0.015, 0.1, -0.3, 0.3, 0.0);
    BitSet fixed = new BitSet();
    fitAndPrintSmile(sviFitter, start, fixed);
  }

  /**
   * Fit the Heston model market implied volatilities.
   * <p>
   * Parameters of the Heston model are:
   * <ul>
   * <li>kappa mean-reverting speed
   * <li>theta mean-reverting level
   * <li>vol0 starting value of volatility
   * <li>omega volatility-of-volatility
   * <li>rho correlation between the spot process and the volatility process
   * </ul>
   * <p>
   * Note: the solution is sensitive to the starting position (many 'sensible' starting points give a local minimum)
   */
  @Test(description = "Demo")
  public void globalHestonFitDemo() {
    HestonVolatilityFunction model = new HestonVolatilityFunction();
    HestonModelFitter fitter = new HestonModelFitter(fwd, strikes, t, iv, errors, model);
    DoubleMatrix1D start = new DoubleMatrix1D(0.1, 0.02, 0.02, 0.4, -0.5);
    BitSet fixed = new BitSet();
    fitAndPrintSmile(fitter, start, fixed);
  }

  /**
   * Fit a mixed log-normal model to market implied volatilities. This example uses 2 normals which are
   * allowed to have different means, so there are 4 (2*3-2) degrees of freedom. In principle 3 normals (7=3*3-2 DoF)
   * will give a better fit, but the plethora of local minima massively hampers this.
   */
  @Test(description = "Demo")
  void mixedLogNormalFitDemo() {
    int nNorms = 2;
    boolean useShiftedMeans = true;
    MixedLogNormalVolatilityFunction model = MixedLogNormalVolatilityFunction.getInstance();
    MixedLogNormalModelFitter fitter = new MixedLogNormalModelFitter(fwd, strikes, t, iv, errors, model, nNorms, useShiftedMeans);
    DoubleMatrix1D start = new DoubleMatrix1D(0.1, 0.2, 1.5, 1.5);
    BitSet fixed = new BitSet();
    fitAndPrintSmile(fitter, start, fixed);
  }

  /**
   * Fit a smile model to the data set, and print the resultant smile between LOWER_STRIKE and UPPER_STRIKE
   * @param fitter the smile fitter
   * @param start this initial starting point of the model parameters
   * @param fixed map of any fixed parameters
   */
  private void fitAndPrintSmile(SmileModelFitter<? extends SmileModelData> fitter, DoubleMatrix1D start, BitSet fixed) {
    LeastSquareResultsWithTransform res = fitter.solve(start, fixed);
    System.out.println("chi-Square: " + res.getChiSq());
    VolatilityFunctionProvider<?> model = fitter.getModel();
    SmileModelData data = fitter.toSmileModelData(res.getModelParameters());

    Function1D<Double, Double> smile = toSmileFunction(fwd, t, data, model);
    printSmile(smile);
  }


  private void fitAndPrintSmile(SmileModelFitter<? extends SmileModelData> fitter, DoubleMatrix1D start, BitSet fixed,
      double lowerStrike, double upperStrike) {
    LeastSquareResultsWithTransform res = fitter.solve(start, fixed);
    System.out.println("chi-Square: " + res.getChiSq());
    VolatilityFunctionProvider<?> model = fitter.getModel();
    SmileModelData data = fitter.toSmileModelData(res.getModelParameters());

    Function1D<Double, Double> smile = toSmileFunction(fwd, t, data, model);
    Function1D<Double, Double> extrapSmile = fitShiftedLogNormalTails(fwd, t, smile, lowerStrike, upperStrike);

    printSmile(extrapSmile);
  }


  private void fitAndPrintSmile(SABRModelFitter fitter, DoubleMatrix1D start, BitSet fixed,
      final double lowerStrike, final double upperStrike, double lowerMu, double upperMu) {
    LeastSquareResultsWithTransform res = fitter.solve(start, fixed);
    System.out.println("chi-Square: " + res.getChiSq());
    VolatilityFunctionProvider<SABRFormulaData> model = fitter.getModel();
    SABRFormulaData data = fitter.toSmileModelData(res.getModelParameters());

    final Function1D<Double, Double> smile = toSmileFunction(fwd, t, data, model);

    BenaimDodgsonKainthExtrapolationFunctionProvider tailPro = new BenaimDodgsonKainthExtrapolationFunctionProvider(lowerMu, upperMu);
    final Function1D<Double, Double> extrapFunc = tailPro.getExtrapolationFunction(data,data,model,fwd,t,lowerStrike,upperStrike);

    Function1D<Double, Double> smileWithExtrap = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double k) {
        if(k < lowerStrike || k > upperStrike) {
          return extrapFunc.evaluate(k);
        }
        return smile.evaluate(k);
      }
    };

    printSmile(smileWithExtrap);
  }



  private void  printSmile( Function1D<Double, Double> smile) {
    System.out.println("Strike\tImplied Volatility");
    double range = (UPPER_STRIKE - LOWER_STRIKE) / (NUM_SAMPLES - 1.0);
    for (int i = 0; i < NUM_SAMPLES; i++) {
      double k = LOWER_STRIKE + i * range;
      double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test
  void golbalSABRwithShiftedLogNormalExtrapolation() {
    ShiftedLogNormalTailExtrapolationFitter tailfitter = new ShiftedLogNormalTailExtrapolationFitter();
  }

  /**
   * Extrapolate a volatility smile to low and high strikes by fitting (separately) a shifted-log-normal model at
   * the low and high strike cutoffs
   * @param forward the forward
   * @param expiry the expiry
   * @param volSmileFunc a function describing the smile (Black vol as a function of strike)
   * @param lowerStrike the lower strike
   * @param upperStrike the upper strike
   * @return a volatility smile (Black vol as a function of strike) that is valid for strikes from zero to infinity
   */
  private Function1D<Double, Double> fitShiftedLogNormalTails(final double forward, final double expiry, final Function1D<Double, Double> volSmileFunc, final double lowerStrike,
      final double upperStrike) {
    ScalarFirstOrderDifferentiator diff = new ScalarFirstOrderDifferentiator();
    Function1D<Double, Double> dVolDkFunc = diff.differentiate(volSmileFunc);
    return fitShiftedLogNormalTails(forward, expiry, volSmileFunc, dVolDkFunc, lowerStrike, upperStrike);
  }

  /**
   * Extrapolate a volatility smile to low and high strikes by fitting (separately) a shifted-log-normal model at
   * the low and high strike cutoffs.
   * @param forward the forward
   * @param expiry the expiry
   * @param volSmileFunc a function describing the smile (Black vol as a function of strike)
   * @param dVolDkFunc the gradient of the smile as a function of strike
   * @param lowerStrike the lower strike
   * @param upperStrike the upper strike
   * @return a volatility smile (Black vol as a function of strike) that is valid for strikes from zero to infinity
   */
  private Function1D<Double, Double> fitShiftedLogNormalTails(final double forward, final double expiry, final Function1D<Double, Double> volSmileFunc, final Function1D<Double, Double> dVolDkFunc,
      final double lowerStrike, final double upperStrike) {
    ShiftedLogNormalTailExtrapolationFitter tailFitter = new ShiftedLogNormalTailExtrapolationFitter();

    double vol = volSmileFunc.evaluate(lowerStrike);
    double volGrad = dVolDkFunc.evaluate(lowerStrike);
    final double[] lowerParms = tailFitter.fitVolatilityAndGrad(forward, lowerStrike, vol, volGrad, expiry);
    vol = volSmileFunc.evaluate(upperStrike);
    volGrad = dVolDkFunc.evaluate(upperStrike);
    final double[] upperParms = tailFitter.fitVolatilityAndGrad(forward, upperStrike, vol, volGrad, expiry);

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        if (k >= lowerStrike && k <= upperStrike) {
          return volSmileFunc.evaluate(k);
        }
        if (k < lowerStrike) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, lowerParms[0], lowerParms[1]);
        }
        return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, upperParms[0], upperParms[1]);
      }
    };
  }

  /**
   * gets a smile function (Black vol as a function of strike) from a VolatilityFunctionProvider and SmileModelData
   * @param fwd the forward
   * @param expiry the expiry
   * @param data parameters of the smile model (e.g. for SABR these would be alpha, beta, rho and nu)
   * @param volModel the volatility model
   * @return the smile function
   */
  private Function1D<Double, Double> toSmileFunction(final double fwd, final double expiry, final SmileModelData data, final VolatilityFunctionProvider<? extends SmileModelData> volModel) {

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        boolean isCall = k >= fwd;
        EuropeanVanillaOption option = new EuropeanVanillaOption(k, expiry, isCall);
        @SuppressWarnings("unchecked")
        Function1D<SmileModelData, Double> func = (Function1D<SmileModelData, Double>) volModel.getVolatilityFunction(option, fwd);
        return func.evaluate(data);
      }
    };
  }

  @Test
  void sabrInterpolationTest() {
    Function1D<Double, Double> func = SABR_INTERPOLATOR.getVolatilityFunction(fwd, strikes, t, iv);
    int nSamples = 100;
    System.out.println("Strike\tImplied Volatility");
    for (int i = 0; i < nSamples; i++) {
      double k = 1500 + 1000. * i / (nSamples - 1.0);
      double vol = func.evaluate(k);
      System.out.println(k + "\t" + vol);
    }

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
      System.out.println(k + "\t" + vol);
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
    for (int i = 0; i < n; i++) {
      var[i] = iv[i] * iv[i];
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
      System.out.println(k + "\t" + vol);
    }
  }

}

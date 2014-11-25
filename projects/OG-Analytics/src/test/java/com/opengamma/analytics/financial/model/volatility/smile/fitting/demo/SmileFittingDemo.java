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
 * The purpose of this class is to demonstrate the various methods in analytics for fitting/interpolating volatility 
 * smiles, and extrapolating out to low and high strikes beyond the data range.
 * <p>
 * The market data is Dec-2014 options on the S&P 500 index. The trade date is 20-Oct-2014 09:40 and the expiry is 19-Dec-2014 21:15
 * (nominal expiry is 20-Dec-2014, which is a Saturday)
 */
public class SmileFittingDemo {
  private static final double FORWARD = 1879.52;
  @SuppressWarnings("unused")
  private static final double RATE = 0.00231;
  private static final double EXPIRY = 60.49514 / 365.;
  private static final double[] STRIKES = new double[] {1700, 1750, 1800, 1850, 1900, 1950, 2000, 2050, 2150, 2250 };
  private static final double[] IMPLIED_VOLS = new double[] {0.25907, 0.23819, 0.21715, 0.19517, 0.17684, 0.15383, 0.13577, 0.12505, 0.1564, 0.17344 };
  private static VolatilityFunctionProvider<SABRFormulaData> SABR = new SABRHaganVolatilityFunction();
  private static double[] ERRORS;

  // These control how often the smile is sampled and the range used
  private static final int NUM_SAMPLES = 101;
  private static final double LOWER_STRIKE = 1500.0;
  private static final double UPPER_STRIKE = 2500.0;

  // For least squares fit use an error of 10bps - this only affects the reported chi-square
  static {
    int n = IMPLIED_VOLS.length;
    ERRORS = new double[n];
    Arrays.fill(ERRORS, 1e-3); // 10bps
  }

  // ****************************************************************************************************************
  // Global fitters: These fit a smile model to market data in a least squares sense. Extrapolation just involves
  // using the calibrated parameters with the model for strikes outside the fitted range.
  // ****************************************************************************************************************

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
    double alpha = atmVol * Math.pow(FORWARD, 1 - beta);

    DoubleMatrix1D start = new DoubleMatrix1D(alpha, beta, rho, nu);
    SmileModelFitter<SABRFormulaData> sabrFitter = new SABRModelFitter(FORWARD, STRIKES, EXPIRY, IMPLIED_VOLS, ERRORS, SABR);
    Function1D<Double, Double> smile = fitSmile(sabrFitter, start, fixed);
    printSmile(smile);
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
    SVIModelFitter fitter = new SVIModelFitter(FORWARD, STRIKES, EXPIRY, IMPLIED_VOLS, ERRORS, model);
    DoubleMatrix1D start = new DoubleMatrix1D(0.015, 0.1, -0.3, 0.3, 0.0);
    BitSet fixed = new BitSet();
    Function1D<Double, Double> smile = fitSmile(fitter, start, fixed);
    printSmile(smile);
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
    HestonModelFitter fitter = new HestonModelFitter(FORWARD, STRIKES, EXPIRY, IMPLIED_VOLS, ERRORS, model);
    DoubleMatrix1D start = new DoubleMatrix1D(0.1, 0.02, 0.02, 0.4, -0.5);
    BitSet fixed = new BitSet();
    Function1D<Double, Double> smile = fitSmile(fitter, start, fixed);
    printSmile(smile);
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
    MixedLogNormalModelFitter fitter = new MixedLogNormalModelFitter(FORWARD, STRIKES, EXPIRY, IMPLIED_VOLS, ERRORS, model, nNorms, useShiftedMeans);
    DoubleMatrix1D start = new DoubleMatrix1D(0.1, 0.2, 1.5, 1.5);
    BitSet fixed = new BitSet();
    Function1D<Double, Double> smile = fitSmile(fitter, start, fixed);
    printSmile(smile);
  }

  // ****************************************************************************************************************
  // SABR Global fitters: These fit SABR to market data in a least squares sense. Extrapolation is with
  // shifted log-normal and Benaim-Dodgson-Kainth
  // ****************************************************************************************************************

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
    double alpha = atmVol * Math.pow(FORWARD, 1 - beta);

    DoubleMatrix1D start = new DoubleMatrix1D(alpha, beta, rho, nu);
    SmileModelFitter<SABRFormulaData> sabrFitter = new SABRModelFitter(FORWARD, STRIKES, EXPIRY, IMPLIED_VOLS, ERRORS, SABR);
    Function1D<Double, Double> smile = fitSmile(sabrFitter, start, fixed, STRIKES[0], STRIKES[STRIKES.length - 1]);
    printSmile(smile);
  }

  /**
   * Again fit global SABR, but now use Benaim-Dodgson-Kainth extrapolation
   * <p>
   * Note: currently our Benaim-Dodgson-Kainth implementation is hard coded to SABR so cannot be used with other smile models
   */
  @Test(description = "Demo")
  public void globalSabrFitWithBDKExtrapolationDemo() {
    BitSet fixed = new BitSet();
    fixed.set(1);
    double atmVol = 0.18;
    double beta = 1.0;
    double rho = -0.9;
    double nu = 1.8;
    double alpha = atmVol * Math.pow(FORWARD, 1 - beta);

    double muLow = 1.0;
    double muHigh = 1.0;

    DoubleMatrix1D start = new DoubleMatrix1D(alpha, beta, rho, nu);
    SABRModelFitter sabrFitter = new SABRModelFitter(FORWARD, STRIKES, EXPIRY, IMPLIED_VOLS, ERRORS, SABR);
    Function1D<Double, Double> smile = fitSmile(sabrFitter, start, fixed, STRIKES[0], STRIKES[STRIKES.length - 1], muLow, muHigh);
    printSmile(smile);
  }

  // ****************************************************************************************************************
  // Local fitters: These can be classed as smile interpolators, in that they fit all the market points. Extrapolation
  // is either native or using shifted log-normal or Benaim-Dodgson-Kainth
  // ****************************************************************************************************************

  /**
   * The SABR interpolator fits the SABR model (with a fixed Beta) to consecutive triplets of implied vols with
   * smooth pasting in between. Extrapolation used the SABR fits for the end points.
   */
  @Test(description = "Demo")
  void sabrInterpolationTest() {
    GeneralSmileInterpolator sabr_interpolator = new SmileInterpolatorSABR();
    Function1D<Double, Double> smile = sabr_interpolator.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, IMPLIED_VOLS);
    printSmile(smile);
  }

  /**
   * Spline interpolator fits a spline (the default is double-quadratic) through the market implied volatilities and
   * uses shifted log-normal to handle the extrapolation.
   */
  @Test
  void splineInterpolatorTest() {
    GeneralSmileInterpolator spline = new SmileInterpolatorSpline();
    Function1D<Double, Double> smile = spline.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, IMPLIED_VOLS);
    printSmile(smile);
  }

  /**
   * Fit the market variances (volatility squared) using a non-parametric curve. The parameter, lambda, controls the
   * smoothness of the curve (penalty on the curvature), so for high values the curve will be smooth, but not match the
   * market values. The extrapolated values will be linear in variance.
   */
  @Test
  void pSplineTest() {
    int nKnots = 20; // 20 internal knots to represent the variance curve
    int degree = 3; // Curve made from third order polynomial pieces
    int penaltyOrder = 2; // Penalty on curvature
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();
    BasisFunctionGenerator gen = new BasisFunctionGenerator();
    BasisFunctionKnots knots = BasisFunctionKnots.fromUniform(LOWER_STRIKE, UPPER_STRIKE, nKnots, degree);
    List<Function1D<Double, Double>> set = gen.generateSet(knots);

    int n = IMPLIED_VOLS.length;
    double[] var = new double[n];
    for (int i = 0; i < n; i++) {
      var[i] = IMPLIED_VOLS[i] * IMPLIED_VOLS[i];
    }

    double log10Lambda = 6;
    double lambda = Math.pow(10.0, log10Lambda);

    GeneralizedLeastSquareResults<Double> res = gls.solve(ArrayUtils.toObject(STRIKES), var, ERRORS, set, lambda, penaltyOrder);
    final Function1D<Double, Double> varFunc = res.getFunction();
    Function1D<Double, Double> smile = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        return Math.sqrt(varFunc.evaluate(k));
      }
    };

    printSmile(smile);
  }

  // ****************************************************************************************************************
  // Helper methods. If 'smile' fitting is brought under a common API, these could form part of that design
  // ****************************************************************************************************************

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

  /**
   * Fit a smile model to the data set and return the smile
   * @param fitter the smile fitter
   * @param start this initial starting point of the model parameters
   * @param fixed map of any fixed parameters
   * @return the smile function
   */
  private Function1D<Double, Double> fitSmile(SmileModelFitter<? extends SmileModelData> fitter, DoubleMatrix1D start, BitSet fixed) {
    LeastSquareResultsWithTransform res = fitter.solve(start, fixed);
    System.out.println("chi-Square: " + res.getChiSq());
    VolatilityFunctionProvider<?> model = fitter.getModel();
    SmileModelData data = fitter.toSmileModelData(res.getModelParameters());

    return toSmileFunction(FORWARD, EXPIRY, data, model);
  }

  /**
   * Fit a smile model to the data set and return the smile. Outside the given range use shifted log-normal extrapolation
   * @param fitter the smile fitter
   * @param start this initial starting point of the model parameters
   * @param fixed map of any fixed parameters
   * @param lowerStrike start of the left extrapolation
   * @param upperStrike start of the right extrapolation
   * @return the smile function
   */
  private Function1D<Double, Double> fitSmile(SmileModelFitter<? extends SmileModelData> fitter, DoubleMatrix1D start, BitSet fixed, double lowerStrike, double upperStrike) {
    LeastSquareResultsWithTransform res = fitter.solve(start, fixed);
    System.out.println("chi-Square: " + res.getChiSq());
    VolatilityFunctionProvider<?> model = fitter.getModel();
    SmileModelData data = fitter.toSmileModelData(res.getModelParameters());

    Function1D<Double, Double> smile = toSmileFunction(FORWARD, EXPIRY, data, model);
    return fitShiftedLogNormalTails(FORWARD, EXPIRY, smile, lowerStrike, upperStrike);
  }

  /**
   * Fit a smile model to the data set and return the smile. Outside the given range use Benaim-Dodgson-Kainth extrapolation
   * @param fitter the smile fitter
   * @param start this initial starting point of the model parameters
   * @param fixed map of any fixed parameters
   * @param lowerStrike start of the left extrapolation
   * @param upperStrike start of the right extrapolation
   * @param lowerMu the left tail control parameter
   * @param upperMu the right tail control parameter
   * @return the smile function
   */
  private Function1D<Double, Double> fitSmile(SABRModelFitter fitter, DoubleMatrix1D start, BitSet fixed, final double lowerStrike, final double upperStrike, double lowerMu, double upperMu) {
    LeastSquareResultsWithTransform res = fitter.solve(start, fixed);
    System.out.println("chi-Square: " + res.getChiSq());
    VolatilityFunctionProvider<SABRFormulaData> model = fitter.getModel();
    SABRFormulaData data = fitter.toSmileModelData(res.getModelParameters());

    final Function1D<Double, Double> smile = toSmileFunction(FORWARD, EXPIRY, data, model);

    BenaimDodgsonKainthExtrapolationFunctionProvider tailPro = new BenaimDodgsonKainthExtrapolationFunctionProvider(lowerMu, upperMu);
    final Function1D<Double, Double> extrapFunc = tailPro.getExtrapolationFunction(data, data, model, FORWARD, EXPIRY, lowerStrike, upperStrike);

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        if (k < lowerStrike || k > upperStrike) {
          return extrapFunc.evaluate(k);
        }
        return smile.evaluate(k);
      }
    };
  }

  /**
   * Print the smile. The number of sample and range is controlled by static variables  
   * @param smile the smile function 
   */
  private void printSmile(Function1D<Double, Double> smile) {
    System.out.println("Strike\tImplied Volatility");
    double range = (UPPER_STRIKE - LOWER_STRIKE) / (NUM_SAMPLES - 1.0);
    for (int i = 0; i < NUM_SAMPLES; i++) {
      double k = LOWER_STRIKE + i * range;
      double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

}

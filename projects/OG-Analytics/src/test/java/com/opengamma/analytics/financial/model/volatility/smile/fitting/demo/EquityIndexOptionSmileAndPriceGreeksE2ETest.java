/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.demo;

import java.util.Arrays;
import java.util.BitSet;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;

/**
 * The demo test showing smile construction from a sparse option data set. 
 */
public class EquityIndexOptionSmileAndPriceGreeksE2ETest {
  private static double SHARES = 10.0;
  private static final int DAYS_TO_MAT = 23;
  private static final int HOURS_TO_MAT = 0;
  private static final int MINS_TO_MAT = 0;
  private static final double EXPIRY = (DAYS_TO_MAT + (HOURS_TO_MAT + MINS_TO_MAT / 60.) / 24.) / 365.;
  private static final double FORWARD = 3360;
  private static final double RATE = 0.204 * 0.01;
  private static final double DIVIDEND = 0.333 * 0.01;
  private static final double DF = Math.exp(-RATE * EXPIRY);
  private static final double SPOT = FORWARD * Math.exp((DIVIDEND - RATE) * EXPIRY);
  private static final double[] PUT_STRIKES = new double[] {3050, 3100, 3125, 3300, 3350, 3550 };
  private static final double[] CALL_STRIKES = new double[] {3075, 3325, 3450, 3525 };
  private static final double[] PUT_PRICES = new double[] {7.57640856221836, 10.4492292389527, 12.3810949922475,
      43.5400250456128, 61.8776592568553, 197.744462599924 };
  private static final double[] CALL_PRICES = new double[] {292.512814702669, 85.6868660157612, 26.1322435193456,
      9.48267272813102 };
  private static final double[] PUT_IV;
  private static final double[] CALL_IV;
  static {
    int nPuts = PUT_STRIKES.length;
    PUT_IV = new double[nPuts];
    for (int i = 0; i < nPuts; i++) {
      PUT_IV[i] = BlackFormulaRepository.impliedVolatility(PUT_PRICES[i] / DF, FORWARD, PUT_STRIKES[i], EXPIRY, false);
    }
    int nCalls = CALL_STRIKES.length;
    CALL_IV = new double[nCalls];
    for (int i = 0; i < nCalls; i++) {
      CALL_IV[i] = BlackFormulaRepository.impliedVolatility(CALL_PRICES[i] / DF, FORWARD, CALL_STRIKES[i], EXPIRY,
          true);
    }
  }
  // (fractional) shifts to volatility smile 
  private static final double[] VOL_SHOCKS = new double[] {-0.1, -0.05, -0.01, 0.0, 0.01, 0.05, 0.1 };

  private static VolatilityFunctionProvider<SABRFormulaData> SABR = new SABRHaganVolatilityFunction();
  private static double[] CALL_ERRORS;
  private static double[] PUT_ERRORS;
  private static final double LOWER_STRIKE = 3000.;
  private static final double UPPER_STRIKE = 3600.;
  private static final double STRIKE_STEP = 25.;
  private static final double[] DISPLAY_STRIKES;
  static {
    int n = (int) ((UPPER_STRIKE - LOWER_STRIKE) / STRIKE_STEP + 1);
    DISPLAY_STRIKES = new double[n];
    for (int i = 0; i < n; i++) {
      DISPLAY_STRIKES[i] = LOWER_STRIKE + i * STRIKE_STEP;
    }
    DISPLAY_STRIKES[n - 1] = UPPER_STRIKE;
  }
  static {
    CALL_ERRORS = new double[CALL_STRIKES.length];
    Arrays.fill(CALL_ERRORS, 1e-3); // 10bps
    PUT_ERRORS = new double[PUT_STRIKES.length];
    Arrays.fill(PUT_ERRORS, 1e-3); // 10bps
  }

  /**
   * Call options, global SABR.
   */
  @Test(description = "Demo", enabled = false)
  public void fitSabrSmileCall() {
    BitSet fixed = new BitSet();
    fixed.set(1); // beta is fixed
    double atmVol = 0.19;
    double beta = 0.5;
    double rho = -0.7;
    double nu = 1.8;
    double alpha = atmVol * Math.pow(FORWARD, 1 - beta);
    DoubleMatrix1D start = new DoubleMatrix1D(alpha, beta, rho, nu);
    SmileModelFitter<SABRFormulaData> sabrFitter = new SABRModelFitter(FORWARD, CALL_STRIKES, EXPIRY, CALL_IV,
        CALL_ERRORS, SABR);
    Function1D<Double, Double> smile = fitSmile(sabrFitter, start, fixed);
    printDetails(smile, DISPLAY_STRIKES, true);
    //    printDetailsWithShift(smile, DISPLAY_STRIKES, true, VOL_SHOCKS); // vol shocks
  }

  /**
   * Put options, global SABR.
   */
  @Test(description = "Demo", enabled = false)
  public void fitSabrSmilePut() {
    BitSet fixed = new BitSet();
    fixed.set(1); // beta is fixed
    double atmVol = 0.19;
    double beta = 0.5;
    double rho = -0.9;
    double nu = 1.8;
    double alpha = atmVol * Math.pow(FORWARD, 1 - beta);
    DoubleMatrix1D start = new DoubleMatrix1D(alpha, beta, rho, nu);
    SmileModelFitter<SABRFormulaData> sabrFitter = new SABRModelFitter(FORWARD, PUT_STRIKES, EXPIRY, PUT_IV,
        PUT_ERRORS, SABR);
    Function1D<Double, Double> smile = fitSmile(sabrFitter, start, fixed);
    printDetails(smile, DISPLAY_STRIKES, false);
    //    printDetailsWithShift(smile, DISPLAY_STRIKES, false, VOL_SHOCKS); // vol shocks
  }

  /**
   * Call options, local SABR.
   */
  @Test(description = "Demo", enabled = false)
  void sabrInterpolationCallTest() {
    GeneralSmileInterpolator sabr_interpolator = new SmileInterpolatorSABR();
    Function1D<Double, Double> smile = sabr_interpolator.getVolatilityFunction(FORWARD, CALL_STRIKES, EXPIRY, CALL_IV);
    printDetails(smile, DISPLAY_STRIKES, true);
    //    printDetailsWithShift(smile, DISPLAY_STRIKES, true, VOL_SHOCKS); // vol shocks
  }

  /**
   * Put options, local SABR.
   */
  @Test(description = "Demo", enabled = false)
  void sabrInterpolationPutTest() {
    GeneralSmileInterpolator sabr_interpolator = new SmileInterpolatorSABR();
    Function1D<Double, Double> smile = sabr_interpolator.getVolatilityFunction(FORWARD, PUT_STRIKES, EXPIRY, PUT_IV);
    printDetails(smile, DISPLAY_STRIKES, false);
    //    printDetailsWithShift(smile, DISPLAY_STRIKES, false, VOL_SHOCKS); // vol shocks
  }

  /**
   * Call options, spline, shifted lognormal with reduced gradient.
   */
  @Test(description = "Demo", enabled = false)
  void splineInterpolationCallTest() {
    GeneralSmileInterpolator spline = new SmileInterpolatorSpline(new DoubleQuadraticInterpolator1D(), "Quiet");
    Function1D<Double, Double> smile = spline.getVolatilityFunction(FORWARD, CALL_STRIKES, EXPIRY, CALL_IV);
    printDetails(smile, DISPLAY_STRIKES, true);
    //    printDetailsWithShift(smile, DISPLAY_STRIKES, true, VOL_SHOCKS); // vol shocks
  }

  /**
   * Put options, spline, shifted lognormal with reduced gradient.
   */
  @Test(description = "Demo", enabled = false)
  void splineInterpolationPutTest() {
    GeneralSmileInterpolator spline = new SmileInterpolatorSpline(new DoubleQuadraticInterpolator1D(), "Quiet");
    Function1D<Double, Double> smile = spline.getVolatilityFunction(FORWARD, PUT_STRIKES, EXPIRY, PUT_IV);
    printDetails(smile, DISPLAY_STRIKES, false);
    //    printDetailsWithShift(smile, DISPLAY_STRIKES, false, VOL_SHOCKS); // vol shocks
  }

  /**
   * Call options, spline, shifted lognormal with zero gradient.
   */
  @Test(description = "Demo", enabled = false)
  void splineInterpolationFlatCallTest() {
    GeneralSmileInterpolator spline = new SmileInterpolatorSpline(new DoubleQuadraticInterpolator1D(), "Flat");
    Function1D<Double, Double> smile = spline.getVolatilityFunction(FORWARD, CALL_STRIKES, EXPIRY, CALL_IV);
    printDetails(smile, DISPLAY_STRIKES, true);
    //    printDetailsWithShift(smile, DISPLAY_STRIKES, true, VOL_SHOCKS); // vol shocks
  }

  /**
   * Put options, spline, shifted lognormal with zero gradient.
   */
  @Test(description = "Demo", enabled = false)
  void splineInterpolationFlatPutTest() {
    GeneralSmileInterpolator spline = new SmileInterpolatorSpline(new DoubleQuadraticInterpolator1D(), "Flat");
    Function1D<Double, Double> smile = spline.getVolatilityFunction(FORWARD, PUT_STRIKES, EXPIRY, PUT_IV);
    printDetails(smile, DISPLAY_STRIKES, false);
    //    printDetailsWithShift(smile, DISPLAY_STRIKES, false, VOL_SHOCKS); // vol shocks
  }

  /**
   * Print strikes and implied vols for market available call options.
   */
  @Test(description = "Demo", enabled = false)
  void printCallStrikeVol() {
    int n = CALL_STRIKES.length;
    for (int i = 0; i < n; ++i) {
      System.out.println(CALL_STRIKES[i] + "\t" + CALL_IV[i]);
    }
  }

  /**
   * Print strikes and implied vols for market available put options.
   */
  @Test(description = "Demo", enabled = false)
  void printPutStrikeVol() {
    int n = PUT_STRIKES.length;
    for (int i = 0; i < n; ++i) {
      System.out.println(PUT_STRIKES[i] + "\t" + PUT_IV[i]);
    }
  }

  private Function1D<Double, Double> toSmileFunction(final double fwd, final double expiry, final SmileModelData data,
      final VolatilityFunctionProvider<? extends SmileModelData> volModel) {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        boolean isCall = k >= fwd;
        EuropeanVanillaOption option = new EuropeanVanillaOption(k, expiry, isCall);
        Function1D<SmileModelData, Double> func = (Function1D<SmileModelData, Double>) volModel.getVolatilityFunction(
            option, fwd);
        return func.evaluate(data);
      }
    };
  }

  private Function1D<Double, Double> fitSmile(SmileModelFitter<? extends SmileModelData> fitter, DoubleMatrix1D start,
      BitSet fixed) {
    LeastSquareResultsWithTransform res = fitter.solve(start, fixed);
    System.out.println("chi-Square: " + res.getChiSq());
    VolatilityFunctionProvider<?> model = fitter.getModel();
    SmileModelData data = fitter.toSmileModelData(res.getModelParameters());
    return toSmileFunction(FORWARD, EXPIRY, data, model);
  }


    /**
  * Print the smile, price, greeks
  */
  private void printDetails(Function1D<Double, Double> smile, double[] strikes, boolean isCall) {
    double facDiv = Math.exp(-DIVIDEND * EXPIRY);
    if (isCall) {
      System.out.println("Call Strike\tImplied Vol\tPrice\tDelta\tGamma\tVega\tTheta\tRho");
    } else {
      System.out.println("Put Strike\tImplied Vol\tPrice\tDelta\tGamma\tVega\tTheta\tRho");
    }
    for (int i = 0; i < strikes.length; i++) {
      double k = strikes[i];
      double vol = smile.evaluate(k);
      double price = DF * BlackFormulaRepository.price(FORWARD, k, EXPIRY, vol, isCall);
      double delta = BlackFormulaRepository.delta(FORWARD, k, EXPIRY, vol, isCall) * facDiv * 100.0;
      double gamma = BlackFormulaRepository.gamma(FORWARD, k, EXPIRY, vol) * facDiv * facDiv / DF * SPOT;
      double vega = DF * BlackFormulaRepository.vega(FORWARD, k, EXPIRY, vol) * SHARES / 100.0;
      double theta = BlackScholesFormulaRepository.theta(SPOT, k, EXPIRY, vol, RATE, RATE - DIVIDEND, isCall) * SHARES /
          365.0;
      double rho = BlackScholesFormulaRepository.rho(SPOT, k, EXPIRY, vol, RATE, RATE - DIVIDEND, isCall) * SHARES /
          10000.0;
      System.out.println(k + "\t" + vol + "\t" + price + "\t" + delta + "\t" + gamma + "\t" + vega + "\t" + theta +
          "\t" + rho);
    }
  }

  /**
  * Print the (shifted) smile, price, greeks with shift
  */
  private void printDetailsWithShift(Function1D<Double, Double> smile, double[] strikes, boolean isCall, double[] shifts) {
    int nShifts = shifts.length;
    double facDiv = Math.exp(-DIVIDEND * EXPIRY);
    for (int j = 0; j < nShifts; ++j) {
      System.out.println();
      System.out.println("Vol shift: " + shifts[j]);
      if (isCall) {
      System.out.println("Call Strike\tShifted Vol\tPrice\tDelta\tGamma\tVega\tTheta\tRho");
      } else {
        System.out.println("Put Strike\tShifted Vol\tPrice\tDelta\tGamma\tVega\tTheta\tRho");
      }
      for (int i = 0; i < strikes.length; i++) {
        double k = strikes[i];
        double vol = smile.evaluate(k) + shifts[j];
        double price = DF * BlackFormulaRepository.price(FORWARD, k, EXPIRY, vol, isCall);
        double delta = BlackFormulaRepository.delta(FORWARD, k, EXPIRY, vol, isCall) * facDiv * 100.0;
        double gamma = BlackFormulaRepository.gamma(FORWARD, k, EXPIRY, vol) * facDiv * facDiv / DF * SPOT;
        double vega = DF * BlackFormulaRepository.vega(FORWARD, k, EXPIRY, vol) * SHARES / 100.0;
        double theta = BlackScholesFormulaRepository.theta(SPOT, k, EXPIRY, vol, RATE, RATE - DIVIDEND, isCall) *
            SHARES / 365.0;
        double rho = BlackScholesFormulaRepository.rho(SPOT, k, EXPIRY, vol, RATE, RATE - DIVIDEND, isCall) * SHARES /
            10000.0;
        System.out.println(k + "\t" + vol + "\t" + price + "\t" + delta + "\t" + gamma + "\t" + vega + "\t" + theta +
          "\t" + rho);
      }
    }
  }

  /**
   * Test below is for debugging
   */
  @Test(enabled = false)
  public void testApr() {
    double time = (62. + (21. + 29. / 60.) / 24.0) / 365.;
    double fwd = 3440.26;
    double rate = 0.119 * 0.01;
    double dividend = 1.875 * 0.01;
    boolean isCall = true;
    double price = 108.1036;
    double strike = 3450.0;
    double spot = fwd * Math.exp((dividend - rate) * time);

    double[] dividends = new double[] {2.298, 0.745, 1.271, 3.867, 2.989 };
    double totalDiv = 0.0;
    for (int i = 0; i < dividends.length; ++i) {
      totalDiv += dividends[i];
    }
    System.out.println(totalDiv);
    System.out.println(Math.log(1.0 + totalDiv / spot) / time);

    printGreeks(price, fwd, strike, time, rate, dividend, isCall);

  }

  private void printGreeks(double price, double fwd, double strike, double time, double rate, double dividend,
      boolean isCall) {
    double facDiv = Math.exp(-dividend * time);
    double df = Math.exp(-rate * time);
    double fwdPrice = price / df;
    double impliedVol = BlackFormulaRepository.impliedVolatility(fwdPrice, fwd, strike, time, isCall);
    System.out.println("impliedVol: " + "\t" + impliedVol);
    double spot = fwd * Math.exp(-(rate - dividend) * time);
    double delta = BlackFormulaRepository.delta(fwd, strike, time, impliedVol, isCall) * facDiv * 100.0;
    System.out.println("delta: " + "\t" + delta);
    double gamma = BlackFormulaRepository.gamma(fwd, strike, time, impliedVol) * facDiv * facDiv / df * spot;
    System.out.println("gamma: " + "\t" + gamma);
    double vega = BlackFormulaRepository.vega(fwd, strike, time, impliedVol) * df * SHARES / 100.0;
    System.out.println("vega: " + "\t" + vega);
    double theta = BlackScholesFormulaRepository.theta(spot, strike, time, impliedVol, rate, rate - dividend, isCall) *
        SHARES / 365.0;
    System.out.println("theta: " + "\t" + theta);
    double rho = (BlackScholesFormulaRepository.rho(spot, strike, time, impliedVol, rate, rate - dividend, isCall)) *
        SHARES / 10000.0;
    System.out.println("rho: " + "\t" + rho);
  }
}

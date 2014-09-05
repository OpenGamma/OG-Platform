/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.lang.annotation.ExternalFunction;
import com.opengamma.util.CompareUtils;

/**
 * Class with the Hagan et al SABR volatility function.
 * Reference: Hagan, P.; Kumar, D.; Lesniewski, A. & Woodward, D. "Managing smile risk", Wilmott Magazine, 2002, September, 84-108
 */
public class SABRHaganVolatilityFunction extends VolatilityFunctionProvider<SABRFormulaData> {

  /**
   * Logger.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(SABRHaganVolatilityFunction.class);

  private static final double CUTOFF_MONEYNESS = 1e-12; //changed from 1e-6 on 3/3/2012 R white
  private static final double SMALL_Z = 1e-6;
  private static final double LARGE_NEG_Z = -1e6;
  private static final double LARGE_POS_Z = 1e8;
  private static final double BETA_EPS = 1e-8;
  private static final double RHO_EPS = 1e-5;
  private static final double RHO_EPS_NEGATIVE = 1e-8;
  private static final double ATM_EPS = 1e-7;

  //  private static final double EPS = 1e-15;

  @Override
  public Function1D<SABRFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    Validate.isTrue(forward >= 0.0, "forward must be greater than zero");

    return new Function1D<SABRFormulaData, Double>() {
      @Override
      public final Double evaluate(final SABRFormulaData data) {
        Validate.notNull(data, "data");
        return getVolatility(option, forward, data);
      }
    };
  }

  @Override
  public Function1D<SABRFormulaData, double[]> getVolatilityAdjointFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    Validate.isTrue(forward >= 0.0, "forward must be greater than zero");

    return new Function1D<SABRFormulaData, double[]>() {
      @Override
      public double[] evaluate(final SABRFormulaData data) {
        Validate.notNull(data, "data");
        return getVolatilityAdjoint(option, forward, data);
      }
    };
  }

  @Override
  public Function1D<SABRFormulaData, double[][]> getVolatilityAdjointFunction(final double forward, final double[] strikes, final double timeToExpiry) {
    return getVolatilityAdjointFunctionByCallingSingleStrikes(forward, strikes, timeToExpiry);
  }

  @Override
  public Function1D<SABRFormulaData, double[]> getModelAdjointFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    Validate.isTrue(forward >= 0.0, "forward must be greater than zero");

    return new Function1D<SABRFormulaData, double[]>() {
      @Override
      public double[] evaluate(final SABRFormulaData data) {
        Validate.notNull(data, "data");
        return getVolatilityModelAdjoint(option, forward, data);
      }
    };
  }

  @Override
  public Function1D<SABRFormulaData, double[][]> getModelAdjointFunction(final double forward, final double[] strikes, final double timeToExpiry) {
    return getModelAdjointFunctionByCallingSingleStrikes(forward, strikes, timeToExpiry);
  }

  /**
   * Standard Hagan formula for log-normal vol
   * @param option The option.
   * @param forward The forward value of the underlying
   * @param data The SABR data.
   * @return The log-normal volatility
   */
  public double getVolatility(final EuropeanVanillaOption option, final double forward, final SABRFormulaData data) {

    final double timeToExpiry = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final double alpha = data.getAlpha();
    final double beta = data.getBeta();
    final double rho = data.getRho();
    final double nu = data.getNu();

    if (alpha == 0.0) {
      return 0.0;
    }

    final double cutoff = forward * CUTOFF_MONEYNESS;
    final double k;
    if (strike < cutoff) {
      s_logger.info("Given strike of {} is less than cutoff at {}, therefore the strike is taken as {}", new Object[] {strike, cutoff, cutoff });
      k = cutoff;
    } else {
      k = strike;
    }
    double vol, z, zOverChi;
    final double beta1 = 1 - beta;
    if (CompareUtils.closeEquals(forward, k, ATM_EPS)) {
      final double f1 = Math.pow(forward, beta1);
      vol = alpha * (1 + timeToExpiry * (beta1 * beta1 * alpha * alpha / 24 / f1 / f1 + rho * alpha * beta * nu / 4 / f1 + nu * nu * (2 - 3 * rho * rho) / 24)) / f1;
    } else {
      if (CompareUtils.closeEquals(beta, 0, BETA_EPS)) {
        final double ln = Math.log(forward / k);
        z = nu * Math.sqrt(forward * k) * ln / alpha;
        zOverChi = getZOverChi(rho, z);
        vol = alpha * ln * zOverChi * (1 + timeToExpiry * (alpha * alpha / forward / k + nu * nu * (2 - 3 * rho * rho)) / 24) / (forward - k);
      } else if (CompareUtils.closeEquals(beta, 1, BETA_EPS)) {
        final double ln = Math.log(forward / k);
        z = nu * ln / alpha;
        zOverChi = getZOverChi(rho, z);
        vol = alpha * zOverChi * (1 + timeToExpiry * (rho * alpha * nu / 4 + nu * nu * (2 - 3 * rho * rho) / 24));
      } else {
        final double ln = Math.log(forward / k);
        final double f1 = Math.pow(forward * k, beta1);
        final double f1Sqrt = Math.sqrt(f1);
        final double lnBetaSq = Math.pow(beta1 * ln, 2);
        z = nu * f1Sqrt * ln / alpha;
        zOverChi = getZOverChi(rho, z);
        final double first = alpha / (f1Sqrt * (1 + lnBetaSq / 24 + lnBetaSq * lnBetaSq / 1920));
        final double second = zOverChi;
        final double third = 1 + timeToExpiry * (beta1 * beta1 * alpha * alpha / 24 / f1 + rho * nu * beta * alpha / 4 / f1Sqrt + nu * nu * (2 - 3 * rho * rho) / 24);

        vol = first * second * third;
      }
    }
    //There is nothing to prevent the nu * nu * (2 - 3 * rho * rho) / 24 part taking the third term, and hence the volatility negative
    return vol;
    // return Math.max(0.0, vol);
  }

  @ExternalFunction
  public double getVolatility(final double forward, final double strike, final double timeToExpiry, final double alpha, final double beta, final double rho, final double nu) {
    Validate.isTrue(forward > 0, "Forward must be > 0");
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, timeToExpiry, true);
    final SABRFormulaData data = new SABRFormulaData(alpha, beta, rho, nu);
    return getVolatility(option, forward, data);
  }

  /**
   * Gets the volatility sensitivity to the SABr parameters
   * @param option The option.
   * @param forward The forward value of the underlying
   * @param data The SABR data.
   * @return array with alpha, beta, rho and nu sensitivities
   */
  public double[] getVolatilityModelAdjoint(final EuropeanVanillaOption option, final double forward, final SABRFormulaData data) {
    final double[] volatilityAdjoint = new double[4];
    final double alpha = data.getAlpha();

    double strike = option.getStrike();
    final double cutoff = forward * CUTOFF_MONEYNESS;
    if (strike < cutoff) {
      s_logger.info("Given strike of {} is less than cutoff at {}, therefore the strike is taken as {}", new Object[] {strike, cutoff, cutoff });
      strike = cutoff;
    }

    final double timeToExpiry = option.getTimeToExpiry();

    final double beta = data.getBeta();
    final double betaStar = 1 - beta;
    final double rho = data.getRho();
    final double nu = data.getNu();

    if (alpha == 0.0) {
      Arrays.fill(volatilityAdjoint, 0.0);
      if (CompareUtils.closeEquals(forward, strike, ATM_EPS)) { //TODO should this is relative
        volatilityAdjoint[3] = (1 + (2 - 3 * rho * rho) * nu * nu / 24 * timeToExpiry) / Math.pow(forward, betaStar);
      } else {
        //for non-atm options the alpha sensitivity at alpha = 0 is infinite. Returning this will most likely break calibrations,
        // so we return an arbitrary large number
        volatilityAdjoint[3] = 1e7;
      }
      return volatilityAdjoint;
    }

    // Implementation note: Forward sweep.
    final double sfK = Math.pow(forward * strike, betaStar / 2);
    final double lnrfK = Math.log(forward / strike);
    final double z = nu / alpha * sfK * lnrfK;
    final double sf1 = sfK * (1 + betaStar * betaStar / 24 * (lnrfK * lnrfK) + Math.pow(betaStar, 4) / 1920 * Math.pow(lnrfK, 4));
    final double sf2 = (1 + (Math.pow(betaStar * alpha / sfK, 2) / 24 + (rho * beta * nu * alpha) / (4 * sfK) + (2 - 3 * rho * rho) * nu * nu / 24) * timeToExpiry);

    // Implementation note: Backward sweep.
    final double[] zOverChi = zOverChiWithDev(rho, z);
    final double vBar = 1;
    final double sf2Bar = alpha / sf1 * zOverChi[0] * vBar;
    final double sf1Bar = -alpha / (sf1 * sf1) * zOverChi[0] * sf2 * vBar;
    final double rzxzBar = alpha / sf1 * sf2 * vBar;
    final double zBar = zOverChi[2] * rzxzBar;
    //    double xzBar = 0;

    final double sfKBar = nu / alpha * lnrfK * zBar + sf1 / sfK * sf1Bar - (Math.pow(betaStar * alpha, 2) / Math.pow(sfK, 3) / 12 + (rho * beta * nu * alpha) / 4 / (sfK * sfK)) * timeToExpiry
        * sf2Bar;

    final double nuBar = 1 / alpha * sfK * lnrfK * zBar + ((rho * beta * alpha) / (4 * sfK) + (2 - 3 * rho * rho) * nu / 12) * timeToExpiry * sf2Bar;
    final double rhoBar = zOverChi[1] * rzxzBar + ((beta * nu * alpha) / (4 * sfK) - rho * nu * nu / 4) * timeToExpiry * sf2Bar;

    final double alphaBar = -nu / (alpha * alpha) * sfK * lnrfK * zBar + ((betaStar * alpha / sfK) * (betaStar / sfK) / 12 + (rho * beta * nu) / (4 * sfK)) * timeToExpiry * sf2Bar + 1 / sf1
        * zOverChi[0] * sf2 * vBar;
    final double betaBar = -0.5 * Math.log(forward * strike) * sfK * sfKBar - sfK * (betaStar / 12 * (lnrfK * lnrfK) + Math.pow(betaStar, 3) / 480 * Math.pow(lnrfK, 4)) * sf1Bar
        + (-betaStar * alpha * alpha / sfK / sfK / 12 + rho * nu * alpha / 4 / sfK) * timeToExpiry * sf2Bar;

    volatilityAdjoint[0] = alphaBar;
    volatilityAdjoint[1] = betaBar;
    volatilityAdjoint[2] = rhoBar;
    volatilityAdjoint[3] = nuBar;

    return volatilityAdjoint;
  }

  /**
   * Return the Black implied volatility in the SABR model and its derivatives.
   * @param option The option.
   * @param forward The forward value of the underlying
   * @param data The SABR data.
   * @return An array with [0] the volatility, [1] Derivative w.r.t the forward, [2] the derivative w.r.t the strike, [3] the derivative w.r.t. to alpha,
   *  [4] the derivative w.r.t. to beta, [5] the derivative w.r.t. to rho, [6] the derivative w.r.t. to nu
   */
  public double[] getVolatilityAdjoint(final EuropeanVanillaOption option, final double forward, final SABRFormulaData data) {
    /**
     * The array storing the price and derivatives.
     */
    final double[] volatilityAdjoint = new double[7];
    final double alpha = data.getAlpha();

    double strike = option.getStrike();
    final double cutoff = forward * CUTOFF_MONEYNESS;
    if (strike < cutoff) {
      s_logger.info("Given strike of {} is less than cutoff at {}, therefore the strike is taken as {}", new Object[] {strike, cutoff, cutoff });
      strike = cutoff;
    }

    final double timeToExpiry = option.getTimeToExpiry();

    final double beta = data.getBeta();
    final double betaStar = 1 - beta;
    final double rho = data.getRho();
    final double nu = data.getNu();
    final double rhoStar = 1.0 - rho;

    if (alpha == 0.0) {
      Arrays.fill(volatilityAdjoint, 0.0);
      if (CompareUtils.closeEquals(forward, strike, ATM_EPS)) { //TODO should this is relative
        volatilityAdjoint[3] = (1 + (2 - 3 * rho * rho) * nu * nu / 24 * timeToExpiry) / Math.pow(forward, betaStar);
      } else {
        //for non-atm options the alpha sensitivity at alpha = 0 is infinite. Returning this will most likely break calibrations,
        // so we return an arbitrary large number
        volatilityAdjoint[3] = 1e7;
      }
      return volatilityAdjoint;
    }

    // Implementation note: Forward sweep.
    final double sfK = Math.pow(forward * strike, betaStar / 2);
    final double lnrfK = Math.log(forward / strike);
    final double z = nu / alpha * sfK * lnrfK;
    double rzxz;
    double xz = 0;
    if (CompareUtils.closeEquals(z, 0.0, SMALL_Z)) {
      rzxz = 1.0 - 0.5 * z * rho; //small z expansion to z^2 terms
    } else {
      if (CompareUtils.closeEquals(rhoStar, 0.0, RHO_EPS)) {
        if (z >= 1.0) {
          if (rhoStar == 0.0) {
            rzxz = 0.0;
            xz = Double.POSITIVE_INFINITY;
          } else {
            xz = (Math.log(2 * (z - 1)) - Math.log(rhoStar));
            rzxz = z / xz;
          }
        } else {
          xz = -Math.log(1 - z) - 0.5 * Math.pow(z / (z - 1.0), 2) * rhoStar;
          rzxz = z / xz;
        }
      } else {
        double arg;
        if (z < LARGE_NEG_Z) {
          arg = (rho * rho - 1) / 2 / z; //get rounding errors due to fine balanced cancellation for very large negative z
        } else if (z > LARGE_POS_Z) {
          arg = 2 * (z - rho);
        } else {
          arg = (Math.sqrt(1 - 2 * rho * z + z * z) + z - rho);
        }
        if (arg <= 0.0) { //Mathematically this cannot be less than zero, but you know what computers are like.
          rzxz = 0.0;
        } else {
          xz = Math.log(arg / (1 - rho));
          rzxz = z / xz;
        }
      }
    }
    final double sf1 = sfK * (1 + betaStar * betaStar / 24 * (lnrfK * lnrfK) + Math.pow(betaStar, 4) / 1920 * Math.pow(lnrfK, 4));
    final double sf2 = (1 + (Math.pow(betaStar * alpha / sfK, 2) / 24 + (rho * beta * nu * alpha) / (4 * sfK) + (2 - 3 * rho * rho) * nu * nu / 24) * timeToExpiry);
    volatilityAdjoint[0] = alpha / sf1 * rzxz * sf2;

    // Implementation note: Backward sweep.
    final double vBar = 1;
    final double sf2Bar = alpha / sf1 * rzxz * vBar;
    final double sf1Bar = -alpha / (sf1 * sf1) * rzxz * sf2 * vBar;
    final double rzxzBar = alpha / sf1 * sf2 * vBar;
    double zBar;
    double xzBar = 0.0;
    if (CompareUtils.closeEquals(z, 0.0, SMALL_Z)) {
      zBar = -rho / 2 * rzxzBar;
    } else {
      if (CompareUtils.closeEquals(rhoStar, 0.0, RHO_EPS)) {
        if (z >= 1.0) {
          if (z == 1.0) {
            zBar = 0.0;
          } else {
            final double chiDz = 1 / (z - 1);
            xzBar = -rzxzBar * z / (xz * xz);
            zBar = volatilityAdjoint[0] / z + chiDz * xzBar;
          }
        } else {
          zBar = -1.0 / Math.log(1 - z) * (1 + z / Math.log(1 - z) / (1 - z)) * rzxzBar;
          xzBar = -z / (xz * xz) * rzxzBar;
        }
      } else {
        if (z < LARGE_NEG_Z) {
          zBar = 1 / xz * rzxzBar + xzBar / (xz * xz) * rzxzBar;
        } else if (z > LARGE_POS_Z) {
          zBar = 1 / xz * rzxzBar - xzBar / (xz * xz) * rzxzBar;
        } else {
          xzBar = -z / (xz * xz) * rzxzBar;
          zBar = 1 / xz * rzxzBar + 1 / ((Math.sqrt(1 - 2 * rho * z + z * z) + z - rho)) * (0.5 * Math.pow(1 - 2 * rho * z + z * z, -0.5) * (-2 * rho + 2 * z) + 1) * xzBar;
        }
      }
    }

    final double lnrfKBar = sfK * (betaStar * betaStar / 12 * lnrfK + Math.pow(betaStar, 4) / 1920 * 4 * Math.pow(lnrfK, 3)) * sf1Bar + nu / alpha * sfK * zBar;
    final double sfKBar = nu / alpha * lnrfK * zBar + sf1 / sfK * sf1Bar - (Math.pow(betaStar * alpha, 2) / Math.pow(sfK, 3) / 12 + (rho * beta * nu * alpha) / 4 / (sfK * sfK)) * timeToExpiry
        * sf2Bar;
    final double strikeBar = -1 / strike * lnrfKBar + betaStar * sfK / (2 * strike) * sfKBar;
    final double forwardBar = 1 / forward * lnrfKBar + betaStar * sfK / (2 * forward) * sfKBar;
    final double nuBar = 1 / alpha * sfK * lnrfK * zBar + ((rho * beta * alpha) / (4 * sfK) + (2 - 3 * rho * rho) * nu / 12) * timeToExpiry * sf2Bar;

    double rhoBar;
    if (Math.abs(forward - strike) < ATM_EPS) {
      rhoBar = -z / 2 * rzxzBar;
    } else {
      if (CompareUtils.closeEquals(rhoStar, 0.0, RHO_EPS)) {
        if (z >= 1) {
          if (rhoStar == 0.0) {
            rhoBar = Double.NEGATIVE_INFINITY; //the derivative at rho = 1 is infinite  - this sets it to some arbitrary large number
          } else {
            rhoBar = xzBar * (1.0 / rhoStar + (0.5 - z) / (z - 1.0) / (z - 1.0));
          }
        } else {
          rhoBar = (0.5 * Math.pow(z / (1 - z), 2) + 0.25 * (z - 4.0) * Math.pow(z / (1.0 - z), 3) / (1.0 - z) * rhoStar) * xzBar;
        }
      } else {
        rhoBar = (1 / (Math.sqrt(1 - 2 * rho * z + z * z) + z - rho) * (-Math.pow(1 - 2 * rho * z + z * z, -0.5) * z - 1) + 1 / rhoStar) * xzBar;
      }
    }
    rhoBar += ((beta * nu * alpha) / (4 * sfK) - rho * nu * nu / 4) * timeToExpiry * sf2Bar;

    final double alphaBar = -nu / (alpha * alpha) * sfK * lnrfK * zBar + ((betaStar * alpha / sfK) * (betaStar / sfK) / 12 + (rho * beta * nu) / (4 * sfK)) * timeToExpiry * sf2Bar + 1 / sf1 * rzxz
        * sf2 * vBar;
    final double betaBar = -0.5 * Math.log(forward * strike) * sfK * sfKBar - sfK * (betaStar / 12 * (lnrfK * lnrfK) + Math.pow(betaStar, 3) / 480 * Math.pow(lnrfK, 4)) * sf1Bar
        + (-betaStar * alpha * alpha / sfK / sfK / 12 + rho * nu * alpha / 4 / sfK) * timeToExpiry * sf2Bar;

    volatilityAdjoint[1] = forwardBar;
    volatilityAdjoint[2] = strikeBar;
    volatilityAdjoint[3] = alphaBar;
    volatilityAdjoint[4] = betaBar;
    volatilityAdjoint[5] = rhoBar;
    volatilityAdjoint[6] = nuBar;

    return volatilityAdjoint;
  }

  /**
   * Computes the first and second order derivatives of the Black implied volatility in the SABR model. 
   * Around ATM, a first order expansion is used to due to some 0/0-type indetermination. The second order derivative produced is poor around ATM.
   * @param option The option.
   * @param forward the forward value of the underlying
   * @param data The SABR data.
   * @param volatilityD The array used to return the first order derivatives. [0] Derivative w.r.t the forward, [1] the derivative w.r.t the strike, [2] the derivative w.r.t. to alpha,
   * [3] the derivative w.r.t. to beta, [4] the derivative w.r.t. to rho, [5] the derivative w.r.t. to nu
   * @param volatilityD2 The array of array used to return the second order derivative. Only the second order derivative with respect to the forward and strike are implemented.
   * [0][0] forward-forward; [0][1] forward-strike; [1][1] strike-strike.
   * @return The Black implied volatility.
   */
  public double getVolatilityAdjoint2(final EuropeanVanillaOption option, final double forward, final SABRFormulaData data, final double[] volatilityD, final double[][] volatilityD2) {
    final double k = Math.max(option.getStrike(), 0.000001);
    final double theta = option.getTimeToExpiry();
    final double alpha = data.getAlpha();
    final double beta = data.getBeta();
    final double rho = data.getRho();
    final double nu = data.getNu();
    // Forward
    final double h0 = (1 - beta) / 2;
    final double h1 = forward * k;
    final double h1h0 = Math.pow(h1, h0);
    final double h12 = h1h0 * h1h0;
    final double h2 = Math.log(forward / k);
    final double h22 = h2 * h2;
    final double h23 = h22 * h2;
    final double h24 = h23 * h2;
    final double f1 = h1h0 * (1 + h0 * h0 / 6.0 * (h22 + h0 * h0 / 20.0 * h24));
    final double f2 = nu / alpha * h1h0 * h2;
    final double f3 = h0 * h0 / 6.0 * alpha * alpha / h12 + rho * beta * nu * alpha / 4.0 / h1h0 + (2 - 3 * rho * rho) / 24.0 * nu * nu;
    final double sqrtf2 = Math.sqrt(1 - 2 * rho * f2 + f2 * f2);
    double f2x = 0.0;
    double x = 0.0, xp = 0, xpp = 0;
    if (CompareUtils.closeEquals(f2, 0.0, SMALL_Z)) {
      f2x = 1.0 - 0.5 * f2 * rho; //small f2 expansion to f2^2 terms
    } else {
      if (CompareUtils.closeEquals(rho, 1.0, RHO_EPS)) {
        x = f2 < 1.0 ? -Math.log(1.0 - f2) - 0.5 * Math.pow(f2 / (f2 - 1.0), 2) * (1.0 - rho) : Math.log(2.0 * f2 - 2.0) - Math.log(1.0 - rho);
      } else {
        x = Math.log((sqrtf2 + f2 - rho) / (1 - rho));
      }
      xp = 1. / sqrtf2;
      xpp = (rho - f2) / Math.pow(sqrtf2, 3.0);
      f2x = f2 / x;
    }
    final double sigma = alpha / f1 * f2x * (1 + f3 * theta);
    // First level
    final double h0Dbeta = -0.5;
    final double sigmaDf1 = -sigma / f1;
    double sigmaDf2 = 0;
    if (CompareUtils.closeEquals(f2, 0.0, SMALL_Z)) {
      sigmaDf2 = alpha / f1 * (1 + f3 * theta) * -0.5 * rho;
    } else {
      sigmaDf2 = alpha / f1 * (1 + f3 * theta) * (1.0 / x - f2 * xp / (x * x));
    }
    final double sigmaDf3 = alpha / f1 * f2x * theta;
    final double sigmaDf4 = f2x / f1 * (1 + f3 * theta);
    final double sigmaDx = -alpha / f1 * f2 / (x * x) * (1 + f3 * theta);
    final double[][] sigmaD2ff = new double[3][3];
    sigmaD2ff[0][0] = -sigmaDf1 / f1 + sigma / (f1 * f1); //OK
    sigmaD2ff[0][1] = -sigmaDf2 / f1;
    sigmaD2ff[0][2] = -sigmaDf3 / f1;
    if (CompareUtils.closeEquals(f2, 0.0, SMALL_Z)) {
      sigmaD2ff[1][2] = alpha / f1 * -0.5 * rho * theta;
    } else {
      sigmaD2ff[1][1] = alpha / f1 * (1 + f3 * theta) * (-2 * xp / (x * x) - f2 * xpp / (x * x) + 2 * f2 * xp * xp / (x * x * x));
      sigmaD2ff[1][2] = alpha / f1 * theta * (1.0 / x - f2 * xp / (x * x));
    }
    sigmaD2ff[2][2] = 0.0;
    //     final double sigma = alpha / f1 * f2x * (1 + f3 * theta);
    // Second level
    final double[] f1Dh = new double[3];
    final double[] f2Dh = new double[3];
    final double[] f3Dh = new double[3];
    f1Dh[0] = h1h0 * (h0 * (h22 / 3.0 + h0 * h0 / 40.0 * h24)) + Math.log(h1) * f1;
    f1Dh[1] = h0 * f1 / h1;
    f1Dh[2] = h1h0 * (h0 * h0 / 6.0 * (2.0 * h2 + h0 * h0 / 5.0 * h23));
    f2Dh[0] = Math.log(h1) * f2;
    f2Dh[1] = h0 * f2 / h1;
    f2Dh[2] = nu / alpha * h1h0;
    f3Dh[0] = h0 / 3.0 * alpha * alpha / h12 - 2 * h0 * h0 / 6.0 * alpha * alpha / h12 * Math.log(h1) - rho * beta * nu * alpha / 4.0 / h1h0 * Math.log(h1);
    f3Dh[1] = -2 * h0 * h0 / 6.0 * alpha * alpha / h12 * h0 / h1 - rho * beta * nu * alpha / 4.0 / h1h0 * h0 / h1;
    f3Dh[2] = 0.0;
    final double[] f1Dp = new double[4]; // Derivative to sabr parameters
    final double[] f2Dp = new double[4];
    final double[] f3Dp = new double[4];
    final double[] f4Dp = new double[4];
    f1Dp[0] = 0.0;
    f1Dp[1] = f1Dh[0] * h0Dbeta;
    f1Dp[2] = 0.0;
    f1Dp[3] = 0.0;
    f2Dp[0] = -f2 / alpha;
    f2Dp[1] = f2Dh[0] * h0Dbeta;
    f2Dp[2] = 0.0;
    f2Dp[3] = h1h0 * h2 / alpha;
    f3Dp[0] = h0 * h0 / 3.0 * alpha / h12 + rho * beta * nu / 4.0 / h1h0;
    f3Dp[1] = rho * nu * alpha / 4.0 / h1h0 + f3Dh[0] * h0Dbeta;
    f3Dp[2] = beta * nu * alpha / 4.0 / h1h0 - rho / 4.0 * nu * nu;
    f3Dp[3] = rho * beta * alpha / 4.0 / h1h0 + (2 - 3 * rho * rho) / 12.0 * nu;
    f4Dp[0] = 1.0;
    f4Dp[1] = 0.0;
    f4Dp[2] = 0.0;
    f4Dp[3] = 0.0;
    final double sigmaDh1 = sigmaDf1 * f1Dh[1] + sigmaDf2 * f2Dh[1] + sigmaDf3 * f3Dh[1];
    final double sigmaDh2 = sigmaDf1 * f1Dh[2] + sigmaDf2 * f2Dh[2] + sigmaDf3 * f3Dh[2];
    final double[][] f1D2hh = new double[2][2]; // No h0
    final double[][] f2D2hh = new double[2][2];
    final double[][] f3D2hh = new double[2][2];
    f1D2hh[0][0] = h0 * (h0 - 1) * f1 / (h1 * h1);
    f1D2hh[0][1] = h0 * h1h0 / h1 * h0 * h0 / 6.0 * (2.0 * h2 + 4.0 * h0 * h0 / 20.0 * h23);
    f1D2hh[1][1] = h1h0 * (h0 * h0 / 6.0 * (2.0 + 12.0 * h0 * h0 / 20.0 * h2));
    f2D2hh[0][0] = h0 * (h0 - 1) * f2 / (h1 * h1);
    f2D2hh[0][1] = nu / alpha * h0 * h1h0 / h1;
    f2D2hh[1][1] = 0.0;
    f3D2hh[0][0] = 2 * h0 * (2 * h0 + 1) * h0 * h0 / 6.0 * alpha * alpha / (h12 * h1 * h1) + h0 * (h0 + 1) * rho * beta * nu * alpha / 4.0 / (h1h0 * h1 * h1);
    f3D2hh[0][1] = 0.0;
    f3D2hh[1][1] = 0.0;
    final double[][] sigmaD2hh = new double[2][2]; // No h0
    for (int loopx = 0; loopx < 2; loopx++) {
      for (int loopy = loopx; loopy < 2; loopy++) {
        sigmaD2hh[loopx][loopy] = (sigmaD2ff[0][0] * f1Dh[loopy + 1] + sigmaD2ff[0][1] * f2Dh[loopy + 1] + sigmaD2ff[0][2] * f3Dh[loopy + 1]) * f1Dh[loopx + 1] + sigmaDf1 * f1D2hh[loopx][loopy]
            + (sigmaD2ff[0][1] * f1Dh[loopy + 1] + sigmaD2ff[1][1] * f2Dh[loopy + 1] + sigmaD2ff[1][2] * f3Dh[loopy + 1]) * f2Dh[loopx + 1] + sigmaDf2 * f2D2hh[loopx][loopy]
            + (sigmaD2ff[0][2] * f1Dh[loopy + 1] + sigmaD2ff[1][2] * f2Dh[loopy + 1] + sigmaD2ff[2][2] * f3Dh[loopy + 1]) * f3Dh[loopx + 1] + sigmaDf3 * f3D2hh[loopx][loopy];
      }
    }
    // Third level
    final double h1Df = k;
    final double h1Dk = forward;
    final double h1D2ff = 0.0;
    final double h1D2kf = 1.0;
    final double h1D2kk = 0.0;
    final double h2Df = 1.0 / forward;
    final double h2Dk = -1.0 / k;
    final double h2D2ff = -1 / (forward * forward);
    final double h2D2fk = 0.0;
    final double h2D2kk = 1.0 / (k * k);
    volatilityD[0] = sigmaDh1 * h1Df + sigmaDh2 * h2Df;
    volatilityD[1] = sigmaDh1 * h1Dk + sigmaDh2 * h2Dk;
    volatilityD[2] = sigmaDf1 * f1Dp[0] + sigmaDf2 * f2Dp[0] + sigmaDf3 * f3Dp[0] + sigmaDf4 * f4Dp[0];
    volatilityD[3] = sigmaDf1 * f1Dp[1] + sigmaDf2 * f2Dp[1] + sigmaDf3 * f3Dp[1] + sigmaDf4 * f4Dp[1];
    if (CompareUtils.closeEquals(f2, 0.0, SMALL_Z)) {
      volatilityD[4] = -0.5 * f2 + sigmaDf3 * f3Dp[2];
    } else {
      double xDr;
      if (CompareUtils.closeEquals(rho, 1.0, RHO_EPS)) {
        xDr = f2 > 1.0 ? 1.0 / (1.0 - rho) + (0.5 - f2) / (f2 - 1.0) / (f2 - 1.0) : 0.5 * Math.pow(f2 / (1.0 - f2), 2.0) + 0.25 * (f2 - 4.0) * Math.pow(f2 / (f2 - 1.0), 3) / (f2 - 1.0) * (1.0 - rho);
        if (Doubles.isFinite(xDr)) {
          volatilityD[4] = sigmaDf1 * f1Dp[2] + sigmaDx * xDr + sigmaDf3 * f3Dp[2] + sigmaDf4 * f4Dp[2];
        } else {
          volatilityD[4] = Double.NEGATIVE_INFINITY;
        }
      } else {
        xDr = (-f2 / sqrtf2 - 1 + (sqrtf2 + f2 - rho) / (1 - rho)) / (sqrtf2 + f2 - rho);
        volatilityD[4] = sigmaDf1 * f1Dp[2] + sigmaDx * xDr + sigmaDf3 * f3Dp[2] + sigmaDf4 * f4Dp[2];
      }
    }
    volatilityD[5] = sigmaDf1 * f1Dp[3] + sigmaDf2 * f2Dp[3] + sigmaDf3 * f3Dp[3] + sigmaDf4 * f4Dp[3];
    volatilityD2[0][0] = (sigmaD2hh[0][0] * h1Df + sigmaD2hh[0][1] * h2Df) * h1Df + sigmaDh1 * h1D2ff + (sigmaD2hh[0][1] * h1Df + sigmaD2hh[1][1] * h2Df) * h2Df + sigmaDh2 * h2D2ff;
    volatilityD2[0][1] = (sigmaD2hh[0][0] * h1Dk + sigmaD2hh[0][1] * h2Dk) * h1Df + sigmaDh1 * h1D2kf + (sigmaD2hh[0][1] * h1Dk + sigmaD2hh[1][1] * h2Dk) * h2Df + sigmaDh2 * h2D2fk;
    volatilityD2[1][0] = volatilityD2[0][1];
    volatilityD2[1][1] = (sigmaD2hh[0][0] * h1Dk + sigmaD2hh[0][1] * h2Dk) * h1Dk + sigmaDh1 * h1D2kk + (sigmaD2hh[0][1] * h1Dk + sigmaD2hh[1][1] * h2Dk) * h2Dk + sigmaDh2 * h2D2kk;
    return sigma;
  }

  private double getZOverChi(final double rho, final double z) {

    // Implementation comment: To avoid numerical instability (0/0) around ATM the first order approximation is used.
    if (CompareUtils.closeEquals(z, 0.0, SMALL_Z)) {
      return 1.0 - rho * z / 2.0;
    }

    final double rhoStar = 1 - rho;
    if (CompareUtils.closeEquals(rhoStar, 0.0, RHO_EPS)) {
      if (z > 1.0) {
        if (rhoStar == 0.0) {
          return 0.0;
        }
        return z / (Math.log(2 * (z - 1)) - Math.log(rhoStar));
      } else if (z < 1.0) {
        return z / (-Math.log(1 - z) - 0.5 * FunctionUtils.square(z / (z - 1.0)) * rhoStar);
      } else {
        return 0.0;
      }
    }

    final double rhoHat = 1 + rho;
    if (CompareUtils.closeEquals(rhoHat, 0.0, RHO_EPS_NEGATIVE)) {
      if (z > -1) {
        return z / Math.log(1 + z);
      } else if (z < -1) {
        if (rhoHat == 0) {
          return 0.0;
        }
        final double chi = Math.log(rhoHat) - Math.log(-(1 + z) / rhoStar);
        return z / chi;
      } else {
        return 0.0;
      }
    }

    double arg;
    if (z < LARGE_NEG_Z) {
      arg = (rho * rho - 1) / 2 / z; //get rounding errors due to fine balanced cancellation for very large negative z
    } else if (z > LARGE_POS_Z) {
      arg = 2 * (z - rho);
    } else {
      arg = (Math.sqrt(1 - 2 * rho * z + z * z) + z - rho);
      //Mathematically this cannot be less than zero, but you know what computers are like.
      if (arg <= 0.0) {
        return 0.0;
      }
    }

    final double chi = Math.log(arg) - Math.log(rhoStar);
    return z / chi;
  }

  /**
   * computes the z/chi(z) term, and its derivatives wrt rho and z for all possible values of rho and z (i.e. the edge cases
   * rho = +- 1 are handled).
   * @param rho
   * @param z
   * @return values, derivative wrt rho, and derivative wrt z
   * 
   */
  private double[] zOverChiWithDev(final double rho, final double z) {
    final double[] res = new double[3];
    if (CompareUtils.closeEquals(z, 0.0, SMALL_Z)) {
      res[0] = 1 - rho * z / 2;
      res[1] = -z / 2;
      res[2] = -rho / 2;
      return res;
    }
    final double rhoStar = 1 - rho;
    if (CompareUtils.closeEquals(rhoStar, 0.0, RHO_EPS)) {
      if (z > 1) {
        if (rhoStar == 0) {
          res[0] = 0.0;
          res[1] = Double.NEGATIVE_INFINITY;
          res[2] = 0;
        } else {
          final double temp = Math.log(2 * (z - 1)) - Math.log(rhoStar);
          res[0] = z / temp;
          res[1] = -z / temp / temp * (1.0 / rhoStar + (0.5 - z) / FunctionUtils.square(z - 1.0));
          res[2] = 1 / temp - z / temp / temp / Math.sqrt(1.0 - 2.0 * rho * z + z * z);
        }
      } else if (z < 1) {
        final double temp = -Math.log(1 - z) - 0.5 * FunctionUtils.square(z / (z - 1.0)) * rhoStar;
        res[0] = z / temp;
        res[1] = -z / temp / temp * (0.5 * FunctionUtils.square(z / (z - 1.0)) + (0.25 * z - 1.0) * FunctionUtils.cube(z / (z - 1.0)) / (z - 1.0) * rhoStar);
        res[2] = 1 / temp - z / temp / temp / Math.sqrt(1.0 - 2.0 * rho * z + z * z);
      } else {
        throw new MathException("can't handle z=1, rho=1");
      }
      return res;
    }
    final double rhoHat = 1 + rho;
    if (CompareUtils.closeEquals(rhoHat, 0.0, RHO_EPS_NEGATIVE)) {
      if (z > -1) {
        final double temp = Math.log(1 + z);
        final double temp2 = temp * temp;
        res[0] = z / temp;
        res[1] = ((2 * z + 1) / 2 / FunctionUtils.square(1 + z) - 1 / rhoStar) * z / temp2;
        res[2] = 1 / temp - z / (1 + z) / temp2;
      } else if (z < -1) {
        if (rhoHat == 0) {
          res[0] = 0;
          final double chi = Math.log(RHO_EPS_NEGATIVE) - Math.log(-(1 + z) / rhoStar);
          final double chiRho = 1 / RHO_EPS_NEGATIVE + 1 / rhoStar - FunctionUtils.square(z / (1 + z));
          res[1] = -chiRho * z / chi / chi; //should be +infinity
          res[2] = 0.0;
        } else {
          final double chi = Math.log(rhoHat) - Math.log(-(1 + z) / rhoStar);
          res[0] = z / chi;
          final double chiRho = 1 / rhoHat + 1 / rhoStar - FunctionUtils.square(z / (1 + z));
          res[1] = -chiRho * z / chi / chi;
          res[2] = 1 / chi + z / chi / chi / (1 + z);
        }
      } else {
        throw new MathException("can't handle z=-1, rho=-1");
      }
      return res;
    }

    //now the non-edge case
    double root = 0;
    double arg;
    double argRho;
    double argZ;
    if (z < LARGE_NEG_Z) {
      root = -z + rho - 1 / 2 / z;
      arg = (rho * rho - 1) / 2 / z; //get rounding errors due to fine balanced cancellation for very large negative z
      argRho = rho / z;
      argZ = -arg / z;
    } else if (z > LARGE_POS_Z) {
      root = z - rho + 1 / 2 / z;
      arg = root + z - rho;
      argRho = -2;
      argZ = 2 - 1 / 2 / z / z;
    } else {
      root = Math.sqrt(1 - 2 * rho * z + z * z);
      arg = root + z - rho;
      argRho = -(z / root + 1);
      argZ = (z - rho) / root + 1;
    }
    if (arg <= 0.0) { //Mathematically this cannot be less than zero, but you know what computers are like.
      res[0] = 0.0;
      res[1] = 0.0;
      res[2] = 0.0;
    } else {
      final double chi = Math.log(arg / (1 - rho));
      res[0] = z / chi;
      final double chiRho = argRho / arg + 1 / rhoStar;
      final double zChi2 = z / chi / chi;
      res[1] = -chiRho * zChi2;
      final double chiZ = argZ / arg;
      res[2] = 1 / chi - zChi2 * chiZ;
    }

    return res;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "SABR (Hagan)";
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * The price function to compute the price of barrier option in the Black world.
 * Reference: E. G. Haug (2007) The complete guide to Option Pricing Formulas. Mc Graw Hill. Section 4.17.1.
 */
public final class BlackBarrierPriceFunction {

  /**
   * The normal distribution implementation used in the pricing.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  /** Static instance */
  private static final BlackBarrierPriceFunction INSTANCE = new BlackBarrierPriceFunction();

  /**
   * Gets the static instance
   * @return The instance
   */
  public static BlackBarrierPriceFunction getInstance() {
    return INSTANCE;
  }

  private BlackBarrierPriceFunction() {
  }

  /**
   * Computes the price of a barrier option in the Black world.
   * @param option The underlying European vanilla option.
   * @param barrier The barrier.
   * @param rebate The rebate. This is paid <b>immediately</b> if the knock-out barrier is hit and at expiry if the knock-in barrier is not hit
   * @param spot The spot price.
   * @param costOfCarry The cost of carry (i.e. the forward = spot*exp(costOfCarry*T) )
   * @param rate The interest rate.
   * @param sigma The Black volatility.
   * @return The price.
   */
  public double getPrice(final EuropeanVanillaOption option, final Barrier barrier, final double rebate, final double spot, final double costOfCarry, final double rate, final double sigma) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(barrier, "barrier");
    final boolean isKnockIn = (barrier.getKnockType() == KnockType.IN);
    final boolean isDown = (barrier.getBarrierType() == BarrierType.DOWN);
    final double h = barrier.getBarrierLevel();
    ArgumentChecker.isTrue(!(barrier.getBarrierType() == BarrierType.DOWN && spot < barrier.getBarrierLevel()), "The Data is not consistent with an alive barrier (DOWN and spot<barrier).");
    ArgumentChecker.isTrue(!(barrier.getBarrierType() == BarrierType.UP && spot > barrier.getBarrierLevel()), "The Data is not consistent with an alive barrier (UP and spot>barrier).");
    final boolean isCall = option.isCall();
    final double t = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final int phi = isCall ? 1 : -1;
    final double eta = isDown ? 1 : -1;
    final double df1 = Math.exp(t * (costOfCarry - rate));
    final double df2 = Math.exp(-rate * t);
    if (CompareUtils.closeEquals(sigma, 0, 1e-16)) {
      return df1 * rebate;
    }
    final double sigmaSq = sigma * sigma;
    final double sigmaT = sigma * Math.sqrt(t);
    final double mu = (costOfCarry - 0.5 * sigmaSq) / sigmaSq;
    final double lambda = Math.sqrt(mu * mu + 2 * rate / sigmaSq);
    final double m1 = sigmaT * (1 + mu);
    final double x1 = Math.log(spot / strike) / sigmaT + m1;
    final double x2 = Math.log(spot / h) / sigmaT + m1;
    final double y1 = Math.log(h * h / spot / strike) / sigmaT + m1;
    final double y2 = Math.log(h / spot) / sigmaT + m1;
    final double z = Math.log(h / spot) / sigmaT + lambda * sigmaT;
    final double xA = getA(spot, strike, df1, df2, x1, sigmaT, phi);
    final double xB = getA(spot, strike, df1, df2, x2, sigmaT, phi);
    final double xC = getC(spot, strike, df1, df2, y1, sigmaT, h, mu, phi, eta);
    final double xD = getC(spot, strike, df1, df2, y2, sigmaT, h, mu, phi, eta);
    final double xE = isKnockIn ? getE(spot, rebate, df2, x2, y2, sigmaT, h, mu, eta) : getF(spot, rebate, z, sigmaT, h, mu, lambda, eta);
    if (isKnockIn) {
      if (isDown) {
        if (isCall) {
          return strike > h ? xC + xE : xA - xB + xD + xE;
        }
        return strike > h ? xB - xC + xD + xE : xA + xE;
      }
      if (isCall) {
        return strike > h ? xA + xE : xB - xC + xD + xE;
      }
      return strike > h ? xA - xB + xD + xE : xC + xE;
    } 
    // KnockOut
    if (isDown) {
      if (isCall) {
        return strike > h ? xA - xC + xE : xB - xD + xE;
      }
      return strike > h ? xA - xB + xC - xD + xE : xE;
    }
    if (isCall) {
      return strike > h ? xE : xA - xB + xC - xD + xE;
    }
    return strike > h ? xB - xD + xE : xA - xC + xE;
  }

  /**
   * Computes the price of a barrier option in the Black world.
   * @param option The underlying European vanilla option.
   * @param barrier The barrier.
   * @param rebate The rebate.
   * @param spot The spot price.
   * @param costOfCarry The cost of carry.
   * @param rate The interest rate.
   * @param sigma The Black volatility.
   * @param derivatives Array used to return the derivatives. Will be changed during the call. The derivatives are [0] spot, [1] strike, [2] rate, [3] cost-of-carry, [4] volatility.
   * @return The price.
   */
  public double getPriceAdjoint(final EuropeanVanillaOption option, final Barrier barrier, final double rebate, final double spot, final double costOfCarry, final double rate, final double sigma,
      final double[] derivatives) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(barrier, "barrier");
    for (int loopder = 0; loopder < 5; loopder++) { // To clean the array.
      derivatives[loopder] = 0.0;
    }
    final boolean isKnockIn = (barrier.getKnockType() == KnockType.IN);
    final boolean isDown = (barrier.getBarrierType() == BarrierType.DOWN);
    final double h = barrier.getBarrierLevel();
    ArgumentChecker.isTrue(!(barrier.getBarrierType() == BarrierType.DOWN && spot < barrier.getBarrierLevel()), "The Data is not consistent with an alive barrier (DOWN and spot<barrier).");
    ArgumentChecker.isTrue(!(barrier.getBarrierType() == BarrierType.UP && spot > barrier.getBarrierLevel()), "The Data is not consistent with an alive barrier (UP and spot>barrier).");
    final boolean isCall = option.isCall();
    final double t = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final int phi = isCall ? 1 : -1;
    final double eta = isDown ? 1 : -1;
    final double df1 = Math.exp(t * (costOfCarry - rate));
    final double df2 = Math.exp(-rate * t);
    if (CompareUtils.closeEquals(sigma, 0, 1e-16)) {
      final double priceBar = 1.0;
      final double df1Bar = rebate * priceBar;
      derivatives[2] = -t * df1 * df1Bar;
      derivatives[3] = t * df1 * df1Bar;
      return df1 * rebate;
    }
    final double sigmaSq = sigma * sigma;
    final double sigmaT = sigma * Math.sqrt(t);
    final double mu = (costOfCarry - 0.5 * sigmaSq) / sigmaSq;
    final double lambda = Math.sqrt(mu * mu + 2 * rate / sigmaSq);
    final double m1 = sigmaT * (1 + mu);
    final double x1 = Math.log(spot / strike) / sigmaT + m1;
    final double x2 = Math.log(spot / h) / sigmaT + m1;
    final double y1 = Math.log(h * h / spot / strike) / sigmaT + m1;
    final double y2 = Math.log(h / spot) / sigmaT + m1;
    final double z = Math.log(h / spot) / sigmaT + lambda * sigmaT;
    final double[] aDerivatives = new double[6];
    final double xA = getAAdjoint(spot, strike, df1, df2, x1, sigmaT, phi, aDerivatives);
    final double[] bDerivatives = new double[6];
    final double xB = getAAdjoint(spot, strike, df1, df2, x2, sigmaT, phi, bDerivatives);
    final double[] cDerivatives = new double[7];
    final double xC = getCAdjoint(spot, strike, df1, df2, y1, sigmaT, h, mu, phi, eta, cDerivatives);
    final double[] dDerivatives = new double[7];
    final double xD = getCAdjoint(spot, strike, df1, df2, y2, sigmaT, h, mu, phi, eta, dDerivatives);
    final double[] fDerivatives = new double[5];
    final double[] eDerivatives = new double[6];
    final double xE = isKnockIn ? getEAdjoint(spot, rebate, df2, x2, y2, sigmaT, h, mu, eta, eDerivatives) : getFAdjoint(spot, rebate, z, sigmaT, h, mu, lambda, eta, fDerivatives);
    double xEBar = 0.0;
    double xDBar = 0.0;
    double xCBar = 0.0;
    double xBBar = 0.0;
    double xABar = 0.0;
    double price;
    if (isKnockIn) { // IN start
      if (isDown) { // DOWN start
        if (isCall) { // Call start
          if (strike > h) {
            xCBar = 1.0;
            xEBar = 1.0;
            price = xC + xE;
          } else {
            xABar = 1.0;
            xBBar = -1.0;
            xDBar = 1.0;
            xEBar = 1.0;
            price = xA - xB + xD + xE;
          }
        } else { // Put start
          if (strike > h) {
            xBBar = 1.0;
            xCBar = -1.0;
            xDBar = 1.0;
            xEBar = 1.0;
            price = xB - xC + xD + xE;
          } else {
            xABar = 1.0;
            xEBar = -1.0;
            price = xA + xE;
          }
        } // DOWN end
      } else { // UP start
        if (isCall) {
          if (strike > h) {
            xABar = 1.0;
            xEBar = -1.0;
            price = xA + xE;
          } else {
            xBBar = 1.0;
            xCBar = -1.0;
            xDBar = 1.0;
            xEBar = 1.0;
            price = xB - xC + xD + xE;
          }
        } else {
          if (strike > h) {
            xABar = 1.0;
            xBBar = -1.0;
            xDBar = 1.0;
            xEBar = 1.0;
            price = xA - xB + xD + xE;
          } else {
            xCBar = 1.0;
            xEBar = 1.0;
            price = xC + xE;
          }
        } // UP end
      } // IN end
    } else { // OUT start
      if (isDown) { // DOWN start
        if (isCall) { // CALL start
          if (strike > h) {
            xABar = 1.0;
            xCBar = -1.0;
            xEBar = 1.0;
            price = xA - xC + xE;
          } else {
            xBBar = 1.0;
            xDBar = -1.0;
            xEBar = 1.0;
            price = xB - xD + xE;
          }
        } else { // PUT start
          if (strike > h) {
            xABar = 1.0;
            xBBar = -1.0;
            xCBar = 1.0;
            xDBar = -1.0;
            xEBar = 1.0;
            price = xA - xB + xC - xD + xE;
          } else {
            xEBar = 1.0;
            price = xE;
          } // PUT end
        } // DOWN end
      } else { // UP start
        if (isCall) {
          if (strike > h) {
            xEBar = 1.0;
            price = xE;
          } else {
            xABar = 1.0;
            xBBar = -1.0;
            xCBar = 1.0;
            xDBar = -1.0;
            xEBar = 1.0;
            price = xA - xB + xC - xD + xE;
          }
        } else {
          if (strike > h) {
            xBBar = 1.0;
            xDBar = -1.0;
            xEBar = 1.0;
            price = xB - xD + xE;
          } else {
            xABar = 1.0;
            xCBar = -1.0;
            xEBar = 1.0;
            price = xA - xC + xE;
          } // PUT end
        } // UP end
      } // OUT end
    }
    // Backward sweep (first step in the forward sweep)
    double zBar = 0.0;
    double y2Bar = 0.0;
    double x2Bar = 0.0;
    double lambdaBar = 0.0;
    double muBar = 0.0;
    double sigmaTBar = 0.0;
    double df2Bar = 0.0;
    if (isKnockIn) {
      y2Bar = eDerivatives[3] * xEBar;
      x2Bar = eDerivatives[2] * xEBar;
      muBar = eDerivatives[5] * xEBar;
      sigmaTBar = eDerivatives[4] * xEBar;
      df2Bar = eDerivatives[1] * xEBar;
      derivatives[0] = eDerivatives[0] * xEBar;
    } else {
      zBar = fDerivatives[1] * xEBar;
      lambdaBar = fDerivatives[4] * xEBar; // only F has lambda dependence, which in turn is a function of mu, see muBar+= below
      muBar = fDerivatives[3] * xEBar;
      sigmaTBar = fDerivatives[2] * xEBar;
      derivatives[0] = fDerivatives[0] * xEBar;
    }
    y2Bar += dDerivatives[4] * xDBar;
    final double y1Bar = cDerivatives[4] * xCBar;
    x2Bar += bDerivatives[4] * xBBar;
    final double x1Bar = aDerivatives[4] * xABar;
    final double m1Bar = x1Bar + x2Bar + y1Bar + y2Bar;
    muBar += cDerivatives[6] * xCBar + dDerivatives[6] * xDBar + sigmaT * m1Bar + mu / lambda * lambdaBar;
    sigmaTBar += aDerivatives[5] * xABar // dA/dsigT - it does not include x1's dependence on sigmaT. This is below in x1Bar 
        + bDerivatives[5] * xBBar // Same as above - A and B share form/function
        + cDerivatives[5] * xCBar // C additionally has mu dependence on sigma. This is captured in muBar
        + dDerivatives[5] * xDBar
              + (lambda - Math.log(h / spot) / (sigmaT * sigmaT)) * zBar
              - Math.log(h / spot) / (sigmaT * sigmaT) * y2Bar
              - Math.log(h * h / spot / strike) / (sigmaT * sigmaT) * y1Bar
              - Math.log(spot / h) / (sigmaT * sigmaT) * x2Bar
              - Math.log(spot / strike) / (sigmaT * sigmaT) * x1Bar
              + (1 + mu) * m1Bar;
    final double sigmaSqBar = -costOfCarry / (sigmaSq * sigmaSq) * muBar - rate / (sigmaSq * sigmaSq) / lambda * lambdaBar;
    df2Bar += aDerivatives[3] * xABar + bDerivatives[3] * xBBar + cDerivatives[3] * xCBar + dDerivatives[3] * xDBar;
    final double df1Bar = aDerivatives[2] * xABar + bDerivatives[2] * xBBar + cDerivatives[2] * xCBar + dDerivatives[2] * xDBar;
    derivatives[0] += aDerivatives[0] * xABar + bDerivatives[0] * xBBar + cDerivatives[0] * xCBar + dDerivatives[0] * xDBar + 1.0 / spot / sigmaT * x1Bar + 1.0 / spot / sigmaT * x2Bar + -1.0 / spot
        / sigmaT * y1Bar + -1.0 / spot / sigmaT * y2Bar - 1.0 / spot / sigmaT * zBar;
    derivatives[1] = aDerivatives[1] * xABar + bDerivatives[1] * xBBar + cDerivatives[1] * xCBar + dDerivatives[1] * xDBar + -1.0 / strike / sigmaT * x1Bar - 1 / strike / sigmaT * y1Bar;
    derivatives[2] = -t * df1 * df1Bar - t * df2 * df2Bar + 1.0 / lambda / sigmaSq * lambdaBar;
    derivatives[3] = t * df1 * df1Bar + 1.0 / sigmaSq * muBar;
    derivatives[4] = 2 * sigma * sigmaSqBar + Math.sqrt(t) * sigmaTBar;
    return price;
  }

  private double getA(final double s, final double k, final double df1, final double df2, final double x, final double sigmaT, final double phi) {
    return phi * (s * df1 * NORMAL.getCDF(phi * x) - k * df2 * NORMAL.getCDF(phi * (x - sigmaT)));
  }

  /**
   * The adjoint version of the quantity A computation.
   * @param s The parameter s.
   * @param k The parameter k.
   * @param df1 The parameter df1.
   * @param df2 The parameter df2.
   * @param x The parameter x.
   * @param sigmaT The parameter sigmaT.
   * @param phi The parameter phi.
   * @param derivatives Array used to return the derivatives. Will be changed during the call. The derivatives are [0] s, [1] k, [2] df1, [3] df2, [4] x, [5] sigmaT.
   * @return The quantity A.
   */
  private double getAAdjoint(final double s, final double k, final double df1, final double df2, final double x, final double sigmaT, final double phi, final double[] derivatives) {
    //  Forward sweep
    final double n1 = NORMAL.getCDF(phi * x);
    final double n2 = NORMAL.getCDF(phi * (x - sigmaT));
    final double a = phi * (s * df1 * n1 - k * df2 * n2);
    // Backward sweep
    final double aBar = 1.0;
    final double n2Bar = phi * -k * df2 * aBar;
    final double n1Bar = phi * s * df1 * aBar;
    derivatives[0] = phi * df1 * n1 * aBar;
    derivatives[1] = phi * -df2 * n2 * aBar;
    derivatives[2] = phi * s * n1 * aBar;
    derivatives[3] = phi * -k * n2 * aBar;
    final double n1df = NORMAL.getPDF(phi * x);
    final double n2df = NORMAL.getPDF(phi * (x - sigmaT));
    derivatives[4] = n1df * phi * n1Bar + n2df * phi * n2Bar;
    derivatives[5] = n2df * -phi * n2Bar;
    return a;
  }

  /**
   * 
   * @param s The spot
   * @param k The strike
   * @param df1 The first discount factor
   * @param df2 The second discount factor
   * @param y y
   * @param sigmaT Volatility multiplied by time
   * @param h h
   * @param mu mu
   * @param phi phi
   * @param eta eta
   * @return C
   */
  private double getC(final double s, final double k, final double df1, final double df2, final double y, final double sigmaT, final double h, final double mu, final double phi, final double eta) {
    return phi * (s * df1 * Math.pow(h / s, 2 * (mu + 1)) * NORMAL.getCDF(eta * y) - k * df2 * Math.pow(h / s, 2 * mu) * NORMAL.getCDF(eta * (y - sigmaT)));
  }

  /**
   * The adjoint version of the quantity C computation.
   * @param s
   * @param k
   * @param df1
   * @param df2
   * @param y
   * @param sigmaT
   * @param h
   * @param mu
   * @param phi
   * @param eta
   * @param derivatives Array used to return the derivatives. Will be changed during the call. The derivatives are [0] s, [1] k, [2] df1, [3] df2, [4] y, [5] sigmaT, [6] mu.
   * @return C and its adjoints
   */
  private double getCAdjoint(final double s, final double k, final double df1, final double df2, final double y, final double sigmaT, final double h, final double mu, final double phi,
      final double eta, final double[] derivatives) {
    //  Forward sweep
    final double n1 = NORMAL.getCDF(eta * y);
    final double n2 = NORMAL.getCDF(eta * (y - sigmaT));
    final double hsMu1 = Math.pow(h / s, 2 * (mu + 1));
    final double hsMu = Math.pow(h / s, 2 * mu);
    final double c = phi * (s * df1 * hsMu1 * n1 - k * df2 * hsMu * n2);
    // Backward sweep
    final double n1df = NORMAL.getPDF(eta * y);
    final double n2df = NORMAL.getPDF(eta * (y - sigmaT));
    final double cBar = 1.0;
    final double hsMuBar = phi * -k * df2 * n2 * cBar;
    final double hsMu1Bar = phi * s * df1 * n1 * cBar;
    final double n2Bar = phi * -k * df2 * hsMu * cBar;
    final double n1Bar = phi * s * df1 * hsMu1 * cBar;
    derivatives[0] = phi * df1 * hsMu1 * n1 * cBar - 2 * mu * hsMu / s * hsMuBar - 2 * (mu + 1) * hsMu1 / s * hsMu1Bar; // s
    derivatives[1] = phi * -df2 * hsMu * n2 * cBar; // k
    derivatives[2] = phi * s * hsMu1 * n1 * cBar; // df1
    derivatives[3] = phi * -k * hsMu * n2 * cBar; // df2
    derivatives[4] = n1df * eta * n1Bar + n2df * eta * n2Bar; // y
    derivatives[5] = -n2df * eta * n2Bar; // sigmaT
    derivatives[6] = 2 * Math.log(h / s) * hsMu * hsMuBar + 2 * Math.log(h / s) * hsMu1 * hsMu1Bar; // mu
    return c;
  }

  /**
   * 
   * @param s
   * @param rebate
   * @param df2
   * @param x
   * @param y
   * @param sigmaT
   * @param h
   * @param mu
   * @param eta
   * @return E
   */
  private double getE(final double s, final double rebate, final double df2, final double x, final double y, final double sigmaT, final double h, final double mu, final double eta) {
    return rebate * df2 * (NORMAL.getCDF(eta * (x - sigmaT)) - Math.pow(h / s, 2 * mu) * NORMAL.getCDF(eta * (y - sigmaT)));
  }

  /**
   * The adjoint version of the quantity E computation.
   * @param s
   * @param rebate
   * @param df2
   * @param x
   * @param y
   * @param sigmaT
   * @param h
   * @param mu
   * @param eta
   * @param derivatives Array used to return the derivatives. Will be changed during the call. The derivatives are [0] s, [1] df2, [2] x, [3] y, [4] sigmaT, [5] mu.
   * @return E and its adjoints
   */
  private double getEAdjoint(final double s, final double rebate, final double df2, final double x, final double y, final double sigmaT, final double h, final double mu, final double eta,
      final double[] derivatives) {
    //  Forward sweep
    final double n1 = NORMAL.getCDF(eta * (x - sigmaT));
    final double n2 = NORMAL.getCDF(eta * (y - sigmaT));
    final double hsMu = Math.pow(h / s, 2 * mu);
    final double e = rebate * df2 * (n1 - hsMu * n2);
    // Backward sweep
    final double eBar = 1.0;
    final double n1df = NORMAL.getPDF(eta * (x - sigmaT));
    final double n2df = NORMAL.getPDF(eta * (y - sigmaT));
    final double hsMuBar = rebate * df2 * -n2 * eBar;
    final double n2Bar = rebate * df2 * -hsMu * eBar;
    final double n1Bar = rebate * df2 * eBar;
    derivatives[0] = -2 * mu * hsMu / s * hsMuBar; // s
    derivatives[1] = rebate * (n1 - hsMu * n2) * eBar; // df2;
    derivatives[2] = n1df * eta * n1Bar; // x
    derivatives[3] = n2df * eta * n2Bar; // y
    derivatives[4] = n2df * -eta * n2Bar + n1df * -eta * n1Bar; // sigmaT
    derivatives[5] = 2 * Math.log(h / s) * hsMu * hsMuBar; // mu
    return e;
  }

  private double getF(final double s, final double rebate, final double z, final double sigmaT, final double h, final double mu, final double lambda, final double eta) {
    return rebate * (Math.pow(h / s, mu + lambda) * NORMAL.getCDF(eta * z) + Math.pow(h / s, mu - lambda) * NORMAL.getCDF(eta * (z - 2 * lambda * sigmaT)));
  }

  /**
   * The adjoint version of the quantity F computation.
   * @param s
   * @param rebate
   * @param z
   * @param sigmaT
   * @param h
   * @param mu
   * @param lambda
   * @param eta
   * @param derivatives Array used to return the derivatives. Will be changed during the call. The derivatives are [0] s, [1] z, [2] sigmaT, [3] mu, [4] lambda.
   * @return F and its adjoints
   */
  private double getFAdjoint(final double s, final double rebate, final double z, final double sigmaT, final double h, final double mu, final double lambda, final double eta,
      final double[] derivatives) {
    //  Forward sweep
    final double n1 = NORMAL.getCDF(eta * z);
    final double n2 = NORMAL.getCDF(eta * (z - 2 * lambda * sigmaT));
    final double hsMuPLa = Math.pow(h / s, mu + lambda);
    final double hsMuMLa = Math.pow(h / s, mu - lambda);
    final double f = rebate * (hsMuPLa * n1 + hsMuMLa * n2);
    // Backward sweep
    final double fBar = 1.0;
    final double n1df = NORMAL.getPDF(eta * z);
    final double n2df = NORMAL.getPDF(eta * (z - 2 * lambda * sigmaT));
    final double hsMuPLaBar = rebate * n1 * fBar;
    final double hsMuMLaBar = rebate * n2 * fBar;
    final double n2Bar = rebate * hsMuMLa * fBar;
    final double n1Bar = rebate * hsMuPLa * fBar;
    derivatives[0] = -(mu + lambda) * hsMuPLa / s * hsMuPLaBar - (mu - lambda) * hsMuMLa / s * hsMuMLaBar; //s
    derivatives[1] = n1df * eta * n1Bar + n2df * eta * n2Bar; // z
    derivatives[2] = -n2df * eta * 2 * lambda * n2Bar; //sigmaT
    derivatives[3] = hsMuPLa * Math.log(h / s) * hsMuPLaBar + hsMuMLa * Math.log(h / s) * hsMuMLaBar; // mu
    derivatives[4] = hsMuPLa * Math.log(h / s) * hsMuPLaBar - hsMuMLa * Math.log(h / s) * hsMuMLaBar; // lambda
    return f;
  }

  //TODO: get derivative (adjoint) with respect to Spot, strike, rate, coc, volatility. (rebate?, barrier level?)

}

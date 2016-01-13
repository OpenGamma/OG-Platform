/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import com.opengamma.analytics.math.differentiation.ValueDerivatives;
import com.opengamma.util.ArgumentChecker;

/**
 * Surface Stochastic Volatility Inspired (SSVI) formula.
 * <p>
 * Reference: Gatheral, Jim and Jacquier, Antoine. Arbitrage-free SVI volatility surfaces. arXiv:1204.0646v4, 2013. Section 4.
 */
public class SSVIVolatilityFunction {
  
  /**
   * Computes the volatility in the SSVI formula.
   * <p>
   * The forward and the strike must be strictly positive. The time to expiration must be strictly positive.
   * 
   * @param forward  the forward (rate or price)
   * @param strike  the option strike
   * @param timeToExpiry  the option time to expiration
   * @param volatilityAtm  the at-the-money volatility
   * @param rho  the correlation parameter
   * @param eta  the eta parameter
   * @return the volatility
   */
  public static double volatility(double forward, double strike, double timeToExpiry,
      double volatilityAtm, double rho, double eta) {
    ArgumentChecker.isTrue(strike > 0, "strike must be strctly positive");
    ArgumentChecker.isTrue(forward > 0, "strike must be strctly positive");
    ArgumentChecker.isTrue(timeToExpiry > 0, "time to expiration must be strctly positive");
    double theta = volatilityAtm * volatilityAtm * timeToExpiry;
    double phi = eta * Math.sqrt(theta);
    double k = Math.log(strike / forward);
    double w = 0.5 * theta * (1.0d + rho * phi * k + Math.sqrt(1.0d + 2 * rho * phi * k + phi * k * phi * k)); 
    return Math.sqrt(w / timeToExpiry);
  }  

  /**
   * Computes the adjoint of the volatility function in the SSVI formula.
   * <p>
   * The derivatives in the output are in the order of the inputs.
   * 
   * @param forward  the forward (rate or price)
   * @param strike  the option strike
   * @param timeToExpiry  the option time to expiration
   * @param volatilityAtm  the at-the-money volatility
   * @param rho  the correlation parameter
   * @param eta  the eta parameter
   * @return the volatility and its derivatives
   */
  public static ValueDerivatives volatilityAdjoint(double forward, double strike, double timeToExpiry,
      double volatilityAtm, double rho, double eta) {
    ArgumentChecker.isTrue(strike > 0, "strike must be strctly positive");
    ArgumentChecker.isTrue(forward > 0, "strike must be strctly positive");
    ArgumentChecker.isTrue(timeToExpiry > 0, "time to expiration must be strctly positive");
    double theta = volatilityAtm * volatilityAtm * timeToExpiry;
    double stheta = Math.sqrt(theta);
    double phi = eta * stheta;
    double k = Math.log(strike / forward);
    double s = Math.sqrt(1.0d + 2 * rho * phi * k + phi * k * phi * k);
    double w = 0.5 * theta * (1.0d + rho * phi * k + s);
    double volatility = Math.sqrt(w / timeToExpiry);
    // Backward sweep.
    double[] derivatives = new double[7]; // 6 inputs + phi
    double volatilityBar = 1.0; // OK
    double wBar = 0.5 * volatility / w * volatilityBar; // OK
    derivatives[2] += -0.5 * volatility / timeToExpiry * volatilityBar;
    double thetaBar = w / theta * wBar;
    derivatives[4] += 0.5 * theta * phi * k * wBar; // OK
    double phiBar = 0.5 * theta * rho * k * wBar;
    double kBar = 0.5 * theta * rho * phi * wBar; // OK
    double sBar = 0.5 * theta * wBar; // OK
    derivatives[4] += phi * k / s * sBar; // OK
    phiBar += (rho * k + phi * k * k) / s * sBar;
    kBar += (rho * phi + phi * phi * k) / s * sBar;
    derivatives[1] += 1.0d / strike * kBar; // OK
    derivatives[0] += -1.0d / forward * kBar; // OK
    derivatives[5] += stheta * phiBar;
    double sthetaBar = eta * phiBar;
    thetaBar += 0.5 / stheta * sthetaBar;
    derivatives[3] += 2 * volatilityAtm * timeToExpiry * thetaBar;
    derivatives[2] += volatilityAtm * volatilityAtm * thetaBar;
    return new ValueDerivatives(volatility, derivatives);
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;

/**
 * The price function to compute the price of barrier option in the Black world.
 */
public class BlackBarrierPriceFunction {

  /**
   * The normal distribution implementation used in the pricing.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the price of a barrier option in the Black world.
   * @param option The underlying European vanilla option.
   * @param barrier The barrier.
   * @param rebate The rebate.
   * @param spot The spot price.
   * @param costOfCarry The cost of carry.
   * @param rate The interest rate.
   * @param sigma The Black volatility.
   * @return The price.
   */
  public double getPrice(final EuropeanVanillaOption option, final Barrier barrier, final double rebate, final double spot, final double costOfCarry, final double rate, final double sigma) {
    Validate.notNull(option, "option");
    Validate.notNull(barrier, "barrier");
    final boolean isKnockIn = (barrier.getKnockType() == KnockType.IN);
    final boolean isDown = (barrier.getBarrierType() == BarrierType.DOWN);
    final double h = barrier.getBarrierLevel();
    Validate.isTrue(!(barrier.getBarrierType() == BarrierType.DOWN && spot < barrier.getBarrierLevel()), "The Data is not consistent with an alive barrier (DOWN and spot<barrier).");
    Validate.isTrue(!(barrier.getBarrierType() == BarrierType.UP && spot > barrier.getBarrierLevel()), "The Data is not consistent with an alive barrier (UP and spot>barrier).");
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

  private double getA(final double s, final double k, final double df1, final double df2, final double x, final double sigmaT, final double phi) {
    return phi * (s * df1 * NORMAL.getCDF(phi * x) - k * df2 * NORMAL.getCDF(phi * (x - sigmaT)));
  }

  private double getC(final double s, final double k, final double df1, final double df2, final double y, final double sigmaT, final double h, final double mu, final double phi, final double eta) {
    return phi * (s * df1 * Math.pow(h / s, 2 * (mu + 1)) * NORMAL.getCDF(eta * y) - k * df2 * Math.pow(h / s, 2 * mu) * NORMAL.getCDF(eta * (y - sigmaT)));
  }

  private double getE(final double s, final double rebate, final double df2, final double x, final double y, final double sigmaT, final double h, final double mu, final double eta) {
    return rebate * df2 * (NORMAL.getCDF(eta * (x - sigmaT)) - Math.pow(h / s, 2 * mu) * NORMAL.getCDF(eta * (y - sigmaT)));
  }

  private double getF(final double s, final double rebate, final double z, final double sigmaT, final double h, final double mu, final double lambda, final double eta) {
    return rebate * (Math.pow(h / s, mu + lambda) * NORMAL.getCDF(eta * z) + Math.pow(h / s, mu - lambda) * NORMAL.getCDF(eta * (z - 2 * lambda * sigmaT)));
  }

}

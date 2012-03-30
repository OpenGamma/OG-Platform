/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.EuropeanStandardBarrierOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class EuropeanStandardBarrierOptionModel extends AnalyticOptionModel<EuropeanStandardBarrierOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final EuropeanStandardBarrierOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    final Barrier barrier = definition.getBarrier();
    final boolean isKnockIn = barrier.getKnockType() == KnockType.IN;
    final boolean isDown = barrier.getBarrierType() == BarrierType.DOWN;
    final double h = barrier.getBarrierLevel();
    final int phi = definition.isCall() ? 1 : -1;
    final double eta = isDown ? 1 : -1;
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data, "data");
        final boolean isCall = definition.isCall();
        final double s = data.getSpot();
        final double b = data.getCostOfCarry();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double rebate = definition.getRebate();
        final double k = definition.getStrike();
        final double r = data.getInterestRate(t);
        final double sigma = data.getVolatility(t, h); //REVIEW emcleod 19-7-10 will only work if volatility is constant
        final double df1 = Math.exp(t * (b - r));
        final double df2 = Math.exp(-r * t);
        if (CompareUtils.closeEquals(sigma, 0, 1e-16)) {
          return df1 * definition.getPayoffFunction().getPayoff(data, null);
        }
        final double sigmaSq = sigma * sigma;
        final double sigmaT = sigma * Math.sqrt(t);
        final double mu = (b - 0.5 * sigmaSq) / sigmaSq;
        final double lambda = Math.sqrt(mu * mu + 2 * r / sigmaSq);
        final double m1 = sigmaT * (1 + mu);
        final double x1 = Math.log(s / k) / sigmaT + m1;
        final double x2 = Math.log(s / h) / sigmaT + m1;
        final double y1 = Math.log(h * h / s / k) / sigmaT + m1;
        final double y2 = Math.log(h / s) / sigmaT + m1;
        final double z = Math.log(h / s) / sigmaT + lambda * sigmaT;
        final double xA = getA(s, k, df1, df2, x1, sigmaT, phi);
        final double xB = getA(s, k, df1, df2, x2, sigmaT, phi);
        final double xC = getC(s, k, df1, df2, y1, sigmaT, h, mu, phi, eta);
        final double xD = getC(s, k, df1, df2, y2, sigmaT, h, mu, phi, eta);
        final double xE = isKnockIn ? getE(s, rebate, df2, x2, y2, sigmaT, h, mu, eta) : getF(s, rebate, z, sigmaT, h, mu, lambda, eta);
        if (isKnockIn) {
          if (isDown) {
            if (isCall) {
              return k > h ? xC + xE : xA - xB + xD + xE;
            }
            return k > h ? xB - xC + xD + xE : xA + xE;
          }
          if (isCall) {
            return k > h ? xA + xE : xB - xC + xD + xE;
          }
          return k > h ? xA - xB + xD + xE : xC + xE;
        }
        if (isDown) {
          if (isCall) {
            return k > h ? xA - xC + xE : xB - xD + xE;
          }
          return k > h ? xA - xB + xC - xD + xE : xE;
        }
        if (isCall) {
          return k > h ? xE : xA - xB + xC - xD + xE;
        }
        return k > h ? xB - xD + xE : xA - xC + xE;
      }

    };
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

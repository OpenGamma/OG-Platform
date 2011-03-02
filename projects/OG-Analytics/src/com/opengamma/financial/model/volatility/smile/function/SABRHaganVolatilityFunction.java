/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SABRHaganVolatilityFunction implements VolatilityFunctionProvider<SABRFormulaData> {
  private static final double EPS = 1e-15;

  @Override
  public Function1D<SABRFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getK();
    final double t = option.getT();
    return new Function1D<SABRFormulaData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public final Double evaluate(final SABRFormulaData data) {
        Validate.notNull(data, "data");
        final double alpha = data.getAlpha();
        final double beta = data.getBeta();
        final double rho = data.getRho();
        final double nu = data.getNu();
        final double f = data.getForward();
        double vol, z, zOverChi;
        final double beta1 = 1 - beta;
        if (CompareUtils.closeEquals(f, k, EPS)) {
          final double f1 = Math.pow(f, beta1);
          vol = alpha * (1 + t * (beta1 * beta1 * alpha * alpha / 24 / f1 / f1 + rho * alpha * beta * nu / 4 / f1 + nu * nu * (2 - 3 * rho * rho) / 24)) / f1;
        } else {
          if (CompareUtils.closeEquals(beta, 0, EPS)) {
            final double ln = Math.log(f / k);
            z = nu * Math.sqrt(f * k) * ln / alpha;
            zOverChi = getZOverChi(rho, z);
            vol = alpha * ln * zOverChi * (1 + t * (alpha * alpha / f / k + nu * nu * (2 - 3 * rho * rho)) / 24) / (f - k);
          } else if (CompareUtils.closeEquals(beta, 1, EPS)) {
            final double ln = Math.log(f / k);
            z = nu * ln / alpha;
            zOverChi = getZOverChi(rho, z);
            vol = alpha * zOverChi * (1 + t * (rho * alpha * nu / 4 + nu * nu * (2 - 3 * rho * rho) / 24));
          } else {
            final double ln = Math.log(f / k);
            final double f1 = Math.pow(f * k, beta1);
            final double f1Sqrt = Math.sqrt(f1);
            final double lnBetaSq = Math.pow(beta1 * ln, 2);
            z = nu * f1Sqrt * ln / alpha;
            zOverChi = getZOverChi(rho, z);
            final double first = alpha / (f1Sqrt * (1 + lnBetaSq / 24 + lnBetaSq * lnBetaSq / 1920));
            final double second = zOverChi;
            final double third = 1 + t * (beta1 * beta1 * alpha * alpha / 24 / f1 + rho * nu * beta * alpha / 4 / f1Sqrt + nu * nu * (2 - 3 * rho * rho) / 24);
            vol = first * second * third;
          }
        }

        return vol;
      }
    };
  }

  private double getZOverChi(final double rho, final double z) {
    if (CompareUtils.closeEquals(z, 0.0, EPS)) {
      return 1.0;
    }

    final double rhoStar = 1 - rho;
    if (CompareUtils.closeEquals(rhoStar, 0.0, 1e-8)) {
      if (z >= 1.0) {
        return 0.0;
      }
      return -z / Math.log(1 - z);
    }
    final double chi = Math.log((Math.sqrt(1 - 2 * rho * z + z * z) + z - rho)) - Math.log(rhoStar);
    return z / chi;
  }
}

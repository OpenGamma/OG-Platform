/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * This is the form given in Obloj, Fine-Tune Your Smile (2008), and supposedly corresponds to that given in Hagan, Managing Smile Risk (2002). However it differs from Hagan
 * {@link SABRBerestyckiVolatilityFunction}
 */
public class SABRHaganAlternativeVolatilityFunction extends VolatilityFunctionProvider<SABRFormulaData> {
  private static final int NUM_PARAMETERS = 4;
  private static final Logger s_logger = LoggerFactory.getLogger(SABRHaganAlternativeVolatilityFunction.class);

  private static final double CUTOFF_MONEYNESS = 1e-6;

  private static final double EPS = 1e-15;

  @Override
  public Function1D<SABRFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    final double strike = option.getStrike();
    final double t = option.getTimeToExpiry();
    return new Function1D<SABRFormulaData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public final Double evaluate(final SABRFormulaData data) {
        Validate.notNull(data, "data");
        final double alpha = data.getAlpha();
        final double beta = data.getBeta();
        final double rho = data.getRho();
        final double nu = data.getNu();

        final double cutoff = forward * CUTOFF_MONEYNESS;
        final double k;
        if (strike < cutoff) {
          s_logger.info("Given strike of " + strike + " is less than cutoff at " + cutoff + ", therefore the strike is taken as " + cutoff);
          k = cutoff;
        } else {
          k = strike;
        }

        double i0;
        final double beta1 = 1 - beta;
        if (CompareUtils.closeEquals(forward, k, EPS)) {
          i0 = alpha / Math.pow(k, beta1);
        } else {
          final double x = Math.log(forward / k);
          if (CompareUtils.closeEquals(nu, 0, EPS)) {
            if (CompareUtils.closeEquals(beta, 1.0, EPS)) {
              return alpha; // this is just log-normal
            }
            i0 = x * alpha * beta1 / (Math.pow(forward, beta1) - Math.pow(k, beta1));
          } else {
            double z, zeta;
            if (beta == 1.0) {
              z = nu * x / alpha;
              zeta = z;
            } else {
              z = nu * (Math.pow(forward, beta1) - Math.pow(k, beta1)) / alpha / beta1;
              zeta = nu * (forward - k) / alpha / Math.pow(forward * k, beta / 2);
            }
            final double temp = (Math.sqrt(1 + zeta * (zeta - 2 * rho)) + zeta - rho) / (1 - rho);
            i0 = nu * x * zeta / z / Math.log(temp);
          }
        }
        final double f1sqrt = Math.pow(forward * k, beta1 / 2);
        final double i1 = beta1 * beta1 * alpha * alpha / 24 / f1sqrt / f1sqrt + rho * alpha * beta * nu / 4 / f1sqrt + nu * nu * (2 - 3 * rho * rho) / 24;
        return i0 * (1 + i1 * t);
      }
    };
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
    return "SABR (Hagan alternative)";
  }

  @Override
  public int getNumberOfParameters() {
    return NUM_PARAMETERS;
  }

  @Override
  public SABRFormulaData toModelData(final double[] parameters) {
    return new SABRFormulaData(parameters);
  }

}

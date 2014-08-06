/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 *  This is the form given in Obloj (2008), "<i>Fine-Tune Your Smile</i>", and supposedly corresponds to that given in Berestycki (2004),
 *  "<i>Computing the implied volatility in stochastic volatility models</i>". However, appears to be the same as Hagan's.
 */
public class SABRBerestyckiVolatilityFunction extends VolatilityFunctionProvider<SABRFormulaData> {
  private static final int NUM_PARAMETERS = 4;
  private static final Logger s_logger = LoggerFactory.getLogger(SABRBerestyckiVolatilityFunction.class);

  private static final double CUTOFF_MONEYNESS = 1e-6;
  private static final double EPS = 1e-15;

  @Override
  public Function1D<SABRFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward) {
    ArgumentChecker.notNull(option, "option");
    final double strike = option.getStrike();

    final double cutoff = forward * CUTOFF_MONEYNESS;
    final double k;
    if (strike < cutoff) {
      s_logger.info("Given strike of " + strike + " is less than cutoff at " + cutoff + ", therefore the strike is taken as " + cutoff);
      k = cutoff;
    } else {
      k = strike;
    }

    final double t = option.getTimeToExpiry();
    return new Function1D<SABRFormulaData, Double>() {

      @Override
      public final Double evaluate(final SABRFormulaData data) {
        ArgumentChecker.notNull(data, "data");
        final double alpha = data.getAlpha();
        final double beta = data.getBeta();
        final double rho = data.getRho();
        final double nu = data.getNu();
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

            double z;
            if (beta == 1.0) {
              z = nu * x / alpha;

            } else {
              z = nu * (Math.pow(forward, beta1) - Math.pow(k, beta1)) / alpha / beta1;
            }
            final double temp = (Math.sqrt(1 + z * (z - 2 * rho)) + z - rho) / (1 - rho);
            i0 = nu * x / Math.log(temp);

          }
        }

        final double f1sqrt = Math.pow(forward * k, beta1 / 2);
        final double i1 = beta1 * beta1 * alpha * alpha / 24 / f1sqrt / f1sqrt + rho * alpha * beta * nu / 4 / f1sqrt + nu * nu * (2 - 3 * rho * rho) / 24;

        return i0 * (1 + i1 * t);
      }
    };
  }

  @Override
  public Function1D<SABRFormulaData, double[]> getVolatilityFunction(final double forward, final double[] strikes, final double timeToExpiry) {

    final int n = strikes.length;
    final List<Function1D<SABRFormulaData, Double>> funcs = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      funcs.add(getVolatilityFunction(new EuropeanVanillaOption(strikes[i], timeToExpiry, true), forward));
    }
    return new Function1D<SABRFormulaData, double[]>() {
      @Override
      public double[] evaluate(final SABRFormulaData data) {
        final double[] res = new double[n];
        for (int i = 0; i < n; i++) {
          res[i] = funcs.get(i).evaluate(data);
        }
        return res;
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
    return "SABR (Berestycki)";
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

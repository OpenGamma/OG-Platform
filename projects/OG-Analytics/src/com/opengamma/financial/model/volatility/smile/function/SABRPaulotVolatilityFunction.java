/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import static com.opengamma.math.FunctionUtils.square;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * Expansion from Paulot, Louis, Asymptotic Implied Volatility at the Second Order With Applications to the SABR Model (2009)
 * <b>DO NOT USE This formulating gives very odd (i.e. wrong) smiles for certain parameters. It is not clear whether this is a problem with the actual paper or the 
 * Implementation.  </b>
 */
public class SABRPaulotVolatilityFunction implements VolatilityFunctionProvider<SABRFormulaData> {
  private static final VolatilityFunctionProvider<SABRFormulaData> HAGAN = new SABRHaganVolatilityFunction();
  private static final double EPS = 1e-15;

  @Override
  public Function1D<SABRFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getStrike();
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
        final double f = data.getForward();

        double sigma0, sigma1;

        final double beta1 = 1 - beta;

        final double x = Math.log(k / f);
        if (CompareUtils.closeEquals(nu, 0, EPS)) {
          if (CompareUtils.closeEquals(beta, 1.0, EPS)) {
            return alpha; // this is just log-normal
          }
          throw new NotImplementedException("Have not implemented the case where nu = 0, beta != 0");
        }

        // the formula behaves very badly close to ATM
        if (CompareUtils.closeEquals(x, 0.0, 1e-3)) {
          final double delta = 1.01e-3;
          final double a0 = (HAGAN.getVolatilityFunction(option)).evaluate(data);
          double kPlus, kMinus;
          kPlus = f * Math.exp(delta);
          kMinus = f * Math.exp(-delta);
          EuropeanVanillaOption other = new EuropeanVanillaOption(kPlus, option.getTimeToExpiry(), option.isCall());
          final double yPlus = getVolatilityFunction(other).evaluate(data);
          other = new EuropeanVanillaOption(kMinus, option.getTimeToExpiry(), option.isCall());
          final double yMinus = getVolatilityFunction(other).evaluate(data);
          final double a2 = (yPlus + yMinus - 2 * a0) / 2 / delta / delta;
          final double a1 = (yPlus - yMinus) / 2 / delta;
          return a2 * x * x + a1 * x + a0;
        }
        final double tScale = nu * nu * t;
        final double alphaScale = alpha / nu;

        double q;
        if (CompareUtils.closeEquals(beta, 1.0, EPS)) {
          q = x;
        } else {
          q = (Math.pow(k, beta1) - Math.pow(f, beta1)) / beta1;
        }

        final double vMin = Math.sqrt(alphaScale * alphaScale + 2 * rho * alphaScale * q + q * q);
        final double logTerm = Math.log((vMin + rho * alphaScale + q) / (1 + rho) / alphaScale);
        sigma0 = x / logTerm;

        final double cTilde = getCTilde(f, k, alphaScale, beta, rho, q);
        sigma1 = -(cTilde + Math.log(sigma0 * Math.sqrt(k * f))) / square(logTerm);
        return nu * sigma0 * (1 + sigma1 * tScale);
      }
    };
  }

  private double getCTilde(final double f, final double k, final double alpha, final double beta, final double rho, final double q) {
    final double rhoStar = Math.sqrt(1 - rho * rho);
    final double beta1 = 1 - beta;
    final double vMin = Math.sqrt(alpha * alpha + 2 * rho * alpha * q + q * q);
    double res = -0.5 * Math.log(alpha * vMin * Math.pow(f * k, beta));
    if (CompareUtils.closeEquals(beta, 1.0, EPS)) {
      res += rho / 2 / rhoStar / rhoStar * (rho * Math.log(k / f) - vMin + alpha);
    } else {
      final double a = Math.pow(f, beta1);
      final double b = beta1 * rhoStar;
      final double c = beta1 * rho;
      final double x1 = -rho * alpha / rhoStar;
      final double x2 = (q - rho * vMin) / rhoStar;
      final double y1 = alpha;
      final double y2 = vMin;
      final double xCap = (x2 * x2 - x1 * x1 + y2 * y2 - y1 * y1) / 2 / (x2 - x1);
      final double rCap = Math.sqrt(y1 * y1 + square(x1 - xCap));
      final double t1 = Math.sqrt((rCap - x1 + xCap) / (rCap + x1 - xCap));
      final double t2 = Math.sqrt((rCap - x2 + xCap) / (rCap + x2 - xCap));
      res -= rho * beta / beta1 / rhoStar * (getG(a, b, c, xCap, rCap, beta, t2) - getG(a, b, c, xCap, rCap, beta, t1));
    }
    return res;
  }

  private double getG(final double a, final double b, final double c, final double xCap, final double rCap, final double beta, final double t) {
    final double beta1 = 1 - beta;
    double res = Math.atan(t);
    final double y = square(a + b * xCap) - square((beta1 * rCap));
    if (y > 0) {
      final double temp = Math.sqrt(y);
      res -= (a + b * xCap) / temp * Math.atan((c * rCap + t * (a + b * (xCap - rCap))) / temp);
    } else if (y < 0) {
      final double temp = Math.sqrt(-y);
      res += (a + b * xCap) / temp * modAtanh((c * rCap + t * (a + b * (xCap - rCap))) / temp);
    } else {
      res += (a + b * xCap) / (c * rCap + t * (a + b * (xCap - rCap)));
    }
    return res;
  }

  private double modAtanh(final double z) {
    return 0.5 * Math.log(Math.abs((1 + z) / (1 - z)));
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteTwoFactorDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class HullWhiteTwoFactorInterestRateModel implements DiscountBondModel<HullWhiteTwoFactorDataBundle> {

  @Override
  public Function1D<HullWhiteTwoFactorDataBundle, Double> getDiscountBondFunction(final ZonedDateTime time, final ZonedDateTime maturity) {
    Validate.notNull(time, "time");
    Validate.notNull(maturity, "maturity");
    return new Function1D<HullWhiteTwoFactorDataBundle, Double>() {

      @Override
      public Double evaluate(final HullWhiteTwoFactorDataBundle data) {
        Validate.notNull(data, "data");
        final double t1 = 0;
        final double t2 = DateUtils.getDifferenceInYears(data.getDate(), time);
        final double t3 = DateUtils.getDifferenceInYears(data.getDate(), maturity);
        final double r2 = data.getShortRate(t2);
        final double r3 = data.getShortRate(t3);
        final double p2 = Math.exp(-r2 * t2);
        final double p3 = Math.exp(-r3 * t3);
        final double alpha = data.getFirstSpeed();
        final double beta = data.getSecondSpeed();
        final double sigma1 = data.getShortRateVolatility(t1);
        final double sigma2 = data.getSecondVolatility(t1);
        final double rho = data.getCorrelation();
        final double eta = getEta(t1, t2, t3, alpha, beta, sigma1, sigma2, rho);
        final double b = getB(t3 - t2, alpha);
        final double c = getC(t3 - t2, alpha, beta);
        final double u = data.getMeanReversionLevel();
        final double f = data.getForwardRate(t1);
        final double lnA = Math.log(p3 / p2) + b * f - eta;
        return Math.exp(lnA - r2 * b - u * c);
      }

    };
  }

  protected Double getB(final Double dt, final Double a) {
    return (1 - Math.exp(-a * dt)) / a;
  }

  protected Double getC(final Double dt, final Double a, final Double b) {
    return Math.exp(-a * dt) / (a * (a - b)) - Math.exp(-b * dt) / (b * (a - b)) + 1. / (a * b);
  }

  protected Double getEta(final double t1, final double t2, final double t3, final double a, final double b, final double sigma1, final double sigma2, final double rho) {
    final double dt12 = t2 - t1;
    final double dt13 = t3 - t1;
    final double dt23 = t3 - t2;
    final double b12 = getB(dt12, a);
    final double b12Sq = b12 * b12;
    final double b13 = getB(dt13, a);
    final double b13Sq = b13 * b13;
    final double b23 = getB(dt23, a);
    final double b23Sq = b23 * b23;
    final double c12 = getC(dt12, a, b);
    final double c12Sq = c12 * c12;
    final double c13 = getC(dt13, a, b);
    final double c13Sq = c13 * c13;
    final double c23 = getC(dt23, a, b);
    final double c23Sq = c23 * c23;
    final double abP = a + b;
    final double abM = a - b;
    final double gamma1 = Math.exp(-abP * dt13) * (Math.exp(abP * dt12) - 1) / (abP * abM) - Math.exp(-2 * a * dt13) * (Math.exp(2 * a * dt12) - 1) / (2 * a * abM);
    final double gamma2 = (gamma1 + c23 - c13 + 0.5 * b23Sq - 0.5 * b13Sq + dt12 / a - (Math.exp(-a * dt23) - Math.exp(-a * dt13)) / (a * a)) / (a * b);
    final double gamma3 = -(Math.exp(-abP * dt12) - 1) / (abP * abM) + (Math.exp(-2 * a * dt12) - 1) / (2 * a * abM);
    final double gamma4 = (gamma3 - c12 - 0.5 * b12Sq + dt12 / a + (Math.exp(-a * dt12) - 1) / (a * a)) / (a * b);
    final double gamma5 = (0.5 * (c23Sq - c13Sq) + gamma2) / b;
    final double gamma6 = (gamma4 - 0.5 * c12Sq) / b;
    return sigma1 * sigma1 * (1 - Math.exp(-2 * a * dt12)) * b23Sq / (4 * a) - rho * sigma1 * sigma2 * (b12 * c12 * b23 + gamma4 - gamma2) - 0.5 * sigma2 * sigma2 * (c12Sq * b23 + gamma6 - gamma5);
  }
}

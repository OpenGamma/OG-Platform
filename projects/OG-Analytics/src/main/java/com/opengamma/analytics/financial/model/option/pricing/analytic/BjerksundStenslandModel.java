/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Class defining an analytical approximation for American option prices as
 * derived by Bjerksund and Stensland (2002).
 * <p>
 * The price of a call is given by:
 * $$
 * \begin{align*}
 * C = &\alpha_2 S^\beta - \alpha_2 \phi(S, t_1, \beta, I_2, I_2)\\
 * & + \phi(S, t_1, 1, I_2, I_2) - \phi(S, t_1, 1, I_1, I_2)\\
 * & - K\phi(S, t_1, 0, I_2, I_2) + K\phi(S, t_1, 0, I_1, I_2)\\
 * & + \alpha_1 \phi(S, t_1, \beta, I_1, I_2) - \alpha_1 \psi(S, T, \beta, I_1, I_2, I_2, t_1)\\
 * & + \psi(S, T, 1, I_1, I2, I_1, t_1) - \psi(S, T, 1, K, I_2, I_1, t_1)\\
 * & - K\psi(S, T, 0, I_1, I_2, I_1, t_1) + K\psi(S, T, 0, K, I_2, I_1, t_1)
 * \end{align*}
 * $$
 * where
 * $$
 * \begin{align*}
 * t_1 &= \frac{(\sqrt{5} - 1)T}{2}\\
 * I_1 &= B_0 + (B_\infty - B_0)(1 - e^{h_1})\\
 * I_2 &= B_0 + (B_\infty - B_0)(1 - e^{h_2})\\
 * B_0 &= \frac{\beta K}{\beta - 1}\\
 * B_\infty &= \max\left(K, \frac{rK}{r-b}\right)\\
 * h_1 &= \frac{(bt_1 + 2\sigma\sqrt{t_1})K^2}{B_0(B_0 - B_\infty)}\\
 * h_2 &= \frac{(bT + 2\sigma\sqrt{T})K^2}{B_0(B_0 - B_\infty)}\\
 * \alpha_1 &= (I_1 - K)I_1^{-\beta}\\
 * \alpha_2 &= (I_2 - K)I_2^{-\beta}\\
 * \beta &= \frac{1}{2} - \frac{b}{\sigma^2} + \sqrt{\left(\frac{b}{\sigma^2} - \frac{1}{2}\right)^2 + \frac{2r}{\sigma^2}}
 * \end{align*}
 * $$
 * The function $\phi(S, T, \gamma, H, I)$ is defined as
 * $$
 * \begin{align*}
 * \phi(S, T, \gamma, H, I) &= e^\lambda S^\gamma\left[N(-d_1) - \left(\frac{I}{S}\right)^\kappa N(-d_2)\right]\\
 * d_1 &= \frac{\ln(\frac{S}{H}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\
 * d_2 &= \frac{\ln(\frac{I^2}{SH}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\
 * \lambda &= -r + \gamma b + \frac{\gamma(\gamma - 1)\sigma^2}{2}\\
 * \kappa &= \frac{2b}{\sigma^2} + 2\gamma + 1
 * \end{align*}
 * $$
 * and the function $\psi(S, T, \gamma, H, I_2, I_1, t_1)$ is defined as
 * $$
 * \begin{align*}
 * \psi(S, T, \gamma, H, I_2, I_1, t_1) = &e^{\lambda T} S^\gamma\left[M(d_1, e_1, \rho) 
 *   -\left(\frac{I_2}{S}\right)^\kappa M(d_2, e_2, \rho) - \left(\frac{I_1}{S}\right)^\kappa M(d_3, e_3, -\rho)
 *   +\left(\frac{I_1}{I_2}\right)^\kappa M(d_4, e_4, \rho)\right]
 * \end{align*}
 * $$
 * where
 * $$
 * \begin{align*}
 * d_1 &= -\frac{\ln(\frac{S}{I_1}) + (b + (\gamma - \frac{1}{2})\sigma^2)t_1}{\sigma\sqrt{t_1}}\\
 * d_2 &= -\frac{\ln(\frac{I_2^2}{SI_1}) + (b + (\gamma - \frac{1}{2})\sigma^2)t_1}{\sigma\sqrt{t_1}}\\
 * d_3 &= -\frac{\ln(\frac{S}{I_1}) - (b + (\gamma - \frac{1}{2})\sigma^2)t_1}{\sigma\sqrt{t_1}}\\
 * d_4 &= -\frac{\ln(\frac{I_2^2}{SI_1}) - (b + (\gamma - \frac{1}{2})\sigma^2)t_1}{\sigma\sqrt{t_1}}\\
 * e_1 &= -\frac{\ln(\frac{S}{H}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\
 * e_2 &= -\frac{\ln(\frac{I_1^2}{SH}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\
 * %e_3 &= -\frac{\ln(\frac{I_2^2}{SH}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\
 * e_4 &= -\frac{\ln(\frac{SI_1^2}{HI_2^2}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}
 * \end{align*}
 * $$
 * and $\rho = \sqrt{\frac{t_1}{T}}$ and $M(\cdot, \cdot, \cdot)$ is the CDF of the bivariate
 * normal distribution (see {@link com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution}).
 *
 * The price of puts is calculated using the Bjerksund-Stensland put-call transformation 
 * $p(S, K, T, r, b, \sigma) = c(K, S, T, r - b, -b, \sigma)$.
 * 
 */
public class BjerksundStenslandModel extends AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<double[]> BIVARIATE_NORMAL = new BivariateNormalDistribution();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final AmericanVanillaOptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        final ZonedDateTime date = data.getDate();
        double s = data.getSpot();
        double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(date);
        final double sigma = data.getVolatility(t, k);
        double r = data.getInterestRate(t);
        double b = data.getCostOfCarry();
        StandardOptionDataBundle newData = data;
        if (!definition.isCall()) {
          if (s == 0) {
            return k;
          }
          r -= b;
          b *= -1;
          final double temp = s;
          s = k;
          k = temp;
          final YieldAndDiscountCurve curve = data.getInterestRateCurve().withParallelShift(-b);
          newData = data.withInterestRateCurve(curve).withSpot(s);
        }
        if (b >= r) {
          final OptionDefinition european = new EuropeanVanillaOptionDefinition(k, definition.getExpiry(), definition.isCall());
          final Function1D<StandardOptionDataBundle, Double> bsm = BSM.getPricingFunction(european);
          return bsm.evaluate(newData);
        }
        return getCallPrice(s, k, sigma, t, r, b);
      }
    };
    return pricingFunction;
  }

  private double getCallPrice(final double s, final double k, final double sigma, final double t2, final double r, final double b) {
    final double sigmaSq = sigma * sigma;
    final double y = 0.5 - b / sigmaSq;
    final double beta = y + Math.sqrt(y * y + 2 * r / sigmaSq);
    final double b0 = Math.max(k, r * k / (r - b));
    final double bInfinity = beta * k / (beta - 1);
    final double t1 = 0.5 * (Math.sqrt(5) - 1) * t2;
    final double h1 = getH(b, t1, sigma, k, b0, bInfinity);
    final double h2 = getH(b, t2, sigma, k, b0, bInfinity);
    final double x1 = getX(b0, bInfinity, h1);
    final double x2 = getX(b0, bInfinity, h2);
    if (s >= x2) {
      return s - k;
    }
    final double alpha1 = getAlpha(x1, beta, k);
    final double alpha2 = getAlpha(x2, beta, k);
    return alpha2 * Math.pow(s, beta) - alpha2 * getPhi(s, t1, beta, x2, x2, r, b, sigma) + getPhi(s, t1, 1, x2, x2, r, b, sigma) - getPhi(s, t1, 1, x1, x2, r, b, sigma) - k
        * getPhi(s, t1, 0, x2, x2, r, b, sigma) + k * getPhi(s, t1, 0, x1, x2, r, b, sigma) + alpha1 * getPhi(s, t1, beta, x1, x2, r, b, sigma) - alpha1
        * getPsi(s, t1, t2, beta, x1, x2, x1, r, b, sigma) + getPsi(s, t1, t2, 1, x1, x2, x1, r, b, sigma) - getPsi(s, t1, t2, 1, k, x2, x1, r, b, sigma) - k
        * getPsi(s, t1, t2, 0, x1, x2, x1, r, b, sigma) + k * getPsi(s, t1, t2, 0, k, x2, x1, r, b, sigma);
  }

  private double getH(final double b, final double t, final double sigma, final double k, final double b0, final double bInfinity) {
    return -(b * t + 2 * sigma * Math.sqrt(t)) * k * k / (b0 * (bInfinity - b0));
  }

  private double getX(final double b0, final double bInfinity, final double h) {
    return b0 + (bInfinity - b0) * (1 - Math.exp(h));
  }

  private double getAlpha(final double i, final double beta, final double k) {
    return Math.pow(i, -beta) * (i - k);
  }

  private double getPhi(final double s, final double t, final double gamma, final double h, final double x, final double r, final double b, final double sigma) {
    final double sigmaSq = sigma * sigma;
    final double denom = getDenom(t, sigma);
    final double lambda = getLambda(r, gamma, b, sigmaSq);
    final double kappa = getKappa(b, gamma, sigmaSq);
    final double y = getY(t, b, sigmaSq, gamma, denom);
    final double d1 = getD(s / h, denom, y);
    final double d2 = getD(x * x / (s * h), denom, y);
    return Math.exp(lambda * t) * Math.pow(s, gamma) * (NORMAL.getCDF(d1) - Math.pow(x / s, kappa) * NORMAL.getCDF(d2));
  }

  private double getPsi(final double s, final double t1, final double t2, final double gamma, final double h, final double x2, final double x1, final double r, final double b, final double sigma) {
    final double sigmaSq = sigma * sigma;
    final double denom1 = getDenom(t1, sigma);
    final double denom2 = getDenom(t2, sigma);
    final double y1 = getY(t1, b, sigmaSq, gamma, denom1);
    final double y2 = getY(t2, b, sigmaSq, gamma, denom2);
    final double d1 = getD(s / x1, denom1, y1);
    final double d2 = getD(x2 * x2 / (s * x1), denom1, y1);
    final double d3 = d1 + 2 * y1;
    final double d4 = d2 + 2 * y1;
    final double e1 = getD(s / h, denom2, y2);
    final double e2 = getD(x2 * x2 / (s * h), denom2, y2);
    final double e3 = getD(x1 * x1 / (s * h), denom2, y2);
    final double e4 = getD(s * x1 * x1 / (h * x2 * x2), denom2, y2);
    final double lambda = getLambda(r, gamma, b, sigmaSq);
    final double kappa = getKappa(b, gamma, sigmaSq);
    final double rho = Math.sqrt(t1 / t2);
    return Math.exp(lambda * t2)
        * Math.pow(s, gamma)
        * (BIVARIATE_NORMAL.getCDF(new double[] {d1, e1, rho}) - Math.pow(x2 / s, kappa) * BIVARIATE_NORMAL.getCDF(new double[] {d2, e2, rho}) - Math.pow(x1 / s, kappa)
            * BIVARIATE_NORMAL.getCDF(new double[] {d3, e3, -rho}) + Math.pow(x1 / x2, kappa) * BIVARIATE_NORMAL.getCDF(new double[] {d4, e4, -rho}));
  }

  private double getLambda(final double r, final double gamma, final double b, final double sigmaSq) {
    return -r + gamma * b + 0.5 * gamma * (gamma - 1) * sigmaSq;
  }

  private double getKappa(final double b, final double gamma, final double sigmaSq) {
    return 2 * b / sigmaSq + 2 * gamma - 1;
  }

  private double getY(final double t, final double b, final double sigmaSq, final double gamma, final double denom) {
    return t * (b + sigmaSq * (gamma - 0.5)) / denom;
  }

  private double getDenom(final double t, final double sigma) {
    return sigma * Math.sqrt(t);
  }

  private double getD(final double x, final double denom, final double y) {
    return -(Math.log(x) / denom + y);
  }
}

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
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
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

  private static final double RHO2 = 0.5 * (Math.sqrt(5) - 1);
  private static final double RHO = Math.sqrt(RHO2);
  private static final double RHO_STAR = Math.sqrt(1 - RHO2);
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
        //  StandardOptionDataBundle newData = data;
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
          //  newData = data.withInterestRateCurve(curve).withSpot(s);
        }
        //        if (b >= r) {
        //          final OptionDefinition european = new EuropeanVanillaOptionDefinition(k, definition.getExpiry(), definition.isCall());
        //          final Function1D<StandardOptionDataBundle, Double> bsm = BSM.getPricingFunction(european);
        //          return bsm.evaluate(newData);
        //        }
        return getCallPrice(s, k, sigma, t, r, b);
      }
    };
    return pricingFunction;
  }

  /**
   * This uses the put-call trnasformation 
   * @param s
   * @param k
   * @param sigma
   * @param t2
   * @param r
   * @param b
   * @return
   */
  protected double getPutPrice(final double s, final double k, final double sigma, final double t2, final double r, final double b) {
    return getCallPrice(k, s, sigma, t2, r - b, -b);
  }

  protected double getCallPrice(final double s, final double k, final double sigma, final double t2, final double r, final double b) {

    if (b >= r) {
      final double fwd = s * Math.exp(b * t2);
      final double df = Math.exp(-r * t2);
      return df * BlackFormulaRepository.price(fwd, k, t2, sigma, true);
    }

    final double sigmaSq = sigma * sigma;
    final double y = 0.5 - b / sigmaSq;
    final double beta = y + Math.sqrt(y * y + 2 * r / sigmaSq);
    final double b0 = Math.max(k, r * k / (r - b));
    final double bInfinity = beta * k / (beta - 1);
    final double t1 = RHO2 * t2;
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

  protected double getPhi(final double s, final double t, final double gamma, final double h, final double x, final double r, final double b, final double sigma) {
    final double sigmaSq = sigma * sigma;
    final double denom = getDenom(t, sigma);
    final double lambda = getLambda(r, gamma, b, sigmaSq);
    final double kappa = getKappa(b, gamma, sigmaSq);
    final double y = getY(t, b, sigmaSq, gamma, denom);
    final double d1 = getD(s / h, denom, y);
    final double d2 = getD(x * x / (s * h), denom, y);
    return Math.exp(lambda * t) * Math.pow(s, gamma) * (NORMAL.getCDF(d1) - Math.pow(x / s, kappa) * NORMAL.getCDF(d2));
  }

  protected double getPsi(final double s, final double t1, final double t2, final double gamma, final double h, final double x2, final double x1, final double r, final double b, final double sigma) {
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
        * (BIVARIATE_NORMAL.getCDF(new double[] {d1, e1, rho }) - Math.pow(x2 / s, kappa) * BIVARIATE_NORMAL.getCDF(new double[] {d2, e2, rho }) - Math.pow(x1 / s, kappa)
            * BIVARIATE_NORMAL.getCDF(new double[] {d3, e3, -rho }) + Math.pow(x1 / x2, kappa) * BIVARIATE_NORMAL.getCDF(new double[] {d4, e4, -rho }));
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

  //**************
  //adjoint stuff

  protected double[] getCallPriceAdjoint(final double s0, final double k, final double r, final double b, final double t, final double sigma) {

    double[] res = new double[7];
    //European option case 
    if (b >= r) {
      final BaroneAdesiWhaleyModel mod = new BaroneAdesiWhaleyModel();
      res[0] = mod.price(s0, k, r, b, t, sigma, true);
      System.arraycopy(mod.getPriceAdjoint(s0, k, r, b, t, sigma, true), 0, res, 1, 6);
      return res;
    }

    final double[] x2Adj = getI2Adjoint(k, r, b, sigma, t);
    //early exicise 
    if (s0 >= x2Adj[0]) {
      res[0] = s0 - k;
      res[1] = 1.0;
      res[2] = -1.0;
      return res;
    }

    final double[] x1Adj = getI1Adjoint(k, r, b, sigma, t);
    final double sigmaSq = sigma * sigma;
    final double[] betaAdj = getBetaAdjoint(r, b, sigmaSq);
    final double[] alpha1Adj = getAlphaAdjoint(k, x1Adj[0], betaAdj[0]);
    final double[] alpha2Adj = getAlphaAdjoint(k, x2Adj[0], betaAdj[0]);

    final double[] phi1Adj = getPhiAdjoint(s0, t, betaAdj[0], x2Adj[0], x2Adj[0], r, b, sigma);
    final double[] phi2Adj = getPhiAdjoint(s0, t, 1.0, x2Adj[0], x2Adj[0], r, b, sigma);
    final double[] phi3Adj = getPhiAdjoint(s0, t, 1.0, x1Adj[0], x2Adj[0], r, b, sigma);
    final double[] phi4Adj = getPhiAdjoint(s0, t, 0.0, x2Adj[0], x2Adj[0], r, b, sigma);
    final double[] phi5Adj = getPhiAdjoint(s0, t, 0.0, x1Adj[0], x2Adj[0], r, b, sigma);
    final double[] phi6Adj = getPhiAdjoint(s0, t, betaAdj[0], x1Adj[0], x2Adj[0], r, b, sigma);

    final double[] psi1Adj = getPsiAdjoint(s0, t, betaAdj[0], x1Adj[0], x2Adj[0], x1Adj[0], r, b, sigma);
    final double[] psi2Adj = getPsiAdjoint(s0, t, 1.0, x1Adj[0], x2Adj[0], x1Adj[0], r, b, sigma);
    final double[] psi3Adj = getPsiAdjoint(s0, t, 1.0, k, x2Adj[0], x1Adj[0], r, b, sigma);
    final double[] psi4Adj = getPsiAdjoint(s0, t, 0.0, x1Adj[0], x2Adj[0], x1Adj[0], r, b, sigma);
    final double[] psi5Adj = getPsiAdjoint(s0, t, 0.0, k, x2Adj[0], x1Adj[0], r, b, sigma);

    final double w1 = Math.pow(s0, betaAdj[0]);
    final double w2 = phi1Adj[0];
    final double w3 = alpha2Adj[0] * (w1 - w2);
    final double w4 = phi2Adj[0] - phi3Adj[0];
    final double w5 = k * (-phi4Adj[0] + phi5Adj[0]);
    final double w6 = alpha1Adj[0] * (phi6Adj[0] - psi1Adj[0]);
    final double w7 = psi2Adj[0] - psi3Adj[0];
    final double w8 = k * (-psi4Adj[0] + psi5Adj[0]);
    final double w9 = w3 + w4 + w5 + w6 + w7 + w8;

    //backwards sweep
    //w3Bar to w9Bar = 1.0;
    final double w2Bar = -alpha2Adj[0];
    final double w1Bar = alpha2Adj[0];
    final double psi5Bar = k;
    final double psi4Bar = -k;
    final double psi3Bar = -1.0;
    final double psi2Bar = 1.0;
    final double psi1Bar = -alpha1Adj[0];
    final double phi6Bar = alpha1Adj[0];
    final double phi5Bar = k;
    final double phi4Bar = -k;
    final double phi3Bar = -1.0;
    final double phi2Bar = 1.0;
    final double phi1Bar = w2Bar;

    final double alpha2Bar = w1 - w2;
    final double alpha1Bar = phi6Adj[0] - psi1Adj[0];

    final double x2Bar = psi5Adj[5] * psi5Bar + psi4Adj[5] * psi4Bar + psi3Adj[5] * psi3Bar + psi2Adj[5] * psi2Bar + psi1Adj[5] * psi1Bar
        + phi6Adj[5] * phi6Bar + phi5Adj[5] * phi5Bar + (phi4Adj[4] + phi4Adj[5]) * phi4Bar + +phi3Adj[5] * phi3Bar + (phi2Adj[4] + phi2Adj[5]) * phi2Bar + (phi1Adj[4] + phi1Adj[5]) * phi1Bar
        + alpha2Adj[2] * alpha2Bar;

    final double x1Bar = psi5Adj[6] * psi5Bar + (psi4Adj[4] + psi4Adj[6]) * psi4Bar + psi3Adj[6] * psi3Bar + (psi2Adj[4] + psi2Adj[6]) * psi2Bar + (psi1Adj[4] + psi1Adj[6]) * psi1Bar
        + phi6Adj[4] * phi6Bar + phi5Adj[4] * phi5Bar + phi3Adj[4] * phi3Bar
        + alpha1Adj[2] * alpha1Bar;

    final double betaBar = Math.log(s0) * w1 * w1Bar + psi1Adj[3] * psi1Bar + phi1Adj[3] * phi1Bar + phi6Adj[3] * phi6Bar
        + alpha2Bar * alpha2Adj[3] + alpha1Bar * alpha1Adj[3];

    final double sBar = betaAdj[0] * w1 / s0 * w1Bar
        + psi5Adj[1] * psi5Bar + psi4Adj[1] * psi4Bar + psi3Adj[1] * psi3Bar + psi2Adj[1] * psi2Bar + psi1Adj[1] * psi1Bar
        + phi6Adj[1] * phi6Bar + phi5Adj[1] * phi5Bar + phi4Adj[1] * phi4Bar + phi3Adj[1] * phi3Bar + phi2Adj[1] * phi2Bar + phi1Adj[1] * phi1Bar;

    final double kBar = -psi4Adj[0] + psi5Adj[0] - phi4Adj[0] + phi5Adj[0]
        + psi5Adj[4] * psi5Bar + psi3Adj[4] * psi3Bar
        + x2Adj[1] * x2Bar + x1Adj[1] * x1Bar
        + alpha1Adj[1] * alpha1Bar + alpha2Adj[1] * alpha2Bar;
    ;
    final double rBar = psi5Adj[7] * psi5Bar + psi4Adj[7] * psi4Bar + psi3Adj[7] * psi3Bar + psi2Adj[7] * psi2Bar + psi1Adj[7] * psi1Bar
        + phi6Adj[6] * phi6Bar + phi5Adj[6] * phi5Bar + phi4Adj[6] * phi4Bar + phi3Adj[6] * phi3Bar + phi2Adj[6] * phi2Bar + phi1Adj[6] * phi1Bar
        + x2Adj[2] * x2Bar + x1Adj[2] * x1Bar + betaAdj[1] * betaBar;

    final double bBar = psi5Adj[8] * psi5Bar + psi4Adj[8] * psi4Bar + psi3Adj[8] * psi3Bar + psi2Adj[8] * psi2Bar + psi1Adj[8] * psi1Bar
        + phi6Adj[7] * phi6Bar + phi5Adj[7] * phi5Bar + phi4Adj[7] * phi4Bar + phi3Adj[7] * phi3Bar + phi2Adj[7] * phi2Bar + phi1Adj[7] * phi1Bar
        + x2Adj[3] * x2Bar + x1Adj[3] * x1Bar + betaAdj[2] * betaBar;

    //TODO tBar is wrong - cannot find why
    final double tBar = psi5Adj[2] * psi5Bar + psi4Adj[2] * psi4Bar + psi3Adj[2] * psi3Bar + psi2Adj[2] * psi2Bar + psi1Adj[2] * psi1Bar
        + (phi6Adj[2] * phi6Bar + phi5Adj[2] * phi5Bar + phi4Adj[2] * phi4Bar + phi3Adj[2] * phi3Bar + phi2Adj[2] * phi2Bar + phi1Adj[2] * phi1Bar)
        + x2Adj[5] * x2Bar + x1Adj[5] * x1Bar;

    final double sigmaBar = psi5Adj[9] * psi5Bar + psi4Adj[9] * psi4Bar + psi3Adj[9] * psi3Bar + psi2Adj[9] * psi2Bar + psi1Adj[9] * psi1Bar
        + phi6Adj[8] * phi6Bar + phi5Adj[8] * phi5Bar + phi4Adj[8] * phi4Bar + phi3Adj[8] * phi3Bar + phi2Adj[8] * phi2Bar + phi1Adj[8] * phi1Bar
        + x2Adj[4] * x2Bar + x1Adj[4] * x1Bar + 2 * sigma * betaAdj[3] * betaBar;

    res[0] = w9;
    res[1] = sBar;
    res[2] = kBar;
    res[3] = rBar;
    res[4] = bBar;
    res[5] = tBar;
    res[6] = sigmaBar;

    return res;
  }

  final double[] getPutPriceAdjoint(final double s0, final double k, final double r, final double b, final double t, final double sigma) {

    final double s0Prime = k;
    final double kPrime = s0;
    final double rPrime = r - b;
    final double bPrime = -b;
    final double[] cAdjoint = getCallPriceAdjoint(s0Prime, kPrime, rPrime, bPrime, t, sigma);
    final double[] res = new double[7];

    res[0] = cAdjoint[0];
    res[1] = cAdjoint[2];
    res[2] = cAdjoint[1];
    res[3] = cAdjoint[3];
    res[4] = -cAdjoint[3] - cAdjoint[4];
    res[5] = cAdjoint[5];
    res[6] = cAdjoint[6];
    return res;
  }

  /**
   * get alpha and its sensitivity to k, x (I) and beta
   * @param k
   * @param x
   * @param beta
   * @return
   */
  protected double[] getAlphaAdjoint(final double k, final double x, final double beta) {

    final double w1 = Math.pow(x, -beta);
    final double w2 = x - k;
    final double w3 = w2 * w1;

    final double w2Bar = w1;
    final double w1Bar = w2;

    final double[] res = new double[4];
    res[0] = w3;
    res[1] = -w2Bar; //kBar
    res[2] = w2Bar - beta * w1 / x * w1Bar; //xBar
    res[3] = -Math.log(x) * w3; //betaBar

    return res;
  }

  /**
   * Get lambda and its sensitivity to gamma, r, b and sigma-squared
   * @param gamma
   * @param r
   * @param b
   * @param sigmaSq
   * @return length 5 array of lambda and its sensitivity to gamma, r, b and sigma-squared
   */
  protected double[] getLambdaAdjoint(final double gamma, final double r, final double b, final double sigmaSq) {
    final double[] res = new double[5];
    final double temp = 0.5 * gamma * (gamma - 1);
    res[0] = -r + gamma * b + temp * sigmaSq; //lambda
    res[1] = b + (gamma - 0.5) * sigmaSq; //gammaBar
    res[2] = -1.0; //rBar
    res[3] = gamma; //bBar
    res[4] = temp; //sigmasqBar
    return res;
  }

  /**
   * Get kappa and its sensitivity to gamma, b and sigma-squared 
   * @param gamma
   * @param b
   * @param sigmaSq
   * @return length 4 array of kappa and its sensitivity to gamma, b and sigma-squared
   */
  protected double[] getKappaAdjoint(final double gamma, final double b, final double sigmaSq) {
    final double[] res = new double[4];
    final double temp = 2 * b / sigmaSq;
    res[0] = temp + 2 * gamma - 1;
    res[1] = 2.0; //gammaBar
    res[2] = 2 / sigmaSq; //bBar
    res[3] = -temp / sigmaSq; //sigmasqBar
    return res;
  }

  /**
   * get phi and its sensitivity to s, t, gamma, h, x (I), r, b & sigma  
   * @param s
   * @param t
   * @param gamma. If this is set to 0 or 1, then the gamma sensitivity should be ignored  
   * @param h
   * @param x
   * @param r
   * @param b
   * @param sigma
   * @param sigmaRootT this just avoids taking the root again 
   * @return length 9 array of  phi and its sensitivity to s, t, gamma, h, x (I), r, b & sigma 
   */
  protected double[] getPhiAdjoint(final double s, final double t, final double gamma, final double h, final double x, final double r, final double b,
      final double sigma) {

    final double t1 = RHO2 * t;
    final double sigmaSq = sigma * sigma;
    final double sigmaRootT = sigma * Math.sqrt(t1);

    final double[] lambdaAdj = getLambdaAdjoint(gamma, r, b, sigmaSq);
    final double[] kappaAdj = getKappaAdjoint(gamma, b, sigmaSq);

    final double w0 = (b + (gamma - 0.5) * sigmaSq);
    final double w1 = w0 * t1;
    final double w2 = Math.log(s / h);
    final double w3 = Math.log(x * x / s / h);
    final double w4 = w2 + w1;
    final double w5 = w3 + w1;
    final double w6 = w4 / sigmaRootT; //d
    final double w7 = w5 / sigmaRootT; //d2
    final double w8 = NORMAL.getCDF(-w6); //N(-d);
    final double w9 = NORMAL.getCDF(-w7); //N(-d2);
    final double w10 = Math.pow(x / s, kappaAdj[0]);
    final double w11 = Math.exp(lambdaAdj[0] * t1);
    final double w12 = Math.pow(s, gamma);
    final double w13 = w8 - w10 * w9;
    final double w14 = w11 * w12 * w13;

    final double w13Bar = w11 * w12;
    final double w12Bar = w11 * w13;
    final double w11Bar = w12 * w13;
    final double w10Bar = -w9 * w13Bar;
    final double w9Bar = -w10 * w13Bar;
    final double w8Bar = w13Bar;
    final double w7Bar = -NORMAL.getPDF(w7) * w9Bar;
    final double w6Bar = -NORMAL.getPDF(w6) * w8Bar;
    final double w5Bar = 1 / sigmaRootT * w7Bar;
    final double w4Bar = 1 / sigmaRootT * w6Bar;
    final double w3Bar = w5Bar;
    final double w2Bar = w4Bar;
    final double w1Bar = w4Bar + w5Bar;
    final double w0Bar = t1 * w1Bar;

    final double[] res = new double[9];
    final double lammbaBar = t1 * w14; //w14 == w11*w11Bar 
    final double kappaBar = Math.log(x / s) * w10 * w10Bar;

    res[0] = w14; //phi
    res[1] = (gamma * w12 * w12Bar - kappaAdj[0] * w10 * w10Bar - w3Bar + w2Bar) / s; //sBar
    res[2] = RHO2 * ((b + (gamma - 0.5) * sigmaSq) * w1Bar + lambdaAdj[0] * w11 * w11Bar - 0.5 / t1 * (w7 * w7Bar + w6 * w6Bar)); //tBar
    res[3] = Math.log(s) * w12 * w12Bar + sigmaSq * t1 * w1Bar + lambdaAdj[1] * lammbaBar + kappaAdj[1] * kappaBar; //gammaBar
    res[4] = -(w2Bar + w3Bar) / h; //hBar
    res[5] = (2 * w3Bar + kappaAdj[0] * w10 * w10Bar) / x; //xBar
    res[6] = lambdaAdj[2] * lammbaBar; //rBar 
    res[7] = t1 * w1Bar + lambdaAdj[3] * lammbaBar + kappaAdj[2] * kappaBar; //bBar
    res[8] = 2 * sigma * ((gamma - 0.5) * t1 * w1Bar + lambdaAdj[4] * lammbaBar + kappaAdj[3] * kappaBar)
        - (w6 * w6Bar + w7 * w7Bar) / sigma; //sigmaBar 

    return res;
  }

  /**
   * get Psi and its sensitivity to s, t, gamma, h, x2, x1, r, b and sigma
   * @param s
   * @param t
   * @param gamma
   * @param h
   * @param x2
   * @param x1
   * @param r
   * @param b
   * @param sigma
   * @return array of length 10 of Psi and its sensitivity to s, t, gamma, h, x2, x1, r, b and sigma
   */
  protected double[] getPsiAdjoint(final double s, final double t, final double gamma, final double h, final double x2, final double x1,
      final double r, final double b, final double sigma) {

    //TODO all of this could be precalculated
    final double rootT = Math.sqrt(t);
    final double sigmarootT = sigma * rootT;
    final double t1 = RHO2 * t;
    final double rootT1 = RHO * rootT;
    final double sigmarootT1 = sigma * rootT1;
    final double sigmaSq = sigma * sigma;
    final double[] lambdaAdj = getLambdaAdjoint(gamma, r, b, sigmaSq);
    final double[] kappaAdj = getKappaAdjoint(gamma, b, sigmaSq);

    final double w1 = b + (gamma - 0.5) * sigmaSq;
    final double w2 = Math.log(s / x1);
    final double w3 = Math.log(s / h);
    final double w4 = Math.log(x2 * x2 / s / x1);
    final double w5 = Math.log(x1 * x1 / s / h);
    final double w6 = Math.log(x2 * x2 / s / h);
    final double w7 = Math.log(s * x1 * x1 / h / x2 / x2);
    final double w8 = w1 * t1;
    final double w9 = w1 * t;
    final double w10 = Math.exp(lambdaAdj[0] * t);
    final double w11 = Math.pow(s, gamma);
    final double w12 = Math.pow(x2 / s, kappaAdj[0]);
    final double w13 = Math.pow(x1 / s, kappaAdj[0]);
    final double w14 = Math.pow(x1 / x2, kappaAdj[0]);

    final double e1 = (w2 + w8) / sigmarootT1;
    final double e2 = (w4 + w8) / sigmarootT1;
    final double e3 = (w2 - w8) / sigmarootT1;
    final double e4 = (w4 - w8) / sigmarootT1;
    final double f1 = (w3 + w9) / sigmarootT;
    final double f2 = (w6 + w9) / sigmarootT;
    final double f3 = (w5 + w9) / sigmarootT;
    final double f4 = (w7 + w9) / sigmarootT;

    final double w15 = BIVARIATE_NORMAL.getCDF(new double[] {-e1, -f1, RHO });
    final double w16 = BIVARIATE_NORMAL.getCDF(new double[] {-e2, -f2, RHO });
    final double w17 = BIVARIATE_NORMAL.getCDF(new double[] {-e3, -f3, -RHO });
    final double w18 = BIVARIATE_NORMAL.getCDF(new double[] {-e4, -f4, -RHO });
    final double w19 = w15 - w12 * w16 - w13 * w17 + w14 * w18;
    final double w20 = w10 * w11 * w19;

    //backwards sweep
    final double w19Bar = w10 * w11;
    final double w18Bar = w14 * w19Bar;
    final double w17Bar = -w13 * w19Bar;
    final double w16Bar = -w12 * w19Bar;
    final double w15Bar = w19Bar;

    final double f4Bar = -NORMAL.getPDF(f4) * NORMAL.getCDF(-(e4 + RHO * f4) / RHO_STAR) * w18Bar;
    final double f3Bar = -NORMAL.getPDF(f3) * NORMAL.getCDF(-(e3 + RHO * f3) / RHO_STAR) * w17Bar;
    final double f2Bar = -NORMAL.getPDF(f2) * NORMAL.getCDF(-(e2 - RHO * f2) / RHO_STAR) * w16Bar;
    final double f1Bar = -NORMAL.getPDF(f1) * NORMAL.getCDF(-(e1 - RHO * f1) / RHO_STAR) * w15Bar;
    final double e4Bar = -NORMAL.getPDF(e4) * NORMAL.getCDF(-(f4 + RHO * e4) / RHO_STAR) * w18Bar;
    final double e3Bar = -NORMAL.getPDF(e3) * NORMAL.getCDF(-(f3 + RHO * e3) / RHO_STAR) * w17Bar;
    final double e2Bar = -NORMAL.getPDF(e2) * NORMAL.getCDF(-(f2 - RHO * e2) / RHO_STAR) * w16Bar;
    final double e1Bar = -NORMAL.getPDF(e1) * NORMAL.getCDF(-(f1 - RHO * e1) / RHO_STAR) * w15Bar;

    final double w14Bar = w18 * w19Bar;
    final double w13Bar = -w17 * w19Bar;
    final double w12Bar = -w16 * w19Bar;
    final double w11Bar = w10 * w19;
    final double w10Bar = w11 * w19;
    final double w9Bar = (f1Bar + f2Bar + f3Bar + f4Bar) / sigmarootT;
    final double w8Bar = (e1Bar + e2Bar - e3Bar - e4Bar) / sigmarootT1;
    final double w7Bar = f4Bar / sigmarootT;
    final double w6Bar = f2Bar / sigmarootT;
    final double w5Bar = f3Bar / sigmarootT;
    final double w4Bar = (e2Bar + e4Bar) / sigmarootT1;
    final double w3Bar = f1Bar / sigmarootT;
    final double w2Bar = (e1Bar + e3Bar) / sigmarootT1;
    final double w1Bar = t * w9Bar + t1 * w8Bar;

    final double kappaBar = Math.log(x1 / x2) * w14 * w14Bar + Math.log(x1 / s) * w13 * w13Bar + Math.log(x2 / s) * w12 * w12Bar;
    final double lambdaBar = t * w10 * w10Bar;

    final double[] res = new double[10];
    res[0] = w20; //Psi
    res[1] = (-kappaAdj[0] * (w13 * w13Bar + w12 * w12Bar) + gamma * w11 * w11Bar + w7Bar - w6Bar - w5Bar - w4Bar + w3Bar + w2Bar) / s; //sBar
    res[2] = lambdaAdj[0] * w10 * w10Bar + w1 * (RHO2 * w8Bar + w9Bar)
        - 0.5 * (f4 * f4Bar + f3 * f3Bar + f2 * f2Bar + f1 * f1Bar + e4 * e4Bar + e3 * e3Bar + e2 * e2Bar + e1 * e1Bar) / t; //tBar
    res[3] = sigmaSq * w1Bar + Math.log(s) * w11 * w11Bar + lambdaAdj[1] * lambdaBar + kappaAdj[1] * kappaBar; //gammaBar
    res[4] = (-w7Bar - w6Bar - w5Bar - w3Bar) / h; //hBar
    res[5] = (kappaAdj[0] * (-w14 * w14Bar + w12 * w12Bar) + 2 * (-w7Bar + w6Bar + w4Bar)) / x2; //x2bar
    res[6] = (kappaAdj[0] * (w14 * w14Bar + w13 * w13Bar) + 2 * (w7Bar + w5Bar) - w4Bar - w2Bar) / x1; //x1Bar
    res[7] = lambdaAdj[2] * lambdaBar; //rBar
    res[8] = w1Bar + lambdaAdj[3] * lambdaBar + kappaAdj[2] * kappaBar; //bBar
    res[9] = -(f4 * f4Bar + f3 * f3Bar + f2 * f2Bar + f1 * f1Bar + e4 * e4Bar + e3 * e3Bar + e2 * e2Bar + e1 * e1Bar) / sigma
        + 2 * sigma * ((gamma - 0.5) * w1Bar + lambdaAdj[4] * lambdaBar + kappaAdj[3] * kappaBar); //sigmaBar

    return res;
  }

  /**
   * Get beta and its sensitivity to r, b and sigma-squared
   * @param r
   * @param b
   * @param sigmaSq
   * @return length 4 array of beta and its sensitivity to r, b and sigma-squared
   */
  protected double[] getBetaAdjoint(final double r, final double b, final double sigmaSq) {
    final double[] res = new double[4];
    final double w1 = 0.5 - b / sigmaSq;
    final double w2 = 2 * r / sigmaSq;
    final double w3 = w1 * w1;
    final double w4 = w3 + w2;
    final double w5 = Math.sqrt(w4);
    final double beta = w1 + w5;

    final double w5Bar = 1.0;
    final double w4Bar = 0.5 / w5 * w5Bar;
    final double w3Bar = w4Bar;
    final double w2Bar = w4Bar;
    final double w1Bar = 1.0 + 2 * w1 * w3Bar;

    res[0] = beta;
    res[1] = 2 / sigmaSq * w2Bar; //rBar
    res[2] = -1 / sigmaSq * w1Bar; //bBar
    res[3] = b / sigmaSq / sigmaSq * w1Bar - w2 / sigmaSq * w2Bar;
    return res;
  }

  /**
   * get I1 and its sensitivity to k, r, b, sigma & t
   * @param k
   * @param r
   * @param b
   * @param sigma
   * @param t
   * @return length 6 array of  I1 and its sensitivity to k, r, b, sigma & t
   */
  protected double[] getI1Adjoint(final double k, final double r, final double b, final double sigma, final double t) {
    return getIAdjoint(k, r, b, sigma, t, true);
  }

  /**
   * get I2 and its sensitivity to k, r, b, sigma & t
   * @param k
   * @param r
   * @param b
   * @param sigma
   * @param t
   * @return length 6 array of  I2 and its sensitivity to k, r, b, sigma & t
   */
  protected double[] getI2Adjoint(final double k, final double r, final double b, final double sigma, final double t) {
    return getIAdjoint(k, r, b, sigma, t, false);
  }

  private double[] getIAdjoint(final double k, final double r, final double b, final double sigma, final double t, final boolean isT1) {

    final double sigmaSq = sigma * sigma;
    final double u = isT1 ? RHO2 * t : t;
    final double rootT = Math.sqrt(u);
    final double sigmaRootT = sigma * rootT;

    final double[] betaAdj = getBetaAdjoint(r, b, sigmaSq);
    final double zeta = (betaAdj[0]) / (betaAdj[0] - 1);
    final double bInf = zeta * k;
    final double z = r / (r - b);
    final double b0 = z < 1 ? k : k * z;
    final double w1 = -(b * u + 2 * sigmaRootT);
    final double w2 = bInf - b0;
    final double w3 = k * k / w2 / b0;
    final double w4 = w1 * w3; //h 
    final double w5 = Math.exp(w4);
    final double w6 = b0 + w2 * (1 - w5);

    final double w5Bar = -w2;
    final double w4Bar = w5 * w5Bar;
    final double w3Bar = w1 * w4Bar;
    final double w2Bar = (1 - w5) - w3 / w2 * w3Bar;
    final double w1Bar = w3 * w4Bar;
    final double b0Bar = 1.0 - w3 / b0 * w3Bar - w2Bar;
    final double bInfBar = w2Bar;
    final double zBar = z < 1 ? 0.0 : k * b0Bar;
    final double zetaBar = k * bInfBar;
    final double betaBar = (1 - zeta) / (betaAdj[0] - 1) * zetaBar;

    final double[] res = new double[6];
    res[0] = w6;
    res[1] = 2 * w3 / k * w3Bar + (z < 1 ? 1.0 : z) * b0Bar + zeta * bInfBar; //kBar
    res[2] = (1 - z) / (r - b) * zBar + betaAdj[1] * betaBar; //rBar
    res[3] = -u * w1Bar + z / (r - b) * zBar + betaAdj[2] * betaBar; //bBar
    res[4] = -2 * rootT * w1Bar + 2 * sigma * betaAdj[3] * betaBar; //sigmaBar
    res[5] = -(b + sigma / rootT) * w1Bar * (isT1 ? RHO2 : 1.0); //tBar

    return res;
  }

  /**
   * get I and its sensitivity to b0, bInf, k, b, sigma and t
   * @param b0
   * @param bInf
   * @param k
   * @param b
   * @param sigma
   * @param t
   * @return length 7 array of I and its sensitivity to b0, bInf, k, b, sigma and t
   */
  private double[] getIAdjoint(final double b0, final double bInf, final double k, final double b, final double sigma, final double t) {

    final double w1 = bInf - b0;
    final double w2 = b0 * w1;
    final double w3 = k * k;
    final double w4 = w3 / w2;
    final double w5 = Math.sqrt(t);
    final double w6 = b * t + 2 * sigma * w5;
    final double w7 = -w6 * w4; //h
    final double w8 = Math.exp(w7);
    final double w9 = 1 - w8;
    final double w10 = w1 * w9;
    final double w11 = b0 + w10; //I

    final double w10Bar = 1.0;
    final double w9Bar = w1 * w10Bar;
    final double w8Bar = -w9Bar;
    final double w7Bar = w8 * w8Bar;
    final double w6Bar = -w4;
    final double w5Bar = 2 * sigma * w6Bar;
    final double w4Bar = -w4 * w7Bar;
    final double w3Bar = 1 / w2 * w4Bar;
    final double w2Bar = -w4 / w2 * w4Bar;
    final double w1Bar = b0 * w2Bar + w9 * w10Bar;

    final double[] res = new double[7];
    res[0] = w11;
    res[1] = -w1Bar + w1 * w2Bar + 1.0; //b0Bar
    res[2] = w1Bar; //bInfbar
    res[3] = 2 * k * w3Bar; //kBar
    res[4] = t * w6Bar; //bBar
    res[5] = 2 * w6Bar * w5Bar; //sigmaBar
    res[6] = 0.5 / w5 * w5Bar + b * w6Bar; //tBar

    return res;
  }

}

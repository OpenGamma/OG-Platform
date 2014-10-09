/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.GenericImpliedVolatiltySolver;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * Class defining an analytical approximation for American option prices as
 * derived by Bjerksund and Stensland (2002).
 * <p>
 * The price of a call is given by: $$ \begin{align*} C = &\alpha_2 S^\beta - \alpha_2 \phi(S, t_1, \beta, I_2, I_2)\\ & + \phi(S, t_1, 1, I_2, I_2) - \phi(S, t_1, 1, I_1, I_2)\\ & - K\phi(S, t_1, 0,
 * I_2, I_2) + K\phi(S, t_1, 0, I_1, I_2)\\ & + \alpha_1 \phi(S, t_1, \beta, I_1, I_2) - \alpha_1 \psi(S, T, \beta, I_1, I_2, I_2, t_1)\\ & + \psi(S, T, 1, I_1, I2, I_1, t_1) - \psi(S, T, 1, K, I_2,
 * I_1, t_1)\\ & - K\psi(S, T, 0, I_1, I_2, I_1, t_1) + K\psi(S, T, 0, K, I_2, I_1, t_1) \end{align*} $$ where $$ \begin{align*} t_1 &= \frac{(\sqrt{5} - 1)T}{2}\\ I_1 &= B_0 + (B_\infty - B_0)(1 -
 * e^{h_1})\\ I_2 &= B_0 + (B_\infty - B_0)(1 - e^{h_2})\\ B_0 &= \frac{\beta K}{\beta - 1}\\ B_\infty &= \max\left(K, \frac{rK}{r-b}\right)\\ h_1 &= \frac{(bt_1 + 2\sigma\sqrt{t_1})K^2}{B_0(B_0 -
 * B_\infty)}\\ h_2 &= \frac{(bT + 2\sigma\sqrt{T})K^2}{B_0(B_0 - B_\infty)}\\ \alpha_1 &= (I_1 - K)I_1^{-\beta}\\ \alpha_2 &= (I_2 - K)I_2^{-\beta}\\ \beta &= \frac{1}{2} - \frac{b}{\sigma^2} +
 * \sqrt{\left(\frac{b}{\sigma^2} - \frac{1}{2}\right)^2 + \frac{2r}{\sigma^2}} \end{align*} $$ The function $\phi(S, T, \gamma, H, I)$ is defined as $$ \begin{align*} \phi(S, T, \gamma, H, I) &=
 * e^\lambda S^\gamma\left[N(-d_1) - \left(\frac{I}{S}\right)^\kappa N(-d_2)\right]\\ d_1 &= \frac{\ln(\frac{S}{H}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\ d_2 &=
 * \frac{\ln(\frac{I^2}{SH}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\ \lambda &= -r + \gamma b + \frac{\gamma(\gamma - 1)\sigma^2}{2}\\ \kappa &= \frac{2b}{\sigma^2} + 2\gamma + 1
 * \end{align*} $$ and the function $\psi(S, T, \gamma, H, I_2, I_1, t_1)$ is defined as $$ \begin{align*} \psi(S, T, \gamma, H, I_2, I_1, t_1) = &e^{\lambda T} S^\gamma\left[M(d_1, e_1, \rho)
 * -\left(\frac{I_2}{S}\right)^\kappa M(d_2, e_2, \rho) - \left(\frac{I_1}{S}\right)^\kappa M(d_3, e_3, -\rho) +\left(\frac{I_1}{I_2}\right)^\kappa M(d_4, e_4, \rho)\right] \end{align*} $$ where $$
 * \begin{align*} d_1 &= -\frac{\ln(\frac{S}{I_1}) + (b + (\gamma - \frac{1}{2})\sigma^2)t_1}{\sigma\sqrt{t_1}}\\ d_2 &= -\frac{\ln(\frac{I_2^2}{SI_1}) + (b + (\gamma -
 * \frac{1}{2})\sigma^2)t_1}{\sigma\sqrt{t_1}}\\ d_3 &= -\frac{\ln(\frac{S}{I_1}) - (b + (\gamma - \frac{1}{2})\sigma^2)t_1}{\sigma\sqrt{t_1}}\\ d_4 &= -\frac{\ln(\frac{I_2^2}{SI_1}) - (b + (\gamma -
 * \frac{1}{2})\sigma^2)t_1}{\sigma\sqrt{t_1}}\\ e_1 &= -\frac{\ln(\frac{S}{H}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\ e_2 &= -\frac{\ln(\frac{I_1^2}{SH}) + (b + (\gamma -
 * \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\ e_3 &= -\frac{\ln(\frac{I_2^2}{SH}) + (b + (\gamma - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}\\ e_4 &= -\frac{\ln(\frac{SI_1^2}{HI_2^2}) + (b + (\gamma -
 * \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}} \end{align*} $$ and $\rho = \sqrt{\frac{t_1}{T}}$ and $M(\cdot, \cdot, \cdot)$ is the CDF of the bivariate normal distribution (see
 * {@link com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution}).
 *
 * The price of puts is calculated using the Bjerksund-Stensland put-call transformation $p(S, K, T, r, b, \sigma) = c(K, S, T, r - b, -b, \sigma)$.
 * 
 */
public class BjerksundStenslandModel {
  private static final double SMALL = 1e-13;
  private static final double R_B_SMALL = 1e-7;

  private static final double RHO2 = 0.5 * (Math.sqrt(5) - 1);
  private static final double RHO = Math.sqrt(RHO2);
  private static final double RHO_STAR = Math.sqrt(1 - RHO2);
  private static final ProbabilityDistribution<double[]> BIVARIATE_NORMAL = new BivariateNormalDistribution();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Returns a pricing function.
   * @param definition The option definition, not null
   * @return The pricing function
   */
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final AmericanVanillaOptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        final ZonedDateTime date = data.getDate();
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(date);
        final double sigma = data.getVolatility(t, k);
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        if (!definition.isCall()) {
          if (s == 0) {
            return k;
          }
          return price(s, k, r, b, t, sigma, false);
        }
        return price(s, k, r, b, t, sigma, true);
      }
    };
    return pricingFunction;
  }

  /**
   * Get the price of an American option by the Bjerksund and Stensland (2002) approximation. We ensure that the price is the maximum of the no early excise (Black-Scholes price),
   * the immediate exercise value and the Bjerksund-Stensland approximation value
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param sigma The volatility
   * @param isCall true for calls
   * @return The American option price
   */
  public double price(final double s0, final double k, final double r, final double b, final double t, final double sigma, final boolean isCall) {

    final double fwd = s0 * Math.exp(b * t);
    final double df = Math.exp(-r * t);
    final double bsPrice = df * BlackFormulaRepository.price(fwd, k, t, sigma, isCall);
    double immediateExPrice = isCall ? Math.max(s0 - k, 0.0) : Math.max(k - s0, 0.0);

    // An American option price must be at least the maximum of immediate exercise and Black-Scholes price
    double lowBoundPrice = Math.max(immediateExPrice, bsPrice);

    // if the volatility is zero it is either optimal to exercise immediatly or wait til expiry
    if (sigma * Math.sqrt(t) < SMALL) {
      return lowBoundPrice;
    }

    if (isCall) {
      return Math.max(lowBoundPrice, getCallPrice(s0, k, r, b, t, sigma, bsPrice));
    }

    final double temp = (2 * b + sigma * sigma) / 2 / sigma;
    final double minR = b - 0.5 * temp * temp;
    // this does not give the best possible lower bound. Bjerksund-Stensland will give an answer for r < 0, but will fail for r < minR (complex beta)
    // TODO review the Bjerksund-Stensland formalisation to see if a general r < 0 (for puts) solution is possible
    if (r < minR) {
      return lowBoundPrice;
    }
    // put using put-call transformation
    return Math.max(lowBoundPrice, getCallPrice(k, s0, r - b, -b, t, sigma, bsPrice));
  }

  /**
   * Get the price of an American call option by the Bjerksund and Stensland (2002) approximation.
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param sigma The volatility
   * @param bsPrice Black-Scholes price
   * @return The American option price
   */
  protected double getCallPrice(final double s0, final double k, final double r, final double b, final double t, final double sigma, final double bsPrice) {

    if (b >= r) { // no early exercise in this case
      return bsPrice;
    }

    final double sigmaSq = sigma * sigma;

    final double t1 = RHO2 * t;
    double x1;
    double x2;
    double beta;
    double b0;
    final double y = 0.5 - b / sigmaSq;
    final double denom = Math.abs(r - b);
    if (denom < R_B_SMALL && y <= 1.0) {
      beta = 1.0;
      x1 = k * (1 + r * t1 + 2 * sigma * Math.sqrt(t1));
      x2 = k * (1 + r * t + 2 * sigma * Math.sqrt(t));
    } else {
      if (denom < R_B_SMALL) {
        b0 = k;
      } else {
        b0 = Math.max(k, r * k / denom);
      }
      final double arg = y * y + 2 * r / sigmaSq;
      ArgumentChecker.isTrue(arg >= 0, "beta is complex. Please check valueso of r & b"); // fail rather than propagate NaN
      beta = y + Math.sqrt(arg);
      final double bInfinity = beta * k / (beta - 1);
      final double h1 = getH(b, t1, sigma, k, b0, bInfinity);
      final double h2 = getH(b, t, sigma, k, b0, bInfinity);
      x1 = getX(b0, bInfinity, h1);
      x2 = getX(b0, bInfinity, h2);
    }

    final double alpha1 = getAlpha(x1, beta, k);
    final double alpha2 = getAlpha(x2, beta, k);
    return alpha2 * Math.pow(s0, beta) - alpha2 * getPhi(s0, t1, beta, x2, x2, r, b, sigma) + getPhi(s0, t1, 1, x2, x2, r, b, sigma) - getPhi(s0, t1, 1, x1, x2, r, b, sigma) - k *
        getPhi(s0, t1, 0, x2, x2, r, b, sigma) + k * getPhi(s0, t1, 0, x1, x2, r, b, sigma) + alpha1 * getPhi(s0, t1, beta, x1, x2, r, b, sigma) - alpha1 *
        getPsi(s0, t1, t, beta, x1, x2, x1, r, b, sigma) + getPsi(s0, t1, t, 1, x1, x2, x1, r, b, sigma) - getPsi(s0, t1, t, 1, k, x2, x1, r, b, sigma) - k *
        getPsi(s0, t1, t, 0, x1, x2, x1, r, b, sigma) + k * getPsi(s0, t1, t, 0, k, x2, x1, r, b, sigma);
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
    final double lambda = getLambda(gamma, r, b, sigmaSq);
    final double kappa = getKappa(gamma, b, sigmaSq);
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
    final double lambda = getLambda(gamma, r, b, sigmaSq);
    final double kappa = getKappa(gamma, b, sigmaSq);
    final double rho = Math.sqrt(t1 / t2);

    return Math.exp(lambda * t2) *
        Math.pow(s, gamma) *
        (BIVARIATE_NORMAL.getCDF(new double[] {d1, e1, rho }) - Math.pow(x2 / s, kappa) * BIVARIATE_NORMAL.getCDF(new double[] {d2, e2, rho }) - Math.pow(x1 / s, kappa) *
            BIVARIATE_NORMAL.getCDF(new double[] {d3, e3, -rho }) + Math.pow(x1 / x2, kappa) * BIVARIATE_NORMAL.getCDF(new double[] {d4, e4, -rho }));
  }

  private double getLambda(final double gamma, final double r, final double b, final double sigmaSq) {
    return -r + gamma * b + 0.5 * gamma * (gamma - 1) * sigmaSq;
  }

  private double getKappa(final double gamma, final double b, final double sigmaSq) {
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

  // **************
  // adjoint stuff

  /**
   * get the price and all the first order Greeks of an American option with the Bjerksund & Stensland (2002) approximation. 
   * These are:
   *<ul>
   *<li>spot delta</li>
   *<li>dual-delta (or strike-delta)</li>
   *<li>rho (sensitivity to risk-free rate). <b>Note:</b> This definition keeps b (cost-of-carry) fixed (which keeps the forward fixed)
   *rather than keeping the yield fixed</li>
   *<li>b-rho (sensitivity to cost-of-carry)</li>
   *<li>theta (sensitivity to the time-to-expiry). <b>Note:</b> This will have the opposite sign to some definitions.</li>
   *<li> vega (sensitivity to sigma)</li>
   *</ul>
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param sigma The volatility
   * @param isCall true for calls
   * @return length 7 arrays containing the price, then the sensitivities (Greeks): delta (spot), dual-delta (strike), rho (risk-free rate),
   * b-rho (cost-of-carry), theta (expiry), vega (sigma)
   */
  public double[] getPriceAdjoint(final double s0, final double k, final double r, final double b, final double t, final double sigma, final boolean isCall) {

    double bsmPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
    if (isCall) {
      return getCallPriceAdjoint(s0, k, r, b, t, sigma, bsmPrice);
    }
    return getPutPriceAdjoint(s0, k, r, b, t, sigma, bsmPrice);
  }


  /**
   * Get the option price, plus its delta and gamma. <b>Note</b> if a put is required, the gamma is found by divided difference on the delta. For a call both delta and gamma
   * are found by Algorithmic Differentiation.
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param sigma The volatility
   * @param isCall true for calls
   * @return length 3 array of price, delta and gamma
   */
  public double[] getPriceDeltaGamma(final double s0, final double k, final double r, final double b, final double t, final double sigma, final boolean isCall) {
    if (isCall) {
      final double[] deltaGamma = getCallDeltaGamma(s0, k, r, b, t, sigma);
      return deltaGammaModifier(s0, k, r, b, t, sigma, true, deltaGamma);
    }
    final double[] deltaGamma = getPutDeltaGamma(s0, k, r, b, t, sigma);
    return deltaGammaModifier(s0, k, r, b, t, sigma, false, deltaGamma);
  }

  private double[] deltaGammaModifier(final double s0, final double k, final double r, final double b, final double t, final double sigma, final boolean isCall, final double[] deltaGamma) {
    final double sign = isCall ? 1. : -1.;
    final double fwd = s0 * Math.exp(b * t);
    final double df = Math.exp(-r * t);
    final double bsPrice = df * BlackFormulaRepository.price(fwd, k, t, sigma, isCall);
    final double intrinsic = sign * (s0 - k);
    final boolean bsIsMin = intrinsic <= bsPrice;
    final double minPrice = bsIsMin ? bsPrice : intrinsic;
    if (deltaGamma[0] < minPrice) {
      final double[] res = new double[3];
      res[0] = minPrice;
      if (bsIsMin) {
        final double expbt = Math.exp(b * t);
        res[1] = expbt * df * BlackFormulaRepository.delta(fwd, k, t, sigma, isCall);
        res[2] = expbt * expbt * df * BlackFormulaRepository.gamma(fwd, k, t, sigma);
        return res;
      }
      res[1] = sign;
      res[2] = 0.0;
      return res;
    }
    return deltaGamma;
  }

  /**
   * Get the price and vega of an American option by the Bjerksund & Stensland (2002) approximation
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param sigma The volatility
   * @param isCall true for calls
   * @return length 2 arrays containing the price and vega
   */
  public double[] getPriceAndVega(final double s0, final double k, final double r, final double b, final double t, final double sigma, final boolean isCall) {
    final double[] temp = getPriceAdjoint(s0, k, r, b, t, sigma, isCall);
    return new double[] {temp[0], temp[6] }; // fairly wasteful to compute all the other Greeks
  }

  /**
   * Get a function for the price and vega of an American option by the Bjerksund & Stensland (2002) approximation in terms of the volatility (sigma).
   * This is primarily used by the GenericImpliedVolatiltySolver to find a (Bjerksund & Stensland) implied volatility for a given market price of an American option
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param isCall true for calls
   * @return A function from volatility (sigma) to price and vega
   */
  public Function1D<Double, double[]> getPriceAndVegaFunction(final double s0, final double k, final double r, final double b, final double t, final boolean isCall) {

    return new Function1D<Double, double[]>() {
      @Override
      public double[] evaluate(final Double sigma) {
        return getPriceAndVega(s0, k, r, b, t, sigma, isCall);
      }
    };
  }

  /**
   * Get the implied volatility according to the Bjerksund & Stensland (2002) approximation for the price of an American option quoted in the market. It is the number that put
   * into the Bjerksund & Stensland (2002) approximation gives the market price. <b>This is not the same as the Black implied volatility</b> (which is only applicable to
   * European options), although it may be numerically close.
   * @param price The market price of an American option
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param isCall true for calls
   * @return The (Bjerksund & Stensland (2002)) implied volatility.
   */
  public double impliedVolatility(final double price, final double s0, final double k, final double r, final double b, final double t, final boolean isCall) {
    final Function1D<Double, double[]> func = getPriceAndVegaFunction(s0, k, r, b, t, isCall);
    GenericImpliedVolatiltySolver solver = new GenericImpliedVolatiltySolver(func);
    return solver.impliedVolatility(price);
  }

  /**
   * Get the implied volatility according to the Bjerksund & Stensland (2002) approximation for the price of an American option quoted in the market. It is the number that put
   * into the Bjerksund & Stensland (2002) approximation gives the market price. <b>This is not the same as the Black implied volatility</b> (which is only applicable to
   * European options), although it may be numerically close.
   * @param price The market price of an American option
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param isCall true for calls
   * @param guess Inital guess of the volatility
   * @return The (Bjerksund & Stensland (2002)) implied volatility.
   */
  public double impliedVolatility(final double price, final double s0, final double k, final double r, final double b, final double t, final boolean isCall, final double guess) {
    final Function1D<Double, double[]> func = getPriceAndVegaFunction(s0, k, r, b, t, isCall);
    GenericImpliedVolatiltySolver solver = new GenericImpliedVolatiltySolver(func);
    return solver.impliedVolatility(price, guess);
  }

  protected double[] getCallPriceAdjoint(final double s0, final double k, final double r, final double b, final double t, final double sigma, double blackScholesMertonPrice) {

    // European option case
    if (b >= r) {
      return lowBoundPriceAdjoint(s0, k, r, b, t, sigma, true, blackScholesMertonPrice);
    }

    final double[] res = new double[7];

    final double[] x2Adj = getI2Adjoint(k, r, b, sigma, t);

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

    /*
     * In the case that either immediate excise or no early excise (i.e. the European price) is worth more than the price
     * calculated by this model, use the maximum value and calculate the adjoint as appropriate.
     */
    double lowerBoundPrice = Math.max(s0 - k, blackScholesMertonPrice);
    if (w9 < lowerBoundPrice) {
      return lowBoundPriceAdjoint(s0, k, r, b, t, sigma, true, blackScholesMertonPrice);
    }

    // backwards sweep
    // w3Bar to w9Bar = 1.0;
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

    final double x2Bar = psi5Adj[5] * psi5Bar + psi4Adj[5] * psi4Bar + psi3Adj[5] * psi3Bar + psi2Adj[5] * psi2Bar + psi1Adj[5] * psi1Bar + phi6Adj[5] * phi6Bar + phi5Adj[5] * phi5Bar +
        (phi4Adj[4] + phi4Adj[5]) * phi4Bar + +phi3Adj[5] * phi3Bar + (phi2Adj[4] + phi2Adj[5]) * phi2Bar + (phi1Adj[4] + phi1Adj[5]) * phi1Bar + alpha2Adj[2] * alpha2Bar;

    final double x1Bar = psi5Adj[6] * psi5Bar + (psi4Adj[4] + psi4Adj[6]) * psi4Bar + psi3Adj[6] * psi3Bar + (psi2Adj[4] + psi2Adj[6]) * psi2Bar + (psi1Adj[4] + psi1Adj[6]) * psi1Bar + phi6Adj[4] *
        phi6Bar + phi5Adj[4] * phi5Bar + phi3Adj[4] * phi3Bar + alpha1Adj[2] * alpha1Bar;

    final double betaBar = Math.log(s0) * w1 * w1Bar + psi1Adj[3] * psi1Bar + phi1Adj[3] * phi1Bar + phi6Adj[3] * phi6Bar + alpha2Bar * alpha2Adj[3] + alpha1Bar * alpha1Adj[3];

    final double sBar = betaAdj[0] * w1 / s0 * w1Bar + psi5Adj[1] * psi5Bar + psi4Adj[1] * psi4Bar + psi3Adj[1] * psi3Bar + psi2Adj[1] * psi2Bar + psi1Adj[1] * psi1Bar + phi6Adj[1] * phi6Bar +
        phi5Adj[1] * phi5Bar + phi4Adj[1] * phi4Bar + phi3Adj[1] * phi3Bar + phi2Adj[1] * phi2Bar + phi1Adj[1] * phi1Bar;

    final double kBar = -psi4Adj[0] + psi5Adj[0] - phi4Adj[0] + phi5Adj[0] + psi5Adj[4] * psi5Bar + psi3Adj[4] * psi3Bar + x2Adj[1] * x2Bar + x1Adj[1] * x1Bar + alpha1Adj[1] * alpha1Bar +
        alpha2Adj[1] * alpha2Bar;

    final double rBar = psi5Adj[7] * psi5Bar + psi4Adj[7] * psi4Bar + psi3Adj[7] * psi3Bar + psi2Adj[7] * psi2Bar + psi1Adj[7] * psi1Bar + phi6Adj[6] * phi6Bar + phi5Adj[6] * phi5Bar + phi4Adj[6] *
        phi4Bar + phi3Adj[6] * phi3Bar + phi2Adj[6] * phi2Bar + phi1Adj[6] * phi1Bar + x2Adj[2] * x2Bar + x1Adj[2] * x1Bar + betaAdj[1] * betaBar;

    final double bBar = psi5Adj[8] * psi5Bar + psi4Adj[8] * psi4Bar + psi3Adj[8] * psi3Bar + psi2Adj[8] * psi2Bar + psi1Adj[8] * psi1Bar + phi6Adj[7] * phi6Bar + phi5Adj[7] * phi5Bar + phi4Adj[7] *
        phi4Bar + phi3Adj[7] * phi3Bar + phi2Adj[7] * phi2Bar + phi1Adj[7] * phi1Bar + x2Adj[3] * x2Bar + x1Adj[3] * x1Bar + betaAdj[2] * betaBar;

    final double tBar = psi5Adj[2] * psi5Bar + psi4Adj[2] * psi4Bar + psi3Adj[2] * psi3Bar + psi2Adj[2] * psi2Bar + psi1Adj[2] * psi1Bar +
        (phi6Adj[2] * phi6Bar + phi5Adj[2] * phi5Bar + phi4Adj[2] * phi4Bar + phi3Adj[2] * phi3Bar + phi2Adj[2] * phi2Bar + phi1Adj[2] * phi1Bar) + x2Adj[5] * x2Bar + x1Adj[5] * x1Bar;

    final double sigmaBar = psi5Adj[9] * psi5Bar + psi4Adj[9] * psi4Bar + psi3Adj[9] * psi3Bar + psi2Adj[9] * psi2Bar + psi1Adj[9] * psi1Bar + phi6Adj[8] * phi6Bar + phi5Adj[8] * phi5Bar +
        phi4Adj[8] * phi4Bar + phi3Adj[8] * phi3Bar + phi2Adj[8] * phi2Bar + phi1Adj[8] * phi1Bar + x2Adj[4] * x2Bar + x1Adj[4] * x1Bar + 2 * sigma * betaAdj[3] * betaBar;

    res[0] = w9;
    res[1] = sBar;
    res[2] = kBar;
    res[3] = rBar;
    res[4] = bBar;
    res[5] = tBar;
    res[6] = sigmaBar;

    return res;
  }

  final double[] getPutPriceAdjoint(final double s0, final double k, final double r, final double b, final double t, final double sigma, double blackScholesMertonPrice) {

    // European option case
    if (0. >= r) {
      return lowBoundPriceAdjoint(s0, k, r, b, t, sigma, false, blackScholesMertonPrice);
    }

    final double temp = (2 * b + sigma * sigma) / 2 / sigma;
    final double minR = b - 0.5 * temp * temp;
    if (r <= minR) { // this will correspond to a complex beta - i.e. the model breaks down. The best we can do is return min price
      return lowBoundPriceAdjoint(s0, k, r, b, t, sigma, false, blackScholesMertonPrice);
    }

    final double fwd = s0 * Math.exp(b * t);
    final double df = Math.exp(-r * t);
    final double bsPrice = df * BlackFormulaRepository.price(fwd, k, t, sigma, false);
    final double minPrice = Math.max(k - s0, bsPrice);
    final double[] cAdjoint = getCallPriceAdjoint(k, s0, r - b, -b, t, sigma, blackScholesMertonPrice);
    if (cAdjoint[0] > minPrice) {
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
    return lowBoundPriceAdjoint(s0, k, r, b, t, sigma, false, blackScholesMertonPrice);
  }

  /**
   * In the case of immediate excise or no early excise (European price) the sensitivities should be handled separately
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param sigma The volatility
   * @param bsPrice
   * @param isCall true for calls
   * @param blackScholesMertonPrice The Black-Scholes-Merton price of a European option
   * @return length 7 arrays containing the price, then the sensitivities (Greeks): delta (spot), dual-delta (strike), rho (risk-free rate),
   * b-rho (cost-of-carry), theta (expiry), vega (sigma)
   */
  private double[] lowBoundPriceAdjoint(final double s0, final double k, final double r, final double b, final double t, final double sigma, boolean isCall, double blackScholesMertonPrice) {
    double immediateExPrice = isCall ? Math.max(s0 - k, 0.0) : Math.max(k - s0, 0.0);
    double[] res = new double[7];
    if (immediateExPrice >= blackScholesMertonPrice) {
      res[0] = immediateExPrice;
      res[1] = isCall ? 1.0 : -1.0;
      res[2] = -res[1];
    } else {
      double expbt = Math.exp(b * t);
      double fwd = s0 * expbt;
      double df = Math.exp(-r * t);
      res[0] = blackScholesMertonPrice;
      res[1] = expbt * df * BlackFormulaRepository.delta(fwd, k, t, sigma, isCall);
      res[2] = df * BlackFormulaRepository.dualDelta(fwd, k, t, sigma, isCall);
      res[3] = -t * blackScholesMertonPrice; // TODO review: This is the rho with b fixed (recall that b=r-q) - a standard rho would have fixed q (yield)
      res[4] = BlackScholesFormulaRepository.carryRho(s0, k, t, sigma, r, b, isCall);
      res[5] = -BlackScholesFormulaRepository.theta(s0, k, t, sigma, r, b, isCall);
      res[6] = df * BlackFormulaRepository.vega(fwd, k, t, sigma);
    }
    return res;
  }

  protected double[] getCallDeltaGamma(final double s0, final double k, final double r, final double b, final double t, final double sigma) {

    final double[] res = new double[3];
    // European option case
    if (b >= r) {
      final double expbt = Math.exp(b * t);
      final double fwd = s0 * expbt;
      final double df = Math.exp(-r * t);
      // TODO these calls have a lot of repeat calculations - could move in-house
      res[0] = df * BlackFormulaRepository.price(fwd, k, t, sigma, true);
      res[1] = expbt * df * BlackFormulaRepository.delta(fwd, k, t, sigma, true);
      res[2] = expbt * expbt * df * BlackFormulaRepository.gamma(fwd, k, t, sigma);
      return res;
    }

    final double sigmaSq = sigma * sigma;
    final double y = 0.5 - b / sigmaSq;
    final double beta = y + Math.sqrt(y * y + 2 * r / sigmaSq);
    final double b0 = Math.max(k, r * k / (r - b));
    final double bInfinity = beta * k / (beta - 1);
    final double h2 = getH(b, t, sigma, k, b0, bInfinity);
    final double x2 = getX(b0, bInfinity, h2);


    final double t1 = RHO2 * t;
    final double h1 = getH(b, t1, sigma, k, b0, bInfinity);
    final double x1 = getX(b0, bInfinity, h1);

    final double alpha1 = getAlpha(x1, beta, k);
    final double alpha2 = getAlpha(x2, beta, k);

    final double[] phi1Dot = getPhiDelta(s0, t, beta, x2, x2, r, b, sigma);
    final double[] phi2Dot = getPhiDelta(s0, t, 1.0, x2, x2, r, b, sigma);
    final double[] phi3Dot = getPhiDelta(s0, t, 1.0, x1, x2, r, b, sigma);
    final double[] phi4Dot = getPhiDelta(s0, t, 0.0, x2, x2, r, b, sigma);
    final double[] phi5Dot = getPhiDelta(s0, t, 0.0, x1, x2, r, b, sigma);
    final double[] phi6Dot = getPhiDelta(s0, t, beta, x1, x2, r, b, sigma);

    final double[] psi1Dot = getPsiDelta(s0, t, beta, x1, x2, x1, r, b, sigma);
    final double[] psi2Dot = getPsiDelta(s0, t, 1.0, x1, x2, x1, r, b, sigma);
    final double[] psi3Dot = getPsiDelta(s0, t, 1.0, k, x2, x1, r, b, sigma);
    final double[] psi4Dot = getPsiDelta(s0, t, 0.0, x1, x2, x1, r, b, sigma);
    final double[] psi5Dot = getPsiDelta(s0, t, 0.0, k, x2, x1, r, b, sigma);

    final double w1 = Math.pow(s0, beta);
    final double w1Dot = beta * w1 / s0;
    final double w1DDot = beta * (beta - 1) * w1 / s0 / s0;
    final double w2 = phi1Dot[0];
    final double w2Dot = phi1Dot[1];
    final double w2DDot = phi1Dot[2];
    final double w3 = alpha2 * (w1 - w2);
    final double w3Dot = alpha2 * (w1Dot - w2Dot);
    final double w3DDot = alpha2 * (w1DDot - w2DDot);
    final double w4 = phi2Dot[0] - phi3Dot[0];
    final double w4Dot = phi2Dot[1] - phi3Dot[1];
    final double w4DDot = phi2Dot[2] - phi3Dot[2];
    final double w5 = k * (-phi4Dot[0] + phi5Dot[0]);
    final double w5Dot = k * (-phi4Dot[1] + phi5Dot[1]);
    final double w5DDot = k * (-phi4Dot[2] + phi5Dot[2]);
    final double w6 = alpha1 * (phi6Dot[0] - psi1Dot[0]);
    final double w6Dot = alpha1 * (phi6Dot[1] - psi1Dot[1]);
    final double w6DDot = alpha1 * (phi6Dot[2] - psi1Dot[2]);
    final double w7 = psi2Dot[0] - psi3Dot[0];
    final double w7Dot = psi2Dot[1] - psi3Dot[1];
    final double w7DDot = psi2Dot[2] - psi3Dot[2];
    final double w8 = k * (-psi4Dot[0] + psi5Dot[0]);
    final double w8Dot = k * (-psi4Dot[1] + psi5Dot[1]);
    final double w8DDot = k * (-psi4Dot[2] + psi5Dot[2]);
    final double w9 = w3 + w4 + w5 + w6 + w7 + w8;
    final double w9Dot = w3Dot + w4Dot + w5Dot + w6Dot + w7Dot + w8Dot;
    final double w9DDot = w3DDot + w4DDot + w5DDot + w6DDot + w7DDot + w8DDot;

    res[0] = w9;
    res[1] = w9Dot;
    res[2] = w9DDot;
    return res;
  }

  /**
   * Get the put option price, plus its delta and gamma from dual-delta and dual-gamma of the call option by using the put-call transformation.
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param sigma The volatility
   * @return price, delta and gamma
   */
  protected double[] getPutDeltaGamma(final double s0, final double k, final double r, final double b, final double t, final double sigma) {

    return BjerksundStenslandModelDualDeltaGammaSolver.getCallDualDeltaGamma(k, s0, r - b, -b, t, sigma);

  }

  /**
   * access given for tests - expert use only
   * <p>
   * get alpha and its sensitivity to k, x (I) and beta
   * @param k strike
   * @param x I
   * @param beta beta
   * @return length 4 array of alpha and its sensitivity to k, x (I) and beta
   */
  protected double[] getAlphaAdjoint(final double k, final double x, final double beta) {

    final double w1 = Math.pow(x, -beta);
    final double w2 = x - k;
    final double w3 = w2 * w1;

    final double w2Bar = w1;
    final double w1Bar = w2;

    final double[] res = new double[4];
    res[0] = w3;
    res[1] = -w2Bar; // kBar
    res[2] = w2Bar - beta * w1 / x * w1Bar; // xBar
    res[3] = -Math.log(x) * w3; // betaBar

    return res;
  }

  /**
   * access given for tests - expert use only
   * <p>
   * Get lambda and its sensitivity to gamma, r, b and sigma-squared
   * @param gamma If this is set to 0 or 1, then the gamma sensitivity should be ignored
   * @param r risk-free rate
   * @param b cost-of-carry
   * @param sigmaSq volatility squared
   * 
   * @return length 5 array of lambda and its sensitivity to gamma, r, b and sigma-squared
   */
  protected double[] getLambdaAdjoint(final double gamma, final double r, final double b, final double sigmaSq) {
    final double[] res = new double[5];
    final double temp = 0.5 * gamma * (gamma - 1);
    res[0] = -r + gamma * b + temp * sigmaSq; // lambda
    res[1] = b + (gamma - 0.5) * sigmaSq; // gammaBar
    res[2] = -1.0; // rBar
    res[3] = gamma; // bBar
    res[4] = temp; // sigmasqBar
    return res;
  }

  /**
   * access given for tests - expert use only
   * <p>
   * Get kappa and its sensitivity to gamma, b and sigma-squared
   * @param gamma If this is set to 0 or 1, then the gamma sensitivity should be ignored
   * @param b cost-of-carry
   * @param sigmaSq volatility squared
   * @return length 4 array of kappa and its sensitivity to gamma, b and sigma-squared
   */
  protected double[] getKappaAdjoint(final double gamma, final double b, final double sigmaSq) {
    final double[] res = new double[4];
    final double temp = 2 * b / sigmaSq;
    res[0] = temp + 2 * gamma - 1;
    res[1] = 2.0; // gammaBar
    res[2] = 2 / sigmaSq; // bBar
    res[3] = -temp / sigmaSq; // sigmasqBar
    return res;
  }

  /**
   * access given for tests - expert use only
   * <p>
   * get phi and its sensitivity to s, t, gamma, h, x (I), r, b & sigma
   * @param s spot
   * @param t expiry
   * @param gamma If this is set to 0 or 1, then the gamma sensitivity should be ignored
   * @param h H
   * @param x I
   * @param r risk-free rate
   * @param b cost-of-carry
   * @param sigma volatility
   * @return length 9 array of phi and its sensitivity to s, t, gamma, h, x (I), r, b & sigma
   */
  protected double[] getPhiAdjoint(final double s, final double t, final double gamma, final double h, final double x, final double r, final double b, final double sigma) {

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
    final double w6 = w4 / sigmaRootT; // d
    final double w7 = w5 / sigmaRootT; // d2
    final double w8 = NORMAL.getCDF(-w6); // N(-d);
    final double w9 = NORMAL.getCDF(-w7); // N(-d2);
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
    // final double w0Bar = t1 * w1Bar;

    final double[] res = new double[9];
    final double lammbaBar = t1 * w14; // w14 == w11*w11Bar
    final double kappaBar = Math.log(x / s) * w10 * w10Bar;

    res[0] = w14; // phi
    res[1] = (gamma * w12 * w12Bar - kappaAdj[0] * w10 * w10Bar - w3Bar + w2Bar) / s; // sBar
    res[2] = RHO2 * (w0 * w1Bar + lambdaAdj[0] * w11 * w11Bar - 0.5 / t1 * (w7 * w7Bar + w6 * w6Bar)); // tBar
    res[3] = Math.log(s) * w12 * w12Bar + sigmaSq * t1 * w1Bar + lambdaAdj[1] * lammbaBar + kappaAdj[1] * kappaBar; // gammaBar
    res[4] = -(w2Bar + w3Bar) / h; // hBar
    res[5] = (2 * w3Bar + kappaAdj[0] * w10 * w10Bar) / x; // xBar
    res[6] = lambdaAdj[2] * lammbaBar; // rBar
    res[7] = t1 * w1Bar + lambdaAdj[3] * lammbaBar + kappaAdj[2] * kappaBar; // bBar
    res[8] = 2 * sigma * ((gamma - 0.5) * t1 * w1Bar + lambdaAdj[4] * lammbaBar + kappaAdj[3] * kappaBar) - (w6 * w6Bar + w7 * w7Bar) / sigma; // sigmaBar

    return res;
  }

  /**
   * @param s The spot
   * @param t The time to expiry
   * @param gamma gamma
   * @param h h
   * @param x x
   * @param r The interest rate
   * @param b The cost-of-carry
   * @param sigma The volatility
   * @return The phi delta array
   */
  protected double[] getPhiDelta(final double s, final double t, final double gamma, final double h, final double x, final double r, final double b, final double sigma) {

    final double t1 = RHO2 * t;
    final double sigmaSq = sigma * sigma;
    final double sigmaRootT = sigma * Math.sqrt(t1);

    final double lambda = -r + gamma * b + 0.5 * gamma * (gamma - 1) * sigmaSq; // lambda
    final double kappa = 2 * b / sigmaSq + 2 * gamma - 1;

    final double w0 = (b + (gamma - 0.5) * sigmaSq);
    final double w1 = w0 * t1;
    final double w2 = Math.log(s / h);
    final double w2Dot = 1 / s;
    final double w2DDot = -w2Dot * w2Dot;
    final double w3 = Math.log(x * x / s / h);
    final double w3Dot = -w2Dot;
    final double w3DDot = -w2DDot;
    final double w4 = w2 + w1;
    final double w4Dot = w2Dot;
    final double w4DDot = w2DDot;
    final double w5 = w3 + w1;
    final double w5Dot = w3Dot;
    final double w5DDot = w3DDot;
    final double w6 = w4 / sigmaRootT; // d
    final double w6Dot = w4Dot / sigmaRootT;
    final double w6DDot = w4DDot / sigmaRootT;
    final double w7 = w5 / sigmaRootT; // d2
    final double w7Dot = w5Dot / sigmaRootT;
    final double w7DDot = w5DDot / sigmaRootT;
    final double w8 = NORMAL.getCDF(-w6); // N(-d);
    final double nd = NORMAL.getPDF(w6);
    final double w8Dot = -nd * w6Dot;
    final double w8DDot = nd * (w6 * w6Dot * w6Dot - w6DDot);
    final double w9 = NORMAL.getCDF(-w7); // N(-d2);
    final double nd2 = NORMAL.getPDF(w7);
    final double w9Dot = -nd2 * w7Dot;
    final double w9DDot = nd2 * (w7 * w7Dot * w7Dot - w7DDot);
    final double w10 = Math.pow(x / s, kappa);
    final double w10Dot = -kappa * w10 / s;
    final double w10DDot = kappa * (kappa + 1) * w10 / s / s;
    final double w11 = Math.exp(lambda * t1);
    final double w12 = Math.pow(s, gamma);
    final double w12Dot = gamma * w12 / s;
    final double w12DDot = gamma * (gamma - 1) * w12 / s / s;
    final double w13 = w8 - w10 * w9;
    final double w13Dot = w8Dot - w9 * w10Dot - w10 * w9Dot;
    final double w13DDot = w8DDot - w10 * w9DDot - w9 * w10DDot - 2 * w9Dot * w10Dot;
    final double w14 = w11 * w12 * w13;
    final double w14Dot = w11 * (w12 * w13Dot + w12Dot * w13);
    final double w14DDot = w11 * (w12 * w13DDot + w13 * w12DDot + 2 * w12Dot * w13Dot);

    final double[] res = new double[3];
    res[0] = w14;
    res[1] = w14Dot;
    res[2] = w14DDot;
    return res;
  }

  /**
   * access given for tests - expert use only
   * <p>
   * get Psi and its sensitivity to s, t, gamma, h, x2, x1, r, b and sigma
   * @param s spot
   * @param t expiry
   * @param gamma If this is set to 0 or 1, then the gamma sensitivity should be ignored
   * @param h H
   * @param x2 I2
   * @param x1 I1
   * @param r risk-free rate
   * @param b cost-of-carry
   * @param sigma volatility
   * @return array of length 10 of Psi and its sensitivity to s, t, gamma, h, x2, x1, r, b and sigma
   */
  protected double[] getPsiAdjoint(final double s, final double t, final double gamma, final double h, final double x2, final double x1, final double r, final double b, final double sigma) {

    // TODO all of this could be pre-calculated
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

    // backwards sweep
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
    res[0] = w20; // Psi
    res[1] = (-kappaAdj[0] * (w13 * w13Bar + w12 * w12Bar) + gamma * w11 * w11Bar + w7Bar - w6Bar - w5Bar - w4Bar + w3Bar + w2Bar) / s; // sBar
    res[2] = lambdaAdj[0] * w10 * w10Bar + w1 * (RHO2 * w8Bar + w9Bar) - 0.5 * (f4 * f4Bar + f3 * f3Bar + f2 * f2Bar + f1 * f1Bar + e4 * e4Bar + e3 * e3Bar + e2 * e2Bar + e1 * e1Bar) / t; // tBar
    res[3] = sigmaSq * w1Bar + Math.log(s) * w11 * w11Bar + lambdaAdj[1] * lambdaBar + kappaAdj[1] * kappaBar; // gammaBar
    res[4] = (-w7Bar - w6Bar - w5Bar - w3Bar) / h; // hBar
    res[5] = (kappaAdj[0] * (-w14 * w14Bar + w12 * w12Bar) + 2 * (-w7Bar + w6Bar + w4Bar)) / x2; // x2bar
    res[6] = (kappaAdj[0] * (w14 * w14Bar + w13 * w13Bar) + 2 * (w7Bar + w5Bar) - w4Bar - w2Bar) / x1; // x1Bar
    res[7] = lambdaAdj[2] * lambdaBar; // rBar
    res[8] = w1Bar + lambdaAdj[3] * lambdaBar + kappaAdj[2] * kappaBar; // bBar
    res[9] = -(f4 * f4Bar + f3 * f3Bar + f2 * f2Bar + f1 * f1Bar + e4 * e4Bar + e3 * e3Bar + e2 * e2Bar + e1 * e1Bar) / sigma + 2 * sigma *
        ((gamma - 0.5) * w1Bar + lambdaAdj[4] * lambdaBar + kappaAdj[3] * kappaBar); // sigmaBar

    return res;
  }

  /**
   * @param s The spot
   * @param t The time to expiry
   * @param gamma gamma
   * @param h h
   * @param x2 x2
   * @param x1 x1
   * @param r The interest rate
   * @param b The cost-of-carry
   * @param sigma The volatility
   * @return The array of psi delta
   */
  protected double[] getPsiDelta(final double s, final double t, final double gamma, final double h, final double x2, final double x1, final double r, final double b, final double sigma) {

    // TODO all of this could be precalculated
    final double rootT = Math.sqrt(t);
    final double sigmarootT = sigma * rootT;
    final double t1 = RHO2 * t;
    final double rootT1 = RHO * rootT;
    final double sigmarootT1 = sigma * rootT1;
    final double sigmaSq = sigma * sigma;
    final double lambda = getLambda(gamma, r, b, sigmaSq);
    final double kappa = getKappa(gamma, b, sigmaSq);
    final double invS = 1 / s;
    final double invS2 = invS * invS;

    final double w1 = b + (gamma - 0.5) * sigmaSq;
    final double w2 = Math.log(s / x1);
    final double w3 = Math.log(s / h);
    final double w4 = Math.log(x2 * x2 / s / x1);
    final double w5 = Math.log(x1 * x1 / s / h);
    final double w6 = Math.log(x2 * x2 / s / h);
    final double w7 = Math.log(s * x1 * x1 / h / x2 / x2);
    final double w8 = w1 * t1;
    final double w9 = w1 * t;
    final double w10 = Math.exp(lambda * t);
    final double w11 = Math.pow(s, gamma);
    final double w11Dot = gamma * w11 * invS;
    final double w11DDot = gamma * (gamma - 1) * w11 * invS2;
    final double w12 = Math.pow(x2 / s, kappa);
    final double w12Dot = -kappa * w12 * invS;
    final double w12DDot = kappa * (kappa + 1) * w12 * invS2;
    final double w13 = Math.pow(x1 / s, kappa);
    final double w13Dot = -kappa * w13 * invS;
    final double w13DDot = kappa * (kappa + 1) * w13 * invS2;
    final double w14 = Math.pow(x1 / x2, kappa);

    final double blah1 = invS / sigmarootT1;
    final double blah2 = blah1 * invS;
    final double e1 = -(w2 + w8) / sigmarootT1;
    final double e1Dot = -blah1;
    final double e1DDot = blah2;
    final double e2 = -(w4 + w8) / sigmarootT1;
    final double e2Dot = blah1;
    final double e2DDot = -blah2;
    final double e3 = -(w2 - w8) / sigmarootT1;
    final double e3Dot = -blah1;
    final double e3DDot = blah2;
    final double e4 = -(w4 - w8) / sigmarootT1;
    final double e4Dot = blah1;
    final double e4DDot = -blah2;

    final double blah3 = invS / sigmarootT;
    final double blah4 = blah3 * invS;
    final double f1 = -(w3 + w9) / sigmarootT;
    final double f1Dot = -blah3;
    final double f1DDot = blah4;
    final double f2 = -(w6 + w9) / sigmarootT;
    final double f2Dot = blah3;
    final double f2DDot = -blah4;
    final double f3 = -(w5 + w9) / sigmarootT;
    final double f3Dot = blah3;
    final double f3DDot = -blah4;
    final double f4 = -(w7 + w9) / sigmarootT;
    final double f4Dot = -blah3;
    final double f4DDot = blah4;

    final double w15 = BIVARIATE_NORMAL.getCDF(new double[] {e1, f1, RHO });
    double[] temp = bivariateNormDiv(e1, f1, true);
    final double w15Dot = temp[0] * e1Dot + temp[1] * f1Dot;
    final double w15DDot = temp[0] * e1DDot + temp[1] * f1DDot + temp[2] * e1Dot * e1Dot + temp[3] * f1Dot * f1Dot + 2 * temp[4] * e1Dot * f1Dot;
    final double w16 = BIVARIATE_NORMAL.getCDF(new double[] {e2, f2, RHO });
    temp = bivariateNormDiv(e2, f2, true);
    final double w16Dot = temp[0] * e2Dot + temp[1] * f2Dot;
    final double w16DDot = temp[0] * e2DDot + temp[1] * f2DDot + temp[2] * e2Dot * e2Dot + temp[3] * f2Dot * f2Dot + 2 * temp[4] * e2Dot * f2Dot;
    final double w17 = BIVARIATE_NORMAL.getCDF(new double[] {e3, f3, -RHO });
    temp = bivariateNormDiv(e3, f3, false);
    final double w17Dot = temp[0] * e3Dot + temp[1] * f3Dot;
    final double w17DDot = temp[0] * e3DDot + temp[1] * f3DDot + temp[2] * e3Dot * e3Dot + temp[3] * f3Dot * f3Dot + 2 * temp[4] * e3Dot * f3Dot;
    final double w18 = BIVARIATE_NORMAL.getCDF(new double[] {e4, f4, -RHO });
    temp = bivariateNormDiv(e4, f4, false);
    final double w18Dot = temp[0] * e4Dot + temp[1] * f4Dot;
    final double w18DDot = temp[0] * e4DDot + temp[1] * f4DDot + temp[2] * e4Dot * e4Dot + temp[3] * f4Dot * f4Dot + 2 * temp[4] * e4Dot * f4Dot;

    final double w19 = w15 - w12 * w16 - w13 * w17 + w14 * w18;
    final double w19Dot = w15Dot - w12 * w16Dot - w16 * w12Dot - w13 * w17Dot - w17 * w13Dot + w14 * w18Dot;
    final double w19DDot = w15DDot - w12 * w16DDot - w16 * w12DDot - 2 * w12Dot * w16Dot - w13 * w17DDot - w17 * w13DDot - 2 * w13Dot * w17Dot + w14 * w18DDot;

    final double w20 = w10 * w11 * w19;
    final double w20Dot = w10 * (w19 * w11Dot + w11 * w19Dot);
    final double w20DDot = w10 * (w19 * w11DDot + w11 * w19DDot + 2 * w11Dot * w19Dot);

    return new double[] {w20, w20Dot, w20DDot };
  }

  /**
   * access given for tests - expert use only
   * <p>
   * Get the first and second derivatives of the bi-variate normal with repect to a and b (rho is fixed)
   * @param a first cooridinate
   * @param b second cooridinate
   * @param posRho true if RHO used, false is -RHO used
   * @return array of length 5 in order, dB/da, dB/db, d^2B/da^2, d^2B/db^2, d^2B/dadb
   */
  protected double[] bivariateNormDiv(final double a, final double b, final boolean posRho) {

    final double rho = posRho ? RHO : -RHO;

    final double na = NORMAL.getPDF(a);
    final double nb = NORMAL.getPDF(b);
    final double x1 = (b - rho * a) / RHO_STAR;
    final double x2 = (a - rho * b) / RHO_STAR;
    final double nx1 = NORMAL.getPDF(x1);
    final double nx2 = NORMAL.getPDF(x2);
    final double cnx1 = NORMAL.getCDF(x1);
    final double cnx2 = NORMAL.getCDF(x2);
    final double[] res = new double[5];
    res[0] = na * cnx1; // dB/da
    res[1] = nb * cnx2; // dB/db
    res[2] = -na * (a * cnx1 + rho / RHO_STAR * nx1); // d^2B/da^2
    res[3] = -nb * (b * cnx2 + rho / RHO_STAR * nx2); // d^2B/db^2
    res[4] = na * nx1 / RHO_STAR;
    return res;
  }

  /**
   * access given for tests - expert use only
   * <p>
   * Get beta and its sensitivity to r, b and sigma-squared
   * @param r risk-free rate
   * @param b cost-of-carry
   * @param sigmaSq volatility squared
   * @return length 4 array of beta and its sensitivity to r, b and sigma-squared
   */
  protected double[] getBetaAdjoint(final double r, final double b, final double sigmaSq) {
    final double[] res = new double[4];
    final double w1 = 0.5 - b / sigmaSq;
    final double w2 = 2 * r / sigmaSq;
    final double w3 = w1 * w1;
    final double w4 = w3 + w2;
    if (w4 < 0) {
      throw new MathException("beta will be complex (see Jira PLAT-2944)");
    }
    final double w5 = Math.sqrt(w4);
    final double beta = w1 + w5;

    final double w5Bar = 1.0;
    final double w4Bar = 0.5 / w5 * w5Bar;
    final double w3Bar = w4Bar;
    final double w2Bar = w4Bar;
    final double w1Bar = 1.0 + 2 * w1 * w3Bar;

    res[0] = beta;
    res[1] = 2 / sigmaSq * w2Bar; // rBar
    res[2] = -1 / sigmaSq * w1Bar; // bBar
    res[3] = b / sigmaSq / sigmaSq * w1Bar - w2 / sigmaSq * w2Bar;
    return res;
  }

  /**
   * access given for tests - expert use only
   * <p>
   * get I1 and its sensitivity to k, r, b, sigma & t
   * @param k strike
   * @param r risk-free rate
   * @param b cost-of-carry
   * @param sigma volatility
   * @param t expiry
   * @return length 6 array of I1 and its sensitivity to k, r, b, sigma & t
   */
  protected double[] getI1Adjoint(final double k, final double r, final double b, final double sigma, final double t) {
    return getIAdjoint(k, r, b, sigma, t, true);
  }

  /**
   * access given for tests - expert use only
   * <p>
   * get I2 and its sensitivity to k, r, b, sigma & t
   * @param k strike
   * @param r risk-free rate
   * @param b cost-of-carry
   * @param sigma volatility
   * @param t expiry
   * 
   * @return length 6 array of I2 and its sensitivity to k, r, b, sigma & t
   */
  protected double[] getI2Adjoint(final double k, final double r, final double b, final double sigma, final double t) {
    return getIAdjoint(k, r, b, sigma, t, false);
  }

  private double[] getIAdjoint(final double k, final double r, final double b, final double sigma, final double t, final boolean isT1) {

    final double sigmaSq = sigma * sigma;
    final double u = isT1 ? RHO2 * t : t;
    final double rootT = Math.sqrt(u);
    final double sigmaRootT = sigma * rootT;

    double z;
    final double[] res = new double[6];
    // should always have r >= b - this stops problems with tests using divided difference
    final double denom = Math.abs(r - b);
    final boolean close;
    if (denom < R_B_SMALL) {
      if (b >= -sigmaSq / 2) {
        final double w1 = r * u + 2 * sigmaRootT;
        res[0] = k * (1 + w1);
        res[1] = 1 + w1;
        res[2] = -k * w1 * w1 / 2 / (b + sigmaSq / 2);
        res[3] = k * u - res[2];
        res[4] = 2 * rootT * k;
        res[5] = k * (b + sigma / rootT) * (isT1 ? RHO2 : 1.0);
        ;
        return res;
      }
      z = 1;
      close = true;
    } else {
      z = r / denom;
      close = false;
    }

    final double[] betaAdj = getBetaAdjoint(r, b, sigmaSq);
    final double zeta = (betaAdj[0]) / (betaAdj[0] - 1);
    final double bInf = zeta * k;
    final double b0 = z < 1 ? k : k * z;
    final double w1 = -(b * u + 2 * sigmaRootT);
    final double w2 = bInf - b0;
    final double w3 = k * k / w2 / b0;
    final double w4 = w1 * w3; // h
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

    final double temp = (close ? 0.0 : (1 - z) / (r - b) * zBar);
    res[0] = w6;
    res[1] = 2 * w3 / k * w3Bar + (z < 1 ? 1.0 : z) * b0Bar + zeta * bInfBar; // kBar
    res[2] = temp + betaAdj[1] * betaBar; // rBar
    res[3] = -u * w1Bar + (close ? 0.0 : z / (r - b) * zBar) + betaAdj[2] * betaBar; // bBar
    res[4] = -2 * rootT * w1Bar + 2 * sigma * betaAdj[3] * betaBar; // sigmaBar
    res[5] = -(b + sigma / rootT) * w1Bar * (isT1 ? RHO2 : 1.0); // tBar

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
  @SuppressWarnings("unused")
  private double[] getIAdjoint(final double b0, final double bInf, final double k, final double b, final double sigma, final double t) {

    final double w1 = bInf - b0;
    final double w2 = b0 * w1;
    final double w3 = k * k;
    final double w4 = w3 / w2;
    final double w5 = Math.sqrt(t);
    final double w6 = b * t + 2 * sigma * w5;
    final double w7 = -w6 * w4; // h
    final double w8 = Math.exp(w7);
    final double w9 = 1 - w8;
    final double w10 = w1 * w9;
    final double w11 = b0 + w10; // I

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
    res[1] = -w1Bar + w1 * w2Bar + 1.0; // b0Bar
    res[2] = w1Bar; // bInfbar
    res[3] = 2 * k * w3Bar; // kBar
    res[4] = t * w6Bar; // bBar
    res[5] = 2 * w6Bar * w5Bar; // sigmaBar
    res[6] = 0.5 / w5 * w5Bar + b * w6Bar; // tBar

    return res;
  }

}

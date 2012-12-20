/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;

/**
 * The Jarrow-Rudd option pricing formula extends the Black-Scholes-Merton
 * model for non-normal skewness and kurtosis in the underlying price
 * distribution.
 * <p>
 * The price of a call option is given by:
 * $$
 * \begin{align*}
 * c = c_{BSM} + \lambda_1 Q_3 + \lambda_2 Q_4
 * \end{align*}
 * $$
 * $c_{BSM}$ is the Black-Scholes-Merton call price (see {@link BlackScholesMertonModel}) and
 * $$
 * \begin{align*}
 * d_1 &= \frac{\ln(\frac{S}{K} + (r + \frac{\sigma^2}{2})T}{\sigma\sqrt{T}}\\
 * d_2 &= d_1 - \sigma\sqrt{T}\\
 * Q_3 &= -\frac{(Se^{-rT})^3(e^{\sigma^2 T} - 1)^{\frac{3}{2}} e^{-rT}}{6}\frac{da(X)}{dS}\\
 * Q_4 &= \frac{(Se^{-rT})^4(e^{\sigma^2 T} - 1)^2 e^{-rT}}{24}\frac{d^2a(X)}{dS^2}\\
 * \lambda_1 &= \gamma_1(F) - \gamma_1(A)\\
 * \lambda_2 &= \gamma_2(F) - \gamma_2(A)\\
 * a(X) &= \frac{e^{-\frac{d_2^2}{2}}}{2\pi K\sigma\sqrt{T}}
 * \end{align*}
 * $$
 * The skewness ($\gamma_1(A)$) and kurtosis ($\gamma_2(A)$) of a lognormal
 * distribution are $\gamma_1 = 3y + y^3$ and
 * $\gamma_2 = 16y^2 + 15y^4 + 6y^6 * + y^8$ where $y = \sqrt{e^{\sigma^2 T} - 1}$.
 * $\gamma_1(F)$ are $\gamma_2(F)$ are the observed skewness and kurtosis of
 * the underlying price distribution.
 * <p>
 * Put options are priced using put-call parity.
 */
public class JarrowRuddSkewnessKurtosisModel extends AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> {
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<SkewKurtosisOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<SkewKurtosisOptionDataBundle, Double> pricingFunction = new Function1D<SkewKurtosisOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final SkewKurtosisOptionDataBundle data) {
        Validate.notNull(data);
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double sigma = data.getVolatility(t, k);
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        final double skew = data.getAnnualizedSkew();
        final double kurtosis = data.getAnnualizedPearsonKurtosis();
        final OptionDefinition callDefinition = definition.isCall() ? definition : new EuropeanVanillaOptionDefinition(k, definition.getExpiry(), true);
        final Function1D<StandardOptionDataBundle, Double> bsm = BSM.getPricingFunction(callDefinition);
        final double bsmCall = bsm.evaluate(data);
        final double d2 = getD2(getD1(s, k, t, sigma, b), sigma, t);
        final double sigmaT = sigma * Math.sqrt(t);
        final double a = getA(d2, k, sigmaT);
        final double call = bsmCall + getLambda1(sigma, t, skew) * getQ3(s, k, sigmaT, t, r, a, d2) + getLambda2(sigma, t, kurtosis) * getQ4(s, k, sigmaT, t, r, a, d2);
        if (!definition.isCall()) {
          return call - s * Math.exp((b - r) * t) + k * Math.exp(-r * t);
        }
        return call;
      }
    };
    return pricingFunction;
  }

  private double getA(final double d2, final double k, final double sigmaT) {
    return Math.exp(-d2 * d2 / 2.) / k / sigmaT / Math.sqrt(2 * Math.PI);
  }

  private double getLambda1(final double sigma, final double t, final double skew) {
    final double q = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    final double skewDistribution = q * (3 + q * q);
    return skew - skewDistribution;
  }

  private double getLambda2(final double sigma, final double t, final double kurtosis) {
    final double q = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    final double q2 = q * q;
    final double q4 = q2 * q2;
    final double q6 = q4 * q2;
    final double q8 = q6 * q2;
    final double kurtosisDistribution = 16 * q2 + 15 * q4 + 6 * q6 + q8 + 3;
    return kurtosis - kurtosisDistribution;
  }

  private double getQ3(final double s, final double k, final double sigmaT, final double t, final double r, final double a, final double d2) {
    final double da = a * (d2 - sigmaT) / (k * sigmaT);
    final double df = Math.exp(-r * t);
    return -Math.pow(s * df, 3) * Math.pow(Math.exp(sigmaT * sigmaT - 1), 1.5) * df * da / 6.;
  }

  private double getQ4(final double s, final double k, final double sigmaT, final double t, final double r, final double a, final double d2) {
    final double sigmaTSq = sigmaT * sigmaT;
    final double x = d2 - sigmaT;
    final double da2 = a * (x * x - sigmaT * x - 1) / (k * k * sigmaTSq);
    final double df = Math.exp(-r * t);
    return Math.pow(s * df, 4) * Math.pow(Math.exp(sigmaTSq) - 1, 2) * df * da2 / 24.;
  }
}

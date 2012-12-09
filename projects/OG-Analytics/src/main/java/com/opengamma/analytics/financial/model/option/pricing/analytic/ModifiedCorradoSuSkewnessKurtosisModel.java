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
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * The Corrado-Su option pricing formula extends the Black-Scholes-Merton model
 * for non-normal skewness and kurtosis in the underlying return distribution.
 * <p>
 * The price of a call option is given by:
 * $$
 * \begin{align*}
 * c = c_{BSM} + \mu_3 Q_3 + (\mu_4 - 3) Q_4
 * \end{align*}
 * $$
 * where $c_{BSM}$ is the Black-Scholes-Merton call price (see {@link BlackScholesMertonModel}),
 * $\mu_3$ is the skewness, $\mu_4$ is the Pearson kurtosis and
 * $$
 * \begin{align*}
 * Q_3 &= \frac{S\sigma\sqrt{T}(2\sigma\sqrt{T} - d)n(d)}{6(1 + w)}\\
 * Q_4 &= \frac{S\sigma\sqrt{T}(d^2 - 3d\sigma\sqrt{T} + 3\sigma^2T - 1)n(d)}{24(1 + w)}\\
 * d &= \frac{\ln(\frac{S}{K}) + (b + \frac{\sigma^2}{2})T - \ln(1 + w)}{\sigma\sqrt{T}}\\
 * w &= \frac{\mu_3 \sigma^3 T^{\frac{3}{2}}}{6} + \frac{\mu_4 \sigma^4 T^2}{24}
 * \end{align*}
 * $$
 * Put options are priced using put-call parity.
 */
public class ModifiedCorradoSuSkewnessKurtosisModel extends AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> {
  /** The Black-Scholes Merton model */
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();
  /** The normal distribution */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

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
        final double kurtosis = data.getAnnualizedFisherKurtosis();
        final double sigmaT = sigma * Math.sqrt(t);
        OptionDefinition callDefinition = definition;
        if (!definition.isCall()) {
          callDefinition = new EuropeanVanillaOptionDefinition(callDefinition.getStrike(), callDefinition.getExpiry(), true);
        }
        final Function1D<StandardOptionDataBundle, Double> bsm = BSM.getPricingFunction(callDefinition);
        final double bsmCall = bsm.evaluate(data);
        final double w = getW(sigma, t, skew, kurtosis);
        final double d = getD(s, k, sigma, t, b, w, sigmaT);
        final double call = bsmCall + skew * getQ3(s, d, w, sigmaT) + kurtosis * getQ4(s, d, w, sigmaT);
        if (!definition.isCall()) {
          return call - s * Math.exp((b - r) * t) + k * Math.exp(-r * t);
        }
        return call;
      }
    };
    return pricingFunction;
  }

  private double getW(final double sigma, final double t, final double skew, final double kurtosis) {
    final double sigma3 = sigma * sigma * sigma;
    return skew * sigma3 * Math.pow(t, 1.5) / 6. + kurtosis * sigma * sigma3 * t * t / 24.;
  }

  private double getD(final double s, final double k, final double sigma, final double t, final double b, final double w, final double sigmaT) {
    return getD1(s, k, t, sigma, b) - Math.log(1 + w) / sigmaT;
  }

  private double getQ3(final double s, final double d, final double w, final double sigmaT) {
    return s * sigmaT * (2 * sigmaT - d) * NORMAL.getPDF(d) / (6 * (1 + w));
  }

  private double getQ4(final double s, final double d, final double w, final double sigmaT) {
    return s * sigmaT * (d * d - 3 * d * sigmaT + 3 * sigmaT * sigmaT - 1) * NORMAL.getPDF(d) / (24 * (1 + w));
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.EuropeanOptionOnEuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * This model can be used to approximate the value of call-on-call or
 * put-on-call options. It is most accurate for at- and in-the-money options.
 * <p>
 * The value of a call-on-call option can be approximated by:
 * $$
 * \begin{align*}
 * c_{call} \approx c_{BSM}N(d_1) - K_2e^{-rT_2}N(d2)
 * \end{align*}
 * $$
 * where
 * $$
 * \begin{align*}
 * d1 &= \frac{\ln(\frac{c_{BSM}}{K_2}) + (b + \frac{\hat{\sigma^2}}{2})T_2}{\hat{\sigma}\sqrt{T_2}}\\
 * \hat{\sigma} &= \frac{\sigma|\Delta_{BSM}|S}{c_{BSM}}\\
 * c_{BSM} &= c_{BSM}(S, K_1, T_1, r, b, \sigma)\\
 * \Delta_{BSM} &= \Delta_{BSM}(S, K_1, T_1, r, b, \sigma)
 * \end{align*}
 * $$
 * where $K_1$ is the strike on the underlying option, $T_1$ is the expiry of
 * the underlying option, $K_2$ is the strike of the option-on-option, $T_2$ is
 * the expiry of the option-on-option, and $BSM$ is the standard
 * Black-Scholes-Merton pricing model ({@link BlackScholesMertonModel}).
 * <p>
 * The value of a put-on-call can be approximated by:
 * $$
 * \begin{align*}
 * p_{call} \approx K_2e^{-eT_2}N(d_2) - c_{BSM}N(d_1)
 * \end{align*}
 * $$
 */
public class BensoussanCrouhyGalaiOptionOnOptionModel extends AnalyticOptionModel<EuropeanOptionOnEuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();
  private static final Set<Greek> REQUIRED_GREEKS = Sets.newHashSet(Greek.FAIR_PRICE, Greek.DELTA);

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final EuropeanOptionOnEuropeanVanillaOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s = data.getSpot();
        final OptionDefinition underlying = definition.getUnderlyingOption();
        final double k1 = definition.getStrike();
        final double k2 = underlying.getStrike();
        final ZonedDateTime date = data.getDate();
        final double t1 = definition.getTimeToExpiry(date);
        final double sigma = data.getVolatility(t1, k1);
        final double r = data.getInterestRate(t1);
        final double b = data.getCostOfCarry();
        final OptionDefinition callDefinition = underlying.isCall() ? underlying : new EuropeanVanillaOptionDefinition(k2, underlying.getExpiry(), true);
        final GreekResultCollection result = BSM.getGreeks(callDefinition, data, REQUIRED_GREEKS);
        final double callBSM = result.get(Greek.FAIR_PRICE);
        final double callDelta = result.get(Greek.DELTA);
        final double underlyingSigma = sigma * Math.abs(callDelta) * s / callBSM;
        final double d1 = getD1(callBSM, k1, t1, underlyingSigma, b);
        final double d2 = getD2(d1, underlyingSigma, t1);
        final int sign = definition.isCall() ? 1 : -1;
        if (underlying.isCall()) {
          return sign * (callBSM * NORMAL.getCDF(sign * d1) - k1 * Math.exp(-r * t1) * NORMAL.getCDF(sign * d2));
        }
        throw new NotImplementedException("This model can only price call-on-call or put-on-call options");
      }

    };
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.CashOrNothingOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * Class for pricing cash-or-nothing options (see {@link com.opengamma.financial.model.option.definition.CashOrNothingOptionDefinition}).
 * The price is calculated using the Reiner-Rubenstein formula:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * c &= K e^{-rT}N(d)\\\\
 * p &= K e^{-rT}N(-d)
 * \\end{align*}
 * }
 * where
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * d = \\frac{\\ln{\\frac{S}{K}} + (b - \\frac{\\sigma^2}{2})T}{\\sigma\\sqrt{T}}
 * \\end{align*}
 * }
 * 
 */
public class CashOrNothingOptionModel extends AnalyticOptionModel<CashOrNothingOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final CashOrNothingOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double sigma = data.getVolatility(t, k);
        final double b = data.getCostOfCarry();
        final double d = getD2(getD1(s, k, t, sigma, b), sigma, t);
        final double payment = definition.getPayment();
        return payment * Math.exp(-r * t) * NORMAL.getCDF(definition.isCall() ? d : -d);
      }

    };
  }

}

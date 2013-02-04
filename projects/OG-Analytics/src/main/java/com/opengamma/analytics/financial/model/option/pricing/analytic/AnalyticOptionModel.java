/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.greeks.GreekVisitor;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.FiniteDifferenceGreekVisitor;
import com.opengamma.analytics.financial.model.option.pricing.OptionModel;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;

/**
 * @param <T> The type of the option definition
 * @param <U> The type of the option data bundle
 * Base class for analytic option models.
 */
public abstract class AnalyticOptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> implements OptionModel<T, U> {

  /**
   * Returns a pricing function.
   * @param definition The option definition, not null
   * @return The pricing function
   */
  public abstract Function1D<U, Double> getPricingFunction(T definition);

  /**
   * Returns a visitor that calculates greeks. By default, the calculation method is finite difference. If a different
   * method is possible (e.g. analytic formulae) then this method should be overridden in the implementing class.
   * @param pricingFunction The pricing function, not null
   * @param data The data, not null
   * @param definition The option definition, not null
   * @return A visitor that calculates greeks
   */
  public GreekVisitor<Double> getGreekVisitor(final Function1D<U, Double> pricingFunction, final U data, final T definition) {
    Validate.notNull(pricingFunction);
    Validate.notNull(data);
    Validate.notNull(definition);
    return new AnalyticOptionModelFiniteDifferenceGreekVisitor<>(pricingFunction, data, definition);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GreekResultCollection getGreeks(final T definition, final U data, final Set<Greek> requiredGreeks) {
    Validate.notNull(definition);
    Validate.notNull(data);
    Validate.notNull(requiredGreeks);
    final Function1D<U, Double> pricingFunction = getPricingFunction(definition);
    final GreekResultCollection results = new GreekResultCollection();
    final GreekVisitor<Double> visitor = getGreekVisitor(pricingFunction, data, definition);
    for (final Greek greek : requiredGreeks) {
      final Double result = greek.accept(visitor);
      results.put(greek, result);
    }
    return results;
  }

  protected double getD1(final double s, final double k, final double t, final double sigma, final double b) {
    final double numerator = (Math.log(s / k) + t * (b + sigma * sigma / 2));
    if (CompareUtils.closeEquals(numerator, 0, 1e-16)) {
      return 0;
    }
    return numerator / (sigma * Math.sqrt(t));
  }

  protected double getD2(final double d1, final double sigma, final double t) {
    return d1 - sigma * Math.sqrt(t);
  }

  protected double getDF(final double r, final double b, final double t) {
    return Math.exp(t * (b - r));
  }

  /**
   * Extends the finite difference greek visitor to allow calculation of dZetaDVol
   * @param <S> The type of the option data bundle
   * @param <R> The type of the option definition
   */
  protected class AnalyticOptionModelFiniteDifferenceGreekVisitor<S extends StandardOptionDataBundle, R extends OptionDefinition> extends FiniteDifferenceGreekVisitor<S, R> {
    private static final double EPS = 1e-3;
    private final S _data;
    private final R _definition;
    private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

    public AnalyticOptionModelFiniteDifferenceGreekVisitor(final Function1D<S, Double> pricingFunction, final S data, final R definition) {
      super(pricingFunction, data, definition);
      _data = data;
      _definition = definition;
    }

    @Override
    public Double visitDZetaDVol() {
      final double s = _data.getSpot();
      final double k = _definition.getStrike();
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double b = _data.getCostOfCarry();
      final double sigma = _data.getVolatility(t, k);
      final int sign = _definition.isCall() ? 1 : -1;
      final double nUp = _normal.getCDF(sign * getD2(getD1(s, k, t, sigma + EPS, b), sigma + EPS, t));
      final double nDown = _normal.getCDF(sign * getD2(getD1(s, k, t, sigma - EPS, b), sigma - EPS, t));
      return (nUp - nDown) / (2 * EPS);
    }
  }
}

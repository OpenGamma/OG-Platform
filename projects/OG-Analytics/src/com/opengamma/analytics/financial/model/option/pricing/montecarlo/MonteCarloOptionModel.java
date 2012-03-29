/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.montecarlo;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.OptionModel;
import com.opengamma.analytics.financial.model.stochastic.StochasticProcess;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.random.RandomNumberGenerator;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <T>
 * @param <U>
 */
public abstract class MonteCarloOptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> implements OptionModel<T, U> {
  private static final Logger s_logger = LoggerFactory.getLogger(MonteCarloOptionModel.class);
  private final int _n;
  private final int _steps;
  private final StochasticProcess<T, U> _process;
  private final RandomNumberGenerator _generator;

  public MonteCarloOptionModel(final int n, final int steps, final StochasticProcess<T, U> process, final RandomNumberGenerator generator) {
    ArgumentChecker.notNegativeOrZero(n, "n");
    ArgumentChecker.notNegativeOrZero(steps, "steps");
    Validate.notNull(process, "process");
    Validate.notNull(generator, "generator");
    _n = n;
    _steps = steps;
    _process = process;
    _generator = generator;
  }

  @Override
  public GreekResultCollection getGreeks(final T definition, final U data, final Set<Greek> requiredGreeks) {
    Validate.notNull(definition, "definition");
    Validate.notNull(data, "data");
    Validate.notNull(requiredGreeks, "required greeks");
    Validate.notEmpty(requiredGreeks, "required greeks");
    if (requiredGreeks.contains(Greek.FAIR_PRICE)) {
      if (requiredGreeks.size() > 1) {
        s_logger.warn("Can only produce fair price");
      }
    } else {
      throw new IllegalArgumentException("Can only produce fair price");
    }
    final GreekResultCollection greeks = new GreekResultCollection();
    final Function1D<U, Double> price = getPricingFunction(definition);
    greeks.put(Greek.FAIR_PRICE, price.evaluate(data));
    return greeks;
  }

  public abstract Function1D<U, Double> getPricingFunction(T definition);

  protected int getN() {
    return _n;
  }

  protected int getSteps() {
    return _steps;
  }

  protected StochasticProcess<T, U> getProcess() {
    return _process;
  }

  protected RandomNumberGenerator getGenerator() {
    return _generator;
  }

}

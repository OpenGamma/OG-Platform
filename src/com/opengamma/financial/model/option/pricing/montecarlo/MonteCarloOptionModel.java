/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.montecarlo;

import java.util.List;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionModel;
import com.opengamma.financial.model.stochastic.StochasticProcess;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.random.RandomNumberGenerator;

/**
 * 
 * @author emcleod
 * 
 */
public abstract class MonteCarloOptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> implements OptionModel<T, U> {
  private final int _n;
  private final int _steps;
  private final StochasticProcess<T, U> _process;
  private final RandomNumberGenerator _generator;
  private final AntitheticVariate<T, U> _antitheticVariate;

  public MonteCarloOptionModel(final int n, final int steps, final StochasticProcess<T, U> process, final RandomNumberGenerator generator) {
    _n = n;
    _steps = steps;
    _process = process;
    _generator = generator;
    _antitheticVariate = null;
  }

  public MonteCarloOptionModel(final int n, final int steps, final StochasticProcess<T, U> process, final RandomNumberGenerator generator,
      final AntitheticVariate<T, U> antitheticVariate) {
    _n = n;
    _steps = steps;
    _process = process;
    _generator = generator;
    _antitheticVariate = antitheticVariate;
  }

  @Override
  public GreekResultCollection getGreeks(final T definition, final U data, final List<Greek> requiredGreeks) {
    final GreekResultCollection greeks = new GreekResultCollection();
    final Function1D<U, Double> price = getPrice(definition);
    greeks.put(Greek.PRICE, new SingleGreekResult(price.evaluate(data)));
    return greeks;
  }

  public abstract Function1D<U, Double> getPrice(T definition);

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

  protected AntitheticVariate<T, U> getAntitheticVariate() {
    return _antitheticVariate;
  }
}

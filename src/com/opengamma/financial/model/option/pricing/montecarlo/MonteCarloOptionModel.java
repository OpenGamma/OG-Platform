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

/**
 * 
 * @author emcleod
 * 
 */
public abstract class MonteCarloOptionModel<T extends OptionDefinition, U extends StochasticProcess<T, V>, V extends StandardOptionDataBundle> implements OptionModel<T, V> {
  private final int _n;

  public MonteCarloOptionModel() {
    _n = 1000;
  }

  public MonteCarloOptionModel(final int n) {
    _n = n;
  }

  @Override
  public GreekResultCollection getGreeks(final T definition, final V data, final List<Greek> requiredGreeks) {
    final GreekResultCollection greeks = new GreekResultCollection();
    greeks.put(Greek.PRICE, new SingleGreekResult(0.));
    return greeks;
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.montecarlo;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.stochastic.StochasticProcess;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.random.RandomNumberGenerator;

/**
 * 
 */
public class EuropeanMonteCarloOptionModel extends MonteCarloOptionModel<OptionDefinition, StandardOptionDataBundle> {

  public EuropeanMonteCarloOptionModel(final int n, final int steps, final StochasticProcess<OptionDefinition, StandardOptionDataBundle> process, final RandomNumberGenerator generator) {
    super(n, steps, process, generator);
  }

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    Validate.notNull(definition, "definition");
    final OptionPayoffFunction<StandardOptionDataBundle> payoffFunction = definition.getPayoffFunction();
    final int steps = getSteps();
    final int n = getN();
    final RandomNumberGenerator randomNumbers = getGenerator();
    final StochasticProcess<OptionDefinition, StandardOptionDataBundle> process = getProcess();
    final Function2D<Double, Double> accumulator = process.getPathAccumulationFunction();
    return new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data, "data");
        final Function1D<Double, Double> generator = process.getPathGeneratingFunction(definition, data, steps);
        double[] e;
        final double s0 = process.getInitialValue(definition, data);
        double st;
        double sum = 0;
        for (int i = 0; i < n; i++) {
          e = randomNumbers.getVector(steps);
          st = s0;
          for (int j = 0; j < steps; j++) {
            st = accumulator.evaluate(generator.evaluate(e[j]), st);
          }
          sum += payoffFunction.getPayoff(data.withSpot(process.getFinalValue(st)), 0.);
        }
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        return Math.exp(-r * t) * sum / n;
      }
    };
  }
}

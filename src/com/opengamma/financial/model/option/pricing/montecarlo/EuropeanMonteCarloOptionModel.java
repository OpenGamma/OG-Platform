/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.montecarlo;

import java.util.Iterator;
import java.util.List;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.stochastic.StochasticProcess;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.Function2D;
import com.opengamma.math.random.RandomNumberGenerator;

/**
 * 
 */
public class EuropeanMonteCarloOptionModel extends MonteCarloOptionModel<OptionDefinition, StandardOptionDataBundle> {

  public EuropeanMonteCarloOptionModel(final int n, final int steps, final StochasticProcess<OptionDefinition, StandardOptionDataBundle> process, final RandomNumberGenerator generator) {
    super(n, steps, process, generator);
  }

  public EuropeanMonteCarloOptionModel(final int n, final int steps, final StochasticProcess<OptionDefinition, StandardOptionDataBundle> process, final RandomNumberGenerator generator,
      final AntitheticVariate<OptionDefinition, StandardOptionDataBundle> antitheticVariate) {
    super(n, steps, process, generator, antitheticVariate);
  }

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPrice(final OptionDefinition definition) {
    final OptionPayoffFunction<StandardOptionDataBundle> payoffFunction = definition.getPayoffFunction();
    final int steps = getSteps();
    final int n = getN();
    final List<double[]> randomNumbers = getGenerator().getVectors(steps, n);
    final StochasticProcess<OptionDefinition, StandardOptionDataBundle> process = getProcess();
    final Function2D<Double, Double> accumulator = process.getPathAccumulationFunction();
    final boolean hasAntithetic = getAntitheticVariate() != null;
    return new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        final Function1D<Double, Double> generator = process.getPathGeneratingFunction(definition, data, steps);
        final Function1D<Double, Double> antithetic = hasAntithetic ? getAntitheticVariate().getVariateFunction(generator) : null;
        final Iterator<double[]> iter = randomNumbers.iterator();
        double[] e;
        final double s0 = process.getInitialValue(definition, data);
        final int length = hasAntithetic ? 2 : 1;
        final double[] st = new double[length];
        double sum = 0;
        for (int i = 0; i < n; i++) {
          e = iter.next();
          st[0] = s0;
          if (hasAntithetic) {
            st[1] = s0;
          }
          for (int j = 0; j < steps; j++) {
            st[0] = accumulator.evaluate(generator.evaluate(e[j]), st[0]);
            if (hasAntithetic) {
              // TODO will variate need its own accumulator?
              st[1] = accumulator.evaluate(antithetic.evaluate(e[j]), st[1]);
            }
          }
          sum += payoffFunction.getPayoff(data.withSpot(process.getFinalValue(st[0])), 0.);
          if (hasAntithetic) {
            sum += payoffFunction.getPayoff(data.withSpot(process.getFinalValue(st[1])), 0.);
          }
        }
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        return Math.exp(-r * t) * sum / (length * n);
      }
    };
  }
}

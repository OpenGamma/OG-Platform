/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.financial.model.tree.RecombiningBinomialTree;

/**
 * 
 * @author emcleod
 */
public class TrisgeorgisBinomialOptionModelDefinition extends BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> {

  @Override
  public double getDownFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    return 1. / getUpFactor(option, data, n, j);
  }

  @Override
  public RecombiningBinomialTree<Double> getUpProbabilityTree(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double t = option.getTimeToExpiry(data.getDate());
    final double r = data.getInterestRate(t);
    final double sigma = data.getVolatility(t, option.getStrike());
    final double dt = t / n;
    final double nu = r - 0.5 * sigma * sigma;
    final double du = getUpFactor(option, data, n, j);
    final Double[][] tree = new Double[n + 1][j];
    final double p = 0.5 * (1 + nu * dt / Math.log(du));
    for (int i = 0; i <= n; i++) {
      for (int ii = 0; ii < j; ii++) {
        tree[i][ii] = p;
      }
    }
    return new RecombiningBinomialTree<Double>(tree);
  }

  @Override
  public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double t = option.getTimeToExpiry(data.getDate());
    final double r = data.getInterestRate(t);
    final double sigma = data.getVolatility(t, option.getStrike());
    final double dt = t / n;
    final double nu = r - 0.5 * sigma * sigma;
    return Math.exp(Math.sqrt(sigma * sigma * dt + nu * nu * dt * dt));
  }

}

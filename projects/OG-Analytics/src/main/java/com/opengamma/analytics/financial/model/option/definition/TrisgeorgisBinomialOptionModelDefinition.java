/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import com.opengamma.analytics.financial.model.tree.ConstantRecombiningBinomialTree;
import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;

/**
 * 
 */
public class TrisgeorgisBinomialOptionModelDefinition extends BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> {

  @Override
  public double getDownFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    return 1. / getUpFactor(option, data, n, j);
  }

  @Override
  public RecombiningBinomialTree<Double> getUpProbabilityTree(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double t = option.getTimeToExpiry(data.getDate());
    final double sigma = data.getVolatility(t, option.getStrike());
    final double b = data.getCostOfCarry();
    final double dt = t / n;
    final double nu = b - 0.5 * sigma * sigma;
    final double du = getUpFactor(option, data, n, j);
    return new ConstantRecombiningBinomialTree<>(0.5 * (1 + nu * dt / Math.log(du)));
  }

  @Override
  public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double t = option.getTimeToExpiry(data.getDate());
    final double sigma = data.getVolatility(t, option.getStrike());
    final double dt = t / n;
    final double b = data.getCostOfCarry();
    final double nu = b - 0.5 * sigma * sigma;
    return Math.exp(Math.sqrt(sigma * sigma * dt + nu * nu * dt * dt));
  }

}

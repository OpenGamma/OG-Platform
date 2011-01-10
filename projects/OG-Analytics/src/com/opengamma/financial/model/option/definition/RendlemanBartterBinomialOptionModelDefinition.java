/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.financial.model.tree.ConstantRecombiningBinomialTree;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;

/**
 * 
 */
public class RendlemanBartterBinomialOptionModelDefinition extends BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> {

  @Override
  public double getDownFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double b = data.getCostOfCarry();
    final double t = option.getTimeToExpiry(data.getDate());
    final double k = option.getStrike();
    final double sigma = data.getVolatility(t, k);
    final double dt = t / n;
    return Math.exp((b - sigma * sigma / 2) * dt - sigma * Math.sqrt(dt));
  }

  @Override
  public RecombiningBinomialTree<Double> getUpProbabilityTree(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    return new ConstantRecombiningBinomialTree<Double>(0.5);
  }

  @Override
  public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double b = data.getCostOfCarry();
    final double t = option.getTimeToExpiry(data.getDate());
    final double k = option.getStrike();
    final double sigma = data.getVolatility(t, k);
    final double dt = t / n;
    return Math.exp((b - sigma * sigma / 2) * dt + sigma * Math.sqrt(dt));
  }

}

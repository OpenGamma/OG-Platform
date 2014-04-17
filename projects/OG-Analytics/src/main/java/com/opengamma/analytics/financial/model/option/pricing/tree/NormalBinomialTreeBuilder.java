/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.GeneralNormalOptionDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
* Builds a binomial tree where the nodes are set to locally match a normal process. The process that the tree is emulating is of the form  df = a(f,t)dt + b(f,t)dw.
* From a node at (f,t) the two daughter nodes f+ and f- (at time t + dt) are set such that p*(1-p)*(f+ - f-)^2 = dt*b(f,t)^2, where p is the probability of reaching f+ from f. 
* The forwarding condition is p*f+ + (1-p)*f- = f + a(f,t)*dt. This is adapted from the paper Derman and Kani, The Volatility Smile and Its Implied Tree. 
* @param <T> A GeneralNormalOptionDataBundle or anything that extends it 
*/
public class NormalBinomialTreeBuilder<T extends GeneralNormalOptionDataBundle> extends BinomialTreeBuilder<T> {

  @Override
  protected DoublesPair getCentralNodePair(final double dt, final double sigma, final double forward, final double centreLevel) {
    final double sigma2dt = sigma * sigma * dt;
    final double b = 2 * centreLevel;
    final double c = forward * (2 * centreLevel - forward) - sigma2dt;
    final double root = b * b - 4 * c;
    Validate.isTrue(root >= 0, "can't find upper node - root negative");
    final double upper = (b + Math.sqrt(root)) / 2;
    final double lower = 2 * centreLevel - upper;
    return DoublesPair.of(lower, upper);
  }

  @Override
  protected double getNextHigherNode(final double dt, final double sigma, final double forward, final double lowerNode) {
    final double sigma2dt = sigma * sigma * dt;
    Validate.isTrue(forward > lowerNode, "need forward > lowerNode");
    return sigma2dt / (forward - lowerNode) + forward;
  }

  @Override
  protected double getNextLowerNode(final double dt, final double sigma, final double forward, final double higherNode) {
    if (forward == 0.0) {
      return 0.0;
    }
    final double sigma2dt = sigma * sigma * dt;
    Validate.isTrue(higherNode > forward, "need higherNode > forward");
    double lowerNode = sigma2dt / (forward - higherNode) + forward;
    if (lowerNode < 0.0) {
      lowerNode = 0.0; // set zero as an absorbing boundary
    }
    return lowerNode;
  }

  @Override
  protected double[] getForwards(final double[] spots, final T data, final double t, final double dt) {
    final int n = spots.length;
    final double[] forwards = new double[n];
    for (int i = 0; i < n; i++) {
      final double drift = data.getLocalDrift(spots[i], t);
      forwards[i] = spots[i] + drift * dt;
    }
    return forwards;
  }

}

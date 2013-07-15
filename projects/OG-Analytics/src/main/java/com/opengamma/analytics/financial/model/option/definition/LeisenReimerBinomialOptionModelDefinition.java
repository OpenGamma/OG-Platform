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
public class LeisenReimerBinomialOptionModelDefinition extends BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> {

  @Override
  public double getDownFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double b = data.getCostOfCarry();
    final double t = option.getTimeToExpiry(data.getDate());
    final double dt = t / n;
    final double p = getUpProbabilityTree(option, data, n, j).getNode(0, 0);
    final double u = getUpFactor(option, data, n, j);
    return (Math.exp(b * dt) - p * u) / (1 - p);
  }

  @Override
  public RecombiningBinomialTree<Double> getUpProbabilityTree(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double b = data.getCostOfCarry();
    final double s = data.getSpot();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry(data.getDate());
    final double sigma = data.getVolatility(t, k);
    final double d1 = getD1(s, k, t, sigma, b);
    final double d2 = getD2(d1, sigma, t);
    return new ConstantRecombiningBinomialTree<>(getH(d2, n));
  }

  @Override
  public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double b = data.getCostOfCarry();
    final double s = data.getSpot();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry(data.getDate());
    final double sigma = data.getVolatility(t, k);
    final double d1 = getD1(s, k, t, sigma, b);
    final double d2 = getD2(d1, sigma, t);
    final double dt = t / n;
    return Math.exp(b * dt) * getH(d1, n) / getH(d2, n);
  }

  private double getD1(final double s, final double k, final double t, final double sigma, final double b) {
    return (Math.log(s / k) + t * (b + sigma * sigma / 2)) / (sigma * Math.sqrt(t));
  }

  private double getD2(final double d1, final double sigma, final double t) {
    return d1 - sigma * Math.sqrt(t);
  }

  private double getH(final double x, final double n) {
    final double eta = x >= 0 ? 1 : -1;
    final double a = Math.pow(x / (n + 1. / 3 + 0.1 / (n + 1)), 2);
    final double b = n + 1. / 6;
    return 0.5 + eta * Math.sqrt(0.25 * (1 - Math.exp(-a * b)));
  }
}

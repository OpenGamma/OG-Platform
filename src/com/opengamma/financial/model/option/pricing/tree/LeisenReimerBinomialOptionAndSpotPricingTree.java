/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

import com.opengamma.financial.model.option.definition.OptionDataBundleWithOptionPrice;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class LeisenReimerBinomialOptionAndSpotPricingTree {
  private static final int DEFAULT_N = 1000;
  private RecombiningBinomialTree<Double> _spotPrices;
  private RecombiningBinomialTree<Double> _optionPrices;

  public LeisenReimerBinomialOptionAndSpotPricingTree(final OptionDefinition definition, final StandardOptionDataBundle vars) {
    createTrees(definition, vars, DEFAULT_N);
  }

  public LeisenReimerBinomialOptionAndSpotPricingTree(final int n, final OptionDefinition definition, final StandardOptionDataBundle vars) {
    createTrees(definition, vars, n);
  }

  public RecombiningBinomialTree<Double> getSpotTree() {
    return _spotPrices;
  }

  public RecombiningBinomialTree<Double> getOptionTree() {
    return _optionPrices;
  }

  private void createTrees(final OptionDefinition definition, final StandardOptionDataBundle vars, final int n) {
    final double spot = vars.getSpot();
    final int nodesAtMaturity = RecombiningBinomialTree.NODES.evaluate(n);
    final Function1D<? super StandardOptionDataBundle, Double> payoff = definition.getPayoffFunction();
    final Function1D<? super OptionDataBundleWithOptionPrice, Boolean> shouldExercise = definition.getExerciseFunction();
    final Double[][] s = new Double[n][nodesAtMaturity];
    final Double[][] o = new Double[n][nodesAtMaturity];
    final double t = definition.getTimeToExpiry(vars.getDate());
    final double sigma = vars.getVolatility(t, definition.getStrike());
    final double r = vars.getInterestRate(t);
    final double dt = t / n;
    final double sigmaSq = sigma * sigma;
    final double nu = r - 0.5 * sigmaSq;
    final double d1 = (Math.log(spot / definition.getStrike()) + nu * t) / (sigma * Math.sqrt(t));
    final double d2 = d1 - sigma * Math.sqrt(t);
    final double p1 = getProbability(d1, n);
    final double p2 = getProbability(d2, n);
    final double rn = Math.exp(r * dt);
    final double pu = rn * p1 / p2;
    final double pd = (rn - p1 * pu) / (1 - p1);
    double newSpot = spot * Math.pow(pd, n);
    for (int i = 0; i < nodesAtMaturity; i++) {
      s[n - 1][i] = newSpot;
      o[n - 1][i] = payoff.evaluate(vars.withSpot(newSpot));
      newSpot *= pu / pd;
    }
    double option;
    OptionDataBundleWithOptionPrice newVarsWithPrice = new OptionDataBundleWithOptionPrice(vars, 0);
    StandardOptionDataBundle newVars = vars;
    final double df = Math.exp(-r * dt);
    for (int i = n - 2; i >= 0; i--) {
      for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
        option = df * (pd * o[i + 1][j] + pu * o[i + 1][j + 1]);
        s[i][j] = s[i + 1][j] / Math.exp(-r * dt); // TODO this isn't right
        newVars = newVars.withSpot(s[i][j]);
        newVarsWithPrice = newVarsWithPrice.withPrice(option).withData(newVars);
        o[i][j] = shouldExercise.evaluate(newVarsWithPrice) ? option : payoff.evaluate(newVars);
      }
    }
    _spotPrices = new RecombiningBinomialTree<Double>(s);
    _optionPrices = new RecombiningBinomialTree<Double>(o);
  }

  private double getProbability(final double d, final int n) {
    return 0.5 * (1 + Math.signum(d) * Math.sqrt(1 - Math.exp(Math.pow(d / (n + 1. / 3), 2) * (n + 1. / 6))));
  }
}

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

public class CoxRossRubinsteinBinomialOptionAndSpotPricingTree {
  private static final int DEFAULT_N = 1000;
  private RecombiningBinomialTree<Double> _spotPrices;
  private RecombiningBinomialTree<Double> _optionPrices;

  public CoxRossRubinsteinBinomialOptionAndSpotPricingTree(final OptionDefinition definition, final StandardOptionDataBundle vars) {
    createTrees(definition, vars, DEFAULT_N);
  }

  public CoxRossRubinsteinBinomialOptionAndSpotPricingTree(final int n, final OptionDefinition definition, final StandardOptionDataBundle vars) {
    createTrees(definition, vars, n);
  }

  public RecombiningBinomialTree<Double> getSpotTree() {
    return _spotPrices;
  }

  private void createTrees(final OptionDefinition definition, final StandardOptionDataBundle vars, final int n) {
    final double spot = vars.getSpot();
    final int nodesAtMaturity = RecombiningBinomialTree.NODES.evaluate(n);
    final Function1D<? super StandardOptionDataBundle, Double> payoff = definition.getPayoffFunction();
    final Function1D<? super OptionDataBundleWithOptionPrice, Boolean> shouldExercise = definition.getExerciseFunction();
    final double t = definition.getTimeToExpiry(vars.getDate());
    final double sigma = vars.getVolatility(t, definition.getStrike());
    final double r = vars.getInterestRate(t);
    final double dt = t / n;
    final double u = Math.exp(sigma * Math.sqrt(dt));
    final double d = 1. / u;
    final double p = (Math.exp(r * dt) - d) / (u - d);
    final Double[][] s = new Double[n][nodesAtMaturity];
    final Double[][] o = new Double[n][nodesAtMaturity];
    double newSpot = spot * Math.pow(d, n);
    for (int i = 0; i < nodesAtMaturity; i++) {
      s[n - 1][i] = newSpot;
      o[n - 1][i] = payoff.evaluate(vars.withSpot(newSpot));
      newSpot *= u / d;
    }
    double option;
    final double df = Math.exp(-r * dt);
    OptionDataBundleWithOptionPrice newVarsWithPrice = new OptionDataBundleWithOptionPrice(vars, 0);
    StandardOptionDataBundle newVars = vars;
    for (int i = n - 2; i >= 0; i--) {
      for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
        option = df * (p * o[i + 1][j] + (1 - p) * o[i + 1][j + 1]);
        s[i][j] = s[i + 1][j] / d;
        newVars = newVars.withSpot(s[i][j]);
        newVarsWithPrice = newVarsWithPrice.withPrice(option).withData(newVars);
        o[i][j] = shouldExercise.evaluate(newVarsWithPrice) ? option : payoff.evaluate(newVars);
      }
    }
    _spotPrices = new RecombiningBinomialTree<Double>(s);
    _optionPrices = new RecombiningBinomialTree<Double>(o);
  }

  public RecombiningBinomialTree<Double> getOptionTree() {
    return _optionPrices;
  }

}

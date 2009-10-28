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

public class TrigeorgisBinomialOptionAndSpotPricingTree {
  private static final int DEFAULT_N = 1000;
  private RecombiningBinomialTree<Double> _spotPrices;
  private RecombiningBinomialTree<Double> _optionPrices;

  public TrigeorgisBinomialOptionAndSpotPricingTree(final OptionDefinition definition, final StandardOptionDataBundle vars) {
    createTrees(definition, vars, DEFAULT_N);
  }

  public TrigeorgisBinomialOptionAndSpotPricingTree(final int n, final OptionDefinition definition, final StandardOptionDataBundle vars) throws Exception {
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
    final double dxu = Math.sqrt(sigmaSq * dt + nu * nu * dt * dt);
    final double dxd = -dxu;
    final double pu = 0.5 * (1 + nu * dt / dxu);
    final double pd = 1 - pu;
    final double df = Math.exp(-r * dt);
    final double dpu = df * pu;
    final double dpd = df * pd;
    final double edxud = Math.exp(dxu - dxd);
    final double edxd = Math.exp(dxd);
    double newSpot = spot * Math.exp(n * dxd);
    for (int i = 0; i < nodesAtMaturity; i++) {
      s[n - 1][i] = newSpot;
      o[n - 1][i] = payoff.evaluate(vars.withSpot(newSpot));
      newSpot *= edxud;
    }
    double option;
    OptionDataBundleWithOptionPrice newVarsWithPrice = new OptionDataBundleWithOptionPrice(vars, 0);
    StandardOptionDataBundle newVars = vars;
    for (int i = n - 2; i >= 0; i--) {
      for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
        option = dpd * o[i + 1][j] + dpu * o[i + 1][j + 1];
        s[i][j] = s[i + 1][j] / edxd;
        newVars = newVars.withSpot(s[i][j]);
        newVarsWithPrice = newVarsWithPrice.withPrice(option).withData(newVars);
        o[i][j] = shouldExercise.evaluate(newVarsWithPrice) ? option : payoff.evaluate(newVars);
      }
    }
    _spotPrices = new RecombiningBinomialTree<Double>(s);
    _optionPrices = new RecombiningBinomialTree<Double>(o);
  }
}

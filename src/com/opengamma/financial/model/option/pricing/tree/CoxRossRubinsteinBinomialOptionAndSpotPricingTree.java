package com.opengamma.financial.model.option.pricing.tree;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.InterpolationException;

/**
 * 
 * @author emcleod
 * 
 */

public class CoxRossRubinsteinBinomialOptionAndSpotPricingTree {
  private static final int DEFAULT_N = 1000;
  private RecombiningBinomialTree<Double> _spotPrices;
  private RecombiningBinomialTree<Double> _optionPrices;

  public CoxRossRubinsteinBinomialOptionAndSpotPricingTree(OptionDefinition definition, StandardOptionDataBundle vars) {
    createTrees(definition, vars, DEFAULT_N);
  }

  public CoxRossRubinsteinBinomialOptionAndSpotPricingTree(int n, OptionDefinition definition, StandardOptionDataBundle vars) {
    createTrees(definition, vars, n);
  }

  public RecombiningBinomialTree<Double> getSpotTree() {
    return _spotPrices;
  }

  private void createTrees(OptionDefinition definition, StandardOptionDataBundle vars, int n) {
    try {
      double spot = vars.getSpot();
      int nodesAtMaturity = RecombiningBinomialTree.NODES.evaluate(n);
      Function<Double, Double> payoff = definition.getPayoffFunction();
      Function<Double, Boolean> shouldExercise = definition.getExerciseFunction();
      double t = definition.getTimeToExpiry(vars.getDate());
      double sigma = vars.getVolatility(t, definition.getStrike());
      double r = vars.getInterestRate(t);
      double dt = t / n;
      double u = Math.exp(sigma * Math.sqrt(dt));
      double d = 1. / u;
      double p = (Math.exp(r * dt) - d) / (u - d);
      Double[][] s = new Double[n][nodesAtMaturity];
      Double[][] o = new Double[n][nodesAtMaturity];
      Double[] spotArray = new Double[] { spot * Math.pow(d, n) };
      s[n - 1][0] = spotArray[0];
      o[n - 1][0] = payoff.evaluate(spotArray);
      for (int i = 1; i < nodesAtMaturity; i++) {
        spotArray[0] *= u / d;
        s[n - 1][i] = spotArray[0];
        o[n - 1][i] = payoff.evaluate(spotArray);
      }
      double option;
      double df = Math.exp(-r * dt);
      for (int i = n - 2; i >= 0; i--) {
        for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
          option = df * (p * o[i + 1][j] + (1 - p) * o[i + 1][j + 1]);
          s[i][j] = s[i + 1][j] / d;
          o[i][j] = shouldExercise.evaluate(s[i][j], option) ? option : payoff.evaluate(new Double[] { s[i][j] });
        }
      }
      _spotPrices = new RecombiningBinomialTree<Double>(s);
      _optionPrices = new RecombiningBinomialTree<Double>(o);
    } catch (InterpolationException e) {
      // TODO
    }
  }

  public RecombiningBinomialTree<Double> getOptionTree() {
    return _optionPrices;
  }

}

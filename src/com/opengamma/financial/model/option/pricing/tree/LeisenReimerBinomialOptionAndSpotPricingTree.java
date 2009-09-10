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

public class LeisenReimerBinomialOptionAndSpotPricingTree {
  private static final int DEFAULT_N = 1000;
  private RecombiningBinomialTree<Double> _spotPrices;
  private RecombiningBinomialTree<Double> _optionPrices;

  public LeisenReimerBinomialOptionAndSpotPricingTree(OptionDefinition definition, StandardOptionDataBundle vars) throws Exception {
    createTrees(definition, vars, DEFAULT_N);
  }

  public LeisenReimerBinomialOptionAndSpotPricingTree(int n, OptionDefinition definition, StandardOptionDataBundle vars) throws Exception {
    createTrees(definition, vars, n);
  }

  public RecombiningBinomialTree<Double> getSpotTree() {
    return _spotPrices;
  }

  public RecombiningBinomialTree<Double> getOptionTree() {
    return _optionPrices;
  }

  private void createTrees(OptionDefinition definition, StandardOptionDataBundle vars, int n) throws Exception {
    try {
      double spot = vars.getSpot();
      int nodesAtMaturity = RecombiningBinomialTree.NODES.evaluate(n);
      Function<Double, Double, ? extends Exception> payoff = definition.getPayoffFunction();
      Function<Double, Boolean, ? extends Exception> shouldExercise = definition.getExerciseFunction();
      Double[][] s = new Double[n][nodesAtMaturity];
      Double[][] o = new Double[n][nodesAtMaturity];
      double t = definition.getTimeToExpiry(vars.getDate());
      double sigma = vars.getVolatility(t, definition.getStrike());
      double r = vars.getInterestRate(t);
      double dt = t / n;
      double sigmaSq = sigma * sigma;
      double nu = r - 0.5 * sigmaSq;
      double d1 = (Math.log(spot / definition.getStrike()) + nu * t) / (sigma * Math.sqrt(t));
      double d2 = d1 - sigma * Math.sqrt(t);
      double p1 = getProbability(d1, n);
      double p2 = getProbability(d2, n);
      double rn = Math.exp(r * dt);
      double pu = rn * p1 / p2;
      double pd = (rn - p1 * pu) / (1 - p1);
      Double[] spotArray = new Double[1];
      spotArray[0] = spot * Math.pow(pd, n);
      s[n - 1][0] = spotArray[0];
      o[n - 1][0] = payoff.evaluate(spotArray);
      for (int i = 1; i < nodesAtMaturity; i++) {
        spotArray[0] *= pu / pd;
        s[n - 1][i] = spotArray[0];
        o[n - 1][i] = payoff.evaluate(spotArray);
      }
      double option;
      double df = Math.exp(-r * dt);
      for (int i = n - 2; i >= 0; i--) {
        for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
          option = df * (pd * o[i + 1][j] + pu * o[i + 1][j + 1]);
          s[i][j] = s[i + 1][j] / Math.exp(-r * dt); // TODO this isn't right
          o[i][j] = shouldExercise.evaluate(s[i][j], option) ? option : payoff.evaluate(new Double[] { s[i][j] });
        }
      }
      _spotPrices = new RecombiningBinomialTree<Double>(s);
      _optionPrices = new RecombiningBinomialTree<Double>(o);
    } catch (InterpolationException e) {
      // TODO
    }
  }

  private double getProbability(double d, int n) {
    return 0.5 * (1 + Math.signum(d) * Math.sqrt(1 - Math.exp(Math.pow(d / (n + 1. / 3), 2) * (n + 1. / 6))));
  }
}

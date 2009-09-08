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

public class TrigeorgisBinomialOptionAndSpotPricingTree {
  private static final int DEFAULT_N = 1000;
  private RecombiningBinomialTree<Double> _spotPrices;
  private RecombiningBinomialTree<Double> _optionPrices;

  public TrigeorgisBinomialOptionAndSpotPricingTree(OptionDefinition definition, StandardOptionDataBundle vars) {
    createTrees(definition, vars, DEFAULT_N);
  }

  public TrigeorgisBinomialOptionAndSpotPricingTree(int n, OptionDefinition definition, StandardOptionDataBundle vars) {
    createTrees(definition, vars, n);
  }

  public RecombiningBinomialTree<Double> getSpotTree() {
    return _spotPrices;
  }

  public RecombiningBinomialTree<Double> getOptionTree() {
    return _optionPrices;
  }

  private void createTrees(OptionDefinition definition, StandardOptionDataBundle vars, int n) {
    try {
      double spot = vars.getSpot();
      int nodesAtMaturity = RecombiningBinomialTree.NODES.evaluate(n);
      Function<Double, Double> payoff = definition.getPayoffFunction();
      Function<Double, Boolean> shouldExercise = definition.getExerciseFunction();
      Double[][] s = new Double[n][nodesAtMaturity];
      Double[][] o = new Double[n][nodesAtMaturity];
      double t = definition.getTimeToExpiry(vars.getDate());
      double sigma = vars.getVolatility(t, definition.getStrike());
      double r = vars.getInterestRate(t);
      double dt = t / n;
      double sigmaSq = sigma * sigma;
      double nu = r - 0.5 * sigmaSq;
      double dxu = Math.sqrt(sigmaSq * dt + nu * nu * dt * dt);
      double dxd = -dxu;
      double pu = 0.5 * (1 + nu * dt / dxu);
      double pd = 1 - pu;
      double df = Math.exp(-r * dt);
      double dpu = df * pu;
      double dpd = df * pd;
      double edxud = Math.exp(dxu - dxd);
      double edxd = Math.exp(dxd);
      Double[] spotArray = new Double[1];
      spotArray[0] = spot * Math.exp(n * dxd);
      s[n - 1][0] = spotArray[0];
      o[n - 1][0] = payoff.evaluate(spotArray);
      for (int i = 1; i < nodesAtMaturity; i++) {
        spotArray[0] *= edxud;
        s[n - 1][i] = spotArray[0];
        o[n - 1][i] = payoff.evaluate(spotArray);
      }
      double option;
      for (int i = n - 2; i >= 0; i--) {
        for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
          option = dpd * o[i + 1][j] + dpu * o[i + 1][j + 1];
          s[i][j] = s[i + 1][j] / edxd;
          o[i][j] = shouldExercise.evaluate(s[i][j], option) ? option : payoff.evaluate(new Double[] { s[i][j] });
        }
      }
      _spotPrices = new RecombiningBinomialTree<Double>(s);
      _optionPrices = new RecombiningBinomialTree<Double>(o);
    } catch (InterpolationException e) {
      // TODO
    }
  }
}

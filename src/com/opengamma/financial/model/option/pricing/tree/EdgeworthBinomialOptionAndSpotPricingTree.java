/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

import com.opengamma.financial.model.option.definition.OptionDataBundleWithOptionPrice;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class EdgeworthBinomialOptionAndSpotPricingTree {
  private static final int DEFAULT_N = 1000;
  private RecombiningBinomialTree<Double> _spotPrices;
  private RecombiningBinomialTree<Double> _optionPrices;

  public EdgeworthBinomialOptionAndSpotPricingTree(final OptionDefinition definition, final SkewKurtosisOptionDataBundle vars) {
    createTrees(definition, vars, DEFAULT_N);
  }

  public EdgeworthBinomialOptionAndSpotPricingTree(final int n, final OptionDefinition definition, final SkewKurtosisOptionDataBundle vars) {
    createTrees(definition, vars, n);
  }

  public RecombiningBinomialTree<Double> getSpotTree() {
    return _spotPrices;
  }

  public RecombiningBinomialTree<Double> getOptionTree() {
    return _optionPrices;
  }

  private void createTrees(final OptionDefinition definition, final SkewKurtosisOptionDataBundle vars, final int n) {
    final double spot = vars.getSpot();
    final double skew = vars.getAnnualizedSkew();
    final double kurtosis = vars.getAnnualizedKurtosis();
    final int nodesAtMaturity = RecombiningBinomialTree.NODES.evaluate(n);
    final Function1D<? super StandardOptionDataBundle, Double> payoff = definition.getPayoffFunction();
    final Function1D<? super OptionDataBundleWithOptionPrice, Boolean> shouldExercise = definition.getExerciseFunction();
    final double t = definition.getTimeToExpiry(vars.getDate());
    final double sigma = vars.getVolatility(t, definition.getStrike());
    final double r = vars.getInterestRate(t);
    final double dt = t / n;

    final double[] y = new double[nodesAtMaturity];
    final double[] x = new double[nodesAtMaturity];
    final double[] b = new double[nodesAtMaturity];
    final double[] f = new double[nodesAtMaturity];
    final double[] q = new double[nodesAtMaturity];
    final double[][] p1 = new double[n][nodesAtMaturity];
    final double[][] p2 = new double[n][nodesAtMaturity];
    final double[] combin = new double[nodesAtMaturity];
    final Double[][] s = new Double[n][nodesAtMaturity];
    final Double[][] o = new Double[n][nodesAtMaturity];

    final double sqrtN = Math.sqrt(nodesAtMaturity);
    final double power = Math.pow(2, -nodesAtMaturity);
    double fSum = 0;
    for (int i = 0; i < nodesAtMaturity; i++) {
      y[i] = (2 * i - nodesAtMaturity) / sqrtN;
      combin[i] = getCombinatorial(nodesAtMaturity, i);
      b[i] = power * combin[i];
      f[i] = getExpansion(skew, kurtosis, y[i], b[i]);
      if (f[i] < 0)
        throw new IllegalArgumentException();// TODO
      fSum += f[i];
    }
    double mean = 0;
    double temp;
    for (int i = 0; i < nodesAtMaturity; i++) {
      q[i] = f[i] / fSum;
      temp = q[i] / combin[i];
      p1[i][nodesAtMaturity - 1] = temp;
      p2[i][nodesAtMaturity - 1] = temp;
      mean += q[i] * y[i];
    }
    final double std = Math.sqrt(getVariance(q, y, mean));
    final double nu = sigma * Math.sqrt(t);
    final double expSigma = Math.exp(nu);
    double pe = 0;
    for (int i = 0; i < nodesAtMaturity; i++) {
      x[i] = (y[i] - mean) / std;
      pe += q[i] * expSigma * Math.exp(x[i]);
    }
    final double mu = r - Math.log(pe) / t;
    double newSpot;
    for (int i = 0; i < nodesAtMaturity; i++) {
      newSpot = spot * Math.exp(mu * t + nu * x[i]);
      s[n - 1][i] = newSpot;
      o[n - 1][i] = payoff.evaluate(vars.withSpot(newSpot));
    }
    double option;
    final double df = Math.exp(-r * dt);
    OptionDataBundleWithOptionPrice newVarsWithPrice = new OptionDataBundleWithOptionPrice(vars, 0);
    StandardOptionDataBundle newVars = vars;
    for (int i = n - 2; i >= 0; i--) {
      for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
        p1[i][j] = p1[i + 1][j] + p1[i + 1][j + 1];
        temp = p1[i + 1][j] / p1[i][j];
        p2[i][j] = temp;
        s[i][j] = df * (temp * s[i + 1][j] + (1 - temp) * s[i + 1][j + 1]);
        option = df * (temp * o[i + 1][j] + (1 - temp) * o[i + 1][j + 1]);
        newVars = newVars.withSpot(s[i][j]);
        newVarsWithPrice = newVarsWithPrice.withPrice(option).withData(newVars);
        o[i][j] = shouldExercise.evaluate(newVarsWithPrice) ? option : payoff.evaluate(newVars);
      }
    }
    _spotPrices = new RecombiningBinomialTree<Double>(s);
    _optionPrices = new RecombiningBinomialTree<Double>(o);
  }

  private double getExpansion(final double skew, final double kurtosis, final double y, final double b) {
    final double y2 = y * y;
    final double y4 = y2 * y2;
    return b * (1 + y * (y2 - 3) / 6. + (kurtosis - 3) * (y4 - 6 * y2 + 3) / 24. + skew * skew * y * (y4 - 10 * y2 + 15) / 72.);
  }

  private double getCombinatorial(final int n, final int j) {
    return (double) getFactorial(n) / (getFactorial(j) * getFactorial(n - j));
  }

  private int getFactorial(final int n) {
    int sum = 2;
    for (int i = 3; i <= n; i++) {
      sum *= i;
    }
    return sum;
  }

  private double getVariance(final double[] p, final double[] y, final double mean) {
    double sum = 0;
    double diff;
    for (int i = 0; i < p.length; i++) {
      diff = y[i] - mean;
      sum += p[i] * diff * diff;
    }
    return sum;
  }
}

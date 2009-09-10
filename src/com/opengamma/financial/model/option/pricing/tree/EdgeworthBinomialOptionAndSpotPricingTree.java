package com.opengamma.financial.model.option.pricing.tree;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.InterpolationException;

/**
 * 
 * @author emcleod
 * 
 */

public class EdgeworthBinomialOptionAndSpotPricingTree {
  private static final int DEFAULT_N = 1000;
  private RecombiningBinomialTree<Double> _spotPrices;
  private RecombiningBinomialTree<Double> _optionPrices;

  public EdgeworthBinomialOptionAndSpotPricingTree(OptionDefinition definition, SkewKurtosisOptionDataBundle vars) throws Exception {
    createTrees(definition, vars, DEFAULT_N);
  }

  public EdgeworthBinomialOptionAndSpotPricingTree(int n, OptionDefinition definition, SkewKurtosisOptionDataBundle vars) throws Exception {
    createTrees(definition, vars, n);
  }

  public RecombiningBinomialTree<Double> getSpotTree() {
    return _spotPrices;
  }

  public RecombiningBinomialTree<Double> getOptionTree() {
    return _optionPrices;
  }

  private void createTrees(OptionDefinition definition, SkewKurtosisOptionDataBundle vars, int n) throws Exception {
    try {
      double spot = vars.getSpot();
      double skew = vars.getSkew();
      double kurtosis = vars.getKurtosis();
      int nodesAtMaturity = RecombiningBinomialTree.NODES.evaluate(n);
      Function<Double, Double, ? extends Exception> payoff = definition.getPayoffFunction();
      Function<Double, Boolean, ? extends Exception> shouldExercise = definition.getExerciseFunction();
      double t = definition.getTimeToExpiry(vars.getDate());
      double sigma = vars.getVolatility(t, definition.getStrike());
      double r = vars.getInterestRate(t);
      double dt = t / n;

      double[] y = new double[nodesAtMaturity];
      double[] x = new double[nodesAtMaturity];
      double[] b = new double[nodesAtMaturity];
      double[] f = new double[nodesAtMaturity];
      double[] q = new double[nodesAtMaturity];
      double[][] p1 = new double[n][nodesAtMaturity];// TODO could make this 1D
      // and overwrite
      double[][] p2 = new double[n][nodesAtMaturity];// TODO could make this 1D
      // and overwrite
      double[] combin = new double[nodesAtMaturity];
      Double[][] s = new Double[n][nodesAtMaturity];
      Double[][] o = new Double[n][nodesAtMaturity];

      double sqrtN = Math.sqrt(nodesAtMaturity);
      double power = Math.pow(2, -nodesAtMaturity);
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
      double std = Math.sqrt(getVariance(q, y, mean));
      double nu = sigma * Math.sqrt(t);
      double expSigma = Math.exp(nu);
      double pe = 0;
      for (int i = 0; i < nodesAtMaturity; i++) {
        x[i] = (y[i] - mean) / std;
        pe += q[i] * expSigma * Math.exp(x[i]);
      }
      double mu = r - Math.log(pe) / t;
      Double[] spotArray = new Double[1];
      for (int i = 0; i < nodesAtMaturity; i++) {
        spotArray[0] = spot * Math.exp(mu * t + nu * x[i]);
        s[n - 1][i] = spotArray[0];
        o[n - 1][i] = payoff.evaluate(spotArray);
      }
      double option;
      double df = Math.exp(-r * dt);
      for (int i = n - 2; i >= 0; i--) {
        for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
          p1[i][j] = p1[i + 1][j] + p1[i + 1][j + 1];
          temp = p1[i + 1][j] / p1[i][j];
          p2[i][j] = temp;
          s[i][j] = df * (temp * s[i + 1][j] + (1 - temp) * s[i + 1][j + 1]);
          option = df * (temp * o[i + 1][j] + (1 - temp) * o[i + 1][j + 1]);
          o[i][j] = shouldExercise.evaluate(s[i][j], option) ? option : payoff.evaluate(new Double[] { s[i][j] });
        }
      }
      _spotPrices = new RecombiningBinomialTree<Double>(s);
      _optionPrices = new RecombiningBinomialTree<Double>(o);
    } catch (InterpolationException e) {
      // TODO
    }
  }

  private double getExpansion(double skew, double kurtosis, double y, double b) {
    double y2 = y * y;
    double y4 = y2 * y2;
    return b * (1 + y * (y2 - 3) / 6. + (kurtosis - 3) * (y4 - 6 * y2 + 3) / 24. + skew * skew * y * (y4 - 10 * y2 + 15) / 72.);
  }

  private double getCombinatorial(int n, int j) {
    return (double) getFactorial(n) / (getFactorial(j) * getFactorial(n - j));
  }

  private int getFactorial(int n) {
    int sum = 2;
    for (int i = 3; i <= n; i++) {
      sum *= i;
    }
    return sum;
  }

  private double getVariance(double[] p, double[] y, double mean) {
    double sum = 0;
    double diff;
    for (int i = 0; i < p.length; i++) {
      diff = y[i] - mean;
      sum += p[i] * diff * diff;
    }
    return sum;
  }
}

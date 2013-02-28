/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;

/**
 * 
 */
public class GramCharlierSkewKurtosisBinomialOptionModelDefinition extends BinomialOptionModelDefinition<OptionDefinition, SkewKurtosisOptionDataBundle> {
  private final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> _rb = new RendlemanBartterBinomialOptionModelDefinition();

  @Override
  public double getDownFactor(final OptionDefinition option, final SkewKurtosisOptionDataBundle data, final int n, final int j) {
    return _rb.getDownFactor(option, data, n, j);
  }

  @Override
  public RecombiningBinomialTree<Double> getUpProbabilityTree(final OptionDefinition option, final SkewKurtosisOptionDataBundle data, final int n, final int j) {
    final Double[][] rbTree = _rb.getUpProbabilityTree(option, data, n, j).getNodes();
    final double[][] tree = new double[n + 1][j];
    final double skew = data.getAnnualizedSkew();
    final double kurtosis = data.getAnnualizedFisherKurtosis();
    double x, x2, x4, x6, p;
    for (int i = 0; i <= n; i++) {
      x = (2. * i - n) / Math.sqrt(Double.valueOf(n));
      x2 = x * x;
      x4 = x2 * x2;
      x6 = x4 * x2;
      for (int ii = 0; ii < j; ii++) {
        p = rbTree[i][ii];
        tree[i][ii] = p * (1 + skew * x * (x2 - 3) / 6. + kurtosis * (x4 - 6 * x2 + 3) / 24. + skew * skew * (x6 - 15 * x4 + 45 * x2 - 15) / 72.);
      }
    }
    return new RecombiningBinomialTree<>(rbTree);
  }

  @Override
  public double getUpFactor(final OptionDefinition option, final SkewKurtosisOptionDataBundle data, final int n, final int j) {
    return _rb.getUpFactor(option, data, n, j);
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.analytics.financial.model.option.definition.CoxRossRubinsteinBinomialOptionModelDefinition;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.tree.BinomialOptionModel;
import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Implementation of the paper by Derman and Kani, The Volatility Smile and its Implied Tree (1994)
 */
public class DermanKaniImpliedBinomialTreeModel implements ImpliedTreeModel<OptionDefinition, StandardOptionDataBundle> {
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> CRR = new CoxRossRubinsteinBinomialOptionModelDefinition();
  private final int _n;

  public DermanKaniImpliedBinomialTreeModel(final int n) {
    Validate.isTrue(n > 0);
    _n = n;
  }
  @Override
  public ImpliedTreeResult getImpliedTrees(final OptionDefinition definition, final StandardOptionDataBundle data) {
    Validate.notNull(definition, "definition");
    Validate.notNull(data, "data");

    final int m1 = RecombiningBinomialTree.NODES.evaluate(_n);
    final int m2 = RecombiningBinomialTree.NODES.evaluate(_n - 1);
    final double[][] impliedTree = new double[_n + 1][m1]; //TODO this wastes space


    final double[] transitionProbabilities = new double[m2];
    double[] arrowDebreu = new double[m1];
    final double[][] localVolatilityTree = new double[_n][m2];
    final double dt = definition.getTimeToExpiry(data.getDate()) / _n;
    double t = 0;
    final double spot = data.getSpot();
    impliedTree[0][0] = spot;
    arrowDebreu[0] = 1;
    int previousNodes = 1;
    final ZonedDateTime date = data.getDate();
    for (int i = 1; i < _n + 1; i++) {
      final int nodes = RecombiningBinomialTree.NODES.evaluate(i);
      final BinomialOptionModel<StandardOptionDataBundle> crrModel = new BinomialOptionModel<>(CRR, i);
      t += dt;
      final double df1 = Math.exp(dt * data.getInterestRate(t));
      final double df2 = Math.exp(dt * data.getCostOfCarry());
      final Expiry expiry = new Expiry(DateUtils.getDateOffsetWithYearFraction(date, t));
      final int mid = i / 2;
      if (i % 2 == 0) {
        impliedTree[i][mid] = spot;
        addUpperNodes(data, impliedTree, arrowDebreu, i, crrModel, df1, df2, expiry, mid + 1);
        addLowerNodes(data, impliedTree, arrowDebreu, i, crrModel, df1, df2, expiry, mid - 1);
      } else {
        final double c = crrModel.getTreeGeneratingFunction(new EuropeanVanillaOptionDefinition(spot, expiry, true)).evaluate(data).getNode(0, 0).second;
        final double sigma = getUpperSigma(impliedTree, arrowDebreu, i - 1, df2, mid + 1);
        impliedTree[i][mid + 1] = spot * (df1 * c + arrowDebreu[mid] * spot - sigma) / (arrowDebreu[mid] * impliedTree[i - 1][mid] * df2 - df1 * c + sigma);
        impliedTree[i][mid] = spot * spot / impliedTree[i][mid + 1];
        addUpperNodes(data, impliedTree, arrowDebreu, i, crrModel, df1, df2, expiry, mid + 2);
        addLowerNodes(data, impliedTree, arrowDebreu, i, crrModel, df1, df2, expiry, mid - 1);
      }
      for (int j = 0; j < previousNodes; j++) {
        final double f = impliedTree[i - 1][j] * df2;
        transitionProbabilities[j] = (f - impliedTree[i][j]) / (impliedTree[i][j + 1] - impliedTree[i][j]);
        //TODO emcleod 31-8-10 Need to check that transition probabilities are positive - use adjustment suggested in "The Volatility Smile and its Implied Tree"
        localVolatilityTree[i - 1][j] = Math.sqrt(transitionProbabilities[j] * (1 - transitionProbabilities[j])) * Math.log(impliedTree[i][j + 1] / impliedTree[i][j]); //TODO need 1/sqrt(dt) here
      }
      final double[] temp = new double[m1];
      temp[0] = (1 - transitionProbabilities[0]) * arrowDebreu[0] / df1;
      temp[nodes - 1] = (transitionProbabilities[previousNodes - 1] * arrowDebreu[previousNodes - 1]) / df1;
      for (int j = 1; j < nodes - 1; j++) {
        temp[j] = (transitionProbabilities[j - 1] * arrowDebreu[j - 1] + (1 - transitionProbabilities[j]) * arrowDebreu[j]) / df1;
      }
      arrowDebreu = temp;
      previousNodes = nodes;
    }
    final Double[][] impliedTreeResult = new Double[_n + 1][m1];
    final Double[][] localVolResult = new Double[_n][m2];
    for (int i = 0; i < impliedTree.length; i++) {
      for (int j = 0; j < impliedTree[i].length; j++) {
        impliedTreeResult[i][j] = impliedTree[i][j];
        if (i < _n && j < m2) {
          localVolResult[i][j] = localVolatilityTree[i][j];
        }
      }
    }
    return new ImpliedTreeResult(new RecombiningBinomialTree<>(impliedTreeResult), new RecombiningBinomialTree<>(localVolResult));
  }

  private void addLowerNodes(final StandardOptionDataBundle data, final double[][] impliedTree, final double[] arrowDebreu, final int step,
      final BinomialOptionModel<StandardOptionDataBundle> crrModel, final double df1, final double df2, final Expiry expiry, final int mid) {
    double sigma = getLowerSigma(impliedTree, arrowDebreu, step - 1, df2, mid);
    for (int i = mid; i >= 0; i--) {
      final double p = crrModel.getTreeGeneratingFunction(new EuropeanVanillaOptionDefinition(impliedTree[step - 1][i], expiry, false)).evaluate(data).getNode(0, 0).second;
      final double forward = impliedTree[step - 1][i] * df2;
      impliedTree[step][i] = (impliedTree[step][i + 1] * (df1 * p - sigma) + arrowDebreu[i] * impliedTree[step - 1][i] * (forward - impliedTree[step][i + 1]))
          / (df1 * p - sigma + arrowDebreu[i] * (forward - impliedTree[step][i + 1]));
      if (i > 0) {
        sigma -= arrowDebreu[i - 1] * (impliedTree[step - 1][i] - impliedTree[step - 1][i - 1] * df2);
      }
    }
  }

  private void addUpperNodes(final StandardOptionDataBundle data, final double[][] impliedTree, final double[] arrowDebreu, final int step,
      final BinomialOptionModel<StandardOptionDataBundle> crrModel, final double df1, final double df2, final Expiry expiry, final int mid) {
    double sigma = getUpperSigma(impliedTree, arrowDebreu, step - 1, df2, mid);
    for (int i = mid; i < RecombiningBinomialTree.NODES.evaluate(step); i++) {
      final double c = crrModel.getTreeGeneratingFunction(new EuropeanVanillaOptionDefinition(impliedTree[step - 1][i - 1], expiry, true)).evaluate(data).getNode(0, 0).second;
      final double forward = impliedTree[step - 1][i - 1] * df2;
      impliedTree[step][i] = (impliedTree[step][i - 1] * (df1 * c - sigma) - arrowDebreu[i - 1] * impliedTree[step - 1][i - 1] * (forward - impliedTree[step][i - 1]))
          / (df1 * c - sigma - arrowDebreu[i - 1] * (forward - impliedTree[step][i - 1]));
      sigma -= arrowDebreu[i] * (impliedTree[step - 1][i] * df2 - impliedTree[step - 1][i - 1]);
    }
  }

  private double getLowerSigma(final double[][] impliedTree, final double[] arrowDebreu, final int previousStep, final double df2, final int start) {
    double sigma = 0;
    for (int i = start - 1; i >= 0; i--) {
      sigma += arrowDebreu[i] * (impliedTree[previousStep][start] - impliedTree[previousStep][i] * df2);
    }
    return sigma;
  }

  private double getUpperSigma(final double[][] impliedTree, final double[] arrowDebreu, final int previousStep, final double df2, final int start) {
    double sigma = 0;
    for (int i = start; i < RecombiningBinomialTree.NODES.evaluate(previousStep + 1); i++) {
      sigma += arrowDebreu[i] * (impliedTree[previousStep][i] * df2 - impliedTree[previousStep][start - 1]);
    }
    return sigma;
  }

}

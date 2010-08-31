/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.local;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.CoxRossRubinsteinBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.tree.BinomialOptionModel;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class DermanKaniImpliedTreeModel {

  public static RecombiningBinomialTree<List<Double>> getTrees(final OptionDefinition definition, final StandardOptionDataBundle data) {
    Validate.notNull(definition, "definition");
    Validate.notNull(data, "data");
    final int n = 5;
    final int m1 = RecombiningBinomialTree.NODES.evaluate(n);
    final int m2 = RecombiningBinomialTree.NODES.evaluate(n - 1);
    final double[][] impliedTree = new double[n + 1][m1];
    final double[][] transitionProbabilities = new double[n][m2];
    final double[][] arrowDebreu = new double[n + 1][m1];
    final double[][] localVolatilities = new double[n][m2];
    final double dt = definition.getTimeToExpiry(data.getDate()) / n;
    double t = 0;
    final double spot = data.getSpot();
    impliedTree[0][0] = spot;
    arrowDebreu[0][0] = 1;
    final ZonedDateTime date = data.getDate();
    for (int i = 1; i < n + 1; i++) {
      final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> crr = new CoxRossRubinsteinBinomialOptionModelDefinition();
      final BinomialOptionModel<StandardOptionDataBundle> crrModel = new BinomialOptionModel<StandardOptionDataBundle>(crr, i);
      t += dt;
      final double df1 = Math.exp(dt * data.getInterestRate(t));
      final double df2 = Math.exp(dt * data.getCostOfCarry());
      final Expiry expiry = new Expiry(DateUtil.getDateOffsetWithYearFraction(date, t));
      final int mid = i / 2;
      if (i % 2 == 1) {
        final double c = crrModel.getTreeGeneratingFunction(new EuropeanVanillaOptionDefinition(spot, expiry, true)).evaluate(data).getNode(0, 0).second;
        final double sigma = getUpperSigma(impliedTree, arrowDebreu, i - 1, df2, mid + 1);
        impliedTree[i][mid + 1] = spot * (df1 * c + arrowDebreu[i - 1][mid] * spot - sigma) / (arrowDebreu[i - 1][mid] * impliedTree[i - 1][mid] * df2 - df1 * c + sigma);
        impliedTree[i][mid] = spot * spot / impliedTree[i][mid + 1];
        getUpperNodes(data, impliedTree, arrowDebreu, i, crrModel, df1, df2, expiry, mid + 2);
        getLowerNodes(data, impliedTree, arrowDebreu, i, crrModel, df1, df2, expiry, mid - 1);
      } else {
        impliedTree[i][mid] = spot;
        getUpperNodes(data, impliedTree, arrowDebreu, i, crrModel, df1, df2, expiry, mid + 1);
        getLowerNodes(data, impliedTree, arrowDebreu, i, crrModel, df1, df2, expiry, mid - 1);
      }
      for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i - 1); j++) {
        final double f = impliedTree[i - 1][j] * df2;
        transitionProbabilities[i - 1][j] = (f - impliedTree[i][j]) / (impliedTree[i][j + 1] - impliedTree[i][j]);
        localVolatilities[i - 1][j] = Math.sqrt(transitionProbabilities[i - 1][j] * (1 - transitionProbabilities[i - 1][j])) * Math.log(impliedTree[i][j + 1] / impliedTree[i][j]);
      }
      arrowDebreu[i][0] = (1 - transitionProbabilities[i - 1][0]) * arrowDebreu[i - 1][0] / df1;
      arrowDebreu[i][RecombiningBinomialTree.NODES.evaluate(i) - 1] = (transitionProbabilities[i - 1][RecombiningBinomialTree.NODES.evaluate(i - 1) - 1] * arrowDebreu[i - 1][RecombiningBinomialTree.NODES
          .evaluate(i - 1) - 1])
          / df1;
      for (int j = 1; j < RecombiningBinomialTree.NODES.evaluate(i) - 1; j++) {
        arrowDebreu[i][j] = (transitionProbabilities[i - 1][j - 1] * arrowDebreu[i - 1][j - 1] + (1 - transitionProbabilities[i - 1][j]) * arrowDebreu[i - 1][j]) / df1;
      }
    }
    System.out.println("Implied tree");
    for (int i = 0; i < impliedTree.length; i++) {
      for (int j = 0; j < impliedTree[0].length; j++) {
        System.out.println(i + "\t" + j + "\t" + impliedTree[i][j]);
      }
      System.out.println();
    }
    System.out.println("Transition probabilities");
    for (int i = 0; i < transitionProbabilities.length; i++) {
      for (int j = 0; j < transitionProbabilities[0].length; j++) {
        System.out.println(i + "\t" + j + "\t" + transitionProbabilities[i][j]);
      }
      System.out.println();
    }
    System.out.println("Arrow-Debreu");
    for (int i = 0; i < arrowDebreu.length; i++) {
      for (int j = 0; j < arrowDebreu[0].length; j++) {
        System.out.println(i + "\t" + j + "\t" + arrowDebreu[i][j]);
      }
      System.out.println();
    }
    System.out.println("Local vol");
    for (int i = 0; i < localVolatilities.length; i++) {
      for (int j = 0; j < localVolatilities[0].length; j++) {
        System.out.println(i + "\t" + j + "\t" + localVolatilities[i][j]);
      }
      System.out.println();
    }
    return null;
  }

  private static void getLowerNodes(final StandardOptionDataBundle data, final double[][] impliedTree, final double[][] arrowDebreu, final int step,
      final BinomialOptionModel<StandardOptionDataBundle> crrModel, final double df1, final double df2, final Expiry expiry, final int mid) {
    double sigma = getLowerSigma(impliedTree, arrowDebreu, step - 1, df2, mid);
    for (int j = mid; j >= 0; j--) {
      final double p = crrModel.getTreeGeneratingFunction(new EuropeanVanillaOptionDefinition(impliedTree[step - 1][j], expiry, false)).evaluate(data).getNode(0, 0).second;
      final double forward = impliedTree[step - 1][j] * df2;
      impliedTree[step][j] = (impliedTree[step][j + 1] * (df1 * p - sigma) + arrowDebreu[step - 1][j] * impliedTree[step - 1][j] * (forward - impliedTree[step][j + 1]))
          / (df1 * p - sigma + arrowDebreu[step - 1][j] * (forward - impliedTree[step][j + 1]));
      if (j > 0) {
        sigma -= arrowDebreu[step - 1][j - 1] * (impliedTree[step - 1][j] - impliedTree[step - 1][j - 1] * df2);
      }
    }
  }

  private static void getUpperNodes(final StandardOptionDataBundle data, final double[][] impliedTree, final double[][] arrowDebreu, final int step,
      final BinomialOptionModel<StandardOptionDataBundle> crrModel, final double df1, final double df2, final Expiry expiry, final int mid) {
    double sigma = getUpperSigma(impliedTree, arrowDebreu, step - 1, df2, mid);
    for (int j = mid; j < RecombiningBinomialTree.NODES.evaluate(step); j++) {
      final double c = crrModel.getTreeGeneratingFunction(new EuropeanVanillaOptionDefinition(impliedTree[step - 1][j - 1], expiry, true)).evaluate(data).getNode(0, 0).second;
      final double forward = impliedTree[step - 1][j - 1] * df2;
      impliedTree[step][j] = (impliedTree[step][j - 1] * (df1 * c - sigma) - arrowDebreu[step - 1][j - 1] * impliedTree[step - 1][j - 1] * (forward - impliedTree[step][j - 1]))
          / (df1 * c - sigma - arrowDebreu[step - 1][j - 1] * (forward - impliedTree[step][j - 1]));
      sigma -= arrowDebreu[step - 1][j] * (impliedTree[step - 1][j] * df2 - impliedTree[step - 1][j - 1]);
    }
  }

  private static double getLowerSigma(final double[][] impliedTree, final double[][] arrowDebreu, final int step, final double df2, final int start) {
    double sigma = 0;
    for (int i = start - 1; i >= 0; i--) {
      sigma += arrowDebreu[step][i] * (impliedTree[step][start] - impliedTree[step][i] * df2);
    }
    return sigma;
  }

  private static double getUpperSigma(final double[][] impliedTree, final double[][] arrowDebreu, final int step, final double df2, final int start) {
    double sigma = 0;
    for (int i = start; i < RecombiningBinomialTree.NODES.evaluate(step + 1); i++) {
      sigma += arrowDebreu[step][i] * (impliedTree[step][i] * df2 - impliedTree[step][start - 1]);
    }
    return sigma;
  }

}

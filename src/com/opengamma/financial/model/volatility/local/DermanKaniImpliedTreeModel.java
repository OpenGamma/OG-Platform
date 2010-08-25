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

  @SuppressWarnings("unchecked")
  public static RecombiningBinomialTree<List<Double>> getTrees(final OptionDefinition definition, final StandardOptionDataBundle data) {
    Validate.notNull(definition, "definition");
    Validate.notNull(data, "data");
    final int n = 5;
    final int m1 = RecombiningBinomialTree.NODES.evaluate(n + 1);
    final int m2 = RecombiningBinomialTree.NODES.evaluate(n);
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
      try {
        if (i == 1) {
          final double c = crrModel.getTreeGeneratingFunction(new EuropeanVanillaOptionDefinition(impliedTree[i - 1][0], new Expiry(DateUtil.getDateOffsetWithYearFraction(date, t)), true)).evaluate(
              data).getNode(0, 0).second;
          impliedTree[i][1] = spot * (df1 * c + arrowDebreu[i - 1][0] * spot) / (arrowDebreu[i - 1][0] * impliedTree[i - 1][0] * df2 - df1 * c);
          impliedTree[i][0] = spot * spot / impliedTree[i][1];
        } else {
          final int mid = i / 2;
          final double c = crrModel.getTreeGeneratingFunction(new EuropeanVanillaOptionDefinition(impliedTree[i - 1][mid], new Expiry(DateUtil.getDateOffsetWithYearFraction(date, t)), true))
              .evaluate(data).getNode(0, 0).second;
          final double p = crrModel.getTreeGeneratingFunction(new EuropeanVanillaOptionDefinition(impliedTree[i - 1][mid - 1], new Expiry(DateUtil.getDateOffsetWithYearFraction(date, t)), false))
              .evaluate(data).getNode(0, 0).second;
          double sigma = 0;
          if (i % 2 == 1) {
            final double f = impliedTree[i - 1][mid] * df2;
            for (int j = mid + 2; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
              final double f1 = impliedTree[i - 1][j] * df2;
              sigma += arrowDebreu[i - 1][j] * (f1 - impliedTree[i - 1][mid]);
            }
            System.out.println(i + " " + sigma);
            impliedTree[i][mid + 1] = spot * (df1 * c + arrowDebreu[i - 1][mid] * spot - sigma) / (arrowDebreu[i - 1][mid] * f - df1 * c + sigma);
            impliedTree[i][mid] = spot * spot / impliedTree[i][mid + 1];
          } else {
            impliedTree[i][mid] = spot;
          }
          for (int j = mid + 1; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
            final double f = impliedTree[i - 1][j - 1] * df2;
            impliedTree[i][j] = (impliedTree[i][j - 1] * (df1 * c - sigma) - arrowDebreu[i - 1][j - 1] * impliedTree[i - 1][j - 1] * (f - impliedTree[i][j - 1]))
                / (df1 * c - sigma - arrowDebreu[i - 1][j - 1] * (f - impliedTree[i][j - 1]));
          }
          for (int j = mid - 1; j >= 0; j--) {
            final double f = impliedTree[i - 1][j] * df2;
            impliedTree[i][j] = (impliedTree[i][j + 1] * (df1 * p - sigma) + arrowDebreu[i - 1][j] * impliedTree[i - 1][j] * (f - impliedTree[i][j + 1]))
                / (df1 * p - sigma + arrowDebreu[i - 1][j] * (f - impliedTree[i][j + 1]));
          }
        }
        for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i - 1); j++) {
          final double f = impliedTree[i - 1][j] * df2;
          transitionProbabilities[i - 1][j] = (f - impliedTree[i][j]) / (impliedTree[i][j + 1] - impliedTree[i][j]);
          localVolatilities[i - 1][j] = Math.sqrt(transitionProbabilities[i - 1][j] * (1 - transitionProbabilities[i - 1][j])) * Math.log(impliedTree[i][j + 1] / impliedTree[i][j]);
        }
        arrowDebreu[i][0] = (1 - transitionProbabilities[i - 1][0] * arrowDebreu[i - 1][0]) / df1;
        arrowDebreu[i][RecombiningBinomialTree.NODES.evaluate(i) - 1] = (transitionProbabilities[i - 1][RecombiningBinomialTree.NODES.evaluate(i - 1) - 1] * arrowDebreu[i - 1][RecombiningBinomialTree.NODES
            .evaluate(i - 1) - 1])
            / df1;
        for (int j = 1; j < RecombiningBinomialTree.NODES.evaluate(i) - 1; j++) {
          arrowDebreu[i][j] = (transitionProbabilities[i - 1][j - 1] * arrowDebreu[i - 1][j - 1] + (1 - transitionProbabilities[i - 1][j]) * arrowDebreu[i - 1][j]) / df1;
        }
      } catch (final Exception e) {
        for (final StackTraceElement a : e.getStackTrace()) {
          System.out.println(a);
        }
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
}

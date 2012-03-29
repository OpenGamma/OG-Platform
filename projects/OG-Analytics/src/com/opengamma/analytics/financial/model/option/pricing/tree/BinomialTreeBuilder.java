/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.analytics.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Builds a binomial tree
 * @param <T> StandardOptionDataBundle
 */
public abstract class BinomialTreeBuilder<T extends StandardOptionDataBundle> {

  /**
   * Builds a tree of an asset prices 
   * @param maturity The time span (in years) of the tree
   * @param data OptionDataBundle
   * @param nSteps The number of steps in the tree (need at least 1 step)
   * @return tree of an asset prices 
   */
  @SuppressWarnings("unchecked")
  public RecombiningBinomialTree<BinomialTreeNode<Double>> buildAssetTree(final double maturity, final T data, final int nSteps) {

    BinomialTreeNode<Double>[][] tree = new BinomialTreeNode[nSteps + 1][];
    double t = 0;
    double spot = data.getSpot();
    double dt = maturity / nSteps;

    double[] spots = new double[1];
    spots[0] = spot;

    for (int i = 1; i <= nSteps; i++) {

      t = (i - 1) * dt;
      double[] forwards = getForwards(spots, data, t, dt);
      double[] nodes = new double[i + 1];
      int jPlus;
      int jMinus;

      if (i % 2 == 0) { // central node set equal to spot
        int k = i / 2;
        jPlus = k + 1;
        jMinus = k - 1;
        nodes[k] = spot; // TODO have an option for the centre node to follow the forward rather than the spot
      } else {
        int k = (i - 1) / 2;
        jPlus = k + 2;
        jMinus = k - 1;
        double sigma = data.getVolatility(t, spots[k]);
        DoublesPair nodePair = getCentralNodePair(dt, sigma, forwards[k], spot);
        nodes[k] = nodePair.first;
        nodes[k + 1] = nodePair.second;
      }

      for (int j = jPlus; j <= i; j++) {
        double sigma = data.getVolatility(t, spots[j - 1]);
        nodes[j] = getNextHigherNode(dt, sigma, forwards[j - 1], nodes[j - 1]);
      }

      for (int j = jMinus; j >= 0; j--) {
        double sigma = data.getVolatility(t, spots[j]);
        nodes[j] = getNextLowerNode(dt, sigma, forwards[j], nodes[j + 1]);
      }

      tree[i - 1] = new BinomialTreeNode[i];

      for (int j = 0; j < i; j++) {
        double diff = nodes[j + 1] - nodes[j];
        double p;
        if (diff == 0.0) {
          // some branches of the tree are stuck at spot = 0.0 - this is not a problem as such
          Validate.isTrue(forwards[j] == nodes[j], "inconsistent nodes");
          p = 0.5; // Arbitrary as nodes are degenerate
        } else {
          p = (forwards[j] - nodes[j]) / diff;
        }
        tree[i - 1][j] = new BinomialTreeNode<Double>(spots[j], p);
      }
      spots = nodes;
    }

    // fill out the final column of nodes - probability is set to zero
    tree[nSteps] = new BinomialTreeNode[nSteps + 1];
    for (int j = 0; j <= nSteps; j++) {
      tree[nSteps][j] = new BinomialTreeNode<Double>(spots[j], 0.0);
    }

    return new RecombiningBinomialTree<BinomialTreeNode<Double>>(tree);
  }

  protected abstract double[] getForwards(final double[] spots, final T data, final double t, final double dt);

  protected abstract double getNextHigherNode(final double dt, final double sigma, final double forward, final double lowerNode);

  protected abstract double getNextLowerNode(final double dt, final double sigma, final double forward, final double higherNode);

  protected abstract DoublesPair getCentralNodePair(final double dt, final double sigma, final double forward, final double centreLevel);

  /**
   * Builds a tree of option prices 
   * @param definition Option Definition
   * @param data OptionDataBundle
   * @param assetTree A previously built asset price tree
   * @return a tree of option prices
   */
  @SuppressWarnings("unchecked")
  public RecombiningBinomialTree<BinomialTreeNode<Double>> buildOptionPriceTree(final OptionDefinition definition, final T data, final RecombiningBinomialTree<BinomialTreeNode<Double>> assetTree) {

    int nSteps = assetTree.getDepth() - 1;
    BinomialTreeNode<Double>[][] tree = new BinomialTreeNode[nSteps + 1][];
    final OptionPayoffFunction<T> payoffFunction = definition.getPayoffFunction();
    final OptionExerciseFunction<T> exerciseFunction = definition.getExerciseFunction();
    YieldAndDiscountCurve yieldCurve = data.getInterestRateCurve();

    double spot;

    tree[nSteps] = new BinomialTreeNode[nSteps + 1];
    for (int j = 0; j <= nSteps; j++) {
      spot = assetTree.getNode(nSteps, j).getValue();
      double value = payoffFunction.getPayoff((T) data.withSpot(spot), 0.0);
      tree[nSteps][j] = new BinomialTreeNode<Double>(value, 0.0); // no need to set the probabilities
    }

    double maturity = definition.getTimeToExpiry(data.getDate());
    double dt = maturity / nSteps;
    double t = maturity;
    for (int i = nSteps - 1; i >= 0; i--) {
      t -= dt;
      double df = yieldCurve.getDiscountFactor(t + dt) / yieldCurve.getDiscountFactor(t);
      tree[i] = new BinomialTreeNode[i + 1];
      for (int j = 0; j <= i; j++) {
        BinomialTreeNode<Double> node = assetTree.getNode(i, j);
        double p = node.getUpProbability();
        spot = node.getValue();
        double optionValue = df * (p * tree[i + 1][j + 1].getValue() + (1 - p) * tree[i + 1][j].getValue());

        T newData = (T) data.withSpot(spot);
        double value = exerciseFunction.shouldExercise(newData, optionValue) ? payoffFunction.getPayoff(newData, optionValue) : optionValue;
        tree[i][j] = new BinomialTreeNode<Double>(value, 0.0);
      }
    }

    return new RecombiningBinomialTree<BinomialTreeNode<Double>>(tree);
  }

}

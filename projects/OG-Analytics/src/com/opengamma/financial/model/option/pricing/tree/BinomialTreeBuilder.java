/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.ForwardOptionDataBundle;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;

/**
 * 
 */
public class BinomialTreeBuilder<T extends ForwardOptionDataBundle> {
  private static final RealSingleRootFinder s_root = new VanWijngaardenDekkerBrentSingleRootFinder();

  @SuppressWarnings("unchecked")
  public RecombiningBinomialTree<BinomialTreeNode<Double>> buildAssetTree(final double maturity, final T data, final int nSteps) {

    // List<BinomialTreeNode<Double>> test = new ArrayList<BinomialTreeNode<Double>>();
    // BinomialTreeNode<Double>[] test2 = new BinomialTreeNode[nSteps];
    BinomialTreeNode<Double>[][] tree = new BinomialTreeNode[nSteps + 1][];
    double t = 0;
    double spot = data.getSpot();
    double dt = maturity / nSteps;

    double[] forwards = new double[nSteps + 1];
    double[] spots = new double[1];
    spots[0] = spot;

    for (int i = 1; i <= nSteps; i++) {
      t = (i - 1) * dt;
      for (int j = 0; j < i; j++) {
        double drift = data.getLocalDrift(spots[j], t);
        forwards[j] = spots[j] + drift * dt;
      }

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
        // Function1D<Double, Double> func = new CentreNode(dt, sigma, forwards[k], spot);
        // nodes[k + 1] = s_root.getRoot(func, forwards[k], k == 0 ? 2 * forwards[k] : forwards[k + 1]);
        nodes[k + 1] = getCentreNode(dt, sigma, forwards[k], spot);
        nodes[k] = spot * spot / nodes[k + 1];
      }
      for (int j = jPlus; j <= i; j++) {
        double sigma = data.getVolatility(t, spots[j - 1]);
        // Function1D<Double, Double> func = new UpperNodes(dt, sigma, forwards[j - 1], nodes[j - 1]);
        // nodes[j] = s_root.getRoot(func, forwards[j - 1], j < i ? forwards[j] : 2 * forwards[j - 1]);
        nodes[j] = getUpperNode(dt, sigma, forwards[j - 1], nodes[j - 1]);
      }
      for (int j = jMinus; j >= 0; j--) {
        double sigma = data.getVolatility(t, spots[j]);
        // Function1D<Double, Double> func = new LowerNodes(dt, sigma, forwards[j], nodes[j + 1]);
        // nodes[j] = s_root.getRoot(func, j > 0 ? forwards[j - 1] : 0.0, forwards[j]);
        nodes[j] = getLowerNode(dt, sigma, forwards[j], nodes[j + 1]);
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

  /**
   * The root of this function gives the next node above the currently know one 
   */
  private class UpperNodes extends Function1D<Double, Double> {

    private double _rootdt;
    private double _sigma;
    private double _f;
    private double _s;

    public UpperNodes(final double dt, final double sigma, final double forward, final double s) {
      _rootdt = Math.sqrt(dt);
      _sigma = sigma;
      _f = forward;
      _s = s;
    }

    @Override
    public Double evaluate(Double x) {
      double p = (_f - _s) / (x - _s);

      double res = _s * Math.exp(_rootdt * _sigma / Math.sqrt(p * (1 - p))) - x;
      return res;
    }
  }

  private double getUpperNode(final double dt, final double sigma, final double forward, final double s) {
    double sigma2dt = sigma * sigma * dt;
    Validate.isTrue(forward > s, "need forward > s");
    return sigma2dt / (forward - s) + forward;
  }

  private class LowerNodes extends Function1D<Double, Double> {

    private double _rootdt;
    private double _sigma;
    private double _f;
    private double _s;

    public LowerNodes(final double dt, final double sigma, final double forward, final double s) {
      _rootdt = Math.sqrt(dt);
      _sigma = sigma;
      _f = forward;
      _s = s;
    }

    @Override
    public Double evaluate(Double x) {
      double p = (_f - x) / (_s - x);
      double res = _s * Math.exp(-_rootdt * _sigma / Math.sqrt(p * (1 - p))) - x;
      return res;
    }
  }

  private double getLowerNode(final double dt, final double sigma, final double forward, final double s) {
    double sigma2dt = sigma * sigma * dt;
    Validate.isTrue(s > forward, "need s > forward");
    return sigma2dt / (forward - s) + forward;
  }

  private class CentreNode extends Function1D<Double, Double> {

    private double _rootdt;
    private double _sigma;
    private double _f;
    private double _spot;

    public CentreNode(final double dt, final double sigma, final double forward, final double spot) {
      _rootdt = Math.sqrt(dt);
      _sigma = sigma;
      _f = forward;
      _spot = spot;
    }

    @Override
    public Double evaluate(Double x) {
      double p;
      if (_f == _spot) {
        p = _f / (x + _f);
      } else {
        Validate.isTrue(x != _spot, "invalide x");
        p = (x * _f - _spot * _spot) / (x * x - _spot * _spot);
      }
      double res = _spot * _spot * Math.exp(_rootdt * _sigma / Math.sqrt(p * (1 - p))) - x * x;
      return res;
    }
  }

  private double getCentreNode(final double dt, final double sigma, final double forward, final double spot) {
    double sigma2dt = sigma * sigma * dt;
    double a = forward;
    double b = forward * forward + spot * spot + sigma2dt;
    double c = spot * spot * forward;
    double root = b * b - 4 * a * c;
    Validate.isTrue(root >= 0, "can't find upper lower - root negative");
    return (b + Math.sqrt(root)) / 2 / a;
  }

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

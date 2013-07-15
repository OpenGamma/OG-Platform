/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.greeks.GreekVisitor;
import com.opengamma.analytics.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.analytics.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.FiniteDifferenceGreekVisitor;
import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 * @param <T>
 */
public class BinomialOptionModel<T extends StandardOptionDataBundle> extends TreeOptionModel<OptionDefinition, T> {
  private final int _n;
  private final int _j;
  private final BinomialOptionModelDefinition<OptionDefinition, T> _model;
  private int _maxDepthToSave; //TODO better names
  private int _maxWidthToSave; //TODO better names

  public BinomialOptionModel(final BinomialOptionModelDefinition<OptionDefinition, T> model) {
    this(model, 1000);
  }

  public BinomialOptionModel(final BinomialOptionModelDefinition<OptionDefinition, T> model, final int n) {
    this(model, n, Math.min(5, n));
  }

  public BinomialOptionModel(final BinomialOptionModelDefinition<OptionDefinition, T> model, final int n, final int maxDepthToSave) {
    Validate.notNull(model, "model");
    ArgumentChecker.notNegativeOrZero(n, "n");
    ArgumentChecker.notNegative(maxDepthToSave, "max. depth to save");
    if (maxDepthToSave > n) {
      throw new IllegalArgumentException("Asked for tree to be saved to depth " + maxDepthToSave + " but will only have a tree of depth " + n);
    }
    _model = model;
    _n = n;
    _j = RecombiningBinomialTree.NODES.evaluate(_n);
    _maxDepthToSave = maxDepthToSave;
    _maxWidthToSave = RecombiningBinomialTree.NODES.evaluate(maxDepthToSave);
  }

  @Override
  public GreekResultCollection getGreeks(final OptionDefinition definition, final T data, final Set<Greek> requiredGreeks) {
    final Function1D<T, RecombiningBinomialTree<DoublesPair>> treeFunction = getTreeGeneratingFunction(definition);
    final GreekResultCollection results = new GreekResultCollection();
    final GreekVisitor<Double> visitor = getGreekVisitor(treeFunction, data, definition);
    for (final Greek greek : requiredGreeks) {
      final Double result = greek.accept(visitor);
      results.put(greek, result);
    }
    return results;
  }

  public GreekVisitor<Double> getGreekVisitor(final Function1D<T, RecombiningBinomialTree<DoublesPair>> treeFunction, final T data, final OptionDefinition definition) {
    final Function1D<T, Double> function = new Function1D<T, Double>() {

      @Override
      public Double evaluate(final T t) {
        return treeFunction.evaluate(t).getNode(0, 0).second;
      }

    };
    return new BinomialModelFiniteDifferenceGreekVisitor(treeFunction.evaluate(data), function, data, definition);
  }

  @Override
  public Function1D<T, RecombiningBinomialTree<DoublesPair>> getTreeGeneratingFunction(final OptionDefinition definition) {
    return new Function1D<T, RecombiningBinomialTree<DoublesPair>>() {

      @SuppressWarnings({"synthetic-access" })
      @Override
      public RecombiningBinomialTree<DoublesPair> evaluate(final T data) {
        final DoublesPair[] tempResults = new DoublesPair[_j];
        final DoublesPair[][] spotAndOptionPrices = new DoublesPair[_maxDepthToSave + 1][_maxWidthToSave];
        final OptionPayoffFunction<T> payoffFunction = definition.getPayoffFunction();
        final OptionExerciseFunction<T> exerciseFunction = definition.getExerciseFunction();
        final double u = _model.getUpFactor(definition, data, _n, _j);
        final double d = _model.getDownFactor(definition, data, _n, _j);
        final RecombiningBinomialTree<Double> pTree = _model.getUpProbabilityTree(definition, data, _n, _j);
        final double spot = data.getSpot();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        double newSpot = spot * Math.pow(d, _n);
        for (int i = 0; i < _j; i++) {
          tempResults[i] = DoublesPair.of(newSpot, payoffFunction.getPayoff((T) data.withSpot(newSpot), 0.));
          if (_n == _maxDepthToSave) {
            spotAndOptionPrices[_n][i] = tempResults[i];
          }
          newSpot *= u / d;
        }
        final double df = Math.exp(-r * t / _n);
        double optionValue, spotValue;
        T newData;
        double p;
        for (int i = _n - 1; i >= 0; i--) {
          for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
            p = pTree.getNode(i, j);
            optionValue = df * ((1 - p) * tempResults[j].second + p * tempResults[j + 1].second);
            spotValue = tempResults[j].first / d;
            newData = (T) data.withSpot(spotValue);
            tempResults[j] = DoublesPair.of(spotValue, exerciseFunction.shouldExercise(newData, optionValue) ? payoffFunction.getPayoff(newData, optionValue) : optionValue);
            if (i <= _maxDepthToSave) {
              spotAndOptionPrices[i][j] = tempResults[j];
            }
          }
        }
        return new RecombiningBinomialTree<>(spotAndOptionPrices);
      }
    };
  }

  /**
   * 
   */
  protected class BinomialModelFiniteDifferenceGreekVisitor extends FiniteDifferenceGreekVisitor<T, OptionDefinition> {
    private final RecombiningBinomialTree<DoublesPair> _tree;
    private final double _dt;

    @SuppressWarnings("synthetic-access")
    public BinomialModelFiniteDifferenceGreekVisitor(final RecombiningBinomialTree<DoublesPair> tree, final Function1D<T, Double> function, final T data, final OptionDefinition definition) {
      super(function, data, definition);
      _tree = tree;
      _dt = definition.getTimeToExpiry(data.getDate()) / _n;
    }

    @Override
    public Double visitDelta() {
      final DoublesPair node11 = _tree.getNode(1, 1);
      final DoublesPair node10 = _tree.getNode(1, 0);
      final double delta = (node11.second - node10.second) / (node11.first - node10.first);
      return delta;
    }

    @Override
    public Double visitGamma() {
      final DoublesPair node22 = _tree.getNode(2, 2);
      final DoublesPair node21 = _tree.getNode(2, 1);
      final DoublesPair node20 = _tree.getNode(2, 0);
      double gamma = (node22.second - node21.second) / (node22.first - node21.first) - (node21.second - node20.second) / (node21.first - node20.first);
      gamma /= 0.5 * (node22.first - node20.first);
      return gamma;
    }

    @Override
    public Double visitTheta() {
      final DoublesPair node21 = _tree.getNode(2, 1);
      final DoublesPair node00 = _tree.getNode(0, 0);
      return (node21.second - node00.second) / (2 * _dt);
    }
  }
}

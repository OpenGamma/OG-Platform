/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import java.util.Set;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.greeks.GreekVisitor;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.analytics.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.TrinomialOptionModelDefinition;
import com.opengamma.analytics.financial.model.option.pricing.FiniteDifferenceGreekVisitor;
import com.opengamma.analytics.financial.model.tree.RecombiningTrinomialTree;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 * @param <T>
 */
public class TrinomialOptionModel<T extends StandardOptionDataBundle> extends TreeOptionModel<OptionDefinition, T> {
  private final int _n;
  private final int _j;
  private final int _maxDepthToSave;
  private final int _maxWidthToSave;
  private final TrinomialOptionModelDefinition<OptionDefinition, T> _model;

  public TrinomialOptionModel(final TrinomialOptionModelDefinition<OptionDefinition, T> model) {
    this(model, 1000);
  }

  public TrinomialOptionModel(final TrinomialOptionModelDefinition<OptionDefinition, T> model, final int n) {
    this(model, n, Math.min(5, n));
  }

  public TrinomialOptionModel(final TrinomialOptionModelDefinition<OptionDefinition, T> model, final int n, final int maxDepthToSave) {
    ArgumentChecker.notNull(model, "model");
    ArgumentChecker.notNegativeOrZero(n, "n");
    ArgumentChecker.notNegative(maxDepthToSave, "max. depth to save");
    if (maxDepthToSave > n) {
      throw new IllegalArgumentException("Asked for tree to be saved to depth " + maxDepthToSave + " but the tree will only be " + n + " levels deep");
    }
    _model = model;
    _n = n;
    _j = RecombiningTrinomialTree.NODES.evaluate(_n);
    _maxDepthToSave = maxDepthToSave;
    _maxWidthToSave = RecombiningTrinomialTree.NODES.evaluate(maxDepthToSave);
  }

  @Override
  public GreekResultCollection getGreeks(final OptionDefinition definition, final T data, final Set<Greek> requiredGreeks) {
    final Function1D<T, RecombiningTrinomialTree<DoublesPair>> treeFunction = getTreeGeneratingFunction(definition);
    final Function1D<T, Double> function = new Function1D<T, Double>() {

      @Override
      public Double evaluate(final T t) {
        return treeFunction.evaluate(t).getNode(0, 0).second;
      }

    };
    final GreekResultCollection results = new GreekResultCollection();
    final GreekVisitor<Double> visitor = new FiniteDifferenceGreekVisitor<>(function, data, definition);
    for (final Greek greek : requiredGreeks) {
      final Double result = greek.accept(visitor);
      results.put(greek, result);
    }
    return results;
  }

  @Override
  public Function1D<T, RecombiningTrinomialTree<DoublesPair>> getTreeGeneratingFunction(final OptionDefinition definition) {
    return new Function1D<T, RecombiningTrinomialTree<DoublesPair>>() {

      @SuppressWarnings({"synthetic-access" })
      @Override
      public RecombiningTrinomialTree<DoublesPair> evaluate(final T data) {
        final DoublesPair[][] spotAndOptionPrices = new DoublesPair[_maxDepthToSave + 1][_maxWidthToSave];
        final OptionPayoffFunction<T> payoffFunction = definition.getPayoffFunction();
        final OptionExerciseFunction<T> exerciseFunction = definition.getExerciseFunction();
        final double u = _model.getUpFactor(definition, data, _n, _j);
        final double m = _model.getMidFactor(definition, data, _n, _j);
        final double d = _model.getDownFactor(definition, data, _n, _j);
        final double spot = data.getSpot();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double edx = Math.exp(_model.getDX(definition, data, _n, _j));
        final double df = Math.exp(-r * t / _n);
        double newSpot = spot * Math.pow(edx, -_n);
        final DoublesPair[] tempResults = new DoublesPair[_j];
        for (int i = 0; i < _j; i++) {
          tempResults[i] = DoublesPair.of(newSpot, payoffFunction.getPayoff((T) data.withSpot(newSpot), 0.));
          if (_n == _maxDepthToSave) {
            spotAndOptionPrices[_n][i] = tempResults[i];
          }
          newSpot *= edx;
        }
        double optionValue, spotValue;
        T newData;
        for (int i = _n - 1; i >= 0; i--) {
          for (int j = 1; j <= RecombiningTrinomialTree.NODES.evaluate(i); j++) {
            optionValue = df * (u * tempResults[j + 1].second + m * tempResults[j].second + d * tempResults[j - 1].second);
            spotValue = df * tempResults[j].first;
            newData = (T) data.withSpot(spotValue);
            tempResults[j - 1] = DoublesPair.of(spotValue, exerciseFunction.shouldExercise(newData, optionValue) ? payoffFunction.getPayoff(newData, optionValue) : optionValue);
            if (i <= _maxDepthToSave) {
              spotAndOptionPrices[i][j - 1] = tempResults[j - 1];
            }
          }
        }
        return new RecombiningTrinomialTree<>(spotAndOptionPrices);
      }

    };
  }
}

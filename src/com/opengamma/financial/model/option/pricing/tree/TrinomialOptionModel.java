/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

import java.util.Set;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.GreekVisitor;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.definition.TrinomialOptionModelDefinition;
import com.opengamma.financial.model.option.pricing.FiniteDifferenceGreekVisitor;
import com.opengamma.financial.model.tree.RecombiningTrinomialTree;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 * @param <T>
 */
public class TrinomialOptionModel<T extends StandardOptionDataBundle> extends TreeOptionModel<OptionDefinition, T> {
  private final int _n;
  private final int _j;
  private final TrinomialOptionModelDefinition<OptionDefinition, T> _model;

  public TrinomialOptionModel(final TrinomialOptionModelDefinition<OptionDefinition, T> model) {
    _model = model;
    _n = 1000;
    _j = RecombiningTrinomialTree.NODES.evaluate(_n);
  }

  public TrinomialOptionModel(final int n, final TrinomialOptionModelDefinition<OptionDefinition, T> model) {
    _model = model;
    _n = n;
    _j = RecombiningTrinomialTree.NODES.evaluate(_n);
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
    final GreekVisitor<Double> visitor = new FiniteDifferenceGreekVisitor<T, OptionDefinition>(function, data, definition);
    for (final Greek greek : requiredGreeks) {
      final Double result = greek.accept(visitor);
      results.put(greek, result);
    }
    return results;
  }

  @Override
  public Function1D<T, RecombiningTrinomialTree<DoublesPair>> getTreeGeneratingFunction(final OptionDefinition definition) {
    return new Function1D<T, RecombiningTrinomialTree<DoublesPair>>() {

      @SuppressWarnings({ "unchecked", "synthetic-access" })
      @Override
      public RecombiningTrinomialTree<DoublesPair> evaluate(final T data) {
        final DoublesPair[][] spotAndOptionPrices = new DoublesPair[_n + 1][_j];
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
        for (int i = 0; i < _j; i++) {
          spotAndOptionPrices[_n][i] = DoublesPair.of(newSpot, payoffFunction.getPayoff((T) data.withSpot(newSpot), 0.));
          newSpot *= edx;
        }
        double optionValue, spotValue;
        T newData;
        for (int i = _n - 1; i >= 0; i--) {
          for (int j = 1; j <= RecombiningTrinomialTree.NODES.evaluate(i); j++) {
            optionValue =
                df
                    * (u * spotAndOptionPrices[i + 1][j + 1].second + m * spotAndOptionPrices[i + 1][j].second + d
                        * spotAndOptionPrices[i + 1][j - 1].second);
            spotValue = df * spotAndOptionPrices[i + 1][j].first;
            newData = (T) data.withSpot(spotValue);
            spotAndOptionPrices[i][j - 1] =
              DoublesPair.of(spotValue, exerciseFunction.shouldExercise(newData, optionValue) ? payoffFunction.getPayoff(newData, optionValue) : optionValue);
          }
        }
        return new RecombiningTrinomialTree<DoublesPair>(spotAndOptionPrices);
      }

    };
  }
}

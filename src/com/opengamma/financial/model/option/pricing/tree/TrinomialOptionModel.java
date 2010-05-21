/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class TrinomialOptionModel<T extends StandardOptionDataBundle> extends TreeOptionModel<OptionDefinition, T> {
  protected final int _n;
  protected final int _j;
  protected TrinomialOptionModelDefinition<OptionDefinition, T> _model;

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
    final Function1D<T, RecombiningTrinomialTree<Pair<Double, Double>>> treeFunction = getTreeGeneratingFunction(definition);
    final Function1D<T, Double> function = new Function1D<T, Double>() {

      @Override
      public Double evaluate(final T t) {
        return treeFunction.evaluate(t).getNode(0, 0).getSecond();
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
  public Function1D<T, RecombiningTrinomialTree<Pair<Double, Double>>> getTreeGeneratingFunction(final OptionDefinition definition) {
    return new Function1D<T, RecombiningTrinomialTree<Pair<Double, Double>>>() {

      @SuppressWarnings("unchecked")
      @Override
      public RecombiningTrinomialTree<Pair<Double, Double>> evaluate(final T data) {
        final Pair<Double, Double>[][] spotAndOptionPrices = new Pair[_n + 1][_j];
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
          spotAndOptionPrices[_n][i] = Pair.of(newSpot, payoffFunction.getPayoff((T) data.withSpot(newSpot), 0.));
          newSpot *= edx;
        }
        Double optionValue, spotValue;
        T newData;
        for (int i = _n - 1; i >= 0; i--) {
          for (int j = 1; j <= RecombiningTrinomialTree.NODES.evaluate(i); j++) {
            optionValue = df
                * (u * spotAndOptionPrices[i + 1][j + 1].getSecond() + m * spotAndOptionPrices[i + 1][j].getSecond() + d * spotAndOptionPrices[i + 1][j - 1].getSecond());
            spotValue = df * spotAndOptionPrices[i + 1][j].getFirst();
            newData = (T) data.withSpot(spotValue);
            spotAndOptionPrices[i][j - 1] = Pair.of(spotValue, exerciseFunction.shouldExercise(newData, optionValue) ? payoffFunction.getPayoff(newData,
                optionValue) : optionValue);
          }
        }
        return new RecombiningTrinomialTree<Pair<Double, Double>>(spotAndOptionPrices);
      }

    };
  }
}

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
import com.opengamma.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.FiniteDifferenceGreekVisitor;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 * @param <T>
 */
public class BinomialOptionModel<T extends StandardOptionDataBundle> extends TreeOptionModel<OptionDefinition, T> {
  private final int _n;
  private final int _j;
  private final BinomialOptionModelDefinition<OptionDefinition, T> _model;

  public BinomialOptionModel(final BinomialOptionModelDefinition<OptionDefinition, T> model) {
    _model = model;
    _n = 1000;
    _j = RecombiningBinomialTree.NODES.evaluate(_n);
  }

  public BinomialOptionModel(final int n, final BinomialOptionModelDefinition<OptionDefinition, T> model) {
    _model = model;
    _n = n;
    _j = RecombiningBinomialTree.NODES.evaluate(_n);
  }

  public GreekResultCollection getGreeks(final OptionDefinition definition, final T data, final Set<Greek> requiredGreeks) {
    final Function1D<T, RecombiningBinomialTree<Pair<Double, Double>>> treeFunction = getTreeGeneratingFunction(definition);
    final GreekResultCollection results = new GreekResultCollection();
    final GreekVisitor<Double> visitor = getGreekVisitor(treeFunction, data, definition);
    for (final Greek greek : requiredGreeks) {
      final Double result = greek.accept(visitor);
      results.put(greek, result);
    }
    return results;
  }

  public GreekVisitor<Double> getGreekVisitor(final Function1D<T, RecombiningBinomialTree<Pair<Double, Double>>> treeFunction, final T data,
      final OptionDefinition definition) {
    final Function1D<T, Double> function = new Function1D<T, Double>() {

      @Override
      public Double evaluate(final T t) {
        return treeFunction.evaluate(t).getNode(0, 0).getSecond();
      }

    };
    return new BinomialModelFiniteDifferenceGreekVisitor(treeFunction.evaluate(data), function, data, definition);
  }

  @Override
  public Function1D<T, RecombiningBinomialTree<Pair<Double, Double>>> getTreeGeneratingFunction(final OptionDefinition definition) {
    return new Function1D<T, RecombiningBinomialTree<Pair<Double, Double>>>() {

      @SuppressWarnings({ "unchecked", "synthetic-access" })
      @Override
      public RecombiningBinomialTree<Pair<Double, Double>> evaluate(final T data) {
        final Pair<Double, Double>[][] spotAndOptionPrices = new Pair[_n + 1][_j];
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
          spotAndOptionPrices[_n][i] = Pair.of(newSpot, payoffFunction.getPayoff((T) data.withSpot(newSpot), 0.));
          newSpot *= u / d;
        }
        final double df = Math.exp(-r * t / _n);
        Double optionValue, spotValue;
        T newData;
        double p;
        for (int i = _n - 1; i >= 0; i--) {
          for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
            p = pTree.getNode(i, j);
            optionValue = df * ((1 - p) * spotAndOptionPrices[i + 1][j].getSecond() + p * spotAndOptionPrices[i + 1][j + 1].getSecond());
            spotValue = spotAndOptionPrices[i + 1][j].getFirst() / d;
            newData = (T) data.withSpot(spotValue);
            spotAndOptionPrices[i][j] =
                Pair.of(spotValue, exerciseFunction.shouldExercise(newData, optionValue) ? payoffFunction.getPayoff(newData, optionValue) : optionValue);
          }
        }
        return new RecombiningBinomialTree<Pair<Double, Double>>(spotAndOptionPrices);
      }
    };
  }

  /**
   * 
   */
  protected class BinomialModelFiniteDifferenceGreekVisitor extends FiniteDifferenceGreekVisitor<T, OptionDefinition> {
    private final RecombiningBinomialTree<Pair<Double, Double>> _tree;
    private final double _dt;

    @SuppressWarnings("synthetic-access")
    public BinomialModelFiniteDifferenceGreekVisitor(final RecombiningBinomialTree<Pair<Double, Double>> tree, final Function1D<T, Double> function,
        final T data, final OptionDefinition definition) {
      super(function, data, definition);
      _tree = tree;
      _dt = definition.getTimeToExpiry(data.getDate()) / _n;
    }

    @Override
    public Double visitDelta() {
      final Pair<Double, Double> node11 = _tree.getNode(1, 1);
      final Pair<Double, Double> node10 = _tree.getNode(1, 0);
      final double delta = (node11.getSecond() - node10.getSecond()) / (node11.getFirst() - node10.getFirst());
      return delta;
    }

    @Override
    public Double visitGamma() {
      final Pair<Double, Double> node22 = _tree.getNode(2, 2);
      final Pair<Double, Double> node21 = _tree.getNode(2, 1);
      final Pair<Double, Double> node20 = _tree.getNode(2, 0);
      double gamma =
          (node22.getSecond() - node21.getSecond()) / (node22.getFirst() - node21.getFirst()) - (node21.getSecond() - node20.getSecond())
              / (node21.getFirst() - node20.getFirst());
      gamma /= 0.5 * (node22.getFirst() - node20.getFirst());
      return gamma;
    }

    @Override
    public Double visitTheta() {
      final Pair<Double, Double> node21 = _tree.getNode(2, 1);
      final Pair<Double, Double> node00 = _tree.getNode(0, 0);
      return (node21.getSecond() - node00.getSecond()) / (2 * _dt);
    }
  }
}

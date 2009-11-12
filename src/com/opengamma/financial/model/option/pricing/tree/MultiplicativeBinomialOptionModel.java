/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

import com.opengamma.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 * 
 */
public class MultiplicativeBinomialOptionModel extends TreeOptionModel<OptionDefinition, StandardOptionDataBundle> {
  protected final int _n;

  public MultiplicativeBinomialOptionModel(final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> model) {
    super(model);
    _n = 1000;
  }

  public MultiplicativeBinomialOptionModel(final int n, final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> model) {
    super(model);
    _n = n;
  }

  @Override
  public Function1D<StandardOptionDataBundle, RecombiningBinomialTree<Pair<Double, Double>>> getTreeGeneratingFunction(final OptionDefinition definition) {
    return new Function1D<StandardOptionDataBundle, RecombiningBinomialTree<Pair<Double, Double>>>() {

      @SuppressWarnings("unchecked")
      @Override
      public RecombiningBinomialTree<Pair<Double, Double>> evaluate(final StandardOptionDataBundle data) {
        // TODO move into constructor
        final int nodesAtMaturity = RecombiningBinomialTree.NODES.evaluate(_n);
        final Pair<Double, Double>[][] spotAndOptionPrices = new Pair[_n + 1][nodesAtMaturity];
        final OptionPayoffFunction<StandardOptionDataBundle> payoffFunction = definition.getPayoffFunction();
        final OptionExerciseFunction<StandardOptionDataBundle> exerciseFunction = definition.getExerciseFunction();
        final double u = _model.getUpFactor(definition, data, _n);
        final double d = _model.getDownFactor(definition, data, _n);
        final double p = _model.getProbability(definition, data, _n);
        final double spot = data.getSpot();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        double newSpot = spot * Math.pow(d, _n);
        for (int i = 0; i < nodesAtMaturity; i++) {
          spotAndOptionPrices[_n][i] = new Pair<Double, Double>(newSpot, payoffFunction.getPayoff(data.withSpot(newSpot), 0.));
          newSpot *= u / d;
        }
        final double df = Math.exp(-r * t / _n);
        Double optionValue, spotValue;
        StandardOptionDataBundle newData;
        for (int i = _n - 1; i >= 0; i--) {
          for (int j = 0; j < RecombiningBinomialTree.NODES.evaluate(i); j++) {
            optionValue = df * ((1 - p) * spotAndOptionPrices[i + 1][j].getSecond() + p * spotAndOptionPrices[i + 1][j + 1].getSecond());
            spotValue = spotAndOptionPrices[i + 1][j].getFirst() / d;
            newData = data.withSpot(spotValue);
            spotAndOptionPrices[i][j] = new Pair<Double, Double>(spotValue, exerciseFunction.shouldExercise(newData, optionValue) ? payoffFunction.getPayoff(newData, optionValue)
                : optionValue);
          }
        }
        return new RecombiningBinomialTree<Pair<Double, Double>>(spotAndOptionPrices);
      }
    };
  }
}

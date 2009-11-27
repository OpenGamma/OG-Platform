/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

import java.util.List;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionModel;
import com.opengamma.financial.model.tree.RecombiningTree;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 * 
 */
public abstract class TreeOptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> implements OptionModel<T, U> {
  protected final BinomialOptionModelDefinition<T, U> _model;

  public TreeOptionModel(final BinomialOptionModelDefinition<T, U> model) {
    _model = model;
  }

  @Override
  public GreekResultCollection getGreeks(final T definition, final U data, final List<Greek> requiredGreeks) {
    final RecombiningTree<Pair<Double, Double>> tree = getTreeGeneratingFunction(definition).evaluate(data);
    final GreekResultCollection greeks = new GreekResultCollection();
    greeks.put(Greek.PRICE, new SingleGreekResult(tree.getNode(0, 0).getSecond()));
    return greeks;
  }

  public abstract Function1D<U, ? extends RecombiningTree<Pair<Double, Double>>> getTreeGeneratingFunction(T definition);
}

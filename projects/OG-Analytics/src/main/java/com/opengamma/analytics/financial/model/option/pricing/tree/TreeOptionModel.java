/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.OptionModel;
import com.opengamma.analytics.financial.model.tree.RecombiningTree;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 * @param <T>
 * @param <U>
 */
public abstract class TreeOptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> implements OptionModel<T, U> {

  public abstract Function1D<U, ? extends RecombiningTree<DoublesPair>> getTreeGeneratingFunction(T definition);
}

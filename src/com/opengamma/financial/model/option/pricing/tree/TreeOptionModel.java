/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

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

  public abstract Function1D<U, ? extends RecombiningTree<Pair<Double, Double>>> getTreeGeneratingFunction(T definition);
}

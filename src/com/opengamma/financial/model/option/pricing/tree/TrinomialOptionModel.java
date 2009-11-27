/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

import java.util.List;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.tree.RecombiningTrinomialTree;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 * 
 */

public class TrinomialOptionModel extends TreeOptionModel<OptionDefinition, StandardOptionDataBundle> {

  public TrinomialOptionModel() {
    super(null);
  }

  @Override
  public GreekResultCollection getGreeks(final OptionDefinition definition, final StandardOptionDataBundle vars, final List<Greek> requiredGreeks) {
    return null;
  }

  @Override
  public Function1D<StandardOptionDataBundle, RecombiningTrinomialTree<Pair<Double, Double>>> getTreeGeneratingFunction(final OptionDefinition definition) {
    // TODO Auto-generated method stub
    return null;
  }

}

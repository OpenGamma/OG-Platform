package com.opengamma.financial.model.option.pricing.tree;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionModel;

/**
 * 
 * @author emcleod
 * 
 */
public abstract class TreeOptionModel<T extends OptionDefinition<U>, U extends StandardOptionDataBundle> implements OptionModel<T, U> {

  // TODO get trees
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.local;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;

/**
 * 
 * @param <T> The option definition type
 * @param <U> The option data bundle type
 */
public interface ImpliedTreeModel<T extends OptionDefinition, U extends StandardOptionDataBundle> {

  ImpliedTreeResult getImpliedTrees(T option, U data);
}

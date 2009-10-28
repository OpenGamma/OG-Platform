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

/**
 * 
 * @author emcleod
 * 
 */
public class BinomialOptionModel extends TreeOptionModel<OptionDefinition, StandardOptionDataBundle> {
  private static final int N = 1000;

  @Override
  public GreekResultCollection getGreeks(final OptionDefinition definition, final StandardOptionDataBundle vars, final List<Greek> requiredGreeks) {
    return null;
  }

}

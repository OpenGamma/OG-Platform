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

public class TrinomialOptionModel extends TreeOptionModel<OptionDefinition<StandardOptionDataBundle>, StandardOptionDataBundle> {

  @Override
  public GreekResultCollection getGreeks(OptionDefinition<StandardOptionDataBundle> definition, StandardOptionDataBundle vars, List<Greek> requiredGreeks) {
    return null;
  }

}

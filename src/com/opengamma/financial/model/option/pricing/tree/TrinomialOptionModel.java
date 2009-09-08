package com.opengamma.financial.model.option.pricing.tree;

import java.util.Map;

import com.opengamma.financial.greeks.Greek.GreekType;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;

/**
 * 
 * @author emcleod
 * 
 */

public class TrinomialOptionModel extends TreeOptionModel<OptionDefinition, StandardOptionDataBundle> {

  @Override
  public Map<GreekType, Double> getGreeks(OptionDefinition definition, StandardOptionDataBundle vars) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getPrice(OptionDefinition definition, StandardOptionDataBundle vars) {
    // TODO Auto-generated method stub
    return 0;
  }

}

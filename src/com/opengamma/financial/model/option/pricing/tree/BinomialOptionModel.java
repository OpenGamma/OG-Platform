package com.opengamma.financial.model.option.pricing.tree;

import java.util.List;
import java.util.Map;

import com.opengamma.financial.greeks.Greek;
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
  public Map<Greek, Map<String, Double>> getGreeks(OptionDefinition definition, StandardOptionDataBundle vars, List<Greek> requiredGreeks) {
    // TODO Auto-generated method stub
    return null;
  }

}

package com.opengamma.financial.model.option.pricing;

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

public interface OptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> {

  public Map<Greek, Map<String, Double>> getGreeks(T definition, U vars, List<Greek> requiredGreeks);

}

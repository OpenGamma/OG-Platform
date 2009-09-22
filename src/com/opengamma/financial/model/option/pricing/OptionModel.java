package com.opengamma.financial.model.option.pricing;

import java.util.Map;

import com.opengamma.financial.greeks.Greek.GreekType;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;

/**
 * 
 * @author emcleod
 * 
 */

public interface OptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> {

  public Map<GreekType, Double> getGreeks(T definition, U vars);

  public double getPrice(T definition, U vars);
}

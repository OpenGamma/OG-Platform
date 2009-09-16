package com.opengamma.financial.model.volatility;

import java.util.Map;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

public interface VolatilitySurfaceModel<S extends OptionDefinition, T extends OptionDefinition, U extends StandardOptionDataBundle> {

  public VolatilitySurface getSurface(S definition, Map<T, Double> prices, U data) throws OptionPricingException;
}

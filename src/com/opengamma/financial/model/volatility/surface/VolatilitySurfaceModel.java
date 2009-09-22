/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;

public interface VolatilitySurfaceModel<T extends OptionDefinition, U extends StandardOptionDataBundle> {

  public VolatilitySurface getSurface(Map<T, Double> prices, U data);
}

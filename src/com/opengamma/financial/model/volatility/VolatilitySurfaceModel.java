package com.opengamma.financial.model.volatility;

import java.util.Date;
import java.util.Map;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

public interface VolatilitySurfaceModel<T extends OptionDefinition> {
  // TODO this is the wrong name
  public VolatilitySurface getSurface(T definition, Map<T, Object[]> data, Date date);
}

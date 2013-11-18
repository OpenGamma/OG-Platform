/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 * Extracts the structure id for a volatility surface from a value specification.
 */
public class VolatilitySurfaceNodeExtractor extends NodeExtractor<VolatilitySurfaceKey> {

  /**
   * Creates a new extractor matching on {@link ValueRequirementNames#VOLATILITY_SURFACE}.
   * 
   * @param valueName The value name to match
   */
  public VolatilitySurfaceNodeExtractor(String valueName) {
    super(valueName);
  }

  @Override
  public StructureIdentifier<VolatilitySurfaceKey> getStructuredIdentifier(final ValueSpecification spec) {
    final UniqueId uniqueId = spec.getTargetSpecification().getUniqueId();
    final String surface = getProperty(spec, ValuePropertyNames.SURFACE);
    final String instrumentType = getProperty(spec, "InstrumentType");
    final String quoteType = getProperty(spec, SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
    final String quoteUnits = getProperty(spec, SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS);
    final VolatilitySurfaceKey key = VolatilitySurfaceKey.of(uniqueId, surface, instrumentType, quoteType, quoteUnits);
    return StructureIdentifier.of(key);
  }

}

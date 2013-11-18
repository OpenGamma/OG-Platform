/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

/**
 * Extracts the structure id for a volatility cube from a value specification.
 */
public class VolatilityCubeNodeExtractor extends NodeExtractor<VolatilityCubeKey> {

  /**
   * Creates a new extractor matching on {@link ValueRequirementNames#VOLATILITY_CUBE}.
   */
  public VolatilityCubeNodeExtractor() {
    super(ValueRequirementNames.VOLATILITY_CUBE);
  }

  @Override
  public StructureIdentifier<VolatilityCubeKey> getStructuredIdentifier(final ValueSpecification spec) {
    final Currency currency = Currency.parse(spec.getTargetSpecification().getUniqueId().getValue());
    final String cube = getProperty(spec, ValuePropertyNames.CUBE);
    if (cube == null) {
      return null;
    }
    final VolatilityCubeKey key = VolatilityCubeKey.of(currency, cube);
    return StructureIdentifier.of(key);
  }
}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.StructureIdentifier;
import com.opengamma.engine.marketdata.manipulator.StructureType;

/**
 *
 */
/* package */ class VolatilitySurfaceSelector implements DistinctMarketDataSelector {

  private final Set<String> _names;
  private final Pattern _nameRegex;
  private final Set<String> _instrumentTypes;
  private final Set<String> _quoteTypes;
  private final Set<String> _quoteUnits;

  /* package */ VolatilitySurfaceSelector(Set<String> names,
                                          Pattern nameRegex,
                                          Set<String> instrumentTypes,
                                          Set<String> quoteTypes,
                                          Set<String> quoteUnits) {
    _names = names;
    _nameRegex = nameRegex;
    _instrumentTypes = instrumentTypes;
    _quoteTypes = quoteTypes;
    _quoteUnits = quoteUnits;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId,
                                                         String calculationConfigurationName) {
    // TODO implement findMatchingSelector()
    throw new UnsupportedOperationException("findMatchingSelector not implemented");
  }

  @Override
  public Set<StructureType> getApplicableStructureTypes() {
    // TODO implement getApplicableStructureTypes()
    throw new UnsupportedOperationException("getApplicableStructureTypes not implemented");
  }
}

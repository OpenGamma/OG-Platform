/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.StructureIdentifier;
import com.opengamma.engine.marketdata.manipulator.StructureType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class PointSelector implements DistinctMarketDataSelector {

  private static final ImmutableSet<StructureType> STRUCTURE_TYPES = ImmutableSet.of(StructureType.MARKET_DATA_POINT);

  private final ExternalId _id;
  private final String _calcConfigName;

  private PointSelector(ExternalId id, String calcConfigName) {
    ArgumentChecker.notNull(id, "id");
    _calcConfigName = calcConfigName;
    _id = id;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId, String calcConfigName) {
    if (!_calcConfigName.equals(calcConfigName)) {
      return null;
    }
    Object value = structureId.getValue();
    if (!(value instanceof ExternalId)) {
      return null;
    }
    if (!_id.equals(value)) {
      return null;
    }
    return this;
  }

  @Override
  public Set<StructureType> getApplicableStructureTypes() {
    return STRUCTURE_TYPES;
  }

  public static class Builder {

    private final String _calcConfigName;
    private final Scenario _scenario;

    private ExternalId _id;

    /* package */ Builder(Scenario scenario, String calcConfigName) {
      _calcConfigName = calcConfigName;
      _scenario = scenario;
    }

    public PointManipulator.Builder apply() {
      if (_id == null) {
        throw new IllegalStateException("No ID specified");
      }
      PointSelector selector = new PointSelector(_id, _calcConfigName);
      return new PointManipulator.Builder(selector, _scenario);
    }

    public Builder id(String scheme, String value) {
      if (_id != null) {
        throw new IllegalStateException("id() can only be called once");
      }
      _id = ExternalId.of(scheme, value);
      return this;
    }
  }
}

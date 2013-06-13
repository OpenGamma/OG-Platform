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

  /** Types of structured data selected by this class. */
  private static final ImmutableSet<StructureType> STRUCTURE_TYPES = ImmutableSet.of(StructureType.MARKET_DATA_POINT);

  /** ID of the market data point to be manipulated. */
  private final ExternalId _id;
  // TODO regex pattern for ID value
  /** Calculation configuration to which the manipulation should be applied. */
  private final String _calcConfigName;

  /* package */ PointSelector(ExternalId id, String calcConfigName) {
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

  /* package */ ExternalId getId() {
    return _id;
  }

  /* package */ String getCalculationConfigurationName() {
    return _calcConfigName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PointSelector that = (PointSelector) o;

    if (!_calcConfigName.equals(that._calcConfigName)) {
      return false;
    }
    return _id.equals(that._id);
  }

  @Override
  public int hashCode() {
    int result = _id.hashCode();
    result = 31 * result + _calcConfigName.hashCode();
    return result;
  }

  public static class Builder {

    /** Calculation configuration to which the manipulation should be applied. */
    private final String _calcConfigName;
    /** Scenario that the transformation will be added to. */
    private final Scenario _scenario;

    /** ID of the market data point to be manipulated. */
    private ExternalId _id;
    // TODO regex pattern for ID value

    /* package */ Builder(Scenario scenario, String calcConfigName) {
      _calcConfigName = calcConfigName;
      _scenario = scenario;
    }

    public PointManipulatorBuilder apply() {
      if (_id == null) {
        throw new IllegalStateException("No ID specified");
      }
      PointSelector selector = new PointSelector(_id, _calcConfigName);
      return new PointManipulatorBuilder(selector, _scenario);
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

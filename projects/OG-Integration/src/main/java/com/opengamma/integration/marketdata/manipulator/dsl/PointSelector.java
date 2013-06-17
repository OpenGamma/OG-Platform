/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.StructureIdentifier;
import com.opengamma.engine.marketdata.manipulator.StructureType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * Selects raw market data points that will be manipulated.
 */
public class PointSelector implements DistinctMarketDataSelector {

  /** Types of structured data selected by this class. */
  private static final ImmutableSet<StructureType> STRUCTURE_TYPES = ImmutableSet.of(StructureType.MARKET_DATA_POINT);

  /** ID of the market data point to be manipulated. */
  private final Set<ExternalId> _ids;
  /** Calculation configuration to which the manipulation should be applied. */
  private final String _calcConfigName;
  /** External ID scheme used when pattern matching ID value. */
  private final ExternalScheme _idMatchScheme;
  /** Regex pattern for matching ID value. */
  private final Pattern _idValuePattern;

  /* package */ PointSelector(String calcConfigName,
                              Set<ExternalId> ids,
                              ExternalScheme idMatchScheme,
                              Pattern idValuePattern) {
    ArgumentChecker.notEmpty(calcConfigName, "calcConfigName");
    if (idMatchScheme == null && idValuePattern != null || idMatchScheme != null && idValuePattern == null) {
      throw new IllegalArgumentException("Scheme and pattern must both be specified to pattern match on ID");
    }
    _idMatchScheme = idMatchScheme;
    _idValuePattern = idValuePattern;
    _calcConfigName = calcConfigName;
    _ids = ids;
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
    if (_ids != null && !_ids.contains(value)) {
      return null;
    }
    return this;
  }

  @Override
  public Set<StructureType> getApplicableStructureTypes() {
    return STRUCTURE_TYPES;
  }

  /* package */ Set<ExternalId> getIds() {
    return _ids;
  }

  /* package */ String getCalculationConfigurationName() {
    return _calcConfigName;
  }

  /* package */ ExternalScheme getIdMatchScheme() {
    return _idMatchScheme;
  }

  /* package */ Pattern getIdValuePattern() {
    return _idValuePattern;
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

    if (_calcConfigName != null ? !_calcConfigName.equals(that._calcConfigName) : that._calcConfigName != null) {
      return false;
    }
    if (_idMatchScheme != null ? !_idMatchScheme.equals(that._idMatchScheme) : that._idMatchScheme != null) {
      return false;
    }
    String thisPattern = _idValuePattern == null ? null : _idValuePattern.pattern();
    String thatPattern = that._idValuePattern == null ? null : that._idValuePattern.pattern();
    if (thisPattern != null) {
      if (!thisPattern.equals(thatPattern)) {
        return false;
      }
    } else {
      if (thatPattern != null) {
        return false;
      }
    }
    if (_ids != null ? !_ids.equals(that._ids) : that._ids != null) {
      return false;
    }

    return true;
  }
  @Override
  public int hashCode() {
    int result = _ids != null ? _ids.hashCode() : 0;
    result = 31 * result + (_calcConfigName != null ? _calcConfigName.hashCode() : 0);
    result = 31 * result + (_idMatchScheme != null ? _idMatchScheme.hashCode() : 0);
    result = 31 * result + (_idValuePattern != null ? _idValuePattern.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PointSelector [" +
        "_ids=" + _ids +
        ", _calcConfigName='" + _calcConfigName + "'" +
        ", _idMatchScheme=" + _idMatchScheme +
        ", _idValuePattern=" + _idValuePattern +
        "]";
  }

  /**
   * Mutable builder to create {@link PointSelector}s.
   */
  public static class Builder {

    /** Calculation configuration to which the manipulation should be applied. */
    private final String _calcConfigName;
    /** Scenario that the transformation will be added to. */
    private final Scenario _scenario;

    /** ID of the market data point to be manipulated. */
    private Set<ExternalId> _ids;
    /** External ID scheme used when pattern matching ID value. */
    private ExternalScheme _idMatchScheme;
    /** Regex pattern for matching ID value. */
    private Pattern _idValuePattern;

    /* package */ Builder(Scenario scenario, String calcConfigName) {
      _calcConfigName = calcConfigName;
      _scenario = scenario;
    }

    /**
     * @return A selector built from this object's data.
     */
    public PointManipulatorBuilder apply() {
      PointSelector selector = new PointSelector(_calcConfigName, _ids, _idMatchScheme, _idValuePattern);
      return new PointManipulatorBuilder(selector, _scenario);
    }

    /**
     * Adds a test for the market data ID value to match exactly.
     * @param scheme External ID scheme that must match the market data's ID scheme
     * @param value External ID value that must match the market data's ID value
     * @return This builder
     */
    public Builder id(String scheme, String value) {
      ArgumentChecker.notEmpty(scheme, "scheme");
      ArgumentChecker.notEmpty(value, "value");
      if (_ids != null) {
        throw new IllegalStateException("id() or ids() can only be called once");
      }
      _ids = ImmutableSet.of(ExternalId.of(scheme, value));
      return this;
    }


    /**
     * Adds a test for the market data ID value to match exactly.
     * @param ids The IDs to match
     * @return This builder
     */
    public Builder ids(ExternalId... ids) {
      ArgumentChecker.notEmpty(ids, "ids");
      if (_ids != null) {
        throw new IllegalStateException("id() or ids() can only be called once");
      }
      _ids = ImmutableSet.copyOf(ids);
      return this;
    }

    /**
     * Adds a test for the market data ID value to match a regular expression.
     * @param scheme External ID scheme that must match the market data's ID scheme
     * @param valueRegex Regular expression that must match the market data's ID value
     * @return This builder
     */
    public Builder idMatches(String scheme, String valueRegex) {
      ArgumentChecker.notEmpty(scheme, "scheme");
      ArgumentChecker.notEmpty(valueRegex, "valueRegex");
      if (scheme != null) {
        throw new IllegalStateException("idMatches can only be called once");
      }
      _idMatchScheme = ExternalScheme.of(scheme);
      _idValuePattern = Pattern.compile(valueRegex);
      return this;
    }
  }
}

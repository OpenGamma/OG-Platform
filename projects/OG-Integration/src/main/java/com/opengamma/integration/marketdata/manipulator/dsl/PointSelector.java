/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
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
  /** Calc configs to which this selector will apply, null will match any config. */
  private final Set<String> _calcConfigNames;
  /** External ID scheme used when pattern matching ID value. */
  private final ExternalScheme _idMatchScheme;
  /** Regex pattern for matching ID value. */
  private final Pattern _idValuePattern;

  /* package */ PointSelector(Set<String> calcConfigNames,
                              Set<ExternalId> ids,
                              ExternalScheme idMatchScheme,
                              Pattern idValuePattern) {
    if (idMatchScheme == null && idValuePattern != null || idMatchScheme != null && idValuePattern == null) {
      throw new IllegalArgumentException("Scheme and pattern must both be specified to pattern match on ID");
    }
    _idMatchScheme = idMatchScheme;
    _idValuePattern = idValuePattern;
    _calcConfigNames = calcConfigNames;
    _ids = ids;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId, String calcConfigName) {
    if (_calcConfigNames != null && !_calcConfigNames.contains(calcConfigName)) {
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

  /* package */ Set<String> getCalculationConfigurationNames() {
    return _calcConfigNames;
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

    if (_calcConfigNames != null ? !_calcConfigNames.equals(that._calcConfigNames) : that._calcConfigNames != null) {
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
    result = 31 * result + (_calcConfigNames != null ? _calcConfigNames.hashCode() : 0);
    result = 31 * result + (_idMatchScheme != null ? _idMatchScheme.hashCode() : 0);
    result = 31 * result + (_idValuePattern != null ? _idValuePattern.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PointSelector [" +
        "_ids=" + _ids +
        ", _calcConfigName='" + _calcConfigNames + "'" +
        ", _idMatchScheme=" + _idMatchScheme +
        ", _idValuePattern=" + _idValuePattern +
        "]";
  }

  /**
   * Mutable builder to create {@link PointSelector}s.
   */
  public static class Builder {
    /** Scenario that the transformation will be added to. */
    private final Scenario _scenario;

    /** ID of the market data point to be manipulated. */
    private Set<ExternalId> _ids;
    /** External ID scheme used when pattern matching ID value. */
    private ExternalScheme _idMatchScheme;
    /** Regex pattern for matching ID value. */
    private Pattern _idValuePattern;

    /* package */ Builder(Scenario scenario) {
      _scenario = scenario;
    }

    /**
     * @return A selector built from this object's data.
     */
    public PointManipulatorBuilder apply() {
      return new PointManipulatorBuilder(_scenario, getSelector());
    }

    /* package */ PointSelector getSelector() {
      return new PointSelector(_scenario.getCalcConfigNames(), _ids, _idMatchScheme, _idValuePattern);
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
     * @param ids The external IDs to match
     * @return This builder
     */
    public Builder ids(String... ids) {
      ArgumentChecker.notEmpty(ids, "ids");
      ArgumentChecker.notEmpty(ids, "ids");
      if (_ids != null) {
        throw new IllegalStateException("id() or ids() can only be called once");
      }
      Set<ExternalId> idSet = Sets.newHashSetWithExpectedSize(ids.length);
      for (String id : ids) {
        idSet.add(ExternalId.parse(id));
      }
      _ids = Collections.unmodifiableSet(idSet);
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
      if (_idMatchScheme != null) {
        throw new IllegalStateException("idMatches can only be called once");
      }
      _idMatchScheme = ExternalScheme.of(scheme);
      _idValuePattern = Pattern.compile(valueRegex);
      return this;
    }

    /* package */ Scenario getScenario() {
      return _scenario;
    }
  }
}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.SelectorResolver;
import com.opengamma.engine.marketdata.manipulator.StructureIdentifier;
import com.opengamma.engine.marketdata.manipulator.StructureType;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class VolatilitySurfaceSelector implements DistinctMarketDataSelector {

  private static final Set<StructureType> STRUCTURE_TYPES = ImmutableSet.of(StructureType.VOLATILITY_SURFACE);

  private final Set<String> _calcConfigNames;
  private final Set<String> _names;
  private final Pattern _nameRegex;
  private final Set<String> _instrumentTypes;
  private final Set<String> _quoteTypes;
  private final Set<String> _quoteUnits;

  /* package */ VolatilitySurfaceSelector(Set<String> calcConfigNames,
                                          Set<String> names,
                                          Pattern nameRegex,
                                          Set<String> instrumentTypes,
                                          Set<String> quoteTypes,
                                          Set<String> quoteUnits) {
    _calcConfigNames = calcConfigNames;
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
                                                         String calculationConfigurationName,
                                                         SelectorResolver resolver) {
    if (_calcConfigNames != null && !_calcConfigNames.contains(calculationConfigurationName)) {
      return null;
    }
    Object value = structureId.getValue();
    if (!(value instanceof VolatilitySurfaceKey)) {
      return null;
    }
    VolatilitySurfaceKey key = (VolatilitySurfaceKey) value;
    if (!contains(_names, key.getName())) {
      return null;
    }
    if (_nameRegex != null && !_nameRegex.matcher(key.getName()).matches()) {
      return null;
    }
    if (!contains(_instrumentTypes, key.getInstrumentType())) {
      return null;
    }
    if (!contains(_quoteTypes, key.getQuoteType())) {
      return null;
    }
    if (!contains(_quoteUnits, key.getQuoteUnits())) {
      return null;
    }
    return this;
  }

  @Override
  public Set<StructureType> getApplicableStructureTypes() {
    return STRUCTURE_TYPES;
  }

  private static boolean contains(Set<String> set, String str) {
    if (set == null) {
      return true;
    } else {
      return set.contains(str);
    }
  }

  /* package */ Set<String> getCalcConfigNames() {
    return _calcConfigNames;
  }

  /* package */ Set<String> getNames() {
    return _names;
  }

  /* package */ Pattern getNameRegex() {
    return _nameRegex;
  }

  /* package */ Set<String> getInstrumentTypes() {
    return _instrumentTypes;
  }

  /* package */ Set<String> getQuoteTypes() {
    return _quoteTypes;
  }

  /* package */ Set<String> getQuoteUnits() {
    return _quoteUnits;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VolatilitySurfaceSelector that = (VolatilitySurfaceSelector) o;

    if (_calcConfigNames != null ? !_calcConfigNames.equals(that._calcConfigNames) : that._calcConfigNames != null) {
      return false;
    }
    if (_instrumentTypes != null ? !_instrumentTypes.equals(that._instrumentTypes) : that._instrumentTypes != null) {
      return false;
    }
    String thisPattern = _nameRegex == null ? null : _nameRegex.pattern();
    String thatPattern = that._nameRegex == null ? null : that._nameRegex.pattern();
    if (thisPattern != null) {
      if (!thisPattern.equals(thatPattern)) {
        return false;
      }
    } else {
      if (thatPattern != null) {
        return false;
      }
    }
    if (_names != null ? !_names.equals(that._names) : that._names != null) {
      return false;
    }
    if (_quoteTypes != null ? !_quoteTypes.equals(that._quoteTypes) : that._quoteTypes != null) {
      return false;
    }
    if (_quoteUnits != null ? !_quoteUnits.equals(that._quoteUnits) : that._quoteUnits != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = _calcConfigNames != null ? _calcConfigNames.hashCode() : 0;
    result = 31 * result + (_names != null ? _names.hashCode() : 0);
    result = 31 * result + (_nameRegex != null ? _nameRegex.hashCode() : 0);
    result = 31 * result + (_instrumentTypes != null ? _instrumentTypes.hashCode() : 0);
    result = 31 * result + (_quoteTypes != null ? _quoteTypes.hashCode() : 0);
    result = 31 * result + (_quoteUnits != null ? _quoteUnits.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "VolatilitySurfaceSelector [" +
        "_calcConfigNames=" + _calcConfigNames +
        ", _names=" + _names +
        ", _nameRegex=" + _nameRegex +
        ", _instrumentTypes=" + _instrumentTypes +
        ", _quoteTypes=" + _quoteTypes +
        ", _quoteUnits=" + _quoteUnits +
        "]";
  }

  public static class Builder {

    /** The scenario to which manipulations are added. */
    private final Scenario _scenario;

    private Set<String> _names;
    private Pattern _namePattern;
    private Set<String> _instrumentTypes;
    private Set<String> _quoteTypes;
    private Set<String> _quoteUnits;

    /* package */ Builder(Scenario scenario) {
      ArgumentChecker.notNull(scenario, "scenario");
      _scenario = scenario;
    }

    public VolatilitySurfaceManipulatorBuilder apply() {
      VolatilitySurfaceSelector selector = getSelector();
      return new VolatilitySurfaceManipulatorBuilder(_scenario, selector);
    }

    /* package */ VolatilitySurfaceSelector getSelector() {
      return new VolatilitySurfaceSelector(_scenario.getCalcConfigNames(), _names, _namePattern,
                                           _instrumentTypes, _quoteTypes, _quoteUnits);
    }

    public Builder named(String... names) {
      ArgumentChecker.notEmpty(names, "names");
      if (_names != null) {
        throw new IllegalStateException("named() can only be called once");
      }
      if (_namePattern != null) {
        throw new IllegalStateException("Can't specify exact name and a regular expression for the name");
      }
      _names = ImmutableSet.copyOf(names);
      return this;
    }

    public Builder nameMatches(String regex) {
      ArgumentChecker.notNull(regex, "regex");
      if (_namePattern != null) {
        throw new IllegalStateException("nameMatches() can only be called once");
      }
      if (_names != null) {
        throw new IllegalStateException("Can't specify exact name and a regular expression for the name");
      }
      _namePattern = Pattern.compile(regex);
      return this;
    }

    public Builder instrumentTypes(String... types) {
      ArgumentChecker.notEmpty(types, "types");
      if (_instrumentTypes != null) {
        throw new IllegalStateException("instrumentTypes() can only be called once");
      }
      _instrumentTypes = ImmutableSet.copyOf(types);
      return this;
    }

    public Builder quoteTypes(String... types) {
      ArgumentChecker.notEmpty(types, "types");
      if (_quoteTypes != null) {
        throw new IllegalStateException("quoteTypes() can only be called once");
      }
      _quoteTypes = ImmutableSet.copyOf(types);
      return this;
    }

    public Builder quoteUnits(String... units) {
      ArgumentChecker.notEmpty(units, "units");
      if (_quoteUnits != null) {
        throw new IllegalStateException("quoteUntis() can only be called once");
      }
      _quoteUnits = ImmutableSet.copyOf(units);
      return this;
    }

    /* package */ Scenario getScenario() {
      return _scenario;
    }
  }
}

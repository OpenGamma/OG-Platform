/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.opengamma.engine.value.ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_SURFACE;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.SelectorResolver;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Selects volatility surfaces for manipulation.
 */
public class VolatilitySurfaceSelector implements DistinctMarketDataSelector {

  private static final ImmutableSet<String> s_compatibleVRNames = ImmutableSet.of(INTERPOLATED_VOLATILITY_SURFACE, VOLATILITY_SURFACE);
  
  
  private final Set<String> _calcConfigNames;
  private final Set<String> _names;
  private final PatternWrapper _nameMatchPattern;
  private final PatternWrapper _nameLikePattern;
  private final Set<String> _instrumentTypes;
  private final Set<String> _quoteTypes;
  private final Set<String> _quoteUnits;

  /* package */ VolatilitySurfaceSelector(Set<String> calcConfigNames,
                                          Set<String> names,
                                          Pattern nameMatchPattern,
                                          Pattern nameLikePattern,
                                          Set<String> instrumentTypes,
                                          Set<String> quoteTypes,
                                          Set<String> quoteUnits) {
    _calcConfigNames = calcConfigNames;
    _names = names;
    _nameMatchPattern = PatternWrapper.wrap(nameMatchPattern);
    _nameLikePattern = PatternWrapper.wrap(nameLikePattern);
    _instrumentTypes = instrumentTypes;
    _quoteTypes = quoteTypes;
    _quoteUnits = quoteUnits;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(ValueSpecification valueSpecification,
                                                         String calculationConfigurationName,
                                                         SelectorResolver resolver) {
    if (_calcConfigNames != null && !_calcConfigNames.contains(calculationConfigurationName)) {
      return null;
    }
    if (!s_compatibleVRNames.contains(valueSpecification.getValueName())) {
      return null;
    }
    VolatilitySurfaceKey key = createKey(valueSpecification);
    if (!contains(_names, key.getName())) {
      return null;
    }
    if (_nameMatchPattern != null && !_nameMatchPattern.getPattern().matcher(key.getName()).matches()) {
      return null;
    }
    if (_nameLikePattern != null && !_nameLikePattern.getPattern().matcher(key.getName()).matches()) {
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

  private static VolatilitySurfaceKey createKey(ValueSpecification valueSpecification) {
    UniqueId uniqueId = valueSpecification.getTargetSpecification().getUniqueId();
    String surface = valueSpecification.getProperties().getStrictValue(ValuePropertyNames.SURFACE);
    String instrumentType = valueSpecification.getProperties().getStrictValue("InstrumentType");
    String quoteType = valueSpecification.getProperties().getStrictValue(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
    String quoteUnits = valueSpecification.getProperties().getStrictValue(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS);
    return VolatilitySurfaceKey.of(uniqueId, surface, instrumentType, quoteType, quoteUnits);
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

  /* package */ Pattern getNameMatchPattern() {
    return _nameMatchPattern == null ? null : _nameMatchPattern.getPattern();
  }

  /* package */ Pattern getNameLikePattern() {
    return _nameLikePattern == null ? null : _nameLikePattern.getPattern();
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
  public int hashCode() {
    return Objects.hash(_calcConfigNames, _names, _nameMatchPattern, _nameLikePattern, _instrumentTypes, _quoteTypes, _quoteUnits);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final VolatilitySurfaceSelector other = (VolatilitySurfaceSelector) obj;
    return Objects.equals(this._calcConfigNames, other._calcConfigNames) &&
        Objects.equals(this._names, other._names) &&
        Objects.equals(this._nameMatchPattern, other._nameMatchPattern) &&
        Objects.equals(this._nameLikePattern, other._nameLikePattern) &&
        Objects.equals(this._instrumentTypes, other._instrumentTypes) &&
        Objects.equals(this._quoteTypes, other._quoteTypes) &&
        Objects.equals(this._quoteUnits, other._quoteUnits);
  }

  @Override
  public String toString() {
    return "VolatilitySurfaceSelector [" +
        "_calcConfigNames=" + _calcConfigNames +
        ", _names=" + _names +
        ", _nameMatchPattern=" + _nameMatchPattern +
        ", _nameLikePattern=" + _nameLikePattern +
        ", _instrumentTypes=" + _instrumentTypes +
        ", _quoteTypes=" + _quoteTypes +
        ", _quoteUnits=" + _quoteUnits +
        "]";
  }

  public static class Builder {

    /** The scenario to which manipulations are added. */
    private final Scenario _scenario;

    private Set<String> _names;
    private Pattern _nameMatchPattern;
    private Pattern _nameLikePattern;
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
      return new VolatilitySurfaceSelector(_scenario.getCalcConfigNames(),
                                           _names,
                                           _nameMatchPattern,
                                           _nameLikePattern,
                                           _instrumentTypes,
                                           _quoteTypes,
                                           _quoteUnits);
    }

    public Builder named(String... names) {
      ArgumentChecker.notEmpty(names, "names");
      if (_names != null) {
        throw new IllegalStateException("named() can only be called once");
      }
      if (_nameMatchPattern != null) {
        throw new IllegalStateException("Can't specify exact name and a match for the name");
      }
      if (_nameLikePattern != null) {
        throw new IllegalStateException("Can't specify exact name and a match for the name");
      }
      _names = ImmutableSet.copyOf(names);
      return this;
    }

    public Builder nameMatches(String regex) {
      ArgumentChecker.notNull(regex, "regex");
      if (_nameMatchPattern != null) {
        throw new IllegalStateException("nameMatches() can only be called once");
      }
      if (_names != null) {
        throw new IllegalStateException("Can't specify exact name and a regular expression for the name");
      }
      if (_nameLikePattern != null) {
        throw new IllegalStateException("Can't specify exact name and a regular expression for the name");
      }
      _nameMatchPattern = Pattern.compile(regex);
      return this;
    }

    public Builder nameLike(String glob) {
      ArgumentChecker.notEmpty(glob, "glob");
      if (_nameLikePattern != null) {
        throw new IllegalStateException("nameLike() can only be called once");
      }
      if (_names != null) {
        throw new IllegalStateException("Can't specify exact name and a regular expression for the name");
      }
      if (_nameMatchPattern != null) {
        throw new IllegalStateException("Can't specify exact name and a regular expression for the name");
      }
      _nameLikePattern = SimulationUtils.patternForGlob(glob);
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

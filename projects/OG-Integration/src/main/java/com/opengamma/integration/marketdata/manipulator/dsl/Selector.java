/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.SelectorResolver;
import com.opengamma.engine.marketdata.manipulator.StructureIdentifier;
import com.opengamma.engine.marketdata.manipulator.StructureType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Selector base class for data structures that will be selected by name and currency.
 * @param <T> The type of data structure handled by this selector
 */
/* package */ abstract class Selector<T> implements DistinctMarketDataSelector {

  private final Set<String> _names;
  private final Set<Currency> _currencies;
  private final PatternWrapper _nameMatchPattern;
  private final PatternWrapper _nameLikePattern;
  private final Set<String> _calcConfigNames;
  private final Class<T> _type;
  private final Set<StructureType> _structureTypes;

  /* package */ Selector(Set<String> calcConfigNames,
                         Set<String> names,
                         Set<Currency> currencies,
                         Pattern nameMatchPattern,
                         Pattern nameLikePattern, Class<T> type,
                         StructureType structureType) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(structureType, "structureType");
    _calcConfigNames = calcConfigNames;
    _names = names;
    _currencies = currencies;
    _nameMatchPattern = PatternWrapper.wrap(nameMatchPattern);
    _nameLikePattern = PatternWrapper.wrap(nameLikePattern);
    _type = type;
    _structureTypes = ImmutableSet.of(structureType);
  }

  /* package */ Set<String> getNames() {
    return _names;
  }

  /* package */ Set<Currency> getCurrencies() {
    return _currencies;
  }

  /* package */ Pattern getNameMatchPattern() {
    return _nameMatchPattern == null ? null : _nameMatchPattern.getPattern();
  }

  /* package */ Pattern getNameLikePattern() {
    return _nameLikePattern == null ? null : _nameLikePattern.getPattern();
  }

  /* package */ Set<String> getCalcConfigNames() {
    return _calcConfigNames;
  }

  /* package */ boolean matches(String name, Currency currency) {
    // TODO can / should these be relaxed?
    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(currency, "currency");
    if (_names != null && !_names.contains(name)) {
      return false;
    }
    if (_nameMatchPattern != null && !_nameMatchPattern.getPattern().matcher(name).matches()) {
      return false;
    }
    if (_nameLikePattern != null && !_nameLikePattern.getPattern().matcher(name).matches()) {
      return false;
    }
    if (_currencies != null && !_currencies.contains(currency)) {
      return false;
    }
    return true;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId,
                                                         String calcConfigName,
                                                         SelectorResolver resolver) {
    if (_calcConfigNames != null && !_calcConfigNames.contains(calcConfigName)) {
      return null;
    }
    Object value = structureId.getValue();
    if (!_type.isInstance(value)) {
      return null;
    }
    T input = _type.cast(value);
    if (matches(input)) {
      return this;
    } else {
      return null;
    }
  }

  /* package */ abstract boolean matches(T key);

  @Override
  public Set<StructureType> getApplicableStructureTypes() {
    return _structureTypes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_names,
                        _currencies,
                        _nameMatchPattern,
                        _nameLikePattern,
                        _calcConfigNames,
                        _type,
                        _structureTypes);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Selector other = (Selector) obj;
    return Objects.equals(this._names, other._names) &&
        Objects.equals(this._currencies, other._currencies) &&
        Objects.equals(this._nameMatchPattern, other._nameMatchPattern) &&
        Objects.equals(this._nameLikePattern, other._nameLikePattern) &&
        Objects.equals(this._calcConfigNames, other._calcConfigNames) &&
        Objects.equals(this._type, other._type) &&
        Objects.equals(this._structureTypes, other._structureTypes);
  }

  @Override
  public String toString() {
    return "Selector [" +
        "_names=" + _names +
        ", _currencies=" + _currencies +
        ", _nameMatchPattern=" + _nameMatchPattern +
        ", _nameLikePattern=" + _nameLikePattern +
        ", _calcConfigNames=" + _calcConfigNames +
        ", _type=" + _type +
        ", _structureTypes=" + _structureTypes +
        "]";
  }

  /* package */ abstract static class Builder {

    private final Scenario _scenario;

    private Set<String> _names;
    private Set<Currency> _currencies;
    private Pattern _nameMatchPattern;
    private Pattern _nameLikePattern;

    /* package */ Builder(Scenario scenario) {
      ArgumentChecker.notNull(scenario, "scenario");
      _scenario = scenario;
    }

    /* package */ Builder named(String... names) {
      ArgumentChecker.notEmpty(names, "names");
      if (_names != null) {
        throw new IllegalStateException("named() can only be called once");
      }
      if (_nameMatchPattern != null) {
        throw new IllegalStateException("Only one of named() and nameMatches() can be used");
      }
      if (_nameLikePattern != null) {
        throw new IllegalStateException("Only one of named() and nameMatches() can be used");
      }
      _names = ImmutableSet.copyOf(names);
      return this;
    }

    /* package */ Builder currencies(String... codes) {
      ArgumentChecker.notEmpty(codes, "codes");
      if (_currencies != null) {
        throw new IllegalStateException("currencies() can only be called once");
      }
      Set<Currency> currencies = Sets.newHashSet();
      for (String code : codes) {
        currencies.add(Currency.of(code));
      }
      _currencies = Collections.unmodifiableSet(currencies);
      return this;
    }

    /* package */ Builder nameMatches(String regex) {
      if (_nameMatchPattern != null) {
        throw new IllegalStateException("nameMatches() can only be called once");
      }
      if (_nameLikePattern != null) {
        throw new IllegalStateException("nameMatches() can only be called once");
      }
      if (_names != null) {
        throw new IllegalStateException("Only one of named() and nameMatches() can be used");
      }
      _nameMatchPattern = Pattern.compile(regex);
      return this;
    }

    /* package */ Builder nameLike(String glob) {
      if (_nameLikePattern != null) {
        throw new IllegalStateException("nameLike() can only be called once");
      }
      if (_names != null) {
        throw new IllegalStateException("Only one of named() and nameMatches() and nameLike() can be used");
      }
      if (_nameMatchPattern != null) {
        throw new IllegalStateException("Only one of named() and nameMatches() and nameLike() can be used");
      }
      _nameLikePattern = SimulationUtils.patternForGlob(glob);
      return this;
    }

    /* package */ Scenario getScenario() {
      return _scenario;
    }

    /* package */ Set<String> getNames() {
      return _names;
    }

    /* package */ Set<Currency> getCurrencies() {
      return _currencies;
    }

    /* package */ Pattern getNameMatchPattern() {
      return _nameMatchPattern;
    }

    /* package */ Pattern getNameLikePattern() {
      return _nameLikePattern;
    }
  }
}

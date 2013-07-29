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
  private final PatternWrapper _namePattern;
  private final Set<String> _calcConfigNames;
  private final Class<T> _type;
  private final Set<StructureType> _structureTypes;

  /* package */ Selector(Set<String> calcConfigNames,
                         Set<String> names,
                         Set<Currency> currencies,
                         Pattern namePattern,
                         Class<T> type,
                         StructureType structureType) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(structureType, "structureType");
    _calcConfigNames = calcConfigNames;
    _names = names;
    _currencies = currencies;
    _namePattern = PatternWrapper.wrap(namePattern);
    _type = type;
    _structureTypes = ImmutableSet.of(structureType);
  }

  /* package */ Set<String> getNames() {
    return _names;
  }

  /* package */ Set<Currency> getCurrencies() {
    return _currencies;
  }

  /* package */ Pattern getNamePattern() {
    return _namePattern == null ? null : _namePattern.getPattern();
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
    if (_namePattern != null && !_namePattern.getPattern().matcher(name).matches()) {
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
    return Objects.hash(_names, _currencies, _namePattern, _calcConfigNames, _type, _structureTypes);
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
        Objects.equals(this._namePattern, other._namePattern) &&
        Objects.equals(this._calcConfigNames, other._calcConfigNames) &&
        Objects.equals(this._type, other._type) &&
        Objects.equals(this._structureTypes, other._structureTypes);
  }

  @Override
  public String toString() {
    return "Selector [" +
        "_names=" + _names +
        ", _currencies=" + _currencies +
        ", _namePattern=" + _namePattern +
        ", _calcConfigNames=" + _calcConfigNames +
        ", _type=" + _type +
        ", _structureTypes=" + _structureTypes +
        "]";
  }

  /* package */ abstract static class Builder {

    private final Scenario _scenario;

    private Set<String> _names;
    private Set<Currency> _currencies;
    private Pattern _namePattern;

    /* package */ Builder(Scenario scenario) {
      ArgumentChecker.notNull(scenario, "scenario");
      _scenario = scenario;
    }

    /* package */ Builder named(String... names) {
      ArgumentChecker.notEmpty(names, "names");
      if (_names != null) {
        throw new IllegalStateException("named() can only be called once");
      }
      if (_namePattern != null) {
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
      if (_namePattern != null) {
        throw new IllegalStateException("nameMatches() can only be called once");
      }
      if (_names != null) {
        throw new IllegalStateException("Only one of named() and nameMatches() can be used");
      }
      _namePattern = Pattern.compile(regex);
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

    /* package */ Pattern getNamePattern() {
      return _namePattern;
    }
  }
}

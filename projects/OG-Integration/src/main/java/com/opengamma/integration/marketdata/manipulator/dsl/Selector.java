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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
/* package */ abstract class Selector<T> implements DistinctMarketDataSelector {

  private final Set<String> _names;
  private final Set<Currency> _currencies;
  private final Pattern _namePattern;
  private final String _calcConfigName;
  private final Class<T> _type;
  private final Set<StructureType> _structureTypes;

  /* package */ Selector(String calcConfigName,
                         Set<String> names,
                         Set<Currency> currencies,
                         Pattern namePattern,
                         Class<T> type,
                         StructureType structureType) {
    ArgumentChecker.notEmpty(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(structureType, "structureType");
    _calcConfigName = calcConfigName;
    _names = names;
    _currencies = currencies;
    _namePattern = namePattern;
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
    return _namePattern;
  }

  /* package */ String getCalcConfigName() {
    return _calcConfigName;
  }

  /* package */ boolean matches(String name, Currency currency) {
    // TODO can / should these be relaxed?
    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(currency, "currency");
    if (_names != null && !_names.contains(name)) {
      return false;
    }
    if (_namePattern != null && !_namePattern.matcher(name).matches()) {
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
  public DistinctMarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId, String calcConfigName) {
    if (!_calcConfigName.equals(calcConfigName)) {
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Selector selector = (Selector) o;

    if (!_calcConfigName.equals(selector._calcConfigName)) {
      return false;
    }
    if (_currencies != null ? !_currencies.equals(selector._currencies) : selector._currencies != null) {
      return false;
    }
    if (_namePattern != null ? !_namePattern.equals(selector._namePattern) : selector._namePattern != null) {
      return false;
    }
    if (_names != null ? !_names.equals(selector._names) : selector._names != null) {
      return false;
    }
    if (!_structureTypes.equals(selector._structureTypes)) {
      return false;
    }
    if (!_type.equals(selector._type)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = _names != null ? _names.hashCode() : 0;
    result = 31 * result + (_currencies != null ? _currencies.hashCode() : 0);
    result = 31 * result + (_namePattern != null ? _namePattern.hashCode() : 0);
    result = 31 * result + _calcConfigName.hashCode();
    result = 31 * result + _type.hashCode();
    result = 31 * result + _structureTypes.hashCode();
    return result;
  }

  /* package */ abstract static class Builder {

    private final String _calcConfigName;
    private final Scenario _scenario;

    private Set<String> _names;
    private Set<Currency> _currencies;
    private Pattern _namePattern;

    protected Builder(Scenario scenario, String calcConfigName) {
      ArgumentChecker.notEmpty(calcConfigName, "calcConfigName");
      ArgumentChecker.notNull(scenario, "scenario");
      _scenario = scenario;
      _calcConfigName = calcConfigName;
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

    /* package */ String getCalcConfigName() {
      return _calcConfigName;
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

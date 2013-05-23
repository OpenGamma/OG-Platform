/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.base.Predicate;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.StructureIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class PredicateSelector<T> implements MarketDataSelector {

  private final String _calcConfigName;
  private final List<Predicate<T>> _predicates;
  private final Class<T> _type;

  public PredicateSelector(String calcConfigName, List<Predicate<T>> predicates, Class<T> type) {
    ArgumentChecker.notNull(predicates, "predicates");
    ArgumentChecker.notEmpty(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(type, "type");
    _type = type;
    _calcConfigName = calcConfigName;
    _predicates = predicates;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public MarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId, String calcConfigName) {
    if (!_calcConfigName.equals(calcConfigName)) {
      return null;
    }
    Object value = structureId.getValue();
    if (!_type.isInstance(value)) {
      return null;
    }
    T input = _type.cast(value);
    for (Predicate<T> predicate : _predicates) {
      if (!predicate.apply(input)) {
        return null;
      }
    }
    return this;
  }

  @Override
  public StructureType getApplicableStructureType() {
    return StructureType.YIELD_CURVE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PredicateSelector that = (PredicateSelector) o;

    if (!_calcConfigName.equals(that._calcConfigName)) {
      return false;
    }
    if (!_predicates.equals(that._predicates)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = _calcConfigName.hashCode();
    result = 31 * result + _predicates.hashCode();
    return result;
  }

  /* package */ String getCalcConfigName() {
    return _calcConfigName;
  }

  /* package */ Class<T> getType() {
    return _type;
  }

  /* package */ List<Predicate<T>> getPredicates() {
    return _predicates;
  }
}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CurveSelector extends Selector {

  private final List<Predicate<YieldCurveKey>> _predicates = Lists.newArrayList();
  private final String _calcConfigName;

  private CurveManipulator _manipulator;

  public CurveSelector(String calcConfigName) {
    ArgumentChecker.notEmpty(calcConfigName, "calcConfigName");
    _calcConfigName = calcConfigName;
  }

  public CurveSelector named(String... names) {
    _predicates.add(new YieldCurveNamedPredicate(names));
    return this;
  }

  public CurveSelector nameMatches(String regex) {
    _predicates.add(new YieldCurveNameMatchesPredicate(regex));
    return this;
  }

  public CurveSelector currencies(String... codes) {
    _predicates.add(new YieldCurveCurrenciesPredicate(codes));
    return this;
  }

  public CurveManipulator apply() {
    if (_manipulator != null) {
      throw new IllegalStateException("apply() can only be called once on a CurveSelector");
    }
    _manipulator = new CurveManipulator();
    return _manipulator;
  }

  @Override
  /* package */ MarketDataSelector getMarketDataSelector() {
    return new PredicateSelector<>(_calcConfigName, ImmutableList.copyOf(_predicates), YieldCurveKey.class);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CurveSelector that = (CurveSelector) o;

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
    int result = _predicates.hashCode();
    result = 31 * result + _calcConfigName.hashCode();
    return result;
  }
}

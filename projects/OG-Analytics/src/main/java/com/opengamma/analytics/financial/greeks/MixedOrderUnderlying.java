/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.pnl.UnderlyingType;

/**
 * 
 */
public class MixedOrderUnderlying implements Underlying {
  private final List<NthOrderUnderlying> _orders;
  private final List<UnderlyingType> _underlyings;
  private final int _totalOrder;

  public MixedOrderUnderlying(final NavigableMap<Integer, UnderlyingType> underlyings) {
    Validate.notNull(underlyings, "underlyings");
    if (underlyings.size() < 2) {
      throw new IllegalArgumentException("Must have at least two underlying types to have mixed order");
    }
    _orders = new ArrayList<>();
    _underlyings = new ArrayList<>();
    int totalOrder = 0;
    UnderlyingType underlying;
    for (final Entry<Integer, UnderlyingType> entry : underlyings.entrySet()) {
      final int key = entry.getKey();
      if (key < 1) {
        throw new IllegalArgumentException("Order must be at least one to have mixed order");
      }
      underlying = entry.getValue();
      _orders.add(new NthOrderUnderlying(key, underlying));
      _underlyings.add(underlying);
      totalOrder += key;
    }
    _totalOrder = totalOrder;
  }

  public MixedOrderUnderlying(final List<NthOrderUnderlying> underlyings) {
    Validate.notNull(underlyings, "underlyings");
    if (underlyings.size() < 2) {
      throw new IllegalArgumentException("Must have at least two nth order underlyings to have mixed order");
    }
    _orders = new ArrayList<>(underlyings);
    _underlyings = new ArrayList<>();
    int totalOrder = 0;
    for (final NthOrderUnderlying nth : underlyings) {
      if (nth.getOrder() < 1) {
        throw new IllegalArgumentException("Order must be at least one to have mixed order");
      }
      totalOrder += nth.getOrder();
      _underlyings.addAll(nth.getUnderlyings());
    }
    _totalOrder = totalOrder;
  }

  @Override
  public int getOrder() {
    return _totalOrder;
  }

  public List<NthOrderUnderlying> getUnderlyingOrders() {
    return _orders;
  }

  @Override
  public List<UnderlyingType> getUnderlyings() {
    return _underlyings;
  }

  // NOTE: hashCode() and equals() are deliberately not overridden. Please do
  // not implement them unless you want to
  // break a load of code
}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.pnl.UnderlyingType;

/**
 * 
 */
public class MixedOrderUnderlying implements Underlying {
  private final Set<NthOrderUnderlying> _orders;
  private final Set<UnderlyingType> _underlyings;
  private final int _totalOrder;

  public MixedOrderUnderlying(final Map<Integer, UnderlyingType> underlyings) {
    Validate.notNull(underlyings, "underlyings");
    if (underlyings.size() < 2) {
      throw new IllegalArgumentException("Must have at least two underlying types to have mixed order");
    }
    _orders = new HashSet<NthOrderUnderlying>();
    _underlyings = new HashSet<UnderlyingType>();
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

  public MixedOrderUnderlying(final Set<NthOrderUnderlying> underlyings) {
    if (underlyings == null) {
      throw new IllegalArgumentException("Set of nth order underlyings was null");
    }
    if (underlyings.size() < 2) {
      throw new IllegalArgumentException("Must have at least two nth order underlyings to have mixed order");
    }
    _orders = new HashSet<NthOrderUnderlying>(underlyings);
    _underlyings = new HashSet<UnderlyingType>();
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

  public Set<NthOrderUnderlying> getUnderlyingOrders() {
    return _orders;
  }

  @Override
  public Set<UnderlyingType> getUnderlyings() {
    return _underlyings;
  }

  // NOTE: hashCode() and equals() are deliberately not overridden. Please do
  // not implement them unless you want to
  // break a load of code
}

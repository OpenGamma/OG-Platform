/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.pnl.UnderlyingType;

/**
 * 
 */
public class NthOrderUnderlying implements Underlying {
  private final int _order;
  private final List<UnderlyingType> _underlyingSet;
  private final UnderlyingType _underlying;

  public NthOrderUnderlying(final int order, final UnderlyingType underlying) {
    if (order < 0) {
      throw new IllegalArgumentException("Order must be greater than or equal to zero");
    }
    if (underlying == null && order != 0) {
      throw new IllegalArgumentException("Underlying type was null");
    }
    _order = order;
    if (order == 0) {
      _underlyingSet = new ArrayList<>(0);
    } else {
      _underlyingSet = Arrays.asList(underlying);
    }
    _underlying = underlying;
  }

  @Override
  public List<UnderlyingType> getUnderlyings() {
    return _underlyingSet;
  }

  @Override
  public int getOrder() {
    return _order;
  }

  public UnderlyingType getUnderlying() {
    return _underlying;
  }
  // NOTE: hashCode() and equals() are deliberately not overridden. Please do
  // not implement them unless you want to
  // break a load of code

}

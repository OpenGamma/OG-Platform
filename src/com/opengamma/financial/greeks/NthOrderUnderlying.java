/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.Collections;
import java.util.Set;

import com.opengamma.financial.pnl.UnderlyingType;

public class NthOrderUnderlying implements Underlying {
  private final int _order;
  private final Set<UnderlyingType> _underlying;

  public NthOrderUnderlying(final int order, final UnderlyingType underlying) {
    if (order < 0)
      throw new IllegalArgumentException("Order must be greater than or equal to zero");
    if (underlying == null)
      throw new IllegalArgumentException("Underlying type was null");
    _order = order;
    _underlying = Collections.singleton(underlying);
  }

  @Override
  public Set<UnderlyingType> getUnderlyings() {
    return _underlying;
  }

  @Override
  public int getOrder() {
    return _order;
  }

  // NOTE: hashCode() and equals() are deliberately not overridden. Please do not implement them unless you want to 
  // break a load of code

}

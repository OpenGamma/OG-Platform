/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.util.ArrayList;
import java.util.Collection;

/* package */abstract class AbstractInfo<T> {

  private final T _underlying;
  private Collection<T> _additionalUnderlying;

  protected AbstractInfo(final T underlying) {
    _underlying = underlying;
  }

  public T getUnderlying() {
    return _underlying;
  }

  public void associateAdditionalUnderlying(final T underlying) {
    if (_additionalUnderlying == null) {
      _additionalUnderlying = new ArrayList<T>();
    }
    _additionalUnderlying.add(underlying);
  }

  public void addUnderlyingToCollection(final Collection<T> destination) {
    destination.add(getUnderlying());
    if (_additionalUnderlying != null) {
      destination.addAll(_additionalUnderlying);
    }
  }

}

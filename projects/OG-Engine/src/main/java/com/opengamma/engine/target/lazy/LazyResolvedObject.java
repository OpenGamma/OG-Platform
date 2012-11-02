/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.io.ObjectStreamException;
import java.io.Serializable;

import com.opengamma.engine.ComputationTargetResolver;

/**
 * Base class for lazily resolved object.
 */
/* package */abstract class LazyResolvedObject<T> implements Serializable {

  private final LazyResolveContext.AtVersionCorrection _context;
  private final T _underlying;

  protected LazyResolvedObject(final LazyResolveContext.AtVersionCorrection context, final T underlying) {
    _context = context;
    _underlying = underlying;
  }

  protected LazyResolveContext.AtVersionCorrection getLazyResolveContext() {
    return _context;
  }

  protected T getUnderlying() {
    return _underlying;
  }

  protected abstract TargetResolverObject targetResolverObject(final ComputationTargetResolver.AtVersionCorrection resolver);

  protected abstract Serializable simpleObject();

  public Object writeReplace() throws ObjectStreamException {
    if (getLazyResolveContext().getTargetResolver() != null) {
      LazyResolveContext.beginWrite();
      try {
        return targetResolverObject(getLazyResolveContext().getTargetResolver());
      } finally {
        LazyResolveContext.endWrite();
      }
    } else {
      return simpleObject();
    }
  }

}

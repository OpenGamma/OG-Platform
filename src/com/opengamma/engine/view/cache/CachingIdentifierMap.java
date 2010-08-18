/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Caches value identifiers on top of another identifier source.
 * This class is internally synchronized.
 */
public class CachingIdentifierMap implements IdentifierMap {
  private final IdentifierMap _underlying;
  // NOTE kirk 2010-08-06 -- This INTENTIONALLY is not an EHCache instance.
  // Since getting a remote value specification identifier has to be a super-fast operation
  // (probably faster than disk anyway), and the only reason we'd ever want to flush is
  // based on low GCs, and the elements are so small, EHCache doesn't actually work here.
  private final WeakHashMap<ValueSpecification, Long> _cachedIdentifiers = new WeakHashMap<ValueSpecification, Long>();
  private final ReadWriteLock _lock = new ReentrantReadWriteLock();

  public CachingIdentifierMap(IdentifierMap underlying) {
    ArgumentChecker.notNull(underlying, "Underlying source");
    _underlying = underlying;
  }

  /**
   * Gets the underlying source.
   * @return the underlying
   */
  public IdentifierMap getUnderlying() {
    return _underlying;
  }

  @Override
  public long getIdentifier(ValueSpecification spec) {
    _lock.readLock().lock();
    try {
      Long value = _cachedIdentifiers.get(spec);
      if (value != null) {
        return value;
      }
    } finally {
      _lock.readLock().unlock();
    }
    long value = getUnderlying().getIdentifier(spec);
    _lock.writeLock().lock();
    try {
      _cachedIdentifiers.put(spec, value);
    } finally {
      _lock.writeLock().unlock();
    }
    return value;
  }

}

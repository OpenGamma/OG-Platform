/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link IdentifierMap} which is backed by an in-memory
 * {@link ConcurrentMap}. This has no facilities for acting as a cache, or for persistence.
 * It should only be used for development and debugging purposes.
 */
public class InMemoryIdentifierMap extends AbstractIdentifierMap implements IdentifierMap {

  private final AtomicLong _nextIdentifier = new AtomicLong(1L);

  private final ConcurrentMap<ValueSpecification, Long> _identifiers = new ConcurrentHashMap<ValueSpecification, Long>();
  private final ConcurrentMap<Long, ValueSpecification> _specifications = new ConcurrentHashMap<Long, ValueSpecification>();

  @Override
  public long getIdentifier(ValueSpecification spec) {
    ArgumentChecker.notNull(spec, "Value specification");
    Long result = _identifiers.get(spec);
    if (result != null) {
      return result;
    }
    long freshIdentifier = _nextIdentifier.getAndIncrement();
    result = _identifiers.putIfAbsent(spec, freshIdentifier);
    if (result == null) {
      result = freshIdentifier;
      _specifications.put(freshIdentifier, spec);
    }
    return result;
  }

  @Override
  public ValueSpecification getValueSpecification(long identifier) {
    return _specifications.get(identifier);
  }
  
  public void clear() {
    _identifiers.clear();
    _specifications.clear();
    // N.B. We don't actually reset the _nextIdentifier map just in case, so that we can diagnostically
    // check whether an ID has accidentally been reused.
  }

}

/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;
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

  // NOTE andrew 2010-09-06 -- Don't use the Google map with weakKeys; it will do comparison by identity which isn't right!
  private final Map<ValueSpecification, Long> _specificationToIdentifier = Collections.synchronizedMap(new WeakHashMap<ValueSpecification, Long>());
  private final ConcurrentMap<Long, ValueSpecification> _identifierToSpecification = new MapMaker().weakValues().makeMap();

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
  public long getIdentifier(final ValueSpecification spec) {
    Long value = _specificationToIdentifier.get(spec);
    if (value != null) {
      return value;
    }
    value = getUnderlying().getIdentifier(spec);
    _specificationToIdentifier.put(spec, value);
    _identifierToSpecification.put(value, spec);
    return value;
  }

  @Override
  public Map<ValueSpecification, Long> getIdentifiers(Collection<ValueSpecification> specs) {
    final Map<ValueSpecification, Long> identifiers = new HashMap<ValueSpecification, Long>();
    List<ValueSpecification> cacheMisses = null;
    for (ValueSpecification spec : specs) {
      final Long value = _specificationToIdentifier.get(spec);
      if (value != null) {
        identifiers.put(spec, value);
      } else {
        if (cacheMisses == null) {
          cacheMisses = new LinkedList<ValueSpecification>();
        }
        cacheMisses.add(spec);
      }
    }
    if (cacheMisses != null) {
      if (cacheMisses.size() == 1) {
        final ValueSpecification spec = cacheMisses.get(0);
        final long value = getUnderlying().getIdentifier(spec);
        _specificationToIdentifier.put(spec, value);
        _identifierToSpecification.put(value, spec);
        identifiers.put(spec, value);
      } else {
        final Map<ValueSpecification, Long> values = getUnderlying().getIdentifiers(cacheMisses);
        for (Map.Entry<ValueSpecification, Long> value : values.entrySet()) {
          _specificationToIdentifier.put(value.getKey(), value.getValue());
          _identifierToSpecification.put(value.getValue(), value.getKey());
        }
        identifiers.putAll(values);
      }
    }
    return identifiers;
  }

  @Override
  public ValueSpecification getValueSpecification(long identifier) {
    ValueSpecification spec = _identifierToSpecification.get(identifier);
    if (spec != null) {
      return spec;
    }
    spec = getUnderlying().getValueSpecification(identifier);
    _specificationToIdentifier.put(spec, identifier);
    _identifierToSpecification.put(identifier, spec);
    return spec;
  }

  @Override
  public Map<Long, ValueSpecification> getValueSpecifications(Collection<Long> identifiers) {
    final Map<Long, ValueSpecification> specifications = new HashMap<Long, ValueSpecification>();
    List<Long> cacheMisses = null;
    for (Long identifier : identifiers) {
      final ValueSpecification specification = _identifierToSpecification.get(identifier);
      if (specification != null) {
        specifications.put(identifier, specification);
      } else {
        if (cacheMisses == null) {
          cacheMisses = new LinkedList<Long>();
        }
        cacheMisses.add(identifier);
      }
    }
    if (cacheMisses != null) {
      if (cacheMisses.size() == 1) {
        final Long identifier = cacheMisses.get(0);
        final ValueSpecification specification = getUnderlying().getValueSpecification(identifier);
        _specificationToIdentifier.put(specification, identifier);
        _identifierToSpecification.put(identifier, specification);
        specifications.put(identifier, specification);
      } else {
        final Map<Long, ValueSpecification> values = getUnderlying().getValueSpecifications(cacheMisses);
        for (Map.Entry<Long, ValueSpecification> value : values.entrySet()) {
          _specificationToIdentifier.put(value.getValue(), value.getKey());
          _identifierToSpecification.put(value.getKey(), value.getValue());
        }
        specifications.putAll(values);
      }
    }
    return specifications;
  }

}

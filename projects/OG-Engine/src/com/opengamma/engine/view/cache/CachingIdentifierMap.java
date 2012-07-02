/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.MapMaker;
import com.opengamma.engine.MemoryUtils;
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
  private final Map<ValueSpecification, Key> _specificationToIdentifier = Collections.synchronizedMap(new WeakHashMap<ValueSpecification, Key>());
  private final ConcurrentMap<Key, ValueSpecification> _identifierToSpecification = new MapMaker().softValues().makeMap();

  private static final class Key {

    private long _identifier;
    private Key _next;

    public Key(final long identifier) {
      _identifier = identifier;
    }

    @Override
    public boolean equals(final Object o) {
      return ((Key) o)._identifier == _identifier;
    }

    @Override
    public int hashCode() {
      return (int) (_identifier ^ (_identifier >>> 32));
    }

  }

  private final AtomicReference<Key> _keys = new AtomicReference<Key>();

  public CachingIdentifierMap(IdentifierMap underlying) {
    ArgumentChecker.notNull(underlying, "Underlying source");
    _underlying = underlying;
  }

  private Key borrowKey(final long identifier) {
    Key key = _keys.get();
    if (key == null) {
      return new Key(identifier);
    }
    do {
      synchronized (key) {
        if (_keys.compareAndSet(key, key._next)) {
          key._identifier = identifier;
          return key;
        }
      }
      key = _keys.get();
    } while (key != null);
    return new Key(identifier);
  }

  private void returnKey(final Key key) {
    synchronized (key) {
      key._next = _keys.get();
      while (!_keys.compareAndSet(key._next, key)) {
        key._next = _keys.get();
      }
    }
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
    Key value = _specificationToIdentifier.get(spec);
    if (value != null) {
      return value._identifier;
    }
    long longValue = getUnderlying().getIdentifier(spec);
    value = new Key(longValue);
    _specificationToIdentifier.put(spec, value);
    _identifierToSpecification.put(value, spec);
    return longValue;
  }

  @Override
  public Object2LongMap<ValueSpecification> getIdentifiers(Collection<ValueSpecification> specs) {
    final Object2LongMap<ValueSpecification> identifiers = new Object2LongOpenHashMap<ValueSpecification>();
    List<ValueSpecification> cacheMisses = null;
    for (ValueSpecification spec : specs) {
      final Key value = _specificationToIdentifier.get(spec);
      if (value != null) {
        identifiers.put(spec, value._identifier);
      } else {
        if (cacheMisses == null) {
          cacheMisses = new LinkedList<ValueSpecification>();
        }
        cacheMisses.add(MemoryUtils.instance(spec));
      }
    }
    if (cacheMisses != null) {
      if (cacheMisses.size() == 1) {
        final ValueSpecification spec = cacheMisses.get(0);
        final long value = getUnderlying().getIdentifier(spec);
        final Key keyValue = new Key(value);
        _specificationToIdentifier.put(spec, keyValue);
        _identifierToSpecification.put(keyValue, spec);
        identifiers.put(spec, value);
      } else {
        final Object2LongMap<ValueSpecification> values = getUnderlying().getIdentifiers(cacheMisses);
        for (Object2LongMap.Entry<ValueSpecification> entry : values.object2LongEntrySet()) {
          final Key value = new Key(entry.getLongValue());
          _specificationToIdentifier.put(entry.getKey(), value);
          _identifierToSpecification.put(value, entry.getKey());
        }
        identifiers.putAll(values);
      }
    }
    return identifiers;
  }

  @Override
  public ValueSpecification getValueSpecification(final long identifier) {
    final Key key = borrowKey(identifier);
    ValueSpecification spec = _identifierToSpecification.get(key);
    if (spec != null) {
      returnKey(key);
      return spec;
    }
    spec = getUnderlying().getValueSpecification(identifier);
    _specificationToIdentifier.put(spec, key);
    _identifierToSpecification.put(key, spec);
    return spec;
  }

  @Override
  public Long2ObjectMap<ValueSpecification> getValueSpecifications(LongCollection identifiers) {
    final Long2ObjectMap<ValueSpecification> specifications = new Long2ObjectOpenHashMap<ValueSpecification>();
    LongList cacheMisses = null;
    for (long identifier : identifiers) {
      final Key key = borrowKey(identifier);
      final ValueSpecification specification = _identifierToSpecification.get(key);
      returnKey(key);
      if (specification != null) {
        specifications.put(identifier, specification);
      } else {
        if (cacheMisses == null) {
          cacheMisses = new LongArrayList(identifiers.size());
        }
        cacheMisses.add(identifier);
      }
    }
    if (cacheMisses != null) {
      if (cacheMisses.size() == 1) {
        final long identifier = cacheMisses.getLong(0);
        final ValueSpecification specification = getUnderlying().getValueSpecification(identifier);
        final Key key = new Key(identifier);
        _specificationToIdentifier.put(specification, key);
        _identifierToSpecification.put(key, specification);
        specifications.put(identifier, specification);
      } else {
        final Long2ObjectMap<ValueSpecification> values = getUnderlying().getValueSpecifications(cacheMisses);
        for (Long2ObjectMap.Entry<ValueSpecification> entry : values.long2ObjectEntrySet()) {
          final Key value = new Key(entry.getLongKey());
          _specificationToIdentifier.put(entry.getValue(), value);
          _identifierToSpecification.put(value, entry.getValue());
        }
        specifications.putAll(values);
      }
    }
    return specifications;
  }

}

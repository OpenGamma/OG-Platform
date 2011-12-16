/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository of object definitions, mapping integer identifiers to the underlying objects. These
 * identifiers will be used by clients to refer to the objects for invocation.
 * 
 * @param <T> the type of object held
 */
public class DefinitionRepository<T extends Definition> {

  private final AtomicInteger _nextIdentifier = new AtomicInteger(1);
  private final Map<Integer, T> _objects = new ConcurrentHashMap<Integer, T>();
  private final ConcurrentMap<String, Integer> _objectNames = new ConcurrentHashMap<String, Integer>();
  private boolean _initialized;

  /**
   * Adds an object to the repository and returns its allocated identifier. If an object
   * already exists with the same name, it is replaced.
   * 
   * @param object the object to add, not null
   * @return the identifier 
   */
  public int add(final T object) {
    Integer identifier = _objectNames.get(object.getName());
    if (identifier == null) {
      identifier = _nextIdentifier.getAndIncrement();
      final Integer existing = _objectNames.putIfAbsent(object.getName(), identifier);
      if (existing != null) {
        identifier = existing;
      }
    }
    _objects.put(identifier, object);
    return identifier;
  }

  /**
   * Returns an object from the repository.
   * 
   * @param identifier the object to retrieve
   * @return the object, null if not found
   */
  public T get(final int identifier) {
    return _objects.get(identifier);
  }

  /**
   * Returns the current definitions from the repository.
   * 
   * @return an unmodifiable map of the current definitions
   */
  public Map<Integer, T> getAll() {
    return Collections.unmodifiableMap(_objects);
  }

  /**
   * Initializes the repository from a definition provider. Any definitions
   * with matching names are replaced. Anything else is created new.
   * 
   * @param provider definition provider
   * @param reinitialize false to ignore if already initialized, true to reinitialize regardless
   * @return true if initialization occurred, false if not
   */
  public synchronized boolean initialize(final DefinitionProvider<T> provider, final boolean reinitialize) {
    if (!reinitialize && _initialized) {
      return false;
    }
    final List<T> definitions = new ArrayList<T>(provider.getDefinitions());
    Collections.sort(definitions, new Comparator<T>() {
      @Override
      public int compare(final T o1, final T o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    for (T definition : definitions) {
      add(definition);
    }
    _initialized = true;
    return true;
  }

}

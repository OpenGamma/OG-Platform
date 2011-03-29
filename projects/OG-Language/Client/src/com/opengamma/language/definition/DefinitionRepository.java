/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository of object definitions, mapping unique integer identifiers to the underlying objects. These
 * identifiers will be used by clients to refer to the objects for invocation. All objects are identified
 * from a single numbering scheme but invocation messages will imply a specific object type to allow
 * behavior filters to be applied to specific clients - a filter in front of the repository will provide
 * the necessary casting.
 * 
 * @param <T> the type of object held
 */
public class DefinitionRepository<T> {

  private final AtomicInteger _nextIdentifier;
  private final Map<Integer, T> _objects;

  @SuppressWarnings("unchecked")
  private DefinitionRepository(final AtomicInteger nextIdentifier, Map<Integer, ?> objects) {
    _nextIdentifier = nextIdentifier;
    _objects = (Map<Integer, T>) objects;
  }

  public DefinitionRepository() {
    this(new AtomicInteger(1), new ConcurrentHashMap<Integer, T>());
  }

  protected DefinitionRepository(final DefinitionRepository<?> underlying) {
    this(underlying._nextIdentifier, underlying._objects);
  }

  /**
   * Adds an object to the repository and returns its allocated identifier.
   * 
   * @param object the object to add, not {@code null}
   * @return the identifier 
   */
  public int add(final T object) {
    final int identifier = _nextIdentifier.getAndIncrement();
    final Object previous = _objects.put(identifier, object);
    assert (previous == null);
    return identifier;
  }

  /**
   * Returns an object from the repository.
   * 
   * @param identifier the object to retrieve
   * @return the object, or {@code null} if not found
   */
  public T get(final int identifier) {
    return _objects.get(identifier);
  }

}

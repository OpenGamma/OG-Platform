/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Partial implementation of {@link DefinitionProvider} that caches the results.
 * 
 * @param <T> the definition type
 */
public abstract class AbstractDefinitionProvider<T extends Definition> implements DefinitionProvider<T> {

  private final boolean _enableCache;
  private volatile Set<T> _cached;

  protected AbstractDefinitionProvider() {
    this(true);
  }

  protected AbstractDefinitionProvider(final boolean enableCache) {
    _enableCache = enableCache;
  }

  /**
   * Adds any definitions to the supplied collection. The implementation must not
   * attempt any caching or retain a reference to the collection.
   *
   * @param definitions the collection to populate, not null
   */
  protected abstract void loadDefinitions(Collection<T> definitions);

  /**
   * Implements {@link #getDefinitions} when caching is disabled, or if the cache
   * has been flushed. A typical implementation should implement {@link #loadDefinitions}
   * and not override this unless it needs to modify the actual collection object
   * returned by the provider.
   * 
   * @return the definitions, null or the empty set if there are none
   */
  protected Set<T> getDefinitionsImpl() {
    final Set<T> definitions = new HashSet<T>();
    loadDefinitions(definitions);
    return Collections.unmodifiableSet(definitions);
  }

  // FunctionProvider

  @Override
  public final Set<T> getDefinitions() {
    if (_enableCache) {
      if (_cached == null) {
        synchronized (this) {
          if (_cached == null) {
            final Set<T> definitions = getDefinitionsImpl();
            if (definitions == null) {
              _cached = Collections.emptySet();
            } else {
              _cached = definitions;
            }
          }
        }
      }
      return _cached;
    } else {
      return getDefinitionsImpl();
    }
  }

  @Override
  public void flush() {
    if (_enableCache) {
      _cached = null;
    }
  }

}

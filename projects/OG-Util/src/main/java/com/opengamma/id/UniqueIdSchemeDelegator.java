/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.ArgumentChecker;

/**
 * Delegator that switches between multiple implementations based on the scheme of a unique identifier.
 * <p>
 * This class can be used on its own, however it is best used by creating a subclass.
 * <p>
 * This class is mutable and thread-safe via internal synchronization.
 * 
 * @param <T>  the type of the delegate
 */
public class UniqueIdSchemeDelegator<T> {

  /**
   * The default delegate.
   */
  private final T _defaultDelegate;
  /**
   * The map of registered delegates.
   */
  private final ConcurrentMap<String, T> _schemeToDelegateMap = new ConcurrentHashMap<String, T>();

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultDelegate  the delegate to use when no scheme matches, not null
   */
  public UniqueIdSchemeDelegator(final T defaultDelegate) {
    ArgumentChecker.notNull(defaultDelegate, "defaultDelegate");
    _defaultDelegate = defaultDelegate;
  }

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultDelegate  the delegate to use when no scheme matches, not null
   * @param schemePrefixToDelegateMap  the map of delegates by scheme to switch on, not null
   */
  public UniqueIdSchemeDelegator(final T defaultDelegate, final Map<String, T> schemePrefixToDelegateMap) {
    ArgumentChecker.notNull(defaultDelegate, "defaultDelegate");
    ArgumentChecker.notNull(schemePrefixToDelegateMap, "schemePrefixToDelegateMap");
    _defaultDelegate = defaultDelegate;
    for (Map.Entry<String, T> delegate : schemePrefixToDelegateMap.entrySet()) {
      registerDelegate(delegate.getKey(), delegate.getValue());
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the default delegate.
   * 
   * @return the default delegate, not null
   */
  public T getDefaultDelegate() {
    return _defaultDelegate;
  }

  /**
   * Gets the map of registered delegates.
   * 
   * @return the registered delegates, unmodifiable, not null
   */
  public Map<String, T> getDelegates() {
    return ImmutableMap.copyOf(_schemeToDelegateMap);
  }
  
  /**
   * Returns the default delegate followed by the mapped one.
   * 
   * @return all delegates
   */
  public Iterable<T> getAllDelegates() {
    return concat(singleton(_defaultDelegate), getDelegates().values());
  }

  //-------------------------------------------------------------------------
  /**
   * Chooses the delegate for a specific identifier scheme.
   * 
   * @param scheme  the identifier scheme, not null
   * @return the delegate, not null
   */
  public T chooseDelegate(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    String[] schemeParts = StringUtils.split(scheme, "-", 2);
    String schemePrefix = schemeParts[0];
    final T delegate = _schemeToDelegateMap.get(schemePrefix);
    return (delegate != null) ? delegate : _defaultDelegate;
  }

  //-------------------------------------------------------------------------
  /**
   * Registers a delegate based on a scheme.
   * 
   * @param scheme  the scheme to match, not null
   * @param delegate  the delegate to use, not null
   * @return false if a delegate with the given scheme was previously registered, true otherwise
   */
  public boolean registerDelegate(final String scheme, final T delegate) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(delegate, "delegate");
    return _schemeToDelegateMap.putIfAbsent(scheme, delegate) == null;
  }

  /**
   * Removes a delegate from those being used.
   * 
   * @param scheme  the scheme to remove, not null
   */
  public void removeDelegate(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _schemeToDelegateMap.remove(scheme);
  }

}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.util.ArgumentChecker;

/**
 * A base class for a service (e.g. a {@link PositionMaster} or {@link SecurityMaster} that delegates functionality
 * based on the scheme of a unique identifier.
 * 
 * @param <T> type of the delegate.
 */
public class DelegateByScheme<T> {

  private final T _defaultDelegate;
  private final Map<String, T> _schemeToDelegateMap = new ConcurrentHashMap<String, T>();

  protected DelegateByScheme(final T defaultDelegate) {
    ArgumentChecker.notNull(defaultDelegate, "defaultDelegate");
    _defaultDelegate = defaultDelegate;
  }

  protected DelegateByScheme(final T defaultDelegate, final Map<String, T> delegates) {
    ArgumentChecker.notNull(defaultDelegate, "defaultDelegate");
    ArgumentChecker.notNull(delegates, "delegates");
    _defaultDelegate = defaultDelegate;
    for (Map.Entry<String, T> delegate : delegates.entrySet()) {
      registerDelegate(delegate.getKey(), delegate.getValue());
    }
  }

  protected T chooseDelegate(final UniqueIdentifier uid) {
    final T delegate = _schemeToDelegateMap.get(uid.getScheme());
    return (delegate != null) ? delegate : _defaultDelegate;
  }

  protected T getDefaultDelegate() {
    return _defaultDelegate;
  }

  protected Collection<T> getDelegates() {
    return _schemeToDelegateMap.values();
  }

  public void registerDelegate(final String scheme, final T delegate) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(delegate, "delegate");
    _schemeToDelegateMap.put(scheme, delegate);
  }

  public void removeDelegate(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _schemeToDelegateMap.remove(scheme);
  }

}

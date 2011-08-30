/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.language.context.UserContext;

/**
 * Represents a {@link ViewClient} managed within a user's context. Language binding specific data can be associated with
 * the object using {@link UserViewClientBinding}.
 */
public final class UserViewClient {

  private final AtomicInteger _refCount = new AtomicInteger(1);
  private final UserContext _userContext;
  private final ViewClient _viewClient;
  private final ViewClientKey _viewClientKey;
  private volatile Map<Object, UserViewClientData> _data;

  protected UserViewClient(final UserContext userContext, final ViewClient viewClient, final ViewClientKey viewClientKey) {
    _userContext = userContext;
    _viewClient = viewClient;
    _viewClientKey = viewClientKey;
    _data = null;
  }

  /**
   * Increments the reference/lock count.
   * 
   * @return true if the count was incremented, false if it was already zero (an unreferenced/dead object)
   */
  protected boolean incrementRefCount() {
    int refCount;
    do {
      refCount = _refCount.get();
      if (refCount <= 0) {
        return false;
      }
    } while (!_refCount.compareAndSet(refCount, refCount + 1));
    return true;
  }

  /**
   * Decrements the reference/lock count.
   * 
   * @return false when the count reaches zero, true otherwise 
   */
  protected boolean decrementRefCount() {
    assert _refCount.get() > 0;
    return _refCount.decrementAndGet() > 0;
  }

  protected boolean isActive() {
    return true;
  }

  protected void destroy() {
    for (UserViewClientData data : _data.values()) {
      data.destroy();
    }
    getViewClient().detachFromViewProcess();
  }

  public UserContext getUserContext() {
    return _userContext;
  }

  public ViewClient getViewClient() {
    return _viewClient;
  }

  public ViewClientKey getViewClientKey() {
    return _viewClientKey;
  }

  public <T extends UserViewClientData> T getData(final UserViewClientBinding<T> binding) {
    return binding.get(this);
  }

  /**
   * Returns a value from the data map. This is used by {@link UserViewClientBinding} to apply type-safety and a data
   * construction service.
   * 
   * @param <T> requested value type
   * @param key key the value was stored against
   * @return the value, or null if none
   */
  @SuppressWarnings("unchecked")
  protected <T extends UserViewClientData> T getData(final Object key) {
    final Map<Object, UserViewClientData> data = _data;
    if (data != null) {
      return (T) data.get(key);
    } else {
      return null;
    }
  }

  /**
   * Sets a value in the data map. This is used by {@link UserViewClientBinding} to apply type-safety and a data
   * construction service.
   * 
   * @param key key to store the value against
   * @param value value to store, never null
   */
  protected synchronized void setData(final Object key, final UserViewClientData value) {
    Map<Object, UserViewClientData> data = _data;
    if (data != null) {
      // Note: The hash map is probably not very efficient for small items; can we do something better for small maps?
      data = new HashMap<Object, UserViewClientData>(data);
      data.put(key, value);
    } else {
      data = Collections.singletonMap(key, value);
    }
    _data = data;
  }

}

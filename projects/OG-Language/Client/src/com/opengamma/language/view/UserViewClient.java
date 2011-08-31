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

import javax.time.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.language.context.UserContext;
import com.opengamma.livedata.UserPrincipal;

/**
 * Represents a {@link ViewClient} managed within a user's context. Language binding specific data can be associated with
 * the object using {@link UserViewClientBinding}.
 */
public final class UserViewClient implements UniqueIdentifiable {

  private static final ViewResultListener[] EMPTY = new ViewResultListener[0];

  private final AtomicInteger _refCount = new AtomicInteger(1);
  private final AtomicInteger _detachCount = new AtomicInteger(0);
  private final UserContext _userContext;
  private final ViewClient _viewClient;
  private final ViewClientKey _viewClientKey;
  private final UniqueId _uniqueId;
  private volatile Map<Object, UserViewClientData> _data;
  private volatile ViewResultListener[] _listeners = EMPTY;
  private volatile boolean _attached;

  private final ViewResultListener _listener = new ViewResultListener() {

    @Override
    public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
      for (ViewResultListener listener : _listeners) {
        listener.cycleCompleted(fullResult, deltaResult);
      }
    }

    @Override
    public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
      for (ViewResultListener listener : _listeners) {
        listener.cycleExecutionFailed(executionOptions, exception);
      }
    }

    @Override
    public UserPrincipal getUser() {
      return getUserContext().getLiveDataUser();
    }

    @Override
    public void processCompleted() {
      for (ViewResultListener listener : _listeners) {
        listener.processCompleted();
      }
    }

    @Override
    public void processTerminated(final boolean executionInterrupted) {
      for (ViewResultListener listener : _listeners) {
        listener.processTerminated(executionInterrupted);
      }
    }

    @Override
    public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
      for (ViewResultListener listener : _listeners) {
        listener.viewDefinitionCompilationFailed(valuationTime, exception);
      }
    }

    @Override
    public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
      for (ViewResultListener listener : _listeners) {
        listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
      }
    }

  };

  protected UserViewClient(final UserContext userContext, final ViewClient viewClient, final ViewClientKey viewClientKey) {
    _userContext = userContext;
    _viewClient = viewClient;
    _viewClientKey = viewClientKey;
    _uniqueId = viewClient.getUniqueId();
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
   * Increments the detachment count.
   * 
   * @return true if the count was non-zero, false if this was the first detachment
   */
  protected boolean incrementDetachCount() {
    return _detachCount.getAndIncrement() > 0;
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

  /**
   * Decrements the detachment count.
   * 
   * @return false when the count reaches zero, true otherwise
   */
  protected boolean decrementDetachCount() {
    assert _detachCount.get() > 0;
    return _detachCount.decrementAndGet() > 0;
  }

  protected boolean isLocked() {
    return _refCount.get() > 0;
  }

  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  protected void destroy() {
    if (_data != null) {
      for (UserViewClientData data : _data.values()) {
        data.destroy();
      }
    }
    getViewClient().shutdown();
  }

  public UserContext getUserContext() {
    return _userContext;
  }

  /**
   * Returns the view client, attached to the remote process.
   * 
   * @return the attached view client
   */
  public ViewClient getViewClient() {
    if (!_attached) {
      synchronized (this) {
        if (!_attached) {
          final ViewClientDescriptor vcd = getViewClientKey().getClientDescriptor();
          _viewClient.attachToViewProcess(vcd.getViewName(), vcd.getExecutionOptions(), !getViewClientKey().isSharedProcess());
          _attached = true;
        }
      }
    }
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
   * @param key key the value was stored against
   * @return the value, or null if none
   */
  protected UserViewClientData getData(final Object key) {
    final Map<Object, UserViewClientData> data = _data;
    if (data != null) {
      return data.get(key);
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

  /**
   * Adds a result listener to the client. The listener callbacks must not throw exceptions, or other sessions sharing
   * the view client may break.
   * 
   * @param resultListener the result listener to add, not null
   */
  public synchronized void addResultListener(final ViewResultListener resultListener) {
    if (_listeners == EMPTY) {
      final ViewResultListener[] listeners = new ViewResultListener[] {resultListener };
      _listeners = listeners;
      _viewClient.setResultListener(_listener);
    } else {
      final ViewResultListener[] listeners = new ViewResultListener[_listeners.length + 1];
      System.arraycopy(_listeners, 0, listeners, 1, _listeners.length);
      listeners[0] = resultListener;
      _listeners = listeners;
    }
  }

  /**
   * Removes a result listener from the client. 
   * 
   * @param resultListener the result listener to remove, not null
   */
  public synchronized void removeResultListener(final ViewResultListener resultListener) {
    for (int i = 0; i < _listeners.length; i++) {
      if (_listeners[i] == resultListener) {
        if (_listeners.length == 1) {
          _viewClient.setResultListener(null);
          _listeners = EMPTY;
          return;
        }
        final ViewResultListener[] listeners = new ViewResultListener[_listeners.length - 1];
        if (i > 0) {
          System.arraycopy(_listeners, 0, listeners, 0, i - 1);
        }
        i++;
        if (i < _listeners.length) {
          System.arraycopy(_listeners, i, listeners, i - 1, _listeners.length - i);
        }
        _listeners = listeners;
        return;
      }
    }
  }

}

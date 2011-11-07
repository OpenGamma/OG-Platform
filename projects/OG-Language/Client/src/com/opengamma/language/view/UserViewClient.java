/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.time.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.language.config.ConfigurationItem;
import com.opengamma.language.context.UserContext;
import com.opengamma.livedata.UserPrincipal;

/**
 * Represents a {@link ViewClient} managed within a user's context. Language binding specific data can be associated with
 * the object using {@link UserViewClientBinding}.
 */
public final class UserViewClient implements UniqueIdentifiable {

  private static final ViewResultListener[] EMPTY = new ViewResultListener[0];

  private interface ViewResultListenerEvent {

    void callback(ViewResultListener listener);

  }

  private final AtomicInteger _refCount = new AtomicInteger(1);
  private final UserContext _userContext;
  private final ViewClient _viewClient;
  private final ViewClientKey _viewClientKey;
  private final UniqueId _uniqueId;
  private final List<ViewResultListenerEvent> _coreEvents = new LinkedList<ViewResultListenerEvent>();
  private volatile Map<Object, UserViewClientData> _data;
  private volatile ViewResultListener[] _listeners = EMPTY;
  private volatile boolean _attached;
  private Set<ConfigurationItem> _appliedConfiguration;

  private final ViewResultListener _listener = new ViewResultListener() {

    @Override
    public void jobResultReceived(ViewResultModel fullResult, ViewDeltaResultModel deltaResult) {
      for (ViewResultListener listener : _listeners) {
        listener.jobResultReceived(fullResult, deltaResult);
      }
    }

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
      ViewResultListener[] listeners;
      synchronized (this) {
        _coreEvents.add(new ViewResultListenerEvent() {
          @Override
          public void callback(final ViewResultListener listener) {
            listener.processCompleted();
          }
        });
        listeners = _listeners;
      }
      for (ViewResultListener listener : listeners) {
        listener.processCompleted();
      }
    }

    @Override
    public void processTerminated(final boolean executionInterrupted) {
      ViewResultListener[] listeners;
      synchronized (this) {
        _coreEvents.add(new ViewResultListenerEvent() {
          @Override
          public void callback(final ViewResultListener listener) {
            listener.processTerminated(executionInterrupted);
          }
        });
        listeners = _listeners;
      }
      for (ViewResultListener listener : listeners) {
        listener.processTerminated(executionInterrupted);
      }
    }

    @Override
    public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
      ViewResultListener[] listeners;
      synchronized (this) {
        _coreEvents.add(new ViewResultListenerEvent() {
          @Override
          public void callback(final ViewResultListener listener) {
            listener.viewDefinitionCompilationFailed(valuationTime, exception);
          }
        });
        listeners = _listeners;
      }
      for (ViewResultListener listener : listeners) {
        listener.viewDefinitionCompilationFailed(valuationTime, exception);
      }
    }

    @Override
    public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
      ViewResultListener[] listeners;
      synchronized (this) {
        _coreEvents.add(new ViewResultListenerEvent() {
          @Override
          public void callback(final ViewResultListener listener) {
            listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
          }
        });
        listeners = _listeners;
      }
      for (ViewResultListener listener : listeners) {
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
    _viewClient.setResultListener(_listener);
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
    // Don't call getViewClient as that will attach to a remote process (probably not what we want)
    _viewClient.setResultListener(null);
    _viewClient.shutdown();
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
          _viewClient.attachToViewProcess(vcd.getViewId(), vcd.getExecutionOptions(), !getViewClientKey().isSharedProcess());
          _attached = true;
        }
      }
    }
    return _viewClient;
  }

  public ViewClientKey getViewClientKey() {
    return _viewClientKey;
  }

  /**
   * Returns the user data associated with the client. The first caller will create the user data. Other callers will be blocked
   * until the data is available.
   * 
   * @param <T> user data type
   * @param binding the data binding, not null
   * @return the user data, null if there was previously an error
   */
  public <T extends UserViewClientData> T getData(final UserViewClientBinding<T> binding) {
    return binding.get(this, true);
  }

  /**
   * Returns the user data associated with the client if it is available.
   * 
   * @param <T> user data type
   * @param binding the data binding, not null
   * @return the user data, null if there was previously an error or the data hasn't been created yet.
   */
  public <T extends UserViewClientData> T tryAndGetData(final UserViewClientBinding<T> binding) {
    return binding.get(this, false);
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
   * the view client may break. After a listener is added, core state events are passed immediately to it.
   * 
   * @param resultListener the result listener to add, not null
   */
  public void addResultListener(final ViewResultListener resultListener) {
    final List<ViewResultListenerEvent> coreEvents;
    synchronized (this) {
      final ViewResultListener[] listeners = new ViewResultListener[_listeners.length + 1];
      System.arraycopy(_listeners, 0, listeners, 1, _listeners.length);
      listeners[0] = resultListener;
      synchronized (_listener) {
        coreEvents = _coreEvents.isEmpty() ? null : new ArrayList<ViewResultListenerEvent>(_coreEvents);
        _listeners = listeners;
      }
    }
    // Note that a new event may be delivered to the listener before (or during) the core events. This is not a good
    // state of affairs, but probably not a major problem.
    if (coreEvents != null) {
      for (ViewResultListenerEvent event : coreEvents) {
        event.callback(resultListener);
      }
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

  protected synchronized Set<ConfigurationItem> getAndSetConfiguration(final Set<ConfigurationItem> configurationItems) {
    final Set<ConfigurationItem> previouslyApplied = _appliedConfiguration;
    _appliedConfiguration = configurationItems;
    return previouslyApplied;
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
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

  private static final Logger s_logger = LoggerFactory.getLogger(UserViewClient.class);
  private static final ViewResultListener[] EMPTY = new ViewResultListener[0];

  private static final int ET_FINISH = 1;
  private static final int ET_VIEWDEF = 2;

  private abstract class ViewResultListenerEvent {

    private final int _type;

    public abstract void callback(ViewResultListener listener);

    public ViewResultListenerEvent(final int type) {
      _type = type;
    }

    @Override
    public boolean equals(final Object o) {
      return _type == ((ViewResultListenerEvent) o)._type;
    }

    @Override
    public int hashCode() {
      return _type;
    }

  }

  private final AtomicInteger _refCount = new AtomicInteger(1);
  private final UserContext _userContext;
  private final ViewClient _viewClient;
  private final ViewClientKey _viewClientKey;
  private final UniqueId _uniqueId;
  private final Collection<ViewResultListenerEvent> _coreEvents = new LinkedList<ViewResultListenerEvent>();
  private volatile Map<Object, UserViewClientData> _data;
  private volatile ViewResultListener[] _listeners = EMPTY;
  private volatile boolean _attached;
  private Set<ConfigurationItem> _appliedConfiguration;

  private final ViewResultListener _listener = new ViewResultListener() {

    @Override
    public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
      final ViewResultListener[] listeners;
      final ViewResultListenerEvent event = new ViewResultListenerEvent(ET_VIEWDEF) {
        @Override
        public void callback(final ViewResultListener listener) {
          listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
        }
      };
      synchronized (this) {
        _coreEvents.remove(event);
        _coreEvents.add(event);
        listeners = _listeners;
      }
      for (ViewResultListener listener : listeners) {
        listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
      }
    }
    
    @Override
    public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
      final ViewResultListener[] listeners;
      final ViewResultListenerEvent event = new ViewResultListenerEvent(ET_VIEWDEF) {
        @Override
        public void callback(final ViewResultListener listener) {
          listener.viewDefinitionCompilationFailed(valuationTime, exception);
        }
      };
      synchronized (this) {
        _coreEvents.remove(event);
        _coreEvents.add(event);
        listeners = _listeners;
      }
      for (ViewResultListener listener : listeners) {
        listener.viewDefinitionCompilationFailed(valuationTime, exception);
      }
    }
    
    @Override
    public void cycleFragmentCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
      for (ViewResultListener listener : _listeners) {
        listener.cycleFragmentCompleted(fullResult, deltaResult);
      }
    }

    @Override
    public void cycleStarted(ViewCycleMetadata cycleMetadata) {
      for (ViewResultListener listener : _listeners) {
        listener.cycleStarted(cycleMetadata);
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
      final ViewResultListener[] listeners;
      final ViewResultListenerEvent event = new ViewResultListenerEvent(ET_FINISH) {
        @Override
        public void callback(final ViewResultListener listener) {
          listener.processCompleted();
        }
      };
      synchronized (this) {
        _coreEvents.remove(event);
        _coreEvents.add(event);
        listeners = _listeners;
      }
      for (ViewResultListener listener : listeners) {
        listener.processCompleted();
      }
    }

    @Override
    public void processTerminated(final boolean executionInterrupted) {
      final ViewResultListener[] listeners;
      final ViewResultListenerEvent event = new ViewResultListenerEvent(ET_FINISH) {
        @Override
        public void callback(final ViewResultListener listener) {
          listener.processTerminated(executionInterrupted);
        }
      };
      synchronized (this) {
        _coreEvents.remove(event);
        _coreEvents.add(event);
        listeners = _listeners;
      }
      for (ViewResultListener listener : listeners) {
        listener.processTerminated(executionInterrupted);
      }
    }

    @Override
    public void clientShutdown(Exception e) {
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
    // We are only doing some housekeeping, so don't bail out if either method throws exceptions (e.g. from the JMS transport)
    try {
      _viewClient.setResultListener(null);
    } catch (Throwable t) {
      s_logger.warn("Error clearing result listener: {}", t.getMessage());
      s_logger.debug("Caught exception", t);
    }
    try {
      _viewClient.shutdown();
    } catch (Throwable t) {
      s_logger.warn("Error shutting down view client: {}", t.getMessage());
      s_logger.debug("Caught exception", t);
    }
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

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link ConfigurationScanner}.
 */
public abstract class AbstractConfigurationScanner implements ConfigurationScanner {

  private final Set<Configuration> _configurations = new HashSet<Configuration>();
  private final Set<ConfigurationListener> _listeners = new HashSet<ConfigurationListener>();
  private boolean _complete;

  /**
   * Adds a configuration that this scanner has found. If there are any listeners registered
   * they will be notified.
   * 
   * @param configuration the configuration found, not null
   */
  protected void addConfiguration(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    Set<Configuration> configurations = null;
    List<ConfigurationListener> listeners = null;
    synchronized (this) {
      if (_complete) {
        throw new IllegalStateException("Configuration scan is complete");
      }
      if (_configurations.add(configuration) && !_listeners.isEmpty()) {
        configurations = getConfigurations();
        listeners = new ArrayList<ConfigurationListener>(_listeners);
      }
    }
    if (configurations != null) {
      for (ConfigurationListener listener : listeners) {
        listener.foundConfigurations(configurations, false);
      }
    }
  }

  /**
   * Adds one or more configurations that this scanner has found. If there are any listeners
   * registered they will be notified.
   * 
   * @param configurations the configurations found, not null
   */
  protected void addConfigurations(final Collection<Configuration> configurations) {
    ArgumentChecker.notNull(configurations, "configurations");
    Set<Configuration> allConfigurations = null;
    List<ConfigurationListener> listeners = null;
    synchronized (this) {
      if (_complete) {
        throw new IllegalStateException("Configuration scan is complete");
      }
      if (_configurations.addAll(configurations) && !_listeners.isEmpty()) {
        allConfigurations = getConfigurations();
        listeners = new ArrayList<ConfigurationListener>(_listeners);
      }
    }
    if (allConfigurations != null) {
      for (ConfigurationListener listener : listeners) {
        listener.foundConfigurations(allConfigurations, false);
      }
    }
  }

  /**
   * Marks the configuration scanner as complete.
   */
  protected void complete() {
    Set<Configuration> configurations = null;
    List<ConfigurationListener> listeners = null;
    synchronized (this) {
      if (_complete) {
        throw new IllegalStateException("Configuration scan is already complete");
      }
      if (!_listeners.isEmpty()) {
        configurations = getConfigurations();
        listeners = new ArrayList<ConfigurationListener>(_listeners);
        _listeners.clear();
      }
      _complete = true;
    }
    if (configurations != null) {
      for (ConfigurationListener listener : listeners) {
        listener.foundConfigurations(configurations, true);
      }
    }
  }

  @Override
  public synchronized Set<Configuration> getConfigurations() {
    return Collections.unmodifiableSet(new HashSet<Configuration>(_configurations));
  }

  @Override
  public synchronized boolean isComplete() {
    return _complete;
  }

  @Override
  public void addListener(final ConfigurationListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    Set<Configuration> configurations = null;
    synchronized (this) {
      if (_complete) {
        configurations = getConfigurations();
      } else {
        _listeners.add(listener);
      }
    }
    if (configurations != null) {
      listener.foundConfigurations(configurations, true);
    }
  }

  @Override
  public synchronized void removeListener(final ConfigurationListener listener) {
    _listeners.remove(listener);
  }

}

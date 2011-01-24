/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.master.listener.NotifyingMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple implementation of {@code ConfigMaster}.
 * <p>
 * This master is either populated by a subclass or by calling {@link #addTypedMaster}.
 * <p>
 * DefaultConfigMaster uses a concurrent map and is thread-safe.
 */
public class DefaultConfigMaster implements ConfigMaster, NotifyingMaster {

  private final CopyOnWriteArraySet<MasterChangeListener> _listeners = new CopyOnWriteArraySet<MasterChangeListener>();

  private MasterChangeListener _listener = new MasterChangeListener() {

    @Override
    public void added(UniqueIdentifier addedItem) {
      notifyAdded(addedItem);
    }

    @Override
    public void corrected(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
      notifyCorrected(oldItem, newItem);
    }

    @Override
    public void removed(UniqueIdentifier removedItem) {
      notifyRemoved(removedItem);
    }

    @Override
    public void updated(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
      notifyUpdated(oldItem, newItem);
    }
  };

  /**
   * Map of masters by class.
   */
  private ConcurrentMap<Class<?>, ConfigTypeMaster<?>> _typedMap = new MapMaker()
      .makeComputingMap(new Function<Class<?>, ConfigTypeMaster<?>>() {
        public ConfigTypeMaster<?> apply(final Class<?> clazz) {
          final ConfigTypeMaster<?> master = createTypedMaster(clazz);
          master.addChangeListener(_listener);
          return master;
        }
      });

  /**
   * Creates an instance.
   */
  public DefaultConfigMaster() {
  }

  //-------------------------------------------------------------------------
  /**
   * Overridable method to create a master for the type specified.
   * 
   * @param <T>  the type of the master to create
   * @param clazz  the class to create a master for, not null
   * @return the created master, not null
   * @throws IllegalArgumentException if a master cannot be created for the specified type
   */
  protected <T> ConfigTypeMaster<T> createTypedMaster(Class<T> clazz) {
    throw new IllegalArgumentException("Unable to create ConfigMaster for " + clazz.getName());
  }

  /**
   * Gets a config master for the type specified.
   * 
   * @param <T>  the type
   * @param clazz  the class to create a master for, not null
   * @return the master, not null
   */
  @SuppressWarnings("unchecked")
  public <T> ConfigTypeMaster<T> getTypedMaster(Class<T> clazz) {
    try {
      return (ConfigTypeMaster<T>) _typedMap.get(clazz);
    } catch (ComputationException ex) {
      if (ex.getCause() instanceof RuntimeException) {
        throw (RuntimeException) ex.getCause();
      }
      throw ex;
    }
  }

  /**
   * Adds a master for the specified type.
   * 
   * @param <T>  the type
   * @param clazz  the class to create a master for, not null
   * @param master  the master to add, not null
   * @throws IllegalStateException if a master already exists for the type
   */
  public <T> void addTypedMaster(Class<T> clazz, ConfigTypeMaster<T> master) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(master, "master");
    ConfigTypeMaster<?> old = _typedMap.putIfAbsent(clazz, master);
    if (old != null) {
      throw new IllegalStateException("Master already exists for " + clazz.getName());
    }
    master.addChangeListener(_listener);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> ConfigTypeMaster<T> typed(Class<T> clazz) {
    return getTypedMaster(clazz);
  }

  @Override
  public void addChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  @Override
  public void removeChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.remove(listener);
  }

  protected void notifyAdded(final UniqueIdentifier identifier) {
    for (MasterChangeListener listener : _listeners) {
      listener.added(identifier);
    }
  }

  protected void notifyRemoved(final UniqueIdentifier identifier) {
    for (MasterChangeListener listener : _listeners) {
      listener.removed(identifier);
    }
  }

  protected void notifyUpdated(final UniqueIdentifier oldIdentifier, final UniqueIdentifier newIdentifier) {
    for (MasterChangeListener listener : _listeners) {
      listener.updated(oldIdentifier, newIdentifier);
    }
  }

  protected void notifyCorrected(final UniqueIdentifier oldIdentifier, final UniqueIdentifier newIdentifier) {
    for (MasterChangeListener listener : _listeners) {
      listener.corrected(oldIdentifier, newIdentifier);
    }
  }

}

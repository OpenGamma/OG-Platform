/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple implementation of {@code ConfigMaster}.
 * <p>
 * This master is either populated by a subclass or by calling {@link #addTypedMaster}.
 * <p>
 * DefaultConfigMaster uses a concurrent map and is thread-safe.
 */
public class DefaultConfigMaster implements ConfigMaster {

  /**
   * Map of masters by class.
   */
  private ConcurrentMap<Class<?>, ConfigTypeMaster<?>> _typedMap = new MapMaker().makeComputingMap(new Function<Class<?>, ConfigTypeMaster<?>>() {
    public ConfigTypeMaster<?> apply(final Class<?> clazz) {
      return createTypedMaster(clazz);
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
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> ConfigTypeMaster<T> typed(Class<T> clazz) {
    return getTypedMaster(clazz);
  }

}

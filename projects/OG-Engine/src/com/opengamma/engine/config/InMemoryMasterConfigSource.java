/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.config;

import com.opengamma.config.ConfigMaster;
import com.opengamma.config.memory.InMemoryConfigMaster;

/**
 * A config source built using the in-memory config master.
 * <p>
 * This class creates instances of {@code InMemoryConfigMaster} on demand and caches them permanently.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 * 
 */
public class InMemoryMasterConfigSource extends MasterConfigSource {

  /**
   * Creates an instance.
   */
  public InMemoryMasterConfigSource() {
  }

  //-------------------------------------------------------------------------
  @Override
  protected <T> ConfigMaster<T> createMaster(final Class<T> clazz) {
    return new InMemoryConfigMaster<T>();
  }

}

/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;


/**
 * A general-purpose configuration master.
 * <p>
 * The configuration master provides a uniform view over storage of configuration elements.
 * This interface provides methods that allow the master to be searched and updated.
 * <p>
 * Many different kinds of configuration element may be stored in a single master.
 * Each type will be stored in an individual {@link ConfigTypeMaster}.
 */
public interface ConfigMaster {

  /**
   * Gets a typed master for the specified type.
   * <p>
   * The master returned will be specific to the requested type.
   * It may, or may not, utilize the same underlying data store.
   * 
   * @param <T>  the type of the typed master to return
   * @param clazz  the type of the required master, not null
   * @return the typed master, not null
   * @throws RuntimeException if the master cannot be created
   */
  <T> ConfigTypeMaster<T> typed(Class<T> clazz);

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicAPI;

/**
 * Resolver capable of providing configuration.
 * <p>
 * This resolver provides lookup of configuration to the engine functions.
 * The lookup may require selecting a single "best match" from a set of potential options.
 * The best match behavior is the key part that distinguishes one implementation from another.
 * Best match selection may use a version-correction, configuration or code as appropriate.
 * Implementations of this interface must specify the rules they use to best match.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicAPI
public interface ConfigResolver {

  /**
   * Gets a configuration element by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single configuration at a single version-correction.
   * As such, there should be no complex matching issues in this lookup.
   * However, if the underlying data store does not handle versioning correctly,
   * then a best match selection may be required.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param uniqueId  the unique identifier to find, not null
   * @return the configuration element, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  <T> T getConfig(Class<T> clazz, UniqueId uniqueId);

  /**
   * Gets a configuration element by object identifier.
   * <p>
   * An object identifier exactly specifies a single configuration, but it provide no information
   * about the version-correction required.
   * As such, it is likely that multiple versions/corrections will match the object identifier.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param objectId  the object identifier to find, not null
   * @return the configuration element, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  <T> T getConfig(Class<T> clazz, ObjectId objectId);

  /**
   * Gets a configuration element by name.
   * <p>
   * Each configuration element has a name and this method allows lookup by name.
   * A name lookup does not guarantee to match a single configuration element but it normally will.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param configName  the configuration name, not null
   * @return the configuration element, null if not found
   * @throws IllegalArgumentException if the name or version-correction is invalid
   * @throws RuntimeException if an error occurs
   */
  <T> T getConfigByName(Class<T> clazz, String configName);

}

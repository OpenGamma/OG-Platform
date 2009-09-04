/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;
import java.util.Set;


/**
 * The base interface through which {@link Security} details can
 * be identified.
 *
 * @author kirk
 */
public interface SecurityMaster {
  
  /**
   * Obtain all securities which match the specified key.
   * If there are none specified, this method must return an
   * empty collection, and not {@code null}.
   * 
   * @param secKey The key to use to lookup security details.
   * @return All securities which match the specified key, or an empty
   *         {@link Collection}.
   */
  Collection<Security> getSecurities(SecurityKey secKey);
  
  /**
   * Obtain the single best-fit {@link Security} for a particular key.
   * In the case where there are multiple securities definied in this
   * {@code SecurityMaster} which match the specified key, implementations
   * may either use a best-fit matching system, or may throw an exception.
   * 
   * @param secKey The key to locate security details.
   * @return The single security matching those details.
   */
  Security getSecurity(SecurityKey secKey);
  
  /**
   * Obtain all security types in this security master.
   * It is up to the implementation to determine whether this returns
   * all those security types currently available, or whether it includes
   * all potential security types.
   * 
   * @return The names of all security types available in this master.
   */
  Set<String> getAllSecurityTypes();

}

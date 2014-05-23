/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.auth;

import java.util.Set;

/**
 * Provides a standard way to access the required permissions of an entity.
 */
public interface Permissionable {

  /**
   * Gets the required permissions of the entity.
   * <p>
   * If a user does not have these permissions, then the user must not be allowed
   * to see the entity.
   * 
   * @return the required permissions, expressed as strings, not null
   */
  Set<String> getRequiredPermissions();

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * Any user known to the OpenGamma Platform installation.
 * <p/> 
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface OGUser extends UniqueIdentifiable {

  /**
   * Gets the external identifier bundle defining the user.
   * <p>
   * Each external system has one or more identifiers by which they refer to the user.
   * Some of these may be unique within that system, while others may be more descriptive.
   * This bundle stores the set of these external identifiers.
   * 
   * @return the bundle, null if not applicable
   */
  ExternalIdBundle getExternalIdBundle();

  /**
   * Gets the name of the user intended for display purposes.
   * 
   * @return the name of the user, not null
   */
  String getName();
  
  /**
   * Obtains the hashed version of the user's password.
   * May be null or empty, particularly if the user is disabled.
   * 
   * @return The hashed password for the user account.
   */
  String getPasswordHash();
}

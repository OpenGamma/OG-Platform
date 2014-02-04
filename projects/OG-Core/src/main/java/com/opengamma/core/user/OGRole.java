/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

import java.util.Set;

/**
 * Any role known to the OpenGamma Platform installation.
 * <p/>
 * A role within the role management system.
 * <p/>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface OGRole extends UniqueIdentifiable {

  /**
   * Gets the role key, used to create capabilities that role is entitled to.
   *
   * @return the role key, may not be null
   */
  String getKey();

  /**
   * Obtains the role entitlements this role is entitled to.
   *
   * @return the entitlements for the role, not null
   */
  Set<OGEntitlement> getEntitlements();

  /**
   * Gets the role name, used to identify the role in a GUI.
   *
   * @return the display role name, may not be null
   */
  String getName();

}

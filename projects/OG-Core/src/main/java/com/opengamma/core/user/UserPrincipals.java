/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicAPI;

/**
 * A logged in user known to the OpenGamma Platform installation.
 * <p>
 * A user principals instance represents a user that has been logged in.
 * The user name is the primary principal used to identify the user.
 * Various secondary-level identifiers are also made available.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface UserPrincipals {

  /**
   * The session attribute key for the user principals.
   * This is used to store the principals in the Apache Shiro session.
   */
  String ATTRIBUTE_KEY = UserPrincipals.class.getName();

  /**
   * Gets the user name that uniquely identifies the user
   * 
   * @return the user name, not null
   */
  String getUserName();

  /**
   * Gets the bundle of alternate user identifiers.
   * <p>
   * This allows the user identifiers of external systems to be associated with the account
   * Some of these may be unique within the external system, others may be more descriptive.
   * 
   * @return the bundle of alternate user identifiers, not null
   */
  ExternalIdBundle getAlternateIds();

  /**
   * The network address of the user, which is intended to be an IP address.
   * <p>
   * Unfortunately it is not possible to guarantee the presence of accuracy of the IP address,
   * notably as a result of web browser and network proxy restrictions.
   * 
   * @return the host address, may be null
   */
  String getNetworkAddress();

  /**
   * The primary email address associated with the user.
   * 
   * @return the primary email address, may be null
   */
  String getEmailAddress();

}

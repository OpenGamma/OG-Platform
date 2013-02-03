/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import java.util.Set;

import org.threeten.bp.ZoneId;

import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * Any user known to the OpenGamma Platform installation.
 * <p>
 * A user within the user management system.
 * Support is provided for external users as well as passwords.
 * <p/> 
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface OGUser extends UniqueIdentifiable, ExternalBundleIdentifiable {

  /**
   * Gets the external identifier bundle defining the user.
   * <p>
   * Each external system has one or more identifiers by which they refer to the user.
   * Some of these may be unique within that system, while others may be more descriptive.
   * This bundle stores the set of these external identifiers.
   * 
   * @return the bundle, not null
   */
  ExternalIdBundle getExternalIdBundle();

  /**
   * Gets the user id that uniquely identifies the user
   * This is used with the password to authenticate.
   * 
   * @return the user id, not null
   */
  String getUserId();

  /**
   * Obtains the hashed version of the user password.
   * May be null or empty, particularly if the user is disabled.
   * 
   * @return the hashed password for the user account, may be null
   */
  String getPasswordHash();

  /**
   * Obtains the user entitlements.
   * Each may be interpreted as a pattern to be applied to a restricted resource.
   * 
   * @return the entitlements for the user in order of processing, not null
   */
  Set<String> getEntitlements();

  /**
   * Gets the display user name, used to identify the user in a GUI.
   * 
   * @return the display user name, may be null
   */
  String getName();

  /**
   * The time-zone used to display local times.
   * 
   * @return the time-zone, not null
   */
  ZoneId getTimeZone();

  /**
   * The primary email address associated with the account.
   * 
   * @return the primary email address, may be null
   */
  String getEmailAddress();

}

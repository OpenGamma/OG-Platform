/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import java.util.Set;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicAPI;

/**
 * A user account known to the OpenGamma Platform installation.
 * <p>
 * A user account within the user management system.
 * Support is provided for external users as well as passwords.
 * User profile data, such as web-site preferences, is held on {@code UserProfile}.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface UserAccount {

  /**
   * Gets the user name that uniquely identifies the user
   * This is used with the password to authenticate.
   * 
   * @return the user name, not null
   */
  String getUserName();

  /**
   * Gets the hashed version of the user password.
   * May be null or empty, particularly if the user is disabled.
   * 
   * @return the hashed password for the user account, may be null
   */
  String getPasswordHash();

  /**
   * Gets the status of the account, which determines if the user is allowed to login.
   * 
   * @return the status, not null
   */
  UserAccountStatus getStatus();

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
   * Gets all the roles that the user belongs to.
   * <p>
   * Roles are used to manage groups of multiple users.
   * This is the combined set of all roles that the user has, expressed as strings.
   * Where and how the roles are stored is not specified.
   * 
   * @return the roles for the user, not null
   */
  Set<String> getRoles();

  /**
   * Gets all the permissions that the user has.
   * <p>
   * Permissions are used to define access control.
   * This is the combined set of all permissions that the user has, expressed as strings.
   * Where and how the permissions are stored is not specified.
   * 
   * @return the permissions for the user, not null
   */
  Set<String> getPermissions();

  /**
   * The primary email address associated with the account.
   * 
   * @return the primary email address, may be null
   */
  String getEmailAddress();

  /**
   * The user profile, containing user settings.
   * 
   * @return the user profile, may be null
   */
  UserProfile getProfile();

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import com.opengamma.DataDuplicationException;
import com.opengamma.DataNotFoundException;
import com.opengamma.DataVersionException;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.user.UserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;

/**
 * A general-purpose role master.
 * <p>
 * The role master provides a uniform view over a set of role definitions.
 * This interface provides methods that allow the master to be searched and updated.
 * <p>
 * The role master supports access by both an identifier and role name.
 * The role name is guaranteed to act as a unique key.
 * The role name of a role may be changed, but this should be done rarely.
 * Old role names may not be reused.
 * Role names are case insensitive by conversion to lower case in the root locale.
 * <p>
 * Unlike some other master interfaces, a role master only maintains lightweight history.
 * Each version is assigned a unique identifier, but only the current version can be queried.
 */
public interface RoleMaster extends ChangeProvider {

  /**
   * Checks if a role name already exists.
   * <p>
   * This will return true if the role name is already in use.
   *
   * @param roleName  the role name to check, not null
   * @return true if in use, false if not in use, not null
   */
  boolean nameExists(String roleName);

  /**
   * Gets a role by name.
   * <p>
   * This will return the role matching the role name.
   * A role name is a unique key for the role master.
   *
   * @param roleName  the role name to retrieve, not null
   * @return the role, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if there is no role with the specified name
   */
  ManageableRole getByName(String roleName);

  /**
   * Gets a role by object identifier.
   * <p>
   * This will return the role matching the object identifier.
   *
   * @param objectId  the object identifier to retrieve, not null
   * @return the role, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if there is no role with the specified identifier
   */
  ManageableRole getById(ObjectId objectId);

  //-------------------------------------------------------------------------
  /**
   * Adds a role to the data store.
   * <p>
   * This adds a role, ensuring that the role does not already exist.
   *
   * @param role  the role to add, not null
   * @return the unique identifier of the added role, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataDuplicationException if the role name is already used
   */
  UniqueId add(ManageableRole role);

  /**
   * Updates a role in the data store.
   * <p>
   * This updates a role, ensuring that the role already exists.
   * The unique identifier must be set in the role and it must have a version.
   *
   * @param role  the role to add, not null
   * @return the unique identifier of the updated role, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if the role to update cannot be found
   * @throws DataVersionException if the unique identifier version is not the latest one
   * @throws DataDuplicationException if the role is being renamed and the name is already used
   */
  UniqueId update(ManageableRole role);

  /**
   * Saves a role in the data store.
   * <p>
   * This saves a role, either adding or updating based on the presence
   * or absence of the object identifier.
   *
   * @param role  the role to save, not null
   * @return the unique identifier of the saved role, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  UniqueId save(ManageableRole role);

  //-------------------------------------------------------------------------
  /**
   * Removes a role from the data store.
   *
   * @param roleName  the role name to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no role with the specified name
   */
  void removeByName(String roleName);

  /**
   * Removes a role from the data store.
   *
   * @param objectId  the object identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no role with the specified identifier
   */
  void removeById(ObjectId objectId);

  //-------------------------------------------------------------------------
  /**
   * Searches for roles matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  RoleSearchResult search(RoleSearchRequest request);

  /**
   * Queries the event history of a single role.
   * <p>
   * If an implementation does not store history, and empty object must be returned.
   * 
   * @param request  the history request, not null
   * @return the role history, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no role with the specified identifier
   */
  RoleEventHistoryResult eventHistory(RoleEventHistoryRequest request);

  //-------------------------------------------------------------------------
  /**
   * Resolves the user account populating the combined set of role and permissions.
   * 
   * @param account  the account to resolve, not null
   * @return the resolved account, not null
   * @throws RuntimeException if an error occurs
   */
  UserAccount resolveAccount(UserAccount account);

}

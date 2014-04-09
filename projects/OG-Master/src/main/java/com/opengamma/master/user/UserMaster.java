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
 * A general-purpose user master.
 * <p>
 * The user master provides a uniform view over a set of user definitions.
 * This interface provides methods that allow the master to be searched and updated.
 * <p>
 * The user master supports access by both an identifier and user name.
 * The user name is guaranteed to act as a unique key.
 * The user name of a user may be changed, but this should be done rarely.
 * Old user names may not be reused.
 * User names are case insensitive by conversion to lower case in the root locale.
 * <p>
 * Unlike some other master interfaces, a user master only maintains lightweight history.
 * Each version is assigned a unique identifier, but only the current version can be queried.
 */
public interface UserMaster extends ChangeProvider {

  /**
   * Checks if a user name already exists.
   * <p>
   * This will return true if the user name is already in use.
   *
   * @param userName  the user name to check, not null
   * @return true if in use, false if not in use, not null
   */
  boolean nameExists(String userName);

  /**
   * Gets a user by name.
   * <p>
   * This will return the user matching the user name.
   * A user name is a unique key for the user master.
   *
   * @param userName  the user name to retrieve, not null
   * @return the user, not null
   * @throws DataNotFoundException if there is no user with the specified name
   */
  ManageableUser getByName(String userName);

  /**
   * Gets a user by object identifier.
   * <p>
   * This will return the user matching the object identifier.
   *
   * @param objectId  the object identifier to retrieve, not null
   * @return the user, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if there is no user with the specified identifier
   */
  ManageableUser getById(ObjectId objectId);

  //-------------------------------------------------------------------------
  /**
   * Adds a user to the data store.
   * <p>
   * This adds a user, ensuring that the user does not already exist.
   *
   * @param user  the user to add, not null
   * @return the unique identifier of the added user, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataDuplicationException if the user name is already used
   */
  UniqueId add(ManageableUser user);

  /**
   * Updates a user in the data store.
   * <p>
   * This updates a user, ensuring that the user already exists.
   * The unique identifier must be set in the user and it must have a version.
   *
   * @param user  the user to add, not null
   * @return the unique identifier of the updated user, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if the user to update cannot be found
   * @throws DataVersionException if the unique identifier version is not the latest one
   * @throws DataDuplicationException if the user is being renamed and the name is already used
   */
  UniqueId update(ManageableUser user);

  /**
   * Saves a user in the data store.
   * <p>
   * This saves a user, either adding or updating based on the presence
   * or absence of the object identifier.
   *
   * @param user  the user to save, not null
   * @return the unique identifier of the saved user, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  UniqueId save(ManageableUser user);

  //-------------------------------------------------------------------------
  /**
   * Removes a user from the data store.
   *
   * @param userName  the user name to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no user with the specified name
   */
  void removeByName(String userName);

  /**
   * Removes a user from the data store.
   *
   * @param objectId  the object identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no user with the specified identifier
   */
  void removeById(ObjectId objectId);

  //-------------------------------------------------------------------------
  /**
   * Searches for users matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  UserSearchResult search(UserSearchRequest request);

  /**
   * Queries the event history of a single user.
   * <p>
   * If an implementation does not store history, and empty object must be returned.
   * 
   * @param request  the history request, not null
   * @return the user history, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no user with the specified identifier
   */
  UserEventHistoryResult eventHistory(UserEventHistoryRequest request);

  //-------------------------------------------------------------------------
  /**
   * Gets the fully resolved user account by user name.
   * 
   * @param userName  the user name, not null
   * @return the user account with the specified user name, not null
   * @throws DataNotFoundException if the user could not be found
   * @throws RuntimeException if an error occurs
   */
  UserAccount getAccount(String userName);

  /**
   * Gets the {@code RoleMaster} used to query and manage users.
   * 
   * @return the user master, not null
   */
  RoleMaster roleMaster();

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.net.URI;

import javax.ws.rs.core.Response.Status;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.user.UserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserEventHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link UserMaster}.
 */
public class RemoteUserMaster
    extends AbstractRemoteMaster
    implements UserMaster {

  /**
   * The role master.
   */
  private final RemoteRoleMaster _roleMaster;

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteUserMaster(final URI baseUri) {
    super(baseUri);
    _roleMaster = new RemoteRoleMaster(baseUri.resolve("users"));
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteUserMaster(final URI baseUri, ChangeManager changeManager) {
    this(baseUri, changeManager, changeManager);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param userChangeManager  the change manager, not null
   * @param roleChangeManager  the change manager, not null
   */
  public RemoteUserMaster(final URI baseUri, ChangeManager userChangeManager, ChangeManager roleChangeManager) {
    super(baseUri, userChangeManager);
    _roleMaster = new RemoteRoleMaster(baseUri.resolve("users"), roleChangeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean nameExists(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    URI uri = DataUserMasterResource.uriNameExists(getBaseUri(), userName);
    ClientResponse response = accessRemote(uri).get(ClientResponse.class);
    if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      return false;
    }
    if (response.getStatus() == Status.OK.getStatusCode()) {
      return true;
    }
    throw new IllegalStateException("Unexpected response from server: " + response.getStatus());
  }

  @Override
  public ManageableUser getByName(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    URI uri = DataUserMasterResource.uriUserByName(getBaseUri(), userName);
    return accessRemote(uri).get(ManageableUser.class);
  }

  @Override
  public ManageableUser getById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    URI uri = DataUserMasterResource.uriUserById(getBaseUri(), objectId);
    return accessRemote(uri).get(ManageableUser.class);
  }

  @Override
  public UniqueId add(ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    URI uri = DataUserMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(UniqueId.class, user);
  }

  @Override
  public UniqueId update(ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(user.getUniqueId(), "user.uniqueId");
    URI uri = DataUserMasterResource.uriUserById(getBaseUri(), user.getUniqueId());
    return accessRemote(uri).put(UniqueId.class, user);
  }

  @Override
  public UniqueId save(ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    if (user.getUniqueId() != null) {
      return update(user);
    } else {
      return add(user);
    }
  }

  @Override
  public void removeByName(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    URI uri = DataUserMasterResource.uriUserByName(getBaseUri(), userName);
    accessRemote(uri).delete();
  }

  @Override
  public void removeById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    URI uri = DataUserMasterResource.uriUserById(getBaseUri(), objectId);
    accessRemote(uri).delete();
  }

  @Override
  public UserSearchResult search(UserSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    URI uri = DataUserMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(UserSearchResult.class, request);
  }

  @Override
  public UserEventHistoryResult eventHistory(UserEventHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    URI uri = DataUserMasterResource.uriEventHistory(getBaseUri(), request);
    return accessRemote(uri).get(UserEventHistoryResult.class);
  }

  @Override
  public UserAccount getAccount(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    URI uri = DataUserMasterResource.uriUserByName(getBaseUri(), userName);
    return accessRemote(uri).get(UserAccount.class);
  }

  @Override
  public RoleMaster roleMaster() {
    return _roleMaster;
  }

}

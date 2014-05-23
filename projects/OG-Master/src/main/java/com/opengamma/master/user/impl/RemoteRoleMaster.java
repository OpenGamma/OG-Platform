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
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.RoleEventHistoryRequest;
import com.opengamma.master.user.RoleEventHistoryResult;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link RoleMaster}.
 */
public class RemoteRoleMaster
    extends AbstractRemoteMaster
    implements RoleMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteRoleMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteRoleMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean nameExists(String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    URI uri = DataRoleMasterResource.uriNameExists(getBaseUri(), roleName);
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
  public ManageableRole getByName(String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    URI uri = DataRoleMasterResource.uriRoleByName(getBaseUri(), roleName);
    return accessRemote(uri).get(ManageableRole.class);
  }

  @Override
  public ManageableRole getById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    URI uri = DataRoleMasterResource.uriRoleById(getBaseUri(), objectId);
    return accessRemote(uri).get(ManageableRole.class);
  }

  @Override
  public UniqueId add(ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    URI uri = DataRoleMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(UniqueId.class, role);
  }

  @Override
  public UniqueId update(ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    ArgumentChecker.notNull(role.getUniqueId(), "role.uniqueId");
    URI uri = DataRoleMasterResource.uriRoleById(getBaseUri(), role.getUniqueId());
    return accessRemote(uri).put(UniqueId.class, role);
  }

  @Override
  public UniqueId save(ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    if (role.getUniqueId() != null) {
      return update(role);
    } else {
      return add(role);
    }
  }

  @Override
  public void removeByName(String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    URI uri = DataRoleMasterResource.uriRoleByName(getBaseUri(), roleName);
    accessRemote(uri).delete();
  }

  @Override
  public void removeById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    URI uri = DataRoleMasterResource.uriRoleById(getBaseUri(), objectId);
    accessRemote(uri).delete();
  }

  @Override
  public RoleSearchResult search(RoleSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    URI uri = DataRoleMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(RoleSearchResult.class, request);
  }

  @Override
  public RoleEventHistoryResult eventHistory(RoleEventHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    URI uri = DataRoleMasterResource.uriEventHistory(getBaseUri(), request);
    return accessRemote(uri).get(RoleEventHistoryResult.class);
  }

  @Override
  public UserAccount resolveAccount(UserAccount account) {
    ArgumentChecker.notNull(account, "account");
    URI uri = DataRoleMasterResource.uriResolveRole(getBaseUri(), account);
    return accessRemote(uri).post(UserAccount.class, account);
  }

}

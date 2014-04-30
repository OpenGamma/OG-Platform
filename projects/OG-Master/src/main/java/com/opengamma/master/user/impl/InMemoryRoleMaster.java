/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Supplier;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.impl.SimpleUserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.HistoryEvent;
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.RoleEventHistoryRequest;
import com.opengamma.master.user.RoleEventHistoryResult;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code RoleMaster}.
 * <p>
 * This master does not support versioning of roles.
 */
public class InMemoryRoleMaster
    extends AbstractInMemoryMaster<ManageableRole>
    implements RoleMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemUsrRole";
  /**
   * The scheme used for removed roles.
   */
  public static final ManageableRole REMOVED = new ManageableRole("%REMOVED%");

  /**
   * Creates an instance.
   */
  public InMemoryRoleMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryRoleMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryRoleMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super("Role", REMOVED, objectIdSupplier, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  String extractName(ManageableRole role) {
    return role.getRoleName();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean nameExists(String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    return super.nameExists(roleName);
  }

  @Override
  public ManageableRole getByName(String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    return super.getByName(roleName);
  }

  @Override
  public ManageableRole getById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    return super.getById(objectId);
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId add(ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    return super.add(role);
  }

  @Override
  public UniqueId update(ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    return super.update(role);
  }

  @Override
  public UniqueId save(ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    return super.save(role);
  }

  //-------------------------------------------------------------------------
  @Override
  public void removeByName(String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    super.removeByName(roleName);
  }

  @Override
  public void removeById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    super.removeById(objectId);
  }

  //-------------------------------------------------------------------------
  @Override
  public RoleSearchResult search(RoleSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    List<ManageableRole> list = new ArrayList<>();
    for (ManageableRole role : getStoredValues()) {
      if (request.matches(role)) {
        list.add(role);
      }
    }
    Collections.sort(list, request.getSortOrder());
    Paging paging = Paging.of(request.getPagingRequest(), list);
    return new RoleSearchResult(paging, request.getPagingRequest().select(list));
  }

  @Override
  public RoleEventHistoryResult eventHistory(RoleEventHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    List<HistoryEvent> history = super.eventHistory(request.getObjectId(), request.getRoleName());
    return new RoleEventHistoryResult(history);
  }

  //-------------------------------------------------------------------------
  @Override
  public UserAccount resolveAccount(UserAccount account) {
    ArgumentChecker.notNull(account, "account");
    SimpleUserAccount resolved = SimpleUserAccount.from(account);
    // find roles containing the user
    RoleSearchRequest request = new RoleSearchRequest();
    request.setAssociatedUser(account.getUserName());
    RoleSearchResult result = search(request);
    // find linked roles and permissions
    Set<String> rolesToLoad = new HashSet<>();
    for (ManageableRole loaded : result.getRoles()) {
      resolved.getRoles().add(loaded.getRoleName());
      resolved.getPermissions().addAll(loaded.getAssociatedPermissions());
      rolesToLoad.addAll(loaded.getAssociatedRoles());
    }
    rolesToLoad.removeAll(resolved.getRoles());
    // find implied roles and permissions
    while (rolesToLoad.size() > 0) {
      String roleToLoad = rolesToLoad.iterator().next();
      ManageableRole loaded = getByName0(roleToLoad);
      resolved.getRoles().add(loaded.getRoleName());
      resolved.getPermissions().addAll(loaded.getAssociatedPermissions());
      rolesToLoad.addAll(loaded.getAssociatedRoles());
      rolesToLoad.removeAll(resolved.getRoles());
    }
    return resolved;
  }

  //-------------------------------------------------------------------------
  @Override
  public final ChangeManager changeManager() {
    return getChangeManager();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return String.format("%s[size=%d]", getClass().getSimpleName(), getStoredValues().size());
  }

}

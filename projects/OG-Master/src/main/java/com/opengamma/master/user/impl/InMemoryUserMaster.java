/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.impl.SimpleUserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.HistoryEvent;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserEventHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code UserMaster}.
 * <p>
 * This master does not support versioning of users.
 */
public class InMemoryUserMaster
    extends AbstractInMemoryMaster<ManageableUser>
    implements UserMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemUsr";
  /**
   * The scheme used for removed users.
   */
  public static final ManageableUser REMOVED = new ManageableUser("%REMOVED%");

  /**
   * The role master.
   */
  private final RoleMaster _roleMaster;

  /**
   * Creates an instance.
   */
  public InMemoryUserMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryUserMaster(final ObjectIdSupplier objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryUserMaster(final ObjectIdSupplier objectIdSupplier, final ChangeManager changeManager) {
    super("User", REMOVED, objectIdSupplier, changeManager);
    _roleMaster = new InMemoryRoleMaster(new ObjectIdSupplier(objectIdSupplier.getScheme() + "Role"));
  }

  //-------------------------------------------------------------------------
  @Override
  String extractName(ManageableUser user) {
    return user.getUserName();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean nameExists(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    return super.nameExists(userName);
  }

  @Override
  public ManageableUser getByName(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    return super.getByName(userName);
  }

  @Override
  public ManageableUser getById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    return super.getById(objectId);
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId add(ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    return super.add(user);
  }

  @Override
  public UniqueId update(ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    return super.update(user);
  }

  @Override
  public UniqueId save(ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    return super.save(user);
  }

  //-------------------------------------------------------------------------
  @Override
  public void removeByName(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    super.removeByName(userName);
  }

  @Override
  public void removeById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    super.removeById(objectId);
  }

  //-------------------------------------------------------------------------
  @Override
  public UserSearchResult search(UserSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    List<ManageableUser> list = new ArrayList<>();
    for (ManageableUser user : getStoredValues()) {
      if (request.matches(user)) {
        list.add(user);
      }
    }
    Collections.sort(list, request.getSortOrder());
    Paging paging = Paging.of(request.getPagingRequest(), list);
    return new UserSearchResult(paging, request.getPagingRequest().select(list));
  }

  @Override
  public UserEventHistoryResult eventHistory(UserEventHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    List<HistoryEvent> history = super.eventHistory(request.getObjectId(), request.getUserName());
    return new UserEventHistoryResult(history);
  }

  //-------------------------------------------------------------------------
  @Override
  public UserAccount getAccount(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    ManageableUser user = getByName0(userName);
    SimpleUserAccount account = new SimpleUserAccount(user.getUserName());
    account.setPasswordHash(user.getPasswordHash());
    account.setAlternateIds(user.getAlternateIds());
    account.setEmailAddress(user.getEmailAddress());
    account.setProfile(user.getProfile());
    return roleMaster().resolveAccount(account);
  }

  @Override
  public RoleMaster roleMaster() {
    return _roleMaster;
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

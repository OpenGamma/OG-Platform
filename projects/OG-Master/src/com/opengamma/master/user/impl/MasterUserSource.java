/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.util.Collection;

import com.opengamma.core.user.OGUser;
import com.opengamma.core.user.UserSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MasterUserSource implements UserSource {
  
  private final UserMaster _userMaster;
  
  public MasterUserSource(UserMaster userMaster) {
    ArgumentChecker.notNull(userMaster, "userMaster");
    _userMaster = userMaster;
  }

  @Override
  public OGUser getUser(UniqueId uniqueId) {
    UserDocument userDoc = _userMaster.get(uniqueId);
    if (userDoc != null) {
      return userDoc.getObject();
    }
    return null;
  }

  @Override
  public OGUser getUser(ObjectId objectId, VersionCorrection versionCorrection) {
    UserDocument userDoc = _userMaster.get(objectId, versionCorrection);
    if (userDoc != null) {
      return userDoc.getObject();
    }
    return null;
  }

  @Override
  public Collection<? extends OGUser> getUsers(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    if (versionCorrection != VersionCorrection.LATEST) {
      throw new UnsupportedOperationException("Search with version correction has not yet been added.");
    }
    UserSearchRequest searchRequest = new UserSearchRequest();
    searchRequest.addExternalIdBundle(bundle);
    return _userMaster.search(searchRequest).getUsers();
  }

  @Override
  public Collection<? extends OGUser> getUsers(String userName, VersionCorrection versionCorrection) {
    if (versionCorrection != VersionCorrection.LATEST) {
      throw new UnsupportedOperationException("Search with version correction has not yet been added.");
    }
    UserSearchRequest searchRequest = new UserSearchRequest();
    searchRequest.setUserName(userName);
    return _userMaster.search(searchRequest).getUsers();
  }

}

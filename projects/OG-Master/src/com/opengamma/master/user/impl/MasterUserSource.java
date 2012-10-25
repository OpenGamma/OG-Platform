/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.util.Collection;
import java.util.List;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.user.OGUser;
import com.opengamma.core.user.UserSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;

/**
 * A {@code UserSource} implemented using an underlying {@code UserMaster}.
 * <p>
 * The {@link UserSource} interface provides exchanges to the application via a narrow API.
 * This class provides the source on top of a standard {@link UserMaster}.
 */
public class MasterUserSource extends AbstractMasterSource<OGUser, UserDocument, UserMaster> implements UserSource {

  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master  the master, not null
   */
  public MasterUserSource(final UserMaster master) {
    super(master);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   * 
   * @param master  the master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterUserSource(final UserMaster master, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<? extends OGUser> getUsers(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    UserSearchRequest searchRequest = new UserSearchRequest(bundle);
    searchRequest.setVersionCorrection(getVersionCorrection());
    return getMaster().search(searchRequest).getUsers();
  }

  @Override
  public OGUser getUser(String userId, VersionCorrection versionCorrection) {
    UserSearchRequest searchRequest = new UserSearchRequest();
    searchRequest.setUserId(userId);
    searchRequest.setVersionCorrection(versionCorrection);
    List<ManageableOGUser> result = getMaster().search(searchRequest).getUsers();
    if (result.size() == 1) {
      return result.get(0);
    }
    if (result.size() == 0) {
      throw new DataNotFoundException("User not found: " + userId);
    }
    throw new IllegalStateException("Multiple users found: " + userId);
  }

}

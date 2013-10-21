/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import java.net.URI;
import java.util.Collection;

import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.user.OGUser;
import com.opengamma.core.user.UserSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;

/**
 * Provides remote access to an {@link UserSource}.
 */
public class RemoteUserSource extends AbstractRemoteSource<OGUser> implements UserSource {

  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteUserSource(final URI baseUri) {
    this(baseUri, DummyChangeManager.INSTANCE);
  }

  public RemoteUserSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public OGUser get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    URI uri = DataUserSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(OGUser.class);
  }

  @Override
  public OGUser get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataUserSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(OGUser.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<OGUser> getUsers(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataUserSourceResource.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  @Override
  public OGUser getUser(String userId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(userId, "buserIdundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataUserSourceResource.uriSearchUserId(getBaseUri(), userId, versionCorrection);
    return accessRemote(uri).get(OGUser.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}

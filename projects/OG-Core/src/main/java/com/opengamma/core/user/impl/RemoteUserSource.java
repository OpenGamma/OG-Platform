/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import java.net.URI;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.UserSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to an {@link UserSource}.
 */
public class RemoteUserSource extends AbstractRemoteClient implements UserSource {

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
  public UserAccount getAccount(String userName) {
    ArgumentChecker.notNull(userName, "userName");

    URI uri = DataUserSourceResource.uriUserByName(getBaseUri(), userName);
    return accessRemote(uri).get(UserAccount.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.impl;

import java.net.URI;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Abstract base class for remote masters.
 * <p>
 * A remote master provides a client-side view of a remote master over REST.
 */
public abstract class AbstractRemoteMaster extends AbstractRemoteClient {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public AbstractRemoteMaster(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public AbstractRemoteMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the change manager in use.
   * 
   * @return the change manager, not null
   */
  public ChangeManager changeManager() {
    return _changeManager;
  }

}

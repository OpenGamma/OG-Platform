/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.net.URI;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to an {@link PositionSource}.
 */
public class RemotePositionSource extends AbstractRemoteClient implements PositionSource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemotePositionSource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemotePositionSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    URI uri = DataPositionSourceResource.uriGetPortfolio(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Portfolio.class);
  }

  @Override
  public Portfolio getPortfolio(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    URI uri = DataPositionSourceResource.uriGetPortfolio(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Portfolio.class);
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    URI uri = DataPositionSourceResource.uriGetNode(getBaseUri(), uniqueId);
    return accessRemote(uri).get(PortfolioNode.class);
  }

  @Override
  public Position getPosition(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataPositionSourceResource.uriGetPosition(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Position.class);
  }

  @Override
  public Position getPosition(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    URI uri = DataPositionSourceResource.uriGetPosition(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Position.class);
  }

  @Override
  public Trade getTrade(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataPositionSourceResource.uriGetTrade(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Trade.class);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}

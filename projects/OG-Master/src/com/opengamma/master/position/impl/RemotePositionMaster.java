/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.net.URI;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link PortfolioMaster}.
 */
public class RemotePositionMaster extends AbstractRemoteMaster implements PositionMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemotePositionMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemotePositionMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult search(final PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataPositionsResource.uri(getBaseUri(), msgBase64);
    return accessRemote(uri).get(PositionSearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataPositionResource.uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(PositionDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataPositionResource.uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(PositionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument add(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    
    URI uri = DataPositionsResource.uri(getBaseUri(), null);
    return accessRemote(uri).post(PositionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument update(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataPositionResource.uri(getBaseUri(), document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).put(PositionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataPositionResource.uri(getBaseUri(), uniqueId, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionHistoryResult history(final PositionHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataPositionResource.uriVersions(getBaseUri(), request.getObjectId(), msgBase64);
    return accessRemote(uri).get(PositionHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument correct(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataPositionResource.uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).get(PositionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    ArgumentChecker.notNull(tradeId, "tradeId");
    
    URI uri = DataPositionResource.uriTrade(getBaseUri(), tradeId);
    return accessRemote(uri).get(ManageableTrade.class);
  }

}

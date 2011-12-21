/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import java.net.URI;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link PortfolioMaster}.
 */
public class RemotePortfolioMaster extends AbstractRemoteMaster implements PortfolioMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemotePortfolioMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemotePortfolioMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioSearchResult search(final PortfolioSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataPortfoliosResource.uri(getBaseUri(), msgBase64);
    return accessRemote(uri).get(PortfolioSearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataPortfolioResource.uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(PortfolioDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataPortfolioResource.uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(PortfolioDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument add(final PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    
    URI uri = DataPortfoliosResource.uri(getBaseUri(), null);
    return accessRemote(uri).post(PortfolioDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument update(final PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataPortfolioResource.uri(getBaseUri(), document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).put(PortfolioDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataPortfolioResource.uri(getBaseUri(), uniqueId, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioHistoryResult history(final PortfolioHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataPortfolioResource.uriVersions(getBaseUri(), request.getObjectId(), msgBase64);
    return accessRemote(uri).get(PortfolioHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument correct(final PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataPortfolioResource.uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).put(PortfolioDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageablePortfolioNode getNode(final UniqueId nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    
    URI uri = DataPortfolioNodeResource.uri(getBaseUri(), nodeId);
    return accessRemote(uri).get(ManageablePortfolioNode.class);
  }

}

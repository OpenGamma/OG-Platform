/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.portfolio.rest;

import java.net.URI;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * Provides access to a remote {@link PortfolioMaster}.
 */
public class RemotePortfolioMaster implements PortfolioMaster {

  /**
   * The base URI to call.
   */
  private final URI _baseUri;
  /**
   * The client API.
   */
  private final FudgeRestClient _client;

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemotePortfolioMaster(final URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioSearchResult search(final PortfolioSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = DataPortfoliosResource.uri(_baseUri, msgBase64);
    return accessRemote(uri).get(PortfolioSearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument get(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataPortfolioResource.uriVersion(_baseUri, uniqueId);
      return accessRemote(uri).get(PortfolioDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataPortfolioResource.uri(_baseUri, objectId, versionCorrection);
    return accessRemote(uri).get(PortfolioDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument add(final PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    
    URI uri = DataPortfoliosResource.uri(_baseUri, null);
    return accessRemote(uri).post(PortfolioDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument update(final PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataPortfolioResource.uri(_baseUri, document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).put(PortfolioDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataPortfolioResource.uri(_baseUri, uniqueId, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioHistoryResult history(final PortfolioHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = DataPortfolioResource.uriVersions(_baseUri, request.getObjectId(), msgBase64);
    return accessRemote(uri).get(PortfolioHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument correct(final PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataPortfolioResource.uriVersion(_baseUri, document.getUniqueId());
    return accessRemote(uri).put(PortfolioDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageablePortfolioNode getNode(final UniqueIdentifier nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    
    URI uri = DataPortfolioResource.uriNode(_baseUri, nodeId);
    return accessRemote(uri).get(ManageablePortfolioNode.class);
  }

  //-------------------------------------------------------------------------
  /**
   * Accesses the remote master.
   * 
   * @param uri  the URI to call, not null
   * @return the resource, suitable for calling get/post/put/delete on, not null
   */
  protected Builder accessRemote(URI uri) {
    // TODO: Better solution to this limitation in JAX-RS (we shouldn't have "data" in URI)
    // this code removes a second duplicate "data"
    String uriStr = uri.toString();
    int pos = uriStr.indexOf("/jax/data/");
    if (pos > 0) {
      pos = uriStr.indexOf("/data/", pos + 10);
      if (pos > 0) {
        uriStr = uriStr.substring(0, pos) + uriStr.substring(pos + 5);
      }
    }
    uri = URI.create(uriStr);
    return _client.access(uri).type(FudgeRest.MEDIA_TYPE).accept(FudgeRest.MEDIA_TYPE);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this master.
   * 
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _baseUri + "]";
  }

}

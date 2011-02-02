/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.net.URI;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * Provides access to a remote {@link PortfolioMaster}.
 */
public class RemotePositionMaster implements PositionMaster {

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
  public RemotePositionMaster(final URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult search(final PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = DataPositionsResource.uri(_baseUri, msgBase64);
    return accessRemote(uri).get(PositionSearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument get(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataPositionResource.uriVersion(_baseUri, uniqueId);
      return accessRemote(uri).get(PositionDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataPositionResource.uri(_baseUri, objectId, versionCorrection);
    return accessRemote(uri).get(PositionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument add(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    
    URI uri = DataPositionsResource.uri(_baseUri, null);
    return accessRemote(uri).post(PositionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument update(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataPositionResource.uri(_baseUri, document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).put(PositionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataPositionResource.uri(_baseUri, uniqueId, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionHistoryResult history(final PositionHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = DataPositionResource.uriVersions(_baseUri, request.getObjectId(), msgBase64);
    return accessRemote(uri).get(PositionHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument correct(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataPositionResource.uriVersion(_baseUri, document.getUniqueId());
    return accessRemote(uri).get(PositionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableTrade getTrade(final UniqueIdentifier tradeId) {
    ArgumentChecker.notNull(tradeId, "tradeId");
    
    URI uri = DataPositionResource.uriTrade(_baseUri, tradeId);
    return accessRemote(uri).get(ManageableTrade.class);
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

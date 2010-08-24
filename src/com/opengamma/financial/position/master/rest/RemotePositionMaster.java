/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.FullPortfolioGetRequest;
import com.opengamma.financial.position.master.FullPortfolioNodeGetRequest;
import com.opengamma.financial.position.master.FullPositionGetRequest;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricResult;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionSearchHistoricRequest;
import com.opengamma.financial.position.master.PositionSearchHistoricResult;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * Provides access to a remote {@link PositionMaster}.
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
   * Creates and instance.
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemotePositionMaster(final URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeSearchResult searchPortfolioTrees(final PortfolioTreeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPortfolioTreesResource.class).queryParam("msg", msgBase64).build();
    return accessRemote(uri).get(PortfolioTreeSearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument getPortfolioTree(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPortfolioTreeResource.class).build(uid.toLatest());
    return accessRemote(uri).get(PortfolioTreeDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument addPortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPortfolioTreesResource.class).build();
    return accessRemote(uri).post(PortfolioTreeDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument updatePortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolioId(), "document.portfolioId");
    
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPortfolioTreeResource.class).build(document.getPortfolioId().toLatest());
    return accessRemote(uri).put(PortfolioTreeDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void removePortfolioTree(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPortfolioTreeResource.class).build(uid.toLatest());
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeSearchHistoricResult searchPortfolioTreeHistoric(final PortfolioTreeSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioId(), "request.portfolioId");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPortfolioTreeResource.class).queryParam("msg", msgBase64).build(request.getPortfolioId().toLatest());
    return accessRemote(uri).get(PortfolioTreeSearchHistoricResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument correctPortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolioId(), "document.portfolioId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult searchPositions(final PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPositionsResource.class).queryParam("msg", msgBase64).build();
    return accessRemote(uri).get(PositionSearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument getPosition(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPositionResource.class).build(uid.toLatest());
    return accessRemote(uri).get(PositionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument addPosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getParentNodeId(), "document.parentNodeId");
    
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPositionsResource.class).build();
    return accessRemote(uri).post(PositionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument updatePosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getPositionId(), "document.positionId");
    
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPositionResource.class).build(document.getPositionId().toLatest());
    return accessRemote(uri).put(PositionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void removePosition(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPositionResource.class).build(uid.toLatest());
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchHistoricResult searchPositionHistoric(final PositionSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPositionId(), "request.positionId");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = UriBuilder.fromUri(_baseUri).path(DataPositionResource.class).queryParam("msg", msgBase64).build(request.getPositionId().toLatest());
    return accessRemote(uri).get(PositionSearchHistoricResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument correctPosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getPositionId(), "document.positionId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getFullPortfolio(final FullPortfolioGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioId(), "document.portfolioId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioNode getFullPortfolioNode(final FullPortfolioNodeGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioNodeId(), "document.portfolioNodeId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public Position getFullPosition(final FullPositionGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPositionId(), "document.positionId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * Accesses the remote position master.
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
   * Returns a string summary of this position master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _baseUri + "]";
  }

}

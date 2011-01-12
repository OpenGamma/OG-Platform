/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.rest;

import java.net.URI;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * Provides access to a remote {@link PositionSource}.
 */
public class RemotePositionSource implements PositionSource {

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
  public RemotePositionSource(final URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    URI uri = DataPositionSourceResource.uriPortfolio(_baseUri, uid);
    return accessRemote(uri).get(Portfolio.class);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    URI uri = DataPositionSourceResource.uriNode(_baseUri, uid);
    return accessRemote(uri).get(PortfolioNode.class);
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    URI uri = DataPositionSourceResource.uriPosition(_baseUri, uid);
    return accessRemote(uri).get(Position.class);
  }

  @Override
  public Trade getTrade(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    URI uri = DataPositionSourceResource.uriTrade(_baseUri, uid);
    return accessRemote(uri).get(Trade.class);
  }

  //-------------------------------------------------------------------------
  /**
   * Accesses the remote object.
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
   * Returns a string summary of this object.
   * 
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _baseUri + "]";
  }

}

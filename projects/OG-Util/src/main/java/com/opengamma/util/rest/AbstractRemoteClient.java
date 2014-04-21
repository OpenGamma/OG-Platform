/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.net.URI;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.sun.jersey.api.client.UniformInterface;

/**
 * Abstract base class for remote clients that communicate over REST.
 */
public abstract class AbstractRemoteClient {

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
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public AbstractRemoteClient(final URI baseUri) {
    this(baseUri, FudgeRestClient.create());
  }

  public AbstractRemoteClient(final URI baseUri, final FudgeRestClient client) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(client, "client");
    _baseUri = baseUri;
    _client = client;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * 
   * @return the base URI, not null
   */
  public URI getBaseUri() {
    return _baseUri;
  }

  /**
   * Gets the RESTful client.
   * 
   * @return the client, not null
   */
  public FudgeRestClient getRestClient() {
    return _client;
  }

  /**
   * Gets the Fudge context.
   * 
   * @return the Fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return OpenGammaFudgeContext.getInstance();
  }

  //-------------------------------------------------------------------------
  /**
   * Accesses the remote master.
   * 
   * @param uri the URI to call, not null
   * @return the resource, suitable for calling get/post/put/delete on, not null
   */
  protected UniformInterface accessRemote(URI uri) {
    return getRestClient().accessFudge(uri);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this client.
   * 
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getBaseUri() + "]";
  }

}

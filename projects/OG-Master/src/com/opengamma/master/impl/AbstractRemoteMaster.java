/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.impl;

import java.net.URI;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * Abstract base class for remote masters.
 * <p>
 * A remote master provides a client-side view of a remote master over REST.
 */
public abstract class AbstractRemoteMaster {

  /**
   * The base URI to call.
   */
  private final URI _baseUri;
  /**
   * The client API.
   */
  private final FudgeRestClient _client;
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
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
    _changeManager = changeManager;
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

  //-------------------------------------------------------------------------
  /**
   * Gets the change manager in use.
   * 
   * @return the change manager, not null
   */
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Accesses the remote master.
   * 
   * @param uri  the URI to call, not null
   * @return the resource, suitable for calling get/post/put/delete on, not null
   */
  protected Builder accessRemote(URI uri) {
    // manipulates the URI to remove any duplicate "data"
    // this may no longer be necessary
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

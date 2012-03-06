/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import java.net.URI;

import com.opengamma.component.ComponentServer;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Remote client for accessing remote components.
 */
public class RemoteComponentServer extends AbstractRemoteClient {

  /**
   * Creates the resource.
   * 
   * @param baseUri  the base URI, not null
   */
  public RemoteComponentServer(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the remote components at the URI.
   * <p>
   * Any relative URIs in the response from the server will be converted into absolute URIs.
   * 
   * @return the list of remote components
   */
  public ComponentServer getComponentServer() {
    URI uri = DataComponentServerResource.uri(getBaseUri());
    ComponentServer server = accessRemote(uri).get(ComponentServer.class);
    server.applyBaseUri(uri);
    return server;
  }

}

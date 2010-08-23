/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.net.URI;

import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Fudge-based client to call remote RESTful services.
 * <p>
 * This has configuration to rethrow exceptions sent across the network.
 */
public class FudgeRestClient {

  /**
   * The client.
   */
  private final Client _client;

  /**
   * Creates an instance.
   * @param underlyingClient  the 
   */
  protected FudgeRestClient(final Client underlyingClient) {
    _client = underlyingClient;
  }

  /**
   * Creates an instance, initializing the providers
   * @return the RESTful client, not null
   */
  public static FudgeRestClient create() {
    ClientConfig config = new DefaultClientConfig();
    config.getClasses().add(FudgeObjectBinaryConsumer.class);
    config.getClasses().add(FudgeObjectBinaryProducer.class);
    Client client = Client.create(config);
    client.addFilter(new ExceptionThrowingClientFilter());
    return new FudgeRestClient(client);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the RESTful client.
   * @return the client, not null
   */
  protected Client getClient() {
    return _client;
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a class that can be used to call a remote resource synchronously.
   *
   * @param uri  the URI of the resource, not null
   * @return a class that can be used to call a remote resource, not null
   */
  public WebResource access(final URI uri) {
    return getClient().resource(uri);
  }

  /**
   * Obtains a class that can be used to call a remote resource asynchronously.
   *
   * @param uri  the URI of the resource, not null
   * @return a class that can be used to call a remote resource, not null
   */
  public AsyncWebResource accessAsync(final URI uri) {
    return getClient().asyncResource(uri);
  }

}

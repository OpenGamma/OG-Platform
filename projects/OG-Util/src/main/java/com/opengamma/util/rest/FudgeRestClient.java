/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.net.URI;

import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
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
   * Gets the underlying Jersey RESTful client.
   * @return the client, not null
   */
  public Client getClient() {
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

  /**
   * Obtains a class that can be used to call a remote resource synchronously.
   * <p>
   * This sets the entity type and accepted type to be Fudge.
   *
   * @param uri  the URI of the resource, not null
   * @return a class that can be used to call a remote resource, not null
   */
  public Builder accessFudge(final URI uri) {
    return getClient().resource(uri).type(FudgeRest.MEDIA_TYPE).accept(FudgeRest.MEDIA_TYPE);
  }

}

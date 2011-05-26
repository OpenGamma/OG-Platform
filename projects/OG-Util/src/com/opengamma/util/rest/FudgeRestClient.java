/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.joda.beans.Bean;

import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.opengamma.transport.jaxrs.FudgeRest;
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

  //-------------------------------------------------------------------------
  /**
   * Encodes a bean in base-64 suitable for passing in the URI.
   * <p>
   * This is used to pass a bean in the URI, such as when calling a GET method.
   * 
   * @param bean  the bean to encode, not null
   * @return the encoded version of the bean, not null
   */
  public String encodeBean(final Bean bean) {
    return encode(bean);
  }
  
  /**
   * Encodes a Fudge-serializable object in base-64 suitable for passing in the URI.
   * 
   * @param object  the object to encode, not null
   * @return  the encoded version of the object, not null
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public String encode(final Object object) {
    Class cls = object.getClass();
    Providers providers = getClient().getProviders();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    MessageBodyWriter mbw = providers.getMessageBodyWriter(cls, cls, null, FudgeRest.MEDIA_TYPE);
    try {
      mbw.writeTo(object, cls, cls, null, FudgeRest.MEDIA_TYPE, null, out);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return Base64.encodeBase64URLSafeString(out.toByteArray());
  }

}

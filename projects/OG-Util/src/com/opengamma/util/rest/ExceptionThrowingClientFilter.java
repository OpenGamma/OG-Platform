/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * A Jersey filter to convert returned status codes to exceptions.
 */
public class ExceptionThrowingClientFilter extends ClientFilter {

  /**
   * Header key for exception type.
   */
  public static final String EXCEPTION_TYPE = "X-OpenGamma-ExceptionType";
  /**
   * Header key for exception message.
   */
  public static final String EXCEPTION_MESSAGE = "X-OpenGamma-ExceptionMessage";

  /**
   * Creates the filter.
   * Always create a new instance, never cache.
   */
  public ExceptionThrowingClientFilter() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
    ClientResponse response = getNext().handle(cr);
    if (response.getStatus() < 300) {
      return response;  // normal valid response
    }
    MultivaluedMap<String, String> headers = response.getHeaders();
    String exType = headers.getFirst(EXCEPTION_TYPE);
    String exMsg = headers.getFirst(EXCEPTION_MESSAGE);
    if (exType == null) {
      return response;  // fall through to UniformInterfaceException
    }
    RuntimeException exception;
    try {
      Class<? extends RuntimeException> cls = Thread.currentThread().getContextClassLoader().loadClass(exType).asSubclass(RuntimeException.class);
      if (exMsg == null) {
        exception = cls.newInstance();
      } else {
        exception = cls.getConstructor(String.class).newInstance(exMsg);
      }
    } catch (Exception ex) {
      return response;  // fall through to UniformInterfaceException
    }
    throw exception;  // transparently throw exception as seen on the server
  }

}

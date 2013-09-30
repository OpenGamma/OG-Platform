/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ClassUtils;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
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
   * Header key for exception point.
   */
  public static final String EXCEPTION_POINT = "X-OpenGamma-ExceptionPoint";

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
    if (exMsg == null) {
      exMsg = headers.getFirst(EXCEPTION_POINT);
    }
    UniformInterfaceException uiex;
    if (response.getStatus() == 404) {
      uiex = new UniformInterfaceException404NotFound(response, true);
    } else if (response.getStatus() == 204) {
      uiex = new UniformInterfaceException204NoContent(response, true);
    } else {
      uiex = new UniformInterfaceException(response, true);
    }
    if (exType == null) {
      throw uiex;  // standard UniformInterfaceException as we have nothing to add
    }
    RuntimeException exception;
    try {
      Class<? extends RuntimeException> cls = ClassUtils.loadClass(exType).asSubclass(RuntimeException.class);
      exception = cls.getConstructor(String.class).newInstance("Server threw exception: " + StringUtils.defaultString(exMsg));
    } catch (Exception ex) {
      // unable to create transparently, so use standard exception
      exception = new OpenGammaRuntimeException("Server threw exception: " + exType + ": " + StringUtils.defaultString(exMsg));
    }
    exception.initCause(uiex);
    throw exception;  // transparently throw exception as seen on the server
  }

}

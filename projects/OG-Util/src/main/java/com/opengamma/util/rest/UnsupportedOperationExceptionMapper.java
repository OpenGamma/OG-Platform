/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

/**
 * A JAX-RS exception mapper to convert {@code IllegalArgumentException} to a RESTful 503.
 */
@Provider
public class UnsupportedOperationExceptionMapper
    extends AbstractSpecificExceptionMapper<UnsupportedOperationException> {

  /**
   * Creates the mapper.
   */
  public UnsupportedOperationExceptionMapper() {
    super(Status.SERVICE_UNAVAILABLE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String buildHtmlErrorPage(UnsupportedOperationException exception) {
    Map<String, String> data = new HashMap<>();
    buildOutputMessage(exception, data);
    return createHtmlErrorPage("error-unavailable.html", data);
  }

  @Override
  protected void logHtmlException(UnsupportedOperationException exception, String htmlPage) {
    s_logger.error("RESTful website exception caught", exception);
  }

  @Override
  protected void logRestfulError(UnsupportedOperationException exception) {
    s_logger.error("RESTful web-service exception caught and tunnelled to client:", exception);
  }

}

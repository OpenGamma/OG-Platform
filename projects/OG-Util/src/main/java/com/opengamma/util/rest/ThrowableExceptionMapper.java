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
 * A JAX-RS exception mapper to convert {@code Throwable} to a RESTful 500.
 */
@Provider
public class ThrowableExceptionMapper
    extends AbstractSpecificExceptionMapper<Throwable> {

  /**
   * Creates the mapper.
   */
  public ThrowableExceptionMapper() {
    super(Status.INTERNAL_SERVER_ERROR);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String buildHtmlErrorPage(Throwable exception) {
    Map<String, String> data = new HashMap<>();
    buildOutputMessage(exception, data);
    return createHtmlErrorPage("error-servererror.html", data);
  }

  @Override
  protected void logHtmlException(Throwable exception, String htmlPage) {
    s_logger.error("RESTful website exception caught", exception);
  }

  @Override
  protected void logRestfulError(Throwable exception) {
    s_logger.error("RESTful web-service exception caught and tunnelled to client:", exception);
  }

}

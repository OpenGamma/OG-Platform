/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * A JAX-RS exception mapper that ensures that the internal {@code WebApplicationException}
 * is not trapped by our mappers.
 */
@Provider
public class WebApplicationExceptionMapper
    extends AbstractExceptionMapper<WebApplicationException> {

  /**
   * Creates the mapper.
   */
  public WebApplicationExceptionMapper() {
  }

  //-------------------------------------------------------------------------
  @Override
  protected String buildHtmlErrorPage(WebApplicationException exception) {
    Map<String, String> data = new HashMap<>();
    int status = exception.getResponse().getStatus();
    switch (status) {
      case 400:  // bad request
        buildOutputMessage(exception, data);
        return createHtmlErrorPage("error-badrequest.html", data);
      case 404:  // not found
        return createHtmlErrorPage("error-notfound.html", data);
      case 503:  // service unavailable
        buildOutputMessage(exception, data);
        return createHtmlErrorPage("error-unavailable.html", data);
      default:
        buildOutputMessage(exception, data);
        return createHtmlErrorPage("error-servererror.html", data);
    }
  }

  @Override
  protected Response doHtmlResponse(WebApplicationException exception, String htmlPage) {
    return Response.status(exception.getResponse().getStatus()).entity(htmlPage).build();
  }

  @Override
  protected Response doRestfulResponse(WebApplicationException exception) {
    return exception.getResponse();
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * A JAX-RS exception mapper that ensures that the internal {@code WebApplicationException}
 * is not trapped by our mappers.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

  /**
   * Creates the mapper.
   */
  public WebApplicationExceptionMapper() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Response toResponse(final WebApplicationException exception) {
    return exception.getResponse();
  }

}

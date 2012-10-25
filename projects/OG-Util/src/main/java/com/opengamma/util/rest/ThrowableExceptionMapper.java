/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * A JAX-RS exception mapper to convert {@code Throwable} to a RESTful 500.
 */
@Provider
public class ThrowableExceptionMapper
    extends AbstractExceptionMapper
    implements ExceptionMapper<Throwable> {

  /**
   * Creates the mapper.
   */
  public ThrowableExceptionMapper() {
    super(Status.INTERNAL_SERVER_ERROR);
  }

  //-------------------------------------------------------------------------
  @Override
  public Response toResponse(final Throwable exception) {
    return createResponse(exception);
  }

}

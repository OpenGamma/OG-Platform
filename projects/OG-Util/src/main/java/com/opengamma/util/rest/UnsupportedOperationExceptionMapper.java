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
 * A JAX-RS exception mapper to convert {@code IllegalArgumentException} to a RESTful 503.
 */
@Provider
public class UnsupportedOperationExceptionMapper
    extends AbstractExceptionMapper
    implements ExceptionMapper<UnsupportedOperationException> {

  /**
   * Creates the mapper.
   */
  public UnsupportedOperationExceptionMapper() {
    super(Status.SERVICE_UNAVAILABLE);
  }

  //-------------------------------------------------------------------------
  @Override
  public Response toResponse(final UnsupportedOperationException exception) {
    return createResponse(exception);
  }

}

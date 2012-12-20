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

import com.opengamma.DataNotFoundException;

/**
 * A JAX-RS exception mapper to convert {@code DataNotFoundException} to a RESTful 404.
 */
@Provider
public class DataNotFoundExceptionMapper
    extends AbstractExceptionMapper
    implements ExceptionMapper<DataNotFoundException> {

  /**
   * Creates the mapper.
   */
  public DataNotFoundExceptionMapper() {
    super(Status.NOT_FOUND);
  }

  //-------------------------------------------------------------------------
  @Override
  public Response toResponse(final DataNotFoundException exception) {
    return createResponse(exception);
  }

  protected void logRestfulError(final Throwable exception) {
    s_logger.debug("RESTful web-service exception caught and tunnelled to client", exception);
  }

}

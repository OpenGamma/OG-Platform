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

import com.opengamma.DataVersionException;

/**
 * A JAX-RS exception mapper to convert {@code DataVersionException} to a RESTful 409.
 */
@Provider
public class DataVersionExceptionMapper
    extends AbstractExceptionMapper
    implements ExceptionMapper<DataVersionException> {

  /**
   * Creates the mapper.
   */
  public DataVersionExceptionMapper() {
    super(Status.CONFLICT);
  }

  //-------------------------------------------------------------------------
  @Override
  public Response toResponse(final DataVersionException exception) {
    return createResponse(exception);
  }

}

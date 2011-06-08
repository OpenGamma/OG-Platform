/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class to assist with writing JAX-RS exception mappers.
 */
public class AbstractExceptionMapper {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractExceptionMapper.class);

  /**
   * The status to use.
   */
  private final Status _status;
  /**
   * The RESTful request headers.
   */
  @Context
  private HttpHeaders _headers;

  /**
   * Creates the mapper.
   * @param status  the status to use, not null
   */
  protected AbstractExceptionMapper(final Status status) {
    _status = status;
  }

  //-------------------------------------------------------------------------
  public Response createResponse(final Throwable exception) {
    if (_headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML)) {
      s_logger.error("RESTful website exception caught", exception);
      // output error page
      // TODO: error page
      return Response.status(_status).build();
    } else {
      s_logger.error("RESTful web-service exception caught and tunnelled to client", exception);
      // perform transparent exception tunneling for Fudge messages
      return Response.status(_status)
        .header(ExceptionThrowingClientFilter.EXCEPTION_TYPE, exception.getClass().getName())
        .header(ExceptionThrowingClientFilter.EXCEPTION_MESSAGE, exception.getMessage())
        .type(MediaType.TEXT_PLAIN_TYPE)
        .entity("Status: " + _status.getStatusCode() + " " + _status.getReasonPhrase() + "; Message: " + exception.getMessage())
        .build();
    }
  }

}

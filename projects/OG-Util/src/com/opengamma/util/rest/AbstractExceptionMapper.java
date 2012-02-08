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
  protected static final Logger s_logger = LoggerFactory.getLogger(AbstractExceptionMapper.class);

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
      logRestfulError(exception);
      // perform transparent exception tunneling for Fudge messages
      return Response.status(_status)
        .header(ExceptionThrowingClientFilter.EXCEPTION_TYPE, exception.getClass().getName())
        .header(ExceptionThrowingClientFilter.EXCEPTION_MESSAGE, exception.getMessage())
        .header(ExceptionThrowingClientFilter.EXCEPTION_POINT, packStackTrace(exception))
        .type(MediaType.TEXT_PLAIN_TYPE)
        .entity("Status: " + _status.getStatusCode() + " " + _status.getReasonPhrase() + "; Message: " + exception.getMessage())
        .build();
    }
  }

  private String packStackTrace(Throwable exception) {
    StackTraceElement[] stackTrace = exception.getStackTrace();
    switch (stackTrace.length) {
      case 0:
        return "Unknown";
      case 1:
        return stackTrace[0].toString();
      case 2:
        return stackTrace[0].toString() + " \n" + stackTrace[1].toString();
      default:
        return stackTrace[0].toString() + " \n" + stackTrace[1].toString() + " \n" + stackTrace[2].toString();
    }
  }

  /**
   * Logs the error in the RESTful scenario.
   * Override to block or lower the logging level from warn.
   * 
   * @param exception  the exception, not null
   */
  protected void logRestfulError(final Throwable exception) {
    s_logger.warn("RESTful web-service exception caught and tunnelled to client", exception);
  }

}

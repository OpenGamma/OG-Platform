/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class to assist with writing JAX-RS exception mappers.
 * 
 * @param <T> the mapped exception type
 */
public abstract class AbstractSpecificExceptionMapper<T extends Throwable>
    extends AbstractExceptionMapper<T> {

  /** Logger. */
  protected static final Logger s_logger = LoggerFactory.getLogger(AbstractSpecificExceptionMapper.class);

  /**
   * The status to use.
   */
  private final Status _status;

  /**
   * Creates the mapper.
   * @param status  the status to use, not null
   */
  protected AbstractSpecificExceptionMapper(final Status status) {
    _status = status;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Response doHtmlResponse(T exception, String htmlPage) {
    return Response.status(_status).entity(htmlPage).build();
  }

  @Override
  protected Response doRestfulResponse(T exception) {
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

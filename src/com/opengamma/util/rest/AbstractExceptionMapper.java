/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.opengamma.transport.jaxrs.FudgeRest;

/**
 * An abstract class to assist with writing JAX-RS exception mappers.
 */
public class AbstractExceptionMapper {

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
    if (_headers.getAcceptableMediaTypes().contains(FudgeRest.MEDIA_TYPE)) {
      // perform transparent exception tunneling for Fudge messages
      return Response.status(_status)
        .header("ExceptionType", exception.getClass().getName())
        .header("ExceptionMessage", exception.getMessage())
        .type(MediaType.TEXT_PLAIN_TYPE)
        .entity("Status: " + _status.getStatusCode() + " " + _status.getReasonPhrase() + "; Message: " + exception.getMessage())
        .build();
    } else {
      // output error page
      // TODO: error page
      return Response.status(_status).build();
    }
  }

}

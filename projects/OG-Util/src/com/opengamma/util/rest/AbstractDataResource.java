/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.net.URI;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.joda.beans.Bean;

import com.opengamma.transport.jaxrs.FudgeResponse;

/**
 * Abstract base class for RESTful resources.
 */
public abstract class AbstractDataResource {

  /**
   * Creates the empty RESTful "ok" response object - 200.
   * <p>
   * This is normally used as a response to a ping.
   * 
   * @return the response, not null
   */
  protected Response responseOk() {
    return Response.ok().build();
  }

  /**
   * Creates the empty RESTful "no-content" response object - 204.
   * <p>
   * This is the correct form of response if there is no entity.
   * 
   * @return the response, not null
   */
  protected Response responseOkNoContent() {
    return Response.noContent().build();
  }

  /**
   * Creates the RESTful "created" response object.
   * 
   * @param uri  the URI that was created, may be null if value is null
   * @return the response, not null
   */
  protected Response responseCreated(final URI uri) {
    return Response.created(uri).build();
  }

  /**
   * Creates the RESTful "ok" response object, converting null to a 404.
   * <p>
   * The response will only go via Fudge if the value if a Fudge recognized type.
   * 
   * @param value  the value to contain in the response, or null to trigger a 404
   * @return the response, not null
   */
  protected Response responseOk(final Object value) {
    responseNullTo404(value);
    return Response.ok(value).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the RESTful "ok" response object using Fudge, converting null to a 404.
   * <p>
   * The response will be converted to XML or JSON formatted Fudge on demand.
   * 
   * @param value  the value to contain in the response, or null to trigger a 404
   * @return the response, not null
   */
  protected Response responseOkFudge(final Object value) {
    responseNullTo404(value);
    return Response.ok(encode(value)).build();
  }

  /**
   * Creates the RESTful "created" response object using Fudge, converting null to a 404.
   * <p>
   * The response will be converted to XML or JSON formatted Fudge on demand.
   * 
   * @param uri  the URI that was created, may be null if value is null
   * @param value  the value to contain in the response, or null to trigger a 404
   * @return the response, not null
   */
  protected Response responseCreatedFudge(final URI uri, final Object value) {
    responseNullTo404(value);
    return Response.created(uri).entity(encode(value)).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the value is null and throws a 404 exception.
   * 
   * @param value  the value to check
   * @throws WebApplicationException if the value is null
   */
  protected void responseNullTo404(final Object value) {
    if (value == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  private Object encode(Object value) {
    if (value instanceof FudgeMsgEnvelope || value instanceof FudgeMsg || value instanceof Bean) {
      return value;
    }
    return new FudgeResponse(value);
  }

}

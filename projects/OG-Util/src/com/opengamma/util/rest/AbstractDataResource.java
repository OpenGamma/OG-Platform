/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.net.URI;

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

  //-------------------------------------------------------------------------
  /**
   * Creates the RESTful "ok" response object, converting null to a 404.
   * <p>
   * The response will only go via Fudge if the value if a Fudge recognized type.
   * 
   * @param value  the value to contain in the response, or null to trigger a 404
   * @return the response, not null
   */
  protected Response responseOk(final Object value) {
    if (value != null) {
      return Response.ok(value).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
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
    if (value != null) {
      return Response.ok(encode(value)).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  //-------------------------------------------------------------------------
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
    if (value != null) {
      return Response.created(uri).entity(encode(value)).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  //-------------------------------------------------------------------------
  private Object encode(Object value) {
    if (value instanceof FudgeMsgEnvelope || value instanceof FudgeMsg || value instanceof Bean) {
      return value;
    }
    return new FudgeResponse(value);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.Response;

/**
 * Abstract base class for RESTful resources.
 */
public abstract class AbstractDataResource {

  /**
   * Creates an "ok" response containing the object, or a 404 if given null.
   * 
   * @param value the value to contain in the response, or null to trigger a 404
   * @return the response
   */
  protected Response response(final Object value) {
    if (value != null) {
      return Response.ok(value).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

}

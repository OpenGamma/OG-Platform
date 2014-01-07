/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * REST resource for the user sessions. This resource class specifies the endpoints for user requests.
 */
@Path("user")
public class UserResource {

  @Path("logout")
  @GET
  public Response get(@Context HttpServletRequest hsr) {
    hsr.getSession().invalidate();
    return Response.status(Response.Status.OK).build();
  }

}

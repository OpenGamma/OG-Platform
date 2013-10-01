/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * REST resource for the user sessions. This resource class specifies the endpoints for user requests.
 */
@Path("user")
public class UserResource {

  @Path("logout")
  @PUT
  public Response userLogout() {
    return Response.status(Response.Status.OK).build();
  }

  @Path("login")
  @PUT
  public Response userLogin() {
    return Response.status(Response.Status.OK).build();
  }

}

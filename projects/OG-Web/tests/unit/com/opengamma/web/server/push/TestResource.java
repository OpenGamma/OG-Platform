/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.web.server.push.Subscribe;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 */
@Path("/test")
public class TestResource {

  @GET
  @Produces("text/plain")
  @Path("{uid}")
  //@SubscribeMaster(MasterType.HOLIDAY) // TODO another test method for this
  public String getMessage(@Subscribe @PathParam("uid") String uidStr) {
    return "Hello " + uidStr;
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.test;

import com.opengamma.web.server.push.web.MasterType;
import com.opengamma.web.server.push.web.Subscribe;
import com.opengamma.web.server.push.web.SubscribeMaster;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 *
 */
@Path("/test")
public class TestResource {

  @GET
  @Produces("text/plain")
  @Path("{name}")
  @SubscribeMaster(MasterType.HOLIDAY)
  public String getMessage(@Subscribe @PathParam("name") String name, @QueryParam("clientId") String clientId) {
    return "Hello " + name + ", clientId=" + clientId;
  }
}

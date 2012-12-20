/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;

/**
 *
 */
@Path("test")
public class TestResource {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("{uid}")
  public String getMessage(@Subscribe @PathParam("uid") String uidStr) {
    return "Hello " + uidStr;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("positions")
  @SubscribeMaster(MasterType.POSITION)
  public String getPositions() {
    return "Some positions";
  }
}

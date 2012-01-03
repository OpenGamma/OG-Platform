/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.web.server.push.rest.Subscribe;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 *
 */
@Path("/testsub")
public class TestSubResources {

  @Path("{uid}")
  public TestSubResource getSubResource(@Subscribe @PathParam("uid") String uid) {
    return new TestSubResource(uid);
  }
}

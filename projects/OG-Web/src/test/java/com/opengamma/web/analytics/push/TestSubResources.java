/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.opengamma.web.analytics.rest.Subscribe;

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

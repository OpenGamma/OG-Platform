/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * TODO this is an awful name
 */
public interface DependencyGraphOwnerResource {

  @POST
  @Path("depgraphs")
  public abstract Response openDependencyGraph(@Context UriInfo uriInfo,
                                               @FormParam("row") int row,
                                               @FormParam("col") int col);

  @Path("depgraphs/{graphId}")
  public AbstractGridResource getDependencyGraph(@PathParam("graphId") String graphId);
}

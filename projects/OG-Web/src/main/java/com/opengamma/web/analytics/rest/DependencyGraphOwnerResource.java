/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * TODO this is an awful name
 * @deprecated in favour of {@link WebUiResource}
 */
@Deprecated
public interface DependencyGraphOwnerResource {

  @POST
  @Path("depgraphs")
  Response openDependencyGraph(@Context UriInfo uriInfo,
                                               @FormParam("requestId") int requestId,
                                               @FormParam("row") int row,
                                               @FormParam("col") int col);

  @Path("depgraphs/{graphId}")
  AbstractGridResource getDependencyGraph(@PathParam("graphId") int graphId);

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Providers;

import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for {@link DependencyGraphExplorer}.
 */
public class DataDependencyGraphExplorerResource extends AbstractDataResource {

  // CSOFF: just constants
  public static final String PATH_WHOLE_GRAPH = "wholeGraph";
  public static final String PATH_SUBGRAPH_PRODUCING = "subgraphProducing";
  // CSON: just constants
  
  private final DependencyGraphExplorer _explorer;
  
  public DataDependencyGraphExplorerResource(DependencyGraphExplorer explorer) {
    ArgumentChecker.notNull(explorer, "explorer");
    _explorer = explorer;
  }
  
  @Path(PATH_WHOLE_GRAPH)
  @GET
  public Response getWholeGraph() {
    return Response.ok(_explorer.getWholeGraph()).build();
  }
  
  @Path(PATH_SUBGRAPH_PRODUCING)
  @GET
  public Response getSubgraphProducing(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    ValueSpecification output = decode(ValueSpecification.class, providers, msgBase64);
    return Response.ok(_explorer.getSubgraphProducing(output)).build();
  }
  
  //-------------------------------------------------------------------------
  public static URI uriSubgraph(URI baseUri, String outputMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path(PATH_SUBGRAPH_PRODUCING);
    if (outputMsg != null) {
      bld.queryParam("msg", outputMsg);
    }
    return bld.build();
  }

  public static URI uriWholeGraph(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path(DataDependencyGraphExplorerResource.PATH_WHOLE_GRAPH);
    URI uriWholeGraph = bld.build();
    return uriWholeGraph;
  }
  
}

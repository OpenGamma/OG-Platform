/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import java.net.URI;

import javax.time.Instant;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.time.DateUtils;

/**
 * RESTful resource for function costs.
 * <p>
 * This resource receives and processes RESTful calls to the function costs master.
 */
@Path("/fncMaster")
public class DataFunctionCostsMasterResource extends AbstractDataResource {

  /**
   * The function costs master.
   */
  private final FunctionCostsMaster _functionCostsMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param functionCostsMaster  the underlying master, not null
   */
  public DataFunctionCostsMasterResource(final FunctionCostsMaster functionCostsMaster) {
    ArgumentChecker.notNull(functionCostsMaster, "functionCostsMaster");
    _functionCostsMaster = functionCostsMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the master.
   * 
   * @return the master, not null
   */
  public FunctionCostsMaster getFunctionCostsMaster() {
    return _functionCostsMaster;
  }

  //-------------------------------------------------------------------------
  @HEAD
  @Path("functioncosts")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return Response.ok().build();
  }

  @GET
  @Path("functioncosts")
  public Response search(@QueryParam("configurationName") String configurationName, @QueryParam("functionId") String functionId, @QueryParam("versionAsOf") String versionAsOfStr) {
    Instant versionAsOf = (versionAsOfStr != null ? DateUtils.parseInstant(versionAsOfStr) : null);
    FunctionCostsDocument result = getFunctionCostsMaster().load(configurationName, functionId, versionAsOf);
    return Response.ok(result).build();
  }

  @POST
  @Path("functioncosts")
  public Response store(@Context UriInfo uriInfo, FunctionCostsDocument request) {
    FunctionCostsDocument result = getFunctionCostsMaster().store(request);
    return Response.ok().entity(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the load.
   * 
   * @param baseUri  the base URI, not null
   * @param configurationName  the configuration key, not null
   * @param functionId  the function id, not null
   * @param versionAsOf  the optional instant to retrieve data as of, null means latest
   * @return the URI, not null
   */
  public static URI uriLoad(URI baseUri, String configurationName, String functionId, Instant versionAsOf) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/functioncosts");
    if (configurationName != null) {
      bld.queryParam("configurationName", configurationName);
    }
    if (functionId != null) {
      bld.queryParam("functionId", functionId);
    }
    if (versionAsOf != null) {
      bld.queryParam("versionAsOf", versionAsOf.toString());
    }
    return bld.build();
  }

  /**
   * Builds a URI for the store.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriStore(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/functioncosts");
    return bld.build();
  }

}

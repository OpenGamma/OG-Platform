/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a {@link ViewCycle}
 */
public class DataViewCycleResource extends AbstractDataResource {

  //CSOFF: just constants
  public static final String PATH_UNIQUE_ID = "id";
  public static final String PATH_VIEW_PROCESS_ID = "viewProcessId";
  public static final String PATH_STATE = "state";
  public static final String PATH_DURATION = "duration";
  public static final String PATH_COMPILED_VIEW_DEFINITION = "compiledViewDefinition";
  public static final String PATH_RESULT = "result";
  public static final String PATH_QUERY_CACHES = "queryCaches";
  //CSON: just constants

  private final ViewCycle _cycle;

  public DataViewCycleResource(ViewCycle cycle) {
    _cycle = cycle;
  }

  @GET
  @Path(PATH_UNIQUE_ID)
  public Response getUniqueId() {
    return responseOkFudge(_cycle.getUniqueId());
  }

  @GET
  @Path(PATH_VIEW_PROCESS_ID)
  public Response getViewProcessId() {
    return responseOkFudge(_cycle.getViewProcessId());
  }

  @GET
  @Path(PATH_STATE)
  public Response getState() {
    return responseOkFudge(_cycle.getState());
  }

  @GET
  @Path(PATH_DURATION)
  public Response getDuration() {
    return responseOkFudge(_cycle.getDuration());
  }

  @Path(PATH_COMPILED_VIEW_DEFINITION)
  public DataCompiledViewDefinitionResource getCompiledViewDefinition() {
    return new DataCompiledViewDefinitionResource(_cycle.getCompiledViewDefinition());
  }

  @GET
  @Path(PATH_RESULT)
  public Response getResult() {
    return responseOkFudge(_cycle.getResultModel());
  }

  @POST
  @Path(PATH_QUERY_CACHES)
  public Response queryComputationCaches(ComputationCacheQuery query) {
    ComputationCacheResponse result = _cycle.queryComputationCaches(query);
    return responseOkFudge(result);
  }

}

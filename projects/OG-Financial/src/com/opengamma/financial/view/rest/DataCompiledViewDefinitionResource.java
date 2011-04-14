/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;

/**
 * RESTful resource for {@link CompiledViewDefinitionWithGraphsImpl}.
 */
public class DataCompiledViewDefinitionResource {

  //CSOFF: just constants
  public static final String PATH_VIEW_DEFINITION = "viewDefinition";
  public static final String PATH_PORTFOLIO = "portfolio";
  public static final String PATH_LIVE_DATA_REQUIREMENTS = "liveDataRequirements";
  public static final String PATH_SECURITY_TYPES = "securityTypes";
  public static final String PATH_VALID_FROM = "validFrom";
  public static final String PATH_VALID_TO = "validTo";
  public static final String PATH_OUTPUT_VALUE_NAMES = "outputValueNames";
  public static final String PATH_COMPUTATION_TARGETS = "computationTargets";
  //CSON: just constants
  
  private final CompiledViewDefinition _compiledViewDefinition;
  
  public DataCompiledViewDefinitionResource(CompiledViewDefinition compiledViewDefinition) {
    _compiledViewDefinition = compiledViewDefinition;
  }
  
  @GET
  @Path(PATH_VIEW_DEFINITION)
  public Response getViewDefinition() {
    return Response.ok(_compiledViewDefinition.getViewDefinition()).build();
  }
  
  @GET
  @Path(PATH_PORTFOLIO)
  public Response getPortfolio() {
    return Response.ok(_compiledViewDefinition.getPortfolio()).build();
  }
  
  @GET
  @Path(PATH_LIVE_DATA_REQUIREMENTS)
  public Response getLiveDataRequirements() {
    return Response.ok(_compiledViewDefinition.getLiveDataRequirements()).build();
  }
  
  @GET
  @Path(PATH_SECURITY_TYPES)
  public Response getSecurityTypes() {
    return Response.ok(_compiledViewDefinition.getSecurityTypes()).build();
  }
  
  @GET
  @Path(PATH_VALID_FROM)
  public Response getValidFrom() {
    return Response.ok(_compiledViewDefinition.getValidFrom()).build();
  }
  
  @GET
  @Path(PATH_VALID_TO)
  public Response getValidTo() {
    return Response.ok(_compiledViewDefinition.getValidTo()).build();
  }

  @GET
  @Path(PATH_OUTPUT_VALUE_NAMES)
  public Response getOutputValueNames() {
    return Response.ok(_compiledViewDefinition.getOutputValueNames()).build();
  }
  
  @GET
  @Path(PATH_COMPUTATION_TARGETS)
  public Response getComputationTargets() {
    return Response.ok(_compiledViewDefinition.getComputationTargets()).build();
  }
  
}

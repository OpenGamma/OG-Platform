/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;

/**
 * RESTful resource for {@link CompiledViewDefinitionWithGraphsImpl}.
 */
public class DataCompiledViewDefinitionResource {

  //CSOFF: just constants
  public static final String PATH_VIEW_DEFINITION = "viewDefinition";
  public static final String PATH_PORTFOLIO = "portfolio";
  public static final String PATH_VALID_FROM = "validFrom";
  public static final String PATH_VALID_TO = "validTo";
  public static final String PATH_LIVE_DATA_REQUIREMENTS = "liveDataRequirements";
  public static final String PATH_COMPUTATION_TARGETS = "computationTargets";
  public static final String PATH_COMPILED_CALCULATION_CONFIGURATIONS = "compiledCalculationConfigurations";
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
  @Path(PATH_COMPILED_CALCULATION_CONFIGURATIONS)
  public Response getCompiledCalculationConfigurations() {
    return Response.ok(_compiledViewDefinition.getCompiledCalculationConfigurations()).build();
  }
  
  @Path(PATH_COMPILED_CALCULATION_CONFIGURATIONS + "/{calcConfigName}")
  public Response getCompiledViewCalculationConfiguration(@PathParam("calcConfigName") String calcConfigName) {
    return Response.ok(_compiledViewDefinition.getCompiledCalculationConfiguration(calcConfigName)).build();
  }
  
  @GET
  @Path(PATH_COMPUTATION_TARGETS)
  public Response getComputationTargets() {
    return Response.ok(_compiledViewDefinition.getComputationTargets()).build();
  }
  
  @GET
  @Path(PATH_LIVE_DATA_REQUIREMENTS)
  public Response getLiveDataRequirements() {
    return Response.ok(_compiledViewDefinition.getLiveDataRequirements()).build();
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
  
  //-------------------------------------------------------------------------
  public static URI uriCompiledCalculationConfiguration(URI baseUri, String calcConfigName) {
    return UriBuilder.fromUri(baseUri).segment(calcConfigName).build();
  }

}

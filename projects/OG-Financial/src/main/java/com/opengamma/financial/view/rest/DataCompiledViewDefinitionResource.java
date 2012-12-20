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

import com.google.common.collect.Lists;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for {@link CompiledViewDefinitionWithGraphsImpl}.
 */
public class DataCompiledViewDefinitionResource extends AbstractDataResource {

  //CSOFF: just constants
  public static final String PATH_VIEW_DEFINITION = "viewDefinition";
  public static final String PATH_PORTFOLIO = "portfolio";
  public static final String PATH_VALID_FROM = "validFrom";
  public static final String PATH_VALID_TO = "validTo";
  public static final String PATH_MARKET_DATA_REQUIREMENTS = "marketDataRequirements";
  public static final String PATH_COMPUTATION_TARGETS = "computationTargets";
  public static final String PATH_COMPILED_CALCULATION_CONFIGURATIONS = "compiledCalculationConfigurations";
  public static final String PATH_GRAPHS = "graphs";
  //CSON: just constants
  
  private final CompiledViewDefinitionWithGraphs _compiledViewDefinition;
  
  public DataCompiledViewDefinitionResource(CompiledViewDefinitionWithGraphs compiledViewDefinitionWithGraphs) {
    _compiledViewDefinition = compiledViewDefinitionWithGraphs;
  }
  
  @GET
  @Path(PATH_VIEW_DEFINITION)
  public Response getViewDefinition() {
    return responseOkFudge(_compiledViewDefinition.getViewDefinition());
  }
  
  @GET
  @Path(PATH_PORTFOLIO)
  public Response getPortfolio() {
    return responseOkFudge(_compiledViewDefinition.getPortfolio());
  }
  
  @GET
  @Path(PATH_COMPILED_CALCULATION_CONFIGURATIONS)
  public Response getCompiledCalculationConfigurations() {
    return responseOkFudge(Lists.newArrayList(_compiledViewDefinition.getCompiledCalculationConfigurations()));
  }
  
  @GET
  @Path(PATH_COMPILED_CALCULATION_CONFIGURATIONS + "/{calcConfigName}")
  public Response getCompiledViewCalculationConfiguration(@PathParam("calcConfigName") String calcConfigName) {
    return responseOkFudge(_compiledViewDefinition.getCompiledCalculationConfiguration(calcConfigName));
  }
  
  @GET
  @Path(PATH_COMPUTATION_TARGETS)
  public Response getComputationTargets() {
    return responseOkFudge(_compiledViewDefinition.getComputationTargets());
  }

  @GET
  @Path(PATH_MARKET_DATA_REQUIREMENTS)
  public Response getMarketDataRequirements() {
    return responseOkFudge(_compiledViewDefinition.getMarketDataRequirements());
  }
  
  @GET
  @Path(PATH_VALID_FROM)
  public Response getValidFrom() {
    return responseOkFudge(_compiledViewDefinition.getValidFrom());
  }
  
  @GET
  @Path(PATH_VALID_TO)
  public Response getValidTo() {
    return responseOkFudge(_compiledViewDefinition.getValidTo());
  }
  
  @Path(PATH_GRAPHS + "/{calcConfigName}")
  public DataDependencyGraphExplorerResource getDependencyGraphExplorer(@PathParam("calcConfigName") String calcConfigName) {
    return new DataDependencyGraphExplorerResource(_compiledViewDefinition.getDependencyGraphExplorer(calcConfigName));
  }
  
  //-------------------------------------------------------------------------
  public static URI uriCompiledCalculationConfiguration(URI baseUri, String calcConfigName) {
    return UriBuilder.fromUri(baseUri).segment(calcConfigName).build();
  }

}

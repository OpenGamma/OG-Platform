/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.util.rest.FudgeRestClient;

import static com.opengamma.util.functional.Functional.merge;

/**
 * Remote implementation of {@link CompiledViewDefinition}.
 */
public class RemoteCompiledViewDefinitionWithGraphs implements CompiledViewDefinitionWithGraphs {

  private final URI _baseUri;
  private final FudgeRestClient _client;
  
  public RemoteCompiledViewDefinitionWithGraphs(URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }

  @Override
  public ViewDefinition getViewDefinition() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_VIEW_DEFINITION).build();
    return _client.access(uri).get(ViewDefinition.class);
  }

  @Override
  public Portfolio getPortfolio() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_PORTFOLIO).build();
    return _client.access(uri).get(Portfolio.class);
  }
  
  @Override
  public CompiledViewCalculationConfiguration getCompiledCalculationConfiguration(String viewCalculationConfiguration) {
    URI baseUri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_COMPILED_CALCULATION_CONFIGURATIONS).build();
    URI uri = DataCompiledViewDefinitionResource.uriCompiledCalculationConfiguration(baseUri, viewCalculationConfiguration);
    return _client.access(uri).get(CompiledViewCalculationConfiguration.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<CompiledViewCalculationConfiguration> getCompiledCalculationConfigurations() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_COMPILED_CALCULATION_CONFIGURATIONS).build();
    return _client.access(uri).get(List.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<ValueRequirement, ValueSpecification> getMarketDataRequirements() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_MARKET_DATA_REQUIREMENTS).build();
    return _client.access(uri).get(Map.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputationTarget> getComputationTargets() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_COMPUTATION_TARGETS).build();
    return _client.access(uri).get(Set.class);
  }

  @Override
  public Instant getValidFrom() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_VALID_FROM).build();
    return _client.access(uri).get(Instant.class);
  }

  @Override
  public Instant getValidTo() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_VALID_TO).build();
    return _client.access(uri).get(Instant.class);
  }

  @Override
  public DependencyGraphExplorer getDependencyGraphExplorer(String calcConfig) {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_GRAPHS).path(calcConfig).build();
    return new RemoteDependencyGraphExplorer(uri);
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalValuesRequirements() {
    Map<ValueSpecification, Set<ValueRequirement>> terminalValuesRequirements = new HashMap<ValueSpecification, Set<ValueRequirement>>();
    Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations =  getCompiledCalculationConfigurations();
    for (CompiledViewCalculationConfiguration compiledCalculationConfiguration : compiledCalculationConfigurations) {
      merge(terminalValuesRequirements, compiledCalculationConfiguration.getTerminalOutputSpecifications());
    }
    return terminalValuesRequirements;
  }
}

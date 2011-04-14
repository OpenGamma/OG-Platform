/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.util.rest.FudgeRestClient;

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

  @SuppressWarnings("unchecked")
  @Override
  public Map<ValueRequirement, ValueSpecification> getLiveDataRequirements() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_LIVE_DATA_REQUIREMENTS).build();
    return _client.access(uri).get(Map.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getSecurityTypes() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_SECURITY_TYPES).build();
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

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getOutputValueNames() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_OUTPUT_VALUE_NAMES).build();
    return _client.access(uri).get(Set.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputationTarget> getComputationTargets() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionResource.PATH_COMPUTATION_TARGETS).build();
    return _client.access(uri).get(Set.class);
  }
  
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.calc.ViewCycleState;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link ViewCycle}.
 */
public class RemoteViewCycle implements ViewCycle {

  private final URI _baseUri;
  private final FudgeRestClient _client;
  
  public RemoteViewCycle(URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }
  
  @Override
  public UniqueIdentifier getUniqueId() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleResource.PATH_UNIQUE_ID).build();
    return _client.access(uri).get(UniqueIdentifier.class);
  }

  @Override
  public UniqueIdentifier getViewProcessId() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleResource.PATH_VIEW_PROCESS_ID).build();
    return _client.access(uri).get(UniqueIdentifier.class);
  }

  @Override
  public ViewCycleState getState() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleResource.PATH_STATE).build();
    return _client.access(uri).get(ViewCycleState.class);
  }

  @Override
  public long getDurationNanos() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleResource.PATH_DURATION).build();
    return _client.access(uri).get(Long.class);
  }

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinition() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleResource.PATH_COMPILED_VIEW_DEFINITION).build();
    return new RemoteCompiledViewDefinitionWithGraphs(uri);
  }

  @Override
  public ViewComputationResultModel getResultModel() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleResource.PATH_RESULT).build();
    return _client.access(uri).get(ViewComputationResultModel.class);
  }

  @Override
  public ComputationCacheResponse queryComputationCaches(ComputationCacheQuery computationCacheQuery) {
    ArgumentChecker.notNull(computationCacheQuery, "computationCacheQuery");
    String msgBase64 = _client.encode(computationCacheQuery);
    URI uri = DataViewCycleResource.uriQuery(_baseUri, msgBase64);
    return _client.access(uri).get(ComputationCacheResponse.class);
  }

}

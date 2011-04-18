/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Set;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link ViewDefinitionRepository}.
 */
public class RemoteViewDefinitionRepository implements ViewDefinitionRepository {

  private final URI _baseUri;
  private final FudgeRestClient _client;
  
  public RemoteViewDefinitionRepository(URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getDefinitionNames() {
    return _client.access(_baseUri).get(Set.class);
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    URI uri = DataViewDefinitionRepositoryResource.uriDefinition(_baseUri, definitionName);
    return _client.access(uri).get(ViewDefinition.class);
  }
  
  //-------------------------------------------------------------------------
  protected URI getBaseUri() {
    return _baseUri;
  }
  
  protected FudgeRestClient getClient() {
    return _client;
  }

}

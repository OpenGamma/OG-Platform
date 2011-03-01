/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.UpdateViewDefinitionRequest;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Provides access to a remote {@link ManageableViewDefinitionRepository}.
 */
public class RemoteManagableViewDefinitionRepository implements ManageableViewDefinitionRepository {

  private final URI _baseUri;
  private final FudgeRestClient _client;
  
  public RemoteManagableViewDefinitionRepository(URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }
  
  //-------------------------------------------------------------------------
  // ViewDefinitionRepository implementation
  @Override
  public ViewDefinition getDefinition(String definitionName) {
    throw new NotImplementedException();
  }

  @Override
  public Set<String> getDefinitionNames() {
    throw new NotImplementedException();
  }
  
  //-------------------------------------------------------------------------
  // ManagableViewDefinitionRepository implementation
  @Override
  public boolean isModificationSupported() {
    return true;
  }
  
  @Override
  public void addViewDefinition(AddViewDefinitionRequest request) {
    URI uri = ViewDefinitionsResource.uri(_baseUri);
    _client.access(uri).post(request);
  }
  
  @Override
  public void updateViewDefinition(UpdateViewDefinitionRequest request) {
    URI uri = ViewDefinitionResource.uri(_baseUri, request.getName());
    _client.access(uri).put(request.getViewDefinition());
  }

  @Override
  public void removeViewDefinition(String name) {
    URI uri = ViewDefinitionResource.uri(_baseUri, name);
    _client.access(uri).delete();
  }
  
}

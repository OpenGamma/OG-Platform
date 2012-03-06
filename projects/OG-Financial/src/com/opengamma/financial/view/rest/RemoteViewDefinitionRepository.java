/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.rest.FudgeRestClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Remote implementation of {@link ViewDefinitionRepository}.
 */
public class RemoteViewDefinitionRepository implements ViewDefinitionRepository {

  private final URI _baseUri;
  private final FudgeRestClient _client;
  private final ChangeManager _changeManager;
  
  public RemoteViewDefinitionRepository(URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
    _changeManager = new BasicChangeManager();
  }
   
  @SuppressWarnings("unchecked")
  @Override
  public Set<ObjectId> getDefinitionIds() {
    final URI uri = UriBuilder.fromUri(_baseUri).segment(DataViewDefinitionRepositoryResource.PATH_VIEWDEFINITION_GETIDS).build();
    return _client.accessFudge(uri).get(Set.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<UniqueId, String> getDefinitionEntries() {
    return _client.accessFudge(_baseUri).get(Map.class);
  }
  
  @Override
  public ViewDefinition getDefinition(UniqueId definitionId) {
    final URI uri = DataViewDefinitionRepositoryResource.uriDefinitionId(_baseUri, definitionId);
    try {
      return _client.accessFudge(uri).get(ViewDefinition.class);
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }
  
  @Override
  public ViewDefinition getDefinition(String definitionName) {
    URI uri = DataViewDefinitionRepositoryResource.uriDefinitionName(_baseUri, definitionName);
    try {      
      return _client.accessFudge(uri).get(ViewDefinition.class);
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }

  }
  
  //-------------------------------------------------------------------------
  protected URI getBaseUri() {
    return _baseUri;
  }
  
  protected FudgeRestClient getClient() {
    return _client;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}

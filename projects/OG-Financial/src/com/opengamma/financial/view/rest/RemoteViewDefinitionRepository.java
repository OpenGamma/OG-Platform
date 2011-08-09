/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

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
  public Set<String> getDefinitionNames() {
    return _client.access(_baseUri).get(Set.class);
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    URI uri = DataViewDefinitionRepositoryResource.uriDefinition(_baseUri, definitionName);
    try {
      return _client.access(uri).get(ViewDefinition.class);
    } catch (UniformInterfaceException e) {
      // Translate 404s to a null return. Otherwise rethrow the underlying exception.
      if (e.getResponse().getClientResponseStatus() == ClientResponse.Status.NOT_FOUND) {
        return null;
      } else {
        throw new OpenGammaRuntimeException("Underlying transport exception", e);
      }
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

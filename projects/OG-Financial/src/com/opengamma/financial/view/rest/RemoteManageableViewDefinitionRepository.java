/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.UpdateViewDefinitionRequest;
import com.opengamma.id.UniqueId;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link ManageableViewDefinitionRepository}.
 */
public class RemoteManageableViewDefinitionRepository extends RemoteViewDefinitionRepository implements ManageableViewDefinitionRepository {

  public RemoteManageableViewDefinitionRepository(URI baseUri) {
    super(baseUri);
  }

  @Override
  public boolean isModificationSupported() {
    return true;
  }

  @Override
  public UniqueId addViewDefinition(AddViewDefinitionRequest request) {
    final ClientResponse response = getClient().access(getBaseUri()).post(ClientResponse.class, request);
    final String path = response.getHeaders().getFirst("Location");
    final int slash = path.lastIndexOf('/');
    if (slash > 0) {
      return UniqueId.parse(path.substring(slash + 1));
    } else {
      throw new OpenGammaRuntimeException("Remote end returned " + path);
    }
  }

  @Override
  public void updateViewDefinition(UpdateViewDefinitionRequest request) {
    URI uri = DataViewDefinitionRepositoryResource.uriDefinitionId(getBaseUri(), request.getViewDefinition().getUniqueId());
    getClient().access(uri).put(request.getViewDefinition());
  }

  @Override
  public void removeViewDefinition(UniqueId definitionId) {
    URI uri = DataViewDefinitionRepositoryResource.uriDefinitionId(getBaseUri(), definitionId);
    getClient().access(uri).delete();
  }
  
}

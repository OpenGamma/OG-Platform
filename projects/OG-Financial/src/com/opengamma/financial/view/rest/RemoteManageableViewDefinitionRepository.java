/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.Response.Status;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewDefinition;
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
    ClientResponse response = getClient().access(getBaseUri()).post(ClientResponse.class, request);
    if (response.getStatus() != Status.CREATED.getStatusCode()) {
      throw new OpenGammaRuntimeException("Could not add view definition: " + response);
    }
    URI addedUri = response.getLocation();
    return getClient().access(addedUri).get(ViewDefinition.class).getUniqueId();
  }

  @Override
  public void updateViewDefinition(UpdateViewDefinitionRequest request) {
    URI uri = DataViewDefinitionRepositoryResource.uriDefinitionId(getBaseUri(), request.getId());
    getClient().access(uri).put(request.getViewDefinition());
  }

  @Override
  public void removeViewDefinition(UniqueId definitionId) {
    URI uri = DataViewDefinitionRepositoryResource.uriDefinitionId(getBaseUri(), definitionId);
    getClient().access(uri).delete();
  }
  
}

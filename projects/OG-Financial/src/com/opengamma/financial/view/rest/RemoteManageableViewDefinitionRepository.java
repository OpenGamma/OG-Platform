/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.UpdateViewDefinitionRequest;
import com.opengamma.id.UniqueId;

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
    getClient().access(getBaseUri()).post(request);
    return request.getViewDefinition().getUniqueId(); // currently not retrieving the actual allocated unique id from the remote end
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

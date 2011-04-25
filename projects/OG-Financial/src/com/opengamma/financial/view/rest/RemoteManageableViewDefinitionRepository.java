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
  public void addViewDefinition(AddViewDefinitionRequest request) {
    getClient().access(getBaseUri()).post(request);
  }
  
  @Override
  public void updateViewDefinition(UpdateViewDefinitionRequest request) {
    URI uri = DataViewDefinitionRepositoryResource.uriDefinition(getBaseUri(), request.getName());
    getClient().access(uri).put(request.getViewDefinition());
  }

  @Override
  public void removeViewDefinition(String definitionName) {
    URI uri = DataViewDefinitionRepositoryResource.uriDefinition(getBaseUri(), definitionName);
    getClient().access(uri).delete();
  }
  
}

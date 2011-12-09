/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.UpdateViewDefinitionRequest;
import com.opengamma.id.UniqueId;

/**
 * RESTful resource for a {@link ViewDefinition} that can be modified.
 */
public class DataManageableViewDefinitionResource extends DataViewDefinitionResource {

  private final ManageableViewDefinitionRepository _repository;
  
  public DataManageableViewDefinitionResource(UniqueId definitionId, ManageableViewDefinitionRepository repository) {
    super(definitionId, repository);
    _repository = repository;
  }
  
  public DataManageableViewDefinitionResource(String definitionName, ManageableViewDefinitionRepository repository) {
    super(definitionName, repository);
    _repository = repository;
  }

  //-------------------------------------------------------------------------
  @PUT
  public Response updateViewDefinition(ViewDefinition viewDefinition) {
    final UpdateViewDefinitionRequest request = new UpdateViewDefinitionRequest();
    request.setId(getViewDefinitionId());
    request.setName(viewDefinition.getName());
    request.setViewDefinition(viewDefinition);
    _repository.updateViewDefinition(request);
    return Response.ok().build();
  }
  
  @DELETE
  public Response removeViewDefinition() {
    _repository.removeViewDefinition(getViewDefinitionId());
    return Response.ok().build();
  }
  
}

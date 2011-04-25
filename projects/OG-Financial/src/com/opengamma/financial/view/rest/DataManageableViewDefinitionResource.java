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

/**
 * RESTful resource for a {@link ViewDefinition} that can be modified.
 */
public class DataManageableViewDefinitionResource extends DataViewDefinitionResource {

  private final ManageableViewDefinitionRepository _repository;
  
  public DataManageableViewDefinitionResource(String definitionName, ManageableViewDefinitionRepository repository) {
    super(definitionName, repository);
    _repository = repository;
  }
  
  //-------------------------------------------------------------------------
  @PUT
  public Response updateViewDefinition(UpdateViewDefinitionRequest updateRequest) {
    updateRequest.checkValid();
    _repository.updateViewDefinition(updateRequest);
    return Response.ok().build();
  }
  
  @DELETE
  public Response removeViewDefinition() {
    _repository.removeViewDefinition(getViewDefinitionName());
    return Response.ok().build();
  }
  
}

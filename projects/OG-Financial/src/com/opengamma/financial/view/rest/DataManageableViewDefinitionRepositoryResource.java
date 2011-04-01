/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.transport.jaxrs.FudgeRest;

/**
 * RESTful resource for all views in a {@link ViewDefinitionRepository}
 */
public class DataManageableViewDefinitionRepositoryResource extends DataViewDefinitionRepositoryResource {

  /**
   * The underlying repository.
   */
  private final ManageableViewDefinitionRepository _repository;

  public DataManageableViewDefinitionRepositoryResource(ManageableViewDefinitionRepository repository) {
    super(repository);
    _repository = repository;
  }

  //-------------------------------------------------------------------------
  @POST
  @Path(PATH_DEFINITION)
  @Consumes(FudgeRest.MEDIA)
  public Response addViewDefinition(@Context UriInfo uriInfo, AddViewDefinitionRequest addRequest) {
    addRequest.checkValid();
    _repository.addViewDefinition(addRequest);
    return Response.created(
        DataViewDefinitionRepositoryResource.uriDefinition(
            uriInfo.getBaseUri(), addRequest.getViewDefinition().getName())).build();
  }
  
  @Path(PATH_DEFINITION + "/{definitionName}")
  public DataViewDefinitionResource getViewDefinition(@PathParam("definitionName") String definitionName) {
    return new DataManageableViewDefinitionResource(definitionName, _repository);
  }
  
}

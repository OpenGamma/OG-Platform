/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

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
import com.opengamma.id.UniqueId;
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
  @Consumes(FudgeRest.MEDIA)
  public Response addViewDefinition(@Context UriInfo uriInfo, AddViewDefinitionRequest addRequest) {
    addRequest.checkValid();
    UniqueId addedId = _repository.addViewDefinition(addRequest);
    URI createdUri = DataViewDefinitionRepositoryResource.uriDefinitionId(uriInfo.getRequestUri(), addedId);
    return responseCreated(createdUri);
  }

  @Path(PATH_VIEWDEFINITION_NAME + "/{viewDefinitionName}")
  public DataViewDefinitionResource getDefinitionByName(@PathParam("viewDefinitionName") String viewDefinitionName) {
    return new DataManageableViewDefinitionResource(viewDefinitionName, _repository);
  }

  @Path(PATH_VIEWDEFINITION_UNIQUE_ID + "/{viewDefinitionId}")
  public DataViewDefinitionResource getDefinitionById(@PathParam("viewDefinitionId") String viewDefinitionIdString) {
    UniqueId viewDefinitionId = UniqueId.parse(viewDefinitionIdString);
    return new DataManageableViewDefinitionResource(viewDefinitionId, _repository);
  }
  
}

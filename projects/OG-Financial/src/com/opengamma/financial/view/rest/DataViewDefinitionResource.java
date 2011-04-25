/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;

/**
 * RESTful resource for a {@link ViewDefinition}.
 */
public class DataViewDefinitionResource {

  private final String _definitionName;
  private final ViewDefinitionRepository _repository;
  
  public DataViewDefinitionResource(String definitionName, ViewDefinitionRepository repository) {
    _definitionName = definitionName;
    _repository = repository;
  }
  
  //-------------------------------------------------------------------------
  @GET
  public Response getViewDefinition() {
    ViewDefinition definition = _repository.getDefinition(_definitionName);
    if (definition == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return Response.ok(definition).build();
  }
  
  //-------------------------------------------------------------------------
  protected String getViewDefinitionName() {
    return _definitionName;
  }
  
}

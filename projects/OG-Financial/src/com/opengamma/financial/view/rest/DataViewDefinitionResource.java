/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a {@link ViewDefinition}.
 */
public class DataViewDefinitionResource extends AbstractDataResource {

  private final UniqueId _definitionId;
  private final ViewDefinitionRepository _repository;
  
  public DataViewDefinitionResource(UniqueId definitionId, ViewDefinitionRepository repository) {
    _definitionId = definitionId;
    _repository = repository;
  }
  
  public DataViewDefinitionResource(String definitionName, ViewDefinitionRepository repository) {
    this(repository.getDefinition(definitionName).getUniqueId(), repository);
  }
  //-------------------------------------------------------------------------
  @GET
  public Response getViewDefinition() {
    ViewDefinition definition = _repository.getDefinition(_definitionId);
    return responseOkFudge(definition);
  }
  
  //-------------------------------------------------------------------------
  protected UniqueId getViewDefinitionId() {
    return _definitionId;
  }
  
}

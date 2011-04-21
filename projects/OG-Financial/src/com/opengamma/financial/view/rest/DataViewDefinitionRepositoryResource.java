/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for {@link ViewDefinitionRepository}
 */
public class DataViewDefinitionRepositoryResource {
  
  private final ViewDefinitionRepository _repository;
  
  public DataViewDefinitionRepositoryResource(ViewDefinitionRepository repository) {
    ArgumentChecker.notNull(repository, "repository");
    _repository = repository;
  }
  
  //-------------------------------------------------------------------------
  @GET
  public Set<String> getDefinitionNames() {
    return _repository.getDefinitionNames();
  }
  
  @Path("{definitionName}")
  public DataViewDefinitionResource getViewDefinition(@PathParam("definitionName") String definitionName) {
    return new DataViewDefinitionResource(definitionName, _repository);
  }
  
  //-------------------------------------------------------------------------
  public static URI uri(URI baseUri, String path) { 
    return UriBuilder.fromUri(baseUri).path(path).build();
  }
  
  public static URI uriDefinition(URI baseUri, String definitionName) {
    return UriBuilder.fromUri(baseUri).segment(definitionName).build();
  }
  
}

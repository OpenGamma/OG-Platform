/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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

  //CSOFF: just constants
  public static final String PATH_NAMES = "names";
  public static final String PATH_DEFINITION = "definition";
  //CSON: just constants
  
  private final ViewDefinitionRepository _repository;
  
  public DataViewDefinitionRepositoryResource(ViewDefinitionRepository repository) {
    ArgumentChecker.notNull(repository, "repository");
    _repository = repository;
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_NAMES)
  public Set<String> getDefinitionNames() {
    return _repository.getDefinitionNames();
  }
  
  @Path(PATH_DEFINITION + "/{definitionName}")
  public DataViewDefinitionResource getViewDefinition(@PathParam("definitionName") String definitionName) {
    return new DataViewDefinitionResource(definitionName, _repository);
  }
  
  //-------------------------------------------------------------------------
  public static URI uri(URI baseUri, String path) { 
    return UriBuilder.fromUri(baseUri).path(path).build();
  }
  
  public static URI uriDefinition(URI baseUri, String definitionName) {
    return UriBuilder.fromUri(baseUri).path(PATH_DEFINITION).segment(definitionName).build();
  }
  
}

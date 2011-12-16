/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for {@link ViewDefinitionRepository}
 */
public class DataViewDefinitionRepositoryResource {
  
  private final ViewDefinitionRepository _repository;
  
  //CSOFF
  public static final String PATH_VIEWDEFINITION_GETIDS = "ids";
  public static final String PATH_VIEWDEFINITION_UNIQUE_ID = "id";
  public static final String PATH_VIEWDEFINITION_NAME = "name";

  //CSON
  
  public DataViewDefinitionRepositoryResource(ViewDefinitionRepository repository) {
    ArgumentChecker.notNull(repository, "repository");
    _repository = repository;
  }
  
  //-------------------------------------------------------------------------
  @GET
  public Map<UniqueId, String> getDefinitionEntries() {
    return _repository.getDefinitionEntries();
  }
  
  @GET
  @Path(PATH_VIEWDEFINITION_GETIDS)
  public Set<ObjectId> getDefinitionIds() {
    return _repository.getDefinitionIds();
  }
  
  @Path(PATH_VIEWDEFINITION_NAME + "/{viewDefinitionName}")
  public DataViewDefinitionResource getDefinitionByName(@PathParam("viewDefinitionName") String viewDefinitionName) {
    final ViewDefinition vd = _repository.getDefinition(viewDefinitionName);
    return vd != null ? new DataViewDefinitionResource(vd.getUniqueId(), _repository) : null;
  }

  @Path(PATH_VIEWDEFINITION_UNIQUE_ID + "/{viewDefinitionId}")
  public DataViewDefinitionResource getDefinitionById(@PathParam("viewDefinitionId") String viewDefinitionIdString) {
    UniqueId viewDefinitionId = UniqueId.parse(viewDefinitionIdString);
    return viewDefinitionId != null ? new DataViewDefinitionResource(viewDefinitionId, _repository) : null;
  }

  //-------------------------------------------------------------------------
  public static URI uri(URI baseUri, String path) {
    return UriBuilder.fromUri(baseUri).path(path).build();
  }

  public static URI uriDefinitionName(URI baseUri, String viewDefinitionName) {
    return UriBuilder.fromUri(baseUri).segment(PATH_VIEWDEFINITION_NAME).segment(viewDefinitionName).build();
  }

  public static URI uriDefinitionId(URI baseUri, UniqueId viewDefinitionId) {
    return UriBuilder.fromUri(baseUri).segment(PATH_VIEWDEFINITION_UNIQUE_ID).segment(viewDefinitionId.toString()).build();
  }

}

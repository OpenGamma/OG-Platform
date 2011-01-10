/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.UpdateViewDefinitionRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a {@link ViewDefinition}
 */
@Path("/viewDefinitions/{definitionName}")
public class ViewDefinitionResource {
  
  private final ViewDefinitionsResource _viewDefinitionsResource;
  private final String _definitionName;
  
  /**
   * Creates the resource.
   * 
   * @param viewDefinitionsResource  the parent view definitions resource, not null
   * @param definitionName  the view definition name, not null
   */
  public ViewDefinitionResource(ViewDefinitionsResource viewDefinitionsResource, String definitionName) {
    ArgumentChecker.notNull(viewDefinitionsResource, "viewDefinitionsResource");
    ArgumentChecker.notNull(definitionName, "definitionName");
    _viewDefinitionsResource = viewDefinitionsResource;
    _definitionName = definitionName;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the view definitions resource.
   * @return the view definitions resource, not null
   */
  public ViewDefinitionsResource getViewDefinitionsResource() {
    return _viewDefinitionsResource;
  }
  
  /**
   * Gets the view definition name.
   * 
   * @return the view definition name, not null
   */
  public String getDefinitionName() {
    return _definitionName;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the view definition repository.
   * 
   * @return the view definition repository, not null
   */
  public ManageableViewDefinitionRepository getViewDefinitionRepository() {
    return getViewDefinitionsResource().getViewDefinitionRepository();
  }
  
  /**
   * Gets the URI info.
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return getViewDefinitionsResource().getUriInfo();
  }
  
  //-------------------------------------------------------------------------
  @PUT
  public Response putFudge(ViewDefinition definition) {
    UpdateViewDefinitionRequest request = new UpdateViewDefinitionRequest(definition);
    request.setName(getDefinitionName());
    request.checkValid();
    getViewDefinitionRepository().updateViewDefinition(request);
    URI uri = ViewDefinitionResource.uri(getUriInfo(), getDefinitionName());
    return Response.ok().location(uri).build();
  }
  
  @DELETE
  public Response deleteFudge() {
    getViewDefinitionRepository().removeViewDefinition(getDefinitionName());
    return Response.ok().build();
  }
  
  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a security.
   * @param uriInfo  the URI information, not null
   * @param definitionName  the name of the view definition, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, String definitionName) {
    return uriInfo.getBaseUriBuilder().path(ViewDefinitionResource.class).build(definitionName);
  }
  
}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for all views in a {@link ViewDefinitionRepository}
 */
@Path("/viewDefinitions")
public class ViewDefinitionsResource {

  /**
   * The underlying repository.
   */
  private final ManageableViewDefinitionRepository _repository;
  /**
   * The fudge context to use when deserializing requests and serializing responses.
   */
  private final FudgeContext _fudgeContext;
  /**
   * Information about the URI injected by JSR-311.
   */
  @Context
  private UriInfo _uriInfo;

  // -------------------------------------------------------------------------
  /**
   * Creates the resource 
   * @param repository  the underlying repository, not null
   * @param fudgeContext the Fudge context, not null
   */
  public ViewDefinitionsResource(ManageableViewDefinitionRepository repository, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(repository, "repository");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _repository = repository;

    _fudgeContext = fudgeContext;
  }

  /**
   * Creates the resource.
   * @param uriInfo  the URI information, not null
   * @param repository  the underlying repository, not null
   * @param fudgeContext the Fudge context, not null
   */
  public ViewDefinitionsResource(UriInfo uriInfo, ManageableViewDefinitionRepository repository, FudgeContext fudgeContext) {
    this(repository, fudgeContext);
    ArgumentChecker.notNull(uriInfo, "uriInfo");
    _uriInfo = uriInfo;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the security master.
   * @return the security master, not null
   */
  public ManageableViewDefinitionRepository getViewDefinitionRepository() {
    return _repository;
  }

  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return _uriInfo;
  }

  // -------------------------------------------------------------------------
  @POST
  public void postFudge(FudgeMsgEnvelope env) {
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(_fudgeContext);
    AddViewDefinitionRequest request = deserializationContext.fudgeMsgToObject(AddViewDefinitionRequest.class, env.getMessage());
    request.checkValid();
    getViewDefinitionRepository().addViewDefinition(request);
  }

  // -------------------------------------------------------------------------
  @Path("{definitionName}")
  public ViewDefinitionResource getView(@PathParam("definitionName") String definitionName) {
    return new ViewDefinitionResource(this, definitionName);
  }

  // -------------------------------------------------------------------------
  /**
   * Builds a URI for view definitions.
   * @param uriInfo  the URI information, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(ViewDefinitionsResource.class).build();
  }

}

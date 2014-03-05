/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.opengamma.engine.view.ViewProcess;
import com.opengamma.financial.livedata.rest.DataLiveDataInjectorResource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a {@link ViewProcess}.
 */
public class DataViewProcessResource extends AbstractDataResource {

  private final ViewProcess _viewProcess;
  
  //CSOFF: just constants
  public static final String PATH_UNIQUE_ID = "id";
  public static final String PATH_DEFINITION_ID = "definitionId";
  public static final String PATH_DEFINITION = "definition";
  public static final String PATH_STATE = "state";
  public static final String PATH_LIVE_DATA_OVERRIDE_INJECTOR = "liveDataOverrideInjector";
  //CSON: just constants
  
  /**
   * Creates the resource.
   * 
   * @param viewProcess  the underlying view process
   */
  public DataViewProcessResource(ViewProcess viewProcess) {
    ArgumentChecker.notNull(viewProcess, "viewProcess");
    _viewProcess = viewProcess;
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_UNIQUE_ID)
  public Response getUniqueId() {
    return responseOkObject(_viewProcess.getUniqueId());
  }
  
  @GET
  @Path(PATH_DEFINITION_ID)
  public Response getDefinitionName() {
    return responseOk(_viewProcess.getDefinitionId());
  }
  
  @GET
  @Path(PATH_DEFINITION)
  public Response getLatestViewDefinition() {
    return responseOkObject(_viewProcess.getLatestViewDefinition());
  }
  
  @GET
  @Path(PATH_STATE)
  public Response getState() {
    return responseOkObject(_viewProcess.getState());
  }
  
  @Path(PATH_LIVE_DATA_OVERRIDE_INJECTOR)
  public DataLiveDataInjectorResource getLiveDataOverrideInjector() {
    return new DataLiveDataInjectorResource(_viewProcess.getLiveDataOverrideInjector());
  }
  
  @DELETE
  public void shutdown() {
    _viewProcess.shutdown();
  }
  
}

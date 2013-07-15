/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.threeten.bp.Instant;

import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a {@link EngineResourceReference}
 * 
 * @param <T>  the type of resource
 */
public abstract class DataEngineResourceReferenceResource<T extends UniqueIdentifiable> extends AbstractDataResource {

  //CSOFF: just constants
  public static final String PATH_RESOURCE = "resource";
  //CSON: just constants
  
  private final DataEngineResourceManagerResource<T> _manager;
  private final long _referenceId;
  private final EngineResourceReference<? extends T> _resourceReference;
  
  private volatile Instant _lastHeartbeat = Instant.now();
  
  protected DataEngineResourceReferenceResource(DataEngineResourceManagerResource<T> manager, long referenceId, EngineResourceReference<? extends T> resourceReference) {
    _manager = manager;
    _referenceId = referenceId;
    _resourceReference = resourceReference;
  }
  
  @DELETE
  public void release() {
    _resourceReference.release();
    _manager.referenceReleased(_referenceId);
  }
  
  @POST
  public Response heartbeat(@PathParam("referenceId") long referenceId) {
    updateHeartbeat();
    return responseOk();
  }
  
  @Path(PATH_RESOURCE)
  public Object get() {
    updateHeartbeat();
    return getResourceResource(_resourceReference.get());
  }
  
  protected abstract Object getResourceResource(T resource);
  
  /*package*/ Instant getLastHeartbeat() {
    return _lastHeartbeat;
  }
  
  private void updateHeartbeat() {
    _lastHeartbeat = Instant.now();
  }
  
}

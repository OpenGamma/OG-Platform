/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.time.Instant;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.opengamma.engine.view.calc.ViewCycleReference;

/**
 * RESTful resource for a {@link ViewCycleReference}
 */
public class DataViewCycleReferenceResource {

  //CSOFF: just constants
  public static final String PATH_CYCLE = "cycle";
  //CSON: just constants
  
  private final DataViewCycleManagerResource _manager;
  private final long _referenceId;
  private final ViewCycleReference _cycleReference;
  
  private volatile Instant _lastHeartbeat = Instant.now();
  
  public DataViewCycleReferenceResource(DataViewCycleManagerResource manager, long referenceId, ViewCycleReference cycleReference) {
    _manager = manager;
    _referenceId = referenceId;
    _cycleReference = cycleReference;
  }
  
  @DELETE
  public void release() {
    _cycleReference.release();
    _manager.referenceReleased(_referenceId);
  }
  
  @POST
  public Response heartbeat(@PathParam("referenceId") long referenceId) {
    updateHeartbeat();
    return Response.ok().build();
  }
  
  @Path(PATH_CYCLE)
  public DataViewCycleResource getCycle() {
    updateHeartbeat();
    return new DataViewCycleResource(_cycleReference.getCycle());
  }
  
  /*package*/ Instant getLastHeartbeat() {
    return _lastHeartbeat;
  }
  
  private void updateHeartbeat() {
    _lastHeartbeat = Instant.now();
  }
  
}

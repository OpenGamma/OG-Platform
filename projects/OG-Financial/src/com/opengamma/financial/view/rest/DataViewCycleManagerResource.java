/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.time.Instant;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.view.calc.ViewCycleManager;
import com.opengamma.engine.view.calc.ViewCycleReference;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a {@link ViewCycleManager}
 */
@Path("/data/viewprocessor/{viewProcessorId}/cycles")
public class DataViewCycleManagerResource {

  /**
   * The time after which unused references may be automatically released.
   */
  public static final long REFERENCE_LEASE_MILLIS = 60000;
  
  private final URI _baseUri;
  private final ViewCycleManager _cycleManager;
  private final AtomicLong _nextReferenceId = new AtomicLong();
  private final Map<Long, DataViewCycleReferenceResource> _activeReferences = new ConcurrentHashMap<Long, DataViewCycleReferenceResource>();

  public DataViewCycleManagerResource(URI baseUri, ViewCycleManager cycleManager) {
    _baseUri = baseUri;
    _cycleManager = cycleManager;
  }
  
  @POST
  @Consumes(FudgeRest.MEDIA)
  public Response createReference(UniqueIdentifier cycleId) {
    ViewCycleReference reference = _cycleManager.createReference(cycleId);
    if (reference == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    URI referenceUri = manageReference(reference);
    return Response.created(referenceUri).build();
  }
  
  @Path("{referenceId}")
  public DataViewCycleReferenceResource getReference(@PathParam("referenceId") long referenceId) {
    DataViewCycleReferenceResource referenceResource = _activeReferences.get(referenceId);
    if (referenceResource == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return referenceResource;
  }
  
  /*package*/ void referenceReleased(long cycleId) {
    _activeReferences.remove(cycleId);
  }
  
  public static URI uriReference(URI baseUri, long cycleReferenceId) {
    return UriBuilder.fromUri(baseUri).segment(Long.toString(cycleReferenceId)).build();
  }
  
  private URI getBaseUri() {
    return _baseUri;
  }
  
  /*package*/ URI manageReference(ViewCycleReference reference) {
    long referenceId = _nextReferenceId.getAndIncrement();
    DataViewCycleReferenceResource referenceResource = new DataViewCycleReferenceResource(this, referenceId, reference);
    _activeReferences.put(referenceId, referenceResource);
    return uriReference(getBaseUri(), referenceId);
  }
  
  /**
   * Releases and discards any references that have not received a heartbeat since the given time.
   * 
   * @param oldestHeartbeatTime  the oldest permitted heartbeat time, not null
   */
  public void releaseExpiredReferences(final Instant oldestHeartbeatTime) {
    ArgumentChecker.notNull(oldestHeartbeatTime, "oldestHeartbeatTime");
    final Iterator<Map.Entry<Long, DataViewCycleReferenceResource>> it = _activeReferences.entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<Long, DataViewCycleReferenceResource> entry = it.next();
      DataViewCycleReferenceResource cycleReferenceResource = entry.getValue();
      if (cycleReferenceResource.getLastHeartbeat().isBefore(oldestHeartbeatTime)) {
        // Notifies the manager which removes it from the map
        cycleReferenceResource.release();
      }
    }
  }
  
  public ReleaseExpiredReferencesRunnable createReleaseExpiredReferencesTask() {
    return new ReleaseExpiredReferencesRunnable();
  }
  
  /**
   * Runnable to release expired references.
   */
  class ReleaseExpiredReferencesRunnable implements Runnable {

    @Override
    public void run() {
      releaseExpiredReferences(Instant.now().minusMillis(REFERENCE_LEASE_MILLIS));
    }

    /**
     * Sets the scheduler.
     * @param scheduler  the scheduler, not null
     */
    public void setScheduler(final ScheduledExecutorService scheduler) {
      scheduler.scheduleWithFixedDelay(this, REFERENCE_LEASE_MILLIS, REFERENCE_LEASE_MILLIS, TimeUnit.MILLISECONDS);
    }
    
  }
  
}

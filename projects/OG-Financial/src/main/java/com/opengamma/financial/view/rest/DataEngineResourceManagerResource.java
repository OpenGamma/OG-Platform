/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.resource.EngineResourceManager;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a {@link EngineResourceManager}
 * 
 * @param <T>  the type of resource
 */
public abstract class DataEngineResourceManagerResource<T extends UniqueIdentifiable> extends AbstractDataResource {

  private static final Logger s_logger = LoggerFactory.getLogger(DataEngineResourceManagerResource.class);
  
  /**
   * The time after which unused references may be automatically released.
   */
  public static final long REFERENCE_LEASE_MILLIS = 5000;
  
  private final URI _baseUri;
  private final EngineResourceManager<? extends T> _manager;
  private final AtomicLong _nextReferenceId = new AtomicLong();
  private final Map<Long, DataEngineResourceReferenceResource<T>> _activeReferences = new ConcurrentHashMap<Long, DataEngineResourceReferenceResource<T>>();

  protected DataEngineResourceManagerResource(URI baseUri, EngineResourceManager<? extends T> manager) {
    _baseUri = baseUri;
    _manager = manager;
  }
  
  @POST
  @Consumes(FudgeRest.MEDIA)
  public Response createReference(UniqueId uniqueId) {
    EngineResourceReference<? extends T> reference = _manager.createReference(uniqueId);
    if (reference == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    URI referenceUri = manageReference(reference);
    return responseCreated(referenceUri);
  }
  
  @Path("{referenceId}")
  public DataEngineResourceReferenceResource<T> getReference(@PathParam("referenceId") long referenceId) {
    DataEngineResourceReferenceResource<T> referenceResource = _activeReferences.get(referenceId);
    if (referenceResource == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return referenceResource;
  }
  
  /*package*/ void referenceReleased(long uniqueId) {
    _activeReferences.remove(uniqueId);
  }
  
  public static URI uriReference(URI baseUri, long referenceId) {
    return UriBuilder.fromUri(baseUri).segment(Long.toString(referenceId)).build();
  }
  
  private URI getBaseUri() {
    return _baseUri;
  }
  
  /*package*/ URI manageReference(EngineResourceReference<? extends T> reference) {
    long referenceId = _nextReferenceId.getAndIncrement();
    DataEngineResourceReferenceResource<T> referenceResource = createReferenceResource(referenceId, reference);
    _activeReferences.put(referenceId, referenceResource);
    return uriReference(getBaseUri(), referenceId);
  }
  
  protected abstract DataEngineResourceReferenceResource<T> createReferenceResource(long referenceId, EngineResourceReference<? extends T> reference);
  
  /**
   * Releases and discards any references that have not received a heartbeat since the given time.
   * 
   * @param oldestHeartbeatTime  the oldest permitted heartbeat time, not null
   */
  public void releaseExpiredReferences(final Instant oldestHeartbeatTime) {
    ArgumentChecker.notNull(oldestHeartbeatTime, "oldestHeartbeatTime");
    final Iterator<Map.Entry<Long, DataEngineResourceReferenceResource<T>>> it = _activeReferences.entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<Long, DataEngineResourceReferenceResource<T>> entry = it.next();
      DataEngineResourceReferenceResource<T> referenceResource = entry.getValue();
      if (referenceResource.getLastHeartbeat().isBefore(oldestHeartbeatTime)) {
        // Notifies the manager which removes it from the map
        s_logger.warn("Releasing reference {} which has not received a heartbeat since {}, which exceeds the oldest allowed heartbeat of {}", 
            new Object[] {entry.getKey(), referenceResource.getLastHeartbeat(), oldestHeartbeatTime});
        referenceResource.release();
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

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link EngineResourceReference}.
 * 
 * @param <T>  the type of resource
 */
public abstract class RemoteEngineResourceReference<T extends UniqueIdentifiable> implements EngineResourceReference<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteEngineResourceReference.class);
  
  private final URI _baseUri;
  private final FudgeRestClient _client;
  private final ScheduledFuture<?> _scheduledHeartbeat;
  
  private final AtomicBoolean _isReleased = new AtomicBoolean(false); 
  
  public RemoteEngineResourceReference(URI baseUri, ScheduledExecutorService scheduler) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
    
    _scheduledHeartbeat = scheduler.scheduleAtFixedRate(new HeartbeaterTask(),
        DataEngineResourceManagerResource.REFERENCE_LEASE_MILLIS / 2,
        DataEngineResourceManagerResource.REFERENCE_LEASE_MILLIS / 2, TimeUnit.MILLISECONDS);
  }
  
  @Override
  public T get() {
    if (_isReleased.get()) {
      throw new IllegalStateException("The view cycle reference has been released");
    }
    URI uri = UriBuilder.fromUri(_baseUri).path(DataEngineResourceReferenceResource.PATH_RESOURCE).build();
    return getRemoteResource(uri);
  }
  
  protected abstract T getRemoteResource(URI baseUri);

  @Override
  public void release() {
    if (_isReleased.getAndSet(true)) {
      return;
    }
    try {
      _client.accessFudge(_baseUri).delete();
    } finally {
      _scheduledHeartbeat.cancel(true);
    }
  }
  
  /**
   * For testing
   */
  public void stopHeartbeating() {
    _scheduledHeartbeat.cancel(true);
  }
  
  private class HeartbeaterTask implements Runnable {

    @Override
    public void run() {
      try {
        _client.accessFudge(_baseUri).post();
      } catch (Exception e) {
        s_logger.warn("Failed to heartbeat view cycle reference", e);
      }
    }
    
  }
}

/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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

import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.calc.ViewCycleReference;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link ViewCycleReference}.
 */
public class RemoteViewCycleReference implements ViewCycleReference {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteViewCycleReference.class);
  
  private final URI _baseUri;
  private final FudgeRestClient _client;
  private final ScheduledFuture<?> _scheduledHeartbeat;
  
  private final AtomicBoolean _isReleased = new AtomicBoolean(false); 
  
  public RemoteViewCycleReference(URI baseUri, ScheduledExecutorService scheduler) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
    
    _scheduledHeartbeat = scheduler.scheduleAtFixedRate(new HeartbeaterTask(),
        DataViewCycleManagerResource.REFERENCE_LEASE_MILLIS / 2,
        DataViewCycleManagerResource.REFERENCE_LEASE_MILLIS / 2, TimeUnit.MILLISECONDS);
  }
  
  @Override
  public ViewCycle getCycle() {
    if (_isReleased.get()) {
      throw new IllegalStateException("The view cycle reference has been released");
    }
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleReferenceResource.PATH_CYCLE).build();
    return new RemoteViewCycle(uri);
  }

  @Override
  public void release() {
    if (_isReleased.getAndSet(true)) {
      return;
    }
    try {
      _client.access(_baseUri).delete();
    } finally {
      _scheduledHeartbeat.cancel(true);
    }
  }
  
  private class HeartbeaterTask implements Runnable {

    @Override
    public void run() {
      try {
        _client.access(_baseUri).post();
      } catch (Exception e) {
        s_logger.warn("Failed to heartbeat view cycle reference", e);
      }
    }
    
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.rest;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Instant;

/**
 * Base class for monitoring a collection of {@code AbstractRestfulJmsResultPublisher} resources and expiring those
 * which have not been accessed recently.
 * 
 * @param <T>  the type of the resource
 */
public abstract class AbstractRestfulJmsResultPublisherExpiryJob<T extends AbstractRestfulJmsResultPublisher> {

  private final long _resourceTimeoutMillis;
  
  protected AbstractRestfulJmsResultPublisherExpiryJob(long resourceTimeoutMillis, ScheduledExecutorService scheduler) {
    _resourceTimeoutMillis = resourceTimeoutMillis;
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        removeStaleResources();
      }
    };
    scheduler.scheduleAtFixedRate(runnable, resourceTimeoutMillis, resourceTimeoutMillis, TimeUnit.MILLISECONDS);
  }
  
  /**
   * Gets a modifiable collection of resources. Stale resources will be removed.
   * 
   * @return a modifiable collection of resources, not null
   */
  protected abstract Collection<T> getResources();
  
  //-------------------------------------------------------------------------
  private void removeStaleResources() {
    Instant timeoutBefore = Instant.now().minusMillis(getResourceTimeoutMillis());
    Collection<T> currentResources = getResources();
    Iterator<T> iterator = currentResources.iterator();
    while (iterator.hasNext()) {
      T resource = iterator.next();
      if (resource.isTerminated()) {
        iterator.remove();
        continue;
      }
      if (resource.getLastAccessed().isBefore(timeoutBefore)) {
        iterator.remove();
        resource.expire();
      }
    }
  }
  
  private long getResourceTimeoutMillis() {
    return _resourceTimeoutMillis;
  }
  
}

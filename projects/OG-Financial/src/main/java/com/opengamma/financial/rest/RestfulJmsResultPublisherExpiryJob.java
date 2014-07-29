/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.rest;

import java.util.Collection;
import java.util.Iterator;

import org.threeten.bp.Instant;

/**
 * Class for monitoring a collection of {@code AbstractRestfulJmsResultPublisher}
 * resources and expiring those which have not been accessed recently. It is
 * expected that this will be invoked on a schedule, for example:
 * <code>
 *   private void someMethod() {
 *     _scheduler.scheduleAtFixedRate(createExpiryJob(), VIEW_CLIENT_TIMEOUT_MILLIS, VIEW_CLIENT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
 *   }
 *
 *   private RestfulJmsResultPublisherExpiryJob<DataViewClientResource> createExpiryJob() {
 *     return new RestfulJmsResultPublisherExpiryJob<>( VIEW_CLIENT_TIMEOUT_MILLIS, _createdViewClients.values());
 *   }
 * </code>
 *
 * @param <T> the type of the resource
 */
public class RestfulJmsResultPublisherExpiryJob<T extends AbstractRestfulJmsResultPublisher>
    implements Runnable {

  /**
   * The collection of resources to be checked for termination/expiry. This is expected
   * to be a live collection. Because of the need for multi-threaded access the passed
   * collection needs to be threadsafe.
   */
  private final Collection<T> _resources;

  /**
   * How long to wait after the last access time of a resource
   * before expiring it.
   */
  private final long _resourceTimeoutMillis;

  /**
   * Create the job to expire members of the collection of resources if
   * they have not been accessed in the period specified by the timeout.

   * @param resources the collection of resources to be checked for termination/expiry.
   * This is expected to be a live collection. Because of the need for multi-threaded
   * access the passed collection needs to be threadsafe.
   * @param resourceTimeoutMillis how long to wait after the last access time of a resource
   * before expiring it
   */
  public RestfulJmsResultPublisherExpiryJob(Collection<T> resources,
                                            long resourceTimeoutMillis) {
    _resourceTimeoutMillis = resourceTimeoutMillis;
    _resources = resources;
  }

  public void run() {
    Instant timeoutBefore = Instant.now().minusMillis(_resourceTimeoutMillis);
    for (Iterator<T> iterator = _resources.iterator(); iterator.hasNext(); ) {
      T resource = iterator.next();
      if (resource.isTerminated()) {
        iterator.remove();
      } else if (resource.getLastAccessed().isBefore(timeoutBefore)) {
        iterator.remove();
        resource.expire();
      }
    }
  }
}

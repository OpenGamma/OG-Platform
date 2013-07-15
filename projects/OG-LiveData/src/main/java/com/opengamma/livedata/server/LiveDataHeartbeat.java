/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Collection;

import com.opengamma.livedata.LiveDataSpecification;

/**
 * Service interface for a client to signal a heartbeat on a subscribed topic - indicating to the server that it should continue to publish information on it.
 * <p>
 * Some servers are able to respond with details of subscriptions which it is not currently publishing information on - a well written client can use this information to either free local resources if
 * they are not really needed, or reestablish them if they are.
 */
public interface LiveDataHeartbeat {

  /**
   * Notifies the server of the specifications the client is expecting to receive information from.
   * 
   * @param activeSubscriptions the client's active subscriptions, not null and not containing nulls
   * @return any subscriptions which the server does not recognize, or null/the empty set if all subscriptions are valid
   */
  Collection<LiveDataSpecification> heartbeat(Collection<LiveDataSpecification> activeSubscriptions);

}

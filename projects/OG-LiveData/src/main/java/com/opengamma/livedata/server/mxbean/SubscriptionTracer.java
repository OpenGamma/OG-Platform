/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.mxbean;

import javax.management.MXBean;

/**
 * Defines an MX Bean interface (implemented by the StandardLiveDataServer class) which
 * means that it is possible to trace the status of a subscription via JMX.
 */
@MXBean
public interface SubscriptionTracer {

  /**
   * Gets the trace of a subscription.
   * 
   * @param identifier  the identifier, not null
   * @return the trace, not null
   */
  SubscriptionTrace getSubscriptionTrace(String identifier);

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for REST methods that return data from a master (typically queries).  When the method is called a
 * subscription is created for the calling client.  When any data changes in the master the client will be notified.
 * The notification will contain the REST URL used to invoke the method.  This is to allow clients to repeat a query
 * if the data it returned <em>might</em> be stale.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscribeMaster {

  /**
   * @return The masters whose data is returned by the annotated method.
   */
  MasterType[] value();
}

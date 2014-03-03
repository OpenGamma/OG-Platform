/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.service;

/**
 * A thread-local holder for a {@code ServiceContext} accessible across the system.
 */
public final class ThreadLocalServiceContext {

  /**
   * The thread-local serice context.
   */
  private static ThreadLocal<ServiceContext> s_instance = new InheritableThreadLocal<>();

  //-------------------------------------------------------------------------
  /**
   * Sets the service context applicable to this thread.
   * 
   * @param serviceContext  the context, may be null
   */
  public static void init(ServiceContext serviceContext) {
    s_instance.set(serviceContext);
  }

  /**
   * Gets the service context applicable to this thread.
   * 
   * @return the context, null if not initialized
   */
  public static ServiceContext getInstance() {
    return s_instance.get();
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   */
  private ThreadLocalServiceContext() {
  }

}

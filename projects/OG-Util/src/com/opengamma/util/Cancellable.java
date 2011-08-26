/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Defines an object that may be cancelled.
 */
public interface Cancellable {

  /**
   * Attempts to cancel the operation.
   * 
   * @param mayInterruptIfRunning  true if the thread can be interrupted, false if tasks should be allowed to complete naturally
   * @return true if the operation was canceled
   */
  boolean cancel(boolean mayInterruptIfRunning);

}

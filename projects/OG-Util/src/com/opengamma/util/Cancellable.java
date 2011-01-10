/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * If implemented allows something to be canceled.
 */
public interface Cancellable {

  /**
   * Attempts to cancel the operation.
   * 
   * @param mayInterruptIfRunning {@code true} if the thread can be interrupted, {@code false} if tasks should be allowed to complete naturally
   * @return {@code true} if the operation was canceled, {@code false} otherwise
   */
  boolean cancel(boolean mayInterruptIfRunning);

}

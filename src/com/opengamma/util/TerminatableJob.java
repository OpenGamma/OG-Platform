/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The base class for any system which runs through a job, checking to
 * see whether it has been terminated before each cycle.
 *
 * @author kirk
 */
public abstract class TerminatableJob implements Runnable {
  private final AtomicBoolean _terminated = new AtomicBoolean(false);

  @Override
  public void run() {
    preStart();
    _terminated.set(false);
    while(!_terminated.get()) {
      runOneCycle();
    }
  }

  /**
   * Will be invoked by {@link #run()} immediately before the cycle.
   */
  protected void preStart() {
  }
  
  protected abstract void runOneCycle();

  /**
   * When invoked, will terminate the job after the current cycle
   * completes.
   */
  public void terminate() {
    _terminated.set(true);
  }

}

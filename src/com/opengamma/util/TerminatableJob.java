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
    // REVIEW kirk 2009-10-21 -- Originally we had the following line
    // uncommented out so that you could continually restart the job after terminating.
    // However, in cases where you call start() on the owning thread, and then call
    // job.terminate() BEFORE the thread even starts, this line will prevent the
    // termination from ever being registered. Therefore, I'm changing this for
    // the time being and if we need to support restarts, we'll have to find another
    // mechanism for that.
    //_terminated.set(false);
    while(!_terminated.get()) {
      runOneCycle();
    }
    postRunCycle();
  }

  /**
   * Will be invoked by {@link #run()} immediately before the cycle.
   */
  protected void preStart() {
  }
  
  protected void postRunCycle() {
  }
  
  protected abstract void runOneCycle();

  /**
   * When invoked, will terminate the job after the current cycle
   * completes.
   */
  public void terminate() {
    _terminated.set(true);
  }
  
  /**
   * Return {@code true} iff this job is terminated. 
   */
  public boolean isTerminated() {
    return _terminated.get();
  }

}

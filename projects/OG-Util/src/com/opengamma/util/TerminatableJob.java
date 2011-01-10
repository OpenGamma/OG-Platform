/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * The base class for any system which runs through a job, checking to
 * see whether it has been terminated before each cycle.
 */
public abstract class TerminatableJob implements Runnable {

  /**
   * A flag indicating if the job has terminated.
   */
  private volatile boolean _terminated;

  /**
   * Implements {@code Runnable} to add termination support.
   * To add behavior, use the methods {@link #preStart()}, {@link #runOneCycle()}
   * and {@link #postRunCycle()}.
   */
  @Override
  public final void run() {
    preStart();
    // REVIEW kirk 2009-10-21 -- Originally we had the following line
    // uncommented out so that you could continually restart the job after terminating.
    // However, in cases where you call start() on the owning thread, and then call
    // job.terminate() BEFORE the thread even starts, this line will prevent the
    // termination from ever being registered. Therefore, I'm changing this for
    // the time being and if we need to support restarts, we'll have to find another
    // mechanism for that.
    //_terminated.set(false);
    while (_terminated == false) {
      runOneCycle();
    }
    postRunCycle();
  }

  /**
   * Invoked by {@link #run()} once, immediately before the cycle starts.
   */
  protected void preStart() {
  }

  /**
   * Invoked by {@link #run()} to perform one cycle of the job.
   * Override this to implement actual behavior.
   */
  protected abstract void runOneCycle();

  /**
   * Invoked by {@link #run()} once, after the job is terminated and the last cycle completes.
   */
  protected void postRunCycle() {
  }

  //-------------------------------------------------------------------------
  /**
   * Terminates the job after the current cycle completes.
   */
  public void terminate() {
    _terminated = true;
  }

  /**
   * Checks if the job has been terminated.
   * @return true if terminated
   */
  public boolean isTerminated() {
    return _terminated;
  }

}

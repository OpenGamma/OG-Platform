/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for any job which consists of cycles of work, and may be terminated between any two cycles.
 * This implements {@link Runnable} so that it may be executed in its own thread.
 */
public abstract class TerminatableJob implements Runnable {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(TerminatableJob.class);

  /**
   * A flag to indicate whether the job has been started
   */
  private AtomicBoolean _started = new AtomicBoolean(false);
  /**
   * A flag to indicate whether the job has terminated.
   */
  private volatile boolean _terminated;

  /**
   * Implements {@code Runnable} to add termination support.
   * To add behaviour, use the methods {@link #preStart()}, {@link #runOneCycle()} and {@link #postRunCycle()}.
   */
  @Override
  public final void run() {
    if (_started.getAndSet(true)) {
      throw new IllegalStateException("Job has already been run or is currently running");
    }
    preStart();
    try {
      while (!isTerminated()) {
        runOneCycle();
      }
    } catch (Exception e) {
      s_logger.warn("Job " + this + " terminated with unhandled exception", e);
    } finally {
      // Want to ensure that even if the job terminates with an exception (e.g. InterruptedException), the tidy-up
      // semantics of postRunCycle are preserved
      if (isTerminated()) {
        postRunCycle();
      }
    }
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
   * Gets whether the job has been started.
   * 
   * @return true if the job has been started
   */
  public boolean isStarted() {
    return _started.get();
  }

  /**
   * Gets whether the job has been terminated.
   * 
   * @return true if the job has been terminated
   */
  public boolean isTerminated() {
    return _terminated;
  }

}

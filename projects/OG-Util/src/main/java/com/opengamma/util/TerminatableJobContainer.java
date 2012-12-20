/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Holds a number of {@link TerminatableJob} instances, controls their threads,
 * and allows batch lifecycle operations.
 */
public class TerminatableJobContainer {

  /**
   * The queue of active jobs.
   */
  private final Queue<TerminatableJob> _activeJobs = new LinkedBlockingQueue<TerminatableJob>();

  /**
   * Adds a job to the queue.
   * @param job  the job to add, not null
   */
  public void addJob(TerminatableJob job) {
    ArgumentChecker.notNull(job, "job");
    _activeJobs.add(job);
  }

  /**
   * Adds a job to the queue and start a thread.
   * @param job  the job to add, not null
   * @param threadName  the thread name
   */
  public void addJobAndStartThread(TerminatableJob job, String threadName) {
    ArgumentChecker.notNull(job, "job");
    Thread t = new Thread(job, threadName);
    t.setDaemon(true);
    _activeJobs.add(job);
    t.start();
  }

  /**
   * Cleans up all terminated instances.
   * This removes the job from the queue.
   */
  public void cleanupTerminatedInstances() {
    Iterator<TerminatableJob> jobIter = _activeJobs.iterator();
    while (jobIter.hasNext()) {
      TerminatableJob job = jobIter.next();
      if (job.isTerminated()) {
        jobIter.remove();
      }
    }
  }

  /**
   * Terminates all instances that are active.
   */
  public void terminateAll() {
    TerminatableJob job = _activeJobs.poll();
    while (job != null) {
      job.terminate();
      job = _activeJobs.poll();
    }
  }

}

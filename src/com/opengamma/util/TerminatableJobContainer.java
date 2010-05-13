/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
 *
 */
public class TerminatableJobContainer {
  private final Queue<TerminatableJob> _activeJobs = new LinkedBlockingQueue<TerminatableJob>();
  
  public void addJob(TerminatableJob job) {
    ArgumentChecker.notNull(job, "Job");
    _activeJobs.add(job);
  }
  
  public void addJobAndStartThread(TerminatableJob job, String threadName) {
    ArgumentChecker.notNull(job, "Job");
    Thread t = new Thread(job, threadName);
    t.setDaemon(true);
    _activeJobs.add(job);
    t.start();
  }
  
  public void cleanupTerminatedInstances() {
    Iterator<TerminatableJob> jobIter = _activeJobs.iterator();
    while(jobIter.hasNext()) {
      TerminatableJob job = jobIter.next();
      if(job.isTerminated()) {
        jobIter.remove();
      }
    }
  }
  
  public void terminateAll() {
    TerminatableJob job = _activeJobs.poll();
    while (job != null) {
      job.terminate();
      job = _activeJobs.poll();
    }
  }

}

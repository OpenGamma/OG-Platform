/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 
 *
 * @author kirk
 */
public class LinkedBlockingCompletionQueue implements JobCompletionNotifier,
    JobCompletionRetriever {
  private final LinkedBlockingQueue<CalculationJobSpecification> _queue =
    new LinkedBlockingQueue<CalculationJobSpecification>();

  @Override
  public void jobCompleted(CalculationJobSpecification jobSpecification) {
    _queue.add(jobSpecification);
  }

  @Override
  public CalculationJobSpecification getNextCompleted(long timeout,
      TimeUnit unit) {
    try {
      return _queue.poll(timeout, unit);
    } catch (InterruptedException e) {
      Thread.interrupted();
      // REVIEW kirk 2009-09-25 -- Something better?
      return null;
    }
  }

  @Override
  public CalculationJobSpecification getNextCompletedNoWait() {
    return _queue.poll();
  }

}

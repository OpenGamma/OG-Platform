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
  private final LinkedBlockingQueue<CalculationJobResult> _queue =
    new LinkedBlockingQueue<CalculationJobResult>();

  @Override
  public void jobCompleted(CalculationJobResult jobResult) {
    _queue.add(jobResult);
  }

  @Override
  public CalculationJobResult getNextCompleted(long timeout,
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
  public CalculationJobResult getNextCompletedNoWait() {
    return _queue.poll();
  }

}

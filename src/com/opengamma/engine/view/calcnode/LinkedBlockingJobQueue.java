/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link CalculationNodeJobSink} and
 * {@link CalculationNodeJobSource} backed by a
 * {@link LinkedBlockingQueue}.
 *
 * @author kirk
 */
public class LinkedBlockingJobQueue implements CalculationNodeJobSink,
    CalculationNodeJobSource {
  private final LinkedBlockingQueue<CalculationJob> _queue =
    new LinkedBlockingQueue<CalculationJob>();

  @Override
  public void invoke(CalculationJob job) {
    _queue.add(job);
  }

  @Override
  public CalculationJob getJob(long time, TimeUnit unit) {
    try {
      return _queue.poll(time, unit);
    } catch (InterruptedException e) {
      Thread.interrupted();
      // REVIEW kirk 2009-09-25 -- Do something better?
      return null;
    }
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.TerminatableJob;

/**
 * Consumes the results produced on completion of calculation jobs until all jobs have been executed.
 * <p>
 * The results are passed in batches to a {@link SingleComputationCycle} for processing.
 */
public class CalculationJobResultStreamConsumer extends TerminatableJob {
  
  private static final Logger s_logger = LoggerFactory.getLogger(CalculationJobResultStreamConsumer.class);
  
  private static final ExecutionResult POISON = new ExecutionResult(null, null);
  
  private final BlockingQueue<ExecutionResult> _resultQueue;
  private final SingleComputationCycle _computationCycle;
  
  public CalculationJobResultStreamConsumer(BlockingQueue<ExecutionResult> resultQueue, SingleComputationCycle computationCycle) {
    _resultQueue = resultQueue;
    _computationCycle = computationCycle;
  }
  
  @Override
  protected void runOneCycle() {
    try {
      List<ExecutionResult> results = new LinkedList<ExecutionResult>();
      
      // Block until at least one item is added, then drain any further items
      results.add(_resultQueue.take());
      _resultQueue.drainTo(results);
      
      boolean poisoned = false;
      Iterator<ExecutionResult> it = results.iterator();
      while (it.hasNext()) {
        final ExecutionResult result = it.next();
        if (result == POISON) {
          poisoned = true;
          it.remove();
        }
      }
      if (!results.isEmpty()) {
        _computationCycle.calculationJobsCompleted(results);
      }
      if (poisoned) {
        super.terminate();
      }
    } catch (InterruptedException e) {
      s_logger.debug("Interrupted while waiting for computation job results");
      Thread.interrupted();
      terminate();
    }
  }

  public void terminate() {
    try {
      // Poison the queue
      _resultQueue.put(POISON);
    } catch (InterruptedException e) {
      super.terminate();
    }
  }
  
}

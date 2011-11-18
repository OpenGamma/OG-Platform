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

import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.util.TerminatableJob;

/**
 * 
 */
public class CalculationJobResultStreamConsumer extends TerminatableJob {
  
  private static final Logger s_logger = LoggerFactory.getLogger(CalculationJobResultStreamConsumer.class);
  
  private static final CalculationJobResult POISON = new CalculationJobResult(
      new CalculationJobSpecification(null, null, null, 0), 0, new LinkedList<CalculationJobResultItem>(), "");
  
  private final BlockingQueue<CalculationJobResult> _resultQueue;
  private final SingleComputationCycle _computationCycle;
  
  public CalculationJobResultStreamConsumer(BlockingQueue<CalculationJobResult> resultQueue, SingleComputationCycle computationCycle) {
    _resultQueue = resultQueue;
    _computationCycle = computationCycle;
  }
  
  @Override
  protected void runOneCycle() {
    try {
      List<CalculationJobResult> results = new LinkedList<CalculationJobResult>();
      
      // Block until at least one item is added, then drain any further items
      results.add(_resultQueue.take());
      _resultQueue.drainTo(results);
      
      boolean poisoned = false;
      Iterator<CalculationJobResult> it = results.iterator();
      while (it.hasNext()) {
        CalculationJobResult result = it.next();
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

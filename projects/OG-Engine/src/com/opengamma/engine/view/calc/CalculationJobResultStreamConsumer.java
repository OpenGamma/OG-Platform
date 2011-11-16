/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.util.TerminatableJob;

/**
 * 
 */
public class CalculationJobResultStreamConsumer extends TerminatableJob {
  
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
      CalculationJobResult jobResult = _resultQueue.take();
      if (jobResult == POISON) {
        super.terminate();
      } else {
        _computationCycle.calculationJobCompleted(jobResult);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
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

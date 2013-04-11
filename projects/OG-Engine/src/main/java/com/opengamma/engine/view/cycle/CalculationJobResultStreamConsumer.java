/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.exec.ExecutionResult;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
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
      final InMemoryViewComputationResultModel fragmentResultModel = _computationCycle.constructTemplateResultModel();
      final InMemoryViewComputationResultModel fullResultModel = _computationCycle.getResultModel();
      // Block until at least one item is added, then drain any further items
      ExecutionResult result = _resultQueue.take();
      do {
        if (result == POISON) {
          super.terminate();
        } else {
          _computationCycle.processExecutionResult(result, fragmentResultModel, fullResultModel);
        }
        result = _resultQueue.poll();
      } while (result != null);
      if (!fragmentResultModel.isEmpty()) {
        _computationCycle.notifyFragmentCompleted(fragmentResultModel);
      }
    } catch (InterruptedException e) {
      s_logger.debug("Interrupted while waiting for computation job results");
      Thread.interrupted();
      terminate();
    }
  }

  @Override
  public void terminate() {
    try {
      // Poison the queue
      _resultQueue.put(POISON);
    } catch (InterruptedException e) {
      super.terminate();
    }
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.util.ArgumentChecker;

/**
 * When the "run as fast as possible" flag is set for a view, the sequence is partitioned into chunks and these farmed to delegate workers allowing concurrent execution.
 * <p>
 * For example in the case of a historical simulation, requesting an evaluation on each day for a year might run faster overall if we do each month in parallel. Within each of those twelve jobs the
 * successive days allow for delta operations.
 */
public abstract class SequencePartitioningViewProcessWorkerFactory implements ViewProcessWorkerFactory {

  private final ViewProcessWorkerFactory _delegate;

  public SequencePartitioningViewProcessWorkerFactory(final ViewProcessWorkerFactory delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  protected ViewProcessWorkerFactory getDelegate() {
    return _delegate;
  }

  /**
   * Estimate the saturation level for the execution environment. This is the number of workers that we should run in parallel at any time. If the total number of cycles is known then we should spawn
   * this many workers, each with a fair subset of the total cycles.
   * 
   * @param context the context as passed to {@link #createWorker}
   * @param executionOptions the options as passed to {@link #createWorker}
   * @param viewDefinition the view as passed to {@link #createWorker}
   * @return the estimate
   */
  protected abstract int estimateSaturation(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition);

  /**
   * Estimate the minimum number of cycles to execute in a worker batch. This should be the smallest number such that running two, or more, workers with subsets of the cycles will be slower overall
   * than running one with all of them.
   * 
   * @param context the context as passed to {@link #createWorker}
   * @param executionOptions the options as passed to {@link #createWorker}
   * @param viewDefinition the view as passed to {@link #createWorker}
   * @return the estimate
   */
  protected abstract int estimateMinimumCycles(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition);

  /**
   * Estimate the maximum number of cycles to execute in a worker batch. This should be decided based on a reasonable throughput to have each worker complete within a shortish time so that timeouts
   * may be used to detect crashed/failed remote workers.
   * 
   * @param context the context as passed to {@link #createWorker}
   * @param executionOptions the options as passed to {@link #createWorker}
   * @param viewDefinition the view as passed to {@link #createWorker}
   * @return the estimate
   */
  protected abstract int estimateMaximumCycles(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition);

  @Override
  public ViewProcessWorker createWorker(ViewProcessWorkerContext context, ViewExecutionOptions executionOptions, ViewDefinition viewDefinition) {
    if (!executionOptions.getFlags().contains(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE)) {
      return getDelegate().createWorker(context, executionOptions, viewDefinition);
    }
    final int sequenceLength = executionOptions.getExecutionSequence().estimateRemaining();
    final int saturation = estimateSaturation(context, executionOptions, viewDefinition);
    final int minimumPartition = estimateMinimumCycles(context, executionOptions, viewDefinition);
    final int maximumPartition = estimateMaximumCycles(context, executionOptions, viewDefinition);
    if (sequenceLength <= minimumPartition) {
      // No point in splitting
      return getDelegate().createWorker(context, executionOptions, viewDefinition);
    }
    int partition = sequenceLength / saturation;
    if (partition > maximumPartition) {
      partition = maximumPartition;
    }
    if (partition < minimumPartition) {
      partition = minimumPartition;
    }
    return new SequencePartitioningViewProcessWorker(getDelegate(), context, executionOptions, viewDefinition, partition, saturation);
  }
}

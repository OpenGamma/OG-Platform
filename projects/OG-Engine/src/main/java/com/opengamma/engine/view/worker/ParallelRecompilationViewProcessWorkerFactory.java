/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.EnumSet;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.util.ArgumentChecker;

/**
 * When the "parallel recompilation" flags are set, work is distributed between two delegate workers. The primary worker executes cycles without considering recompilation changes. When a change is
 * detected, a secondary worker begins the recompilation. When compilation is complete, the primary worker is terminated and the secondary worker promoted to a primary role.
 * <p>
 * For example in the case of presenting live market data, this can be used to avoid the interface stalling during configuration changes.
 */
public class ParallelRecompilationViewProcessWorkerFactory implements ViewProcessWorkerFactory {

  private final ViewProcessWorkerFactory _passThrough;
  private ViewProcessWorkerFactory _delegate;

  public ParallelRecompilationViewProcessWorkerFactory(final ViewProcessWorkerFactory passThrough) {
    ArgumentChecker.notNull(passThrough, "passThrough");
    _passThrough = passThrough;
    _delegate = passThrough;
  }

  public ViewProcessWorkerFactory getPassThrough() {
    return _passThrough;
  }

  public ViewProcessWorkerFactory getDelegate() {
    return _delegate;
  }

  public void setDelegate(final ViewProcessWorkerFactory delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  protected ViewExecutionOptions getPrimaryExecutionOptions(final ViewExecutionOptions executionOptions) {
    final EnumSet<ViewExecutionFlags> flags = EnumSet.copyOf(executionOptions.getFlags());
    flags.remove(ViewExecutionFlags.PARALLEL_RECOMPILATION_AND_EXECUTION);
    flags.remove(ViewExecutionFlags.PARALLEL_RECOMPILATION_DEFERRED_EXECUTION);
    flags.remove(ViewExecutionFlags.PARALLEL_RECOMPILATION_IMMEDIATE_EXECUTION);
    flags.add(ViewExecutionFlags.IGNORE_COMPILATION_VALIDITY);
    return new ExecutionOptions(executionOptions.getExecutionSequence(), flags, executionOptions.getMaxSuccessiveDeltaCycles(), executionOptions.getDefaultExecutionOptions());
  }

  protected ViewExecutionOptions getSecondaryExecutionOptions(final ViewExecutionOptions executionOptions) {
    final EnumSet<ViewExecutionFlags> flags = EnumSet.copyOf(executionOptions.getFlags());
    if (!flags.remove(ViewExecutionFlags.WAIT_FOR_INITIAL_TRIGGER)) {
      return executionOptions;
    }
    return new ExecutionOptions(executionOptions.getExecutionSequence(), flags, executionOptions.getMaxSuccessiveDeltaCycles(), executionOptions.getDefaultExecutionOptions());
  }

  protected ParallelRecompilationViewProcessWorker createWorkerImpl(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    return new ParallelRecompilationViewProcessWorker(getDelegate(), context, getSecondaryExecutionOptions(executionOptions), viewDefinition);
  }

  protected ViewProcessWorker parallelExecution(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    final ViewExecutionOptions primaryOptions = getPrimaryExecutionOptions(executionOptions);
    final ParallelRecompilationViewProcessWorker worker = createWorkerImpl(context, primaryOptions, viewDefinition);
    worker.startParallel(primaryOptions);
    return worker;
  }

  protected ViewProcessWorker deferredExecution(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    final ViewExecutionOptions primaryOptions = getPrimaryExecutionOptions(executionOptions);
    final ParallelRecompilationViewProcessWorker worker = createWorkerImpl(context, primaryOptions, viewDefinition);
    worker.startDeferred(primaryOptions);
    return worker;
  }

  protected ViewProcessWorker immediateExecution(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    final ViewExecutionOptions primaryOptions = getPrimaryExecutionOptions(executionOptions);
    final ParallelRecompilationViewProcessWorker worker = createWorkerImpl(context, primaryOptions, viewDefinition);
    worker.startImmediate(primaryOptions);
    return worker;
  }

  // ViewProcessWorkerFactory

  @Override
  public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    if (!executionOptions.getFlags().contains(ViewExecutionFlags.IGNORE_COMPILATION_VALIDITY)) {
      if (executionOptions.getFlags().contains(ViewExecutionFlags.PARALLEL_RECOMPILATION_AND_EXECUTION)) {
        return parallelExecution(context, executionOptions, viewDefinition);
      }
      if (executionOptions.getFlags().contains(ViewExecutionFlags.PARALLEL_RECOMPILATION_DEFERRED_EXECUTION)) {
        return deferredExecution(context, executionOptions, viewDefinition);
      }
      if (executionOptions.getFlags().contains(ViewExecutionFlags.PARALLEL_RECOMPILATION_IMMEDIATE_EXECUTION)) {
        return immediateExecution(context, executionOptions, viewDefinition);
      }
    }
    return getPassThrough().createWorker(context, executionOptions, viewDefinition);
  }
}

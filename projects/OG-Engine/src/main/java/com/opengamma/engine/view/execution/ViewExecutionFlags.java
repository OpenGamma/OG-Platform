/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.PublicAPI;

/**
 * Represents options which can apply to a {@link ViewExecutionOptions} instance. These are not necessarily mutually compatible; incorrect combinations may result in execution errors or unexpected
 * behavior.
 */
@PublicAPI
public enum ViewExecutionFlags {

  /**
   * Indicates that all market data should be present before a cycle is allowed to run.
   */
  AWAIT_MARKET_DATA,

  /**
   * Indicates that a computation cycle should be triggered whenever market data inputs change.
   */
  TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED,

  /**
   * Indicates that a computation cycle should be triggered after a certain time period has elapsed since the last cycle, as configured in the {@link ViewDefinition}.
   */
  TRIGGER_CYCLE_ON_TIME_ELAPSED,

  /**
   * Indicates that the execution sequence should proceed as fast as possible, ignoring any minimum elapsed time between cycles specified in the view definition, and possibly executing cycles
   * concurrently.
   */
  RUN_AS_FAST_AS_POSSIBLE,

  /**
   * Indicates that the execution sequence should wait for an initial trigger; e.g. market data changes (if {@link #TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED} is set), a time elapse (if
   * {@link #TRIGGER_CYCLE_ON_TIME_ELAPSED} is set), or a manual trigger.
   */
  WAIT_FOR_INITIAL_TRIGGER,

  /**
   * Indicates that the view definition should be compiled but not executed. This can be used for example to identify the market data requirements of a view, or to produce estimates of the execution
   * costs for load balancing decisions.
   */
  COMPILE_ONLY,

  /**
   * Indicates that the market data inputs to each cycle should be prepared, but no functions should execute. This can be used for example to capture snapshots of data that would be required for graph
   * execution at a later date.
   */
  FETCH_MARKET_DATA_ONLY,
  
  /**
   * Indicates whether a cycle should be skipped when market data is expected but entirely missing. This is useful in historical runs to skip over weekends and holidays which may occur in the
   * execution sequence. If market data is partially present then the cycle will continue as normal.
   */
  SKIP_CYCLE_ON_NO_MARKET_DATA,

  /**
   * Indicates whether changes to a view definition, portfolio, or any other data that would invalidate a compilation, should be ignored.
   */
  IGNORE_COMPILATION_VALIDITY,

  /**
   * Indicates whether view cycles should continue with a previous view compilation until the new compilation has occurred. The new compilation will occur in parallel to execution of the previous
   * compilation result. Execution will begin as soon as possible. The execution of the previous compilation result will be allowed to continue until the first result is available from the new
   * compilation.
   * <p>
   * Normal operation is to cease execution until everything has been recompiled - this mode of operation is to support classes of view client that can tolerate receiving data for a potentially
   * out-of-date configuration. This flag can give the observed behavior of the a process being slow to respond to change notifications but then acting very quickly on them.
   * <p>
   * This can't be used with {@link #PARALLEL_RECOMPILATION_DEFERRED_EXECUTION} or {@link #PARALLEL_RECOMPILATION_IMMEDIATE_EXECUTION} and uses the most resources of the three modes. If there are
   * sufficient resources such that the previous and new computation can be executed concurrently, it may present results for the new compilation sooner than the other modes. If there are not
   * sufficient resources, it may present results for the new compilation later than the other modes.
   * <p>
   * This flag has no effect if {@link #IGNORE_COMPILATION_VALIDITY} is set.
   */
  PARALLEL_RECOMPILATION_AND_EXECUTION,

  /**
   * Indicates whether view cycles should continue with a previous view compilation until the new compilation has occurred. The new compilation will occur in parallel to execution of the previous
   * compilation result. If a previous compilation is already executing, it will be allowed to complete before execution of the new compilation begins.
   * <p>
   * Normal operation is to cease execution until everything has been recompiled - this mode of operation is to support classes of view client that can tolerate receiving data for a potentially
   * out-of-date configuration. This flag can give the observed behavior of the a process being slow to respond to change notifications but then acting very quickly on them.
   * <p>
   * This can't be used with {@link #PARALLEL_RECOMPILATION_AND_EXECUTION} or {@link #PARALLEL_RECOMPILATION_IMMEDIATE_EXECUTION} and uses the least resources of the three modes. This will typically
   * present results for the new compilation later than the {@code PARALLEL_RECOMPILATION_IMMEDIATE_EXECUTION} mode.
   * <p>
   * This flag has no effect if {@link #IGNORE_COMPILATION_VALIDITY} is set.
   */
  PARALLEL_RECOMPILATION_DEFERRED_EXECUTION,

  /**
   * Indicates whether view cycles should continue with a previous view compilation until the new compilation has occurred. The new compilation will occur in parallel to execution of the previous
   * compilation result. If a previous compilation is already executing it will be aborted so that resources are available for the new execution.
   * <p>
   * Normal operation is to cease execution until everything has been recompiled - this mode of operation is to support classes of view client that can tolerate receiving data for a potentially
   * out-of-date configuration. This flag can give the observed behavior of the a process being slow to respond to change notifications but then acting very quickly on them.
   * <p>
   * This can't be used with {@link #PARALLEL_RECOMPILATION_AND_EXECUTION} or {@link #PARALLEL_RECOMPILATION_DEFERRED_EXECUTION}. This will typically present results for the new compilation sooner
   * than the {@code PARALLEL_RECOMPILATION_DEFERRED_EXECUTION} mode but may consume more resources depending on the cost of (and ability of other system components) aborting the previous execution.
   * <p>
   * This flag has no effect if {@link #IGNORE_COMPILATION_VALIDITY} is set.
   */
  PARALLEL_RECOMPILATION_IMMEDIATE_EXECUTION,

  /**
   * Indicates that the results should be stored in batch database.
   */
  BATCH

}

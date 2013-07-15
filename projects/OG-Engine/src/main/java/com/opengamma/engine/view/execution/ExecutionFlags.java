/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.EnumSet;

import com.opengamma.util.PublicAPI;

/**
 * An execution options flag builder for {@link ViewExecutionOptions}.
 */
@PublicAPI
public final class ExecutionFlags {

  private final EnumSet<ViewExecutionFlags> _flags;

  private ExecutionFlags() {
    _flags = EnumSet.noneOf(ViewExecutionFlags.class);
  }

  private ExecutionFlags(ViewExecutionFlags flag) {
    _flags = EnumSet.<ViewExecutionFlags>of(flag);
  }

  //-------------------------------------------------------------------------

  /**
   * Adds {@link ViewExecutionFlags#AWAIT_MARKET_DATA}.
   * 
   * @return this
   */
  public ExecutionFlags awaitMarketData() {
    _flags.add(ViewExecutionFlags.AWAIT_MARKET_DATA);
    return this;
  }

  /**
   * Adds {@link ViewExecutionFlags#TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED}.
   * 
   * @return this
   */
  public ExecutionFlags triggerOnMarketData() {
    _flags.add(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED);
    return this;
  }

  /**
   * Adds {@link ViewExecutionFlags#TRIGGER_CYCLE_ON_TIME_ELAPSED}.
   * 
   * @return this
   */
  public ExecutionFlags triggerOnTimeElapsed() {
    _flags.add(ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED);
    return this;
  }

  /**
   * Adds {@link ViewExecutionFlags#RUN_AS_FAST_AS_POSSIBLE}.
   * 
   * @return this
   */
  public ExecutionFlags runAsFastAsPossible() {
    _flags.add(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE);
    return this;
  }

  /**
   * Adds {@link ViewExecutionFlags#BATCH}.
   * 
   * @return this
   */
  public ExecutionFlags batch() {
    _flags.add(ViewExecutionFlags.BATCH);
    return this;
  }

  /**
   * Adds {@link ViewExecutionFlags#COMPILE_ONLY}
   * 
   * @return this
   */
  public ExecutionFlags compileOnly() {
    _flags.add(ViewExecutionFlags.COMPILE_ONLY);
    return this;
  }

  /**
   * Adds {@link ViewExecutionFlags#FETCH_MARKET_DATA_ONLY}
   * 
   * @return this
   */
  public ExecutionFlags fetchMarketDataOnly() {
    _flags.add(ViewExecutionFlags.FETCH_MARKET_DATA_ONLY);
    return this;
  }
  
  /**
   * Adds {@link ViewExecutionFlags#SKIP_CYCLE_ON_NO_MARKET_DATA}
   * 
   * @return this
   */
  public ExecutionFlags skipCycleOnNoMarketData() {
    _flags.add(ViewExecutionFlags.SKIP_CYCLE_ON_NO_MARKET_DATA);
    return this;
  }

  /**
   * Adds {@link ViewExecutionFlags#WAIT_FOR_INITIAL_TRIGGER}
   * 
   * @return this
   */
  public ExecutionFlags waitForInitialTrigger() {
    _flags.add(ViewExecutionFlags.WAIT_FOR_INITIAL_TRIGGER);
    return this;
  }

  /**
   * Adds {@link ViewExecutionFlags#IGNORE_COMPILATION_VALIDITY}
   * 
   * @return this
   */
  public ExecutionFlags ignoreCompilationValidity() {
    _flags.add(ViewExecutionFlags.IGNORE_COMPILATION_VALIDITY);
    return this;
  }

  /**
   * Modes of operation for the {@link #parallelCompilation} flag.
   */
  public static enum ParallelRecompilationMode {

    /**
     * The {@link ViewExecutionFlags#PARALLEL_RECOMPILATION_AND_EXECUTION} flag.
     */
    PARALLEL_EXECUTION(ViewExecutionFlags.PARALLEL_RECOMPILATION_AND_EXECUTION),

    /**
     * The {@link ViewExecutionFlags#PARALLEL_RECOMPILATION_DEFERRED_EXECUTION} flag.
     */
    DEFERRED_EXECUTION(ViewExecutionFlags.PARALLEL_RECOMPILATION_DEFERRED_EXECUTION),

    /**
     * The {@link ViewExecutionFlags#PARALLEL_RECOMPILATION_IMMEDIATE_EXECUTION} flag.
     */
    IMMEDIATE_EXECUTION(ViewExecutionFlags.PARALLEL_RECOMPILATION_IMMEDIATE_EXECUTION);

    private final ViewExecutionFlags _flag;

    private ParallelRecompilationMode(final ViewExecutionFlags flag) {
      _flag = flag;
    }

    private static void remove(final EnumSet<ViewExecutionFlags> flags) {
      flags.remove(PARALLEL_EXECUTION);
      flags.remove(DEFERRED_EXECUTION);
      flags.remove(IMMEDIATE_EXECUTION);
    }

    private void apply(final EnumSet<ViewExecutionFlags> flags) {
      flags.add(_flag);
    }

  }

  public ExecutionFlags parallelCompilation(final ParallelRecompilationMode mode) {
    ParallelRecompilationMode.remove(_flags);
    if (mode != null) {
      mode.apply(_flags);
    }
    return this;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets an {@link EnumSet} corresponding to the flags that have been added.
   * 
   * @return the set of flags, not null
   */
  public EnumSet<ViewExecutionFlags> get() {
    return _flags;
  }

  /**
   * Gets a builder starting with the empty set of flags.
   * 
   * @return a builder starting with the empty set of flags, not null
   */
  public static ExecutionFlags none() {
    return new ExecutionFlags();
  }

  /**
   * Gets a builder starting with all trigger-related flags.
   * 
   * @return a builder starting with all trigger-related flags, not null
   */
  public static ExecutionFlags triggersEnabled() {
    return none().triggerOnMarketData().triggerOnTimeElapsed();
  }

}

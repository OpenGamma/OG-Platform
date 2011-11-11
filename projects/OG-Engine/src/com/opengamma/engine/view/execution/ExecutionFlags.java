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
   * Adds {@link ViewExecutionFlags#COMPILE_ONLY}
   * 
   * @return this
   */
  public ExecutionFlags compileOnly() {
    _flags.add(ViewExecutionFlags.COMPILE_ONLY);
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

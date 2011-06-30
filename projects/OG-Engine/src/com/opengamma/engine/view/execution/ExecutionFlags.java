/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.EnumSet;

/**
 * An execution options flag builder for {@link ViewExecutionOptions}.
 */
public final class ExecutionFlags {

  private final EnumSet<ViewExecutionFlags> _flags;
  
  private ExecutionFlags() {
    _flags = EnumSet.noneOf(ViewExecutionFlags.class);
  }
  
  private ExecutionFlags(ViewExecutionFlags flag) {
    _flags = EnumSet.<ViewExecutionFlags>of(flag);
  }
  
  //-------------------------------------------------------------------------
  
  public ExecutionFlags triggerOnLiveData() {
    _flags.add(ViewExecutionFlags.TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED);
    return this;
  }
  
  public ExecutionFlags triggerOnTimeElapsed() {
    _flags.add(ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED);
    return this;
  }
  
  public ExecutionFlags runAsFastAsPossible() {
    _flags.add(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE);
    return this;
  }
  
  public ExecutionFlags compileOnly() {
    _flags.add(ViewExecutionFlags.COMPILE_ONLY);
    return this;
  }
  
  //-------------------------------------------------------------------------
  
  public EnumSet<ViewExecutionFlags> get() {
    return _flags;
  }
  
  public static ExecutionFlags none() {
    return new ExecutionFlags();
  }
  
  public static ExecutionFlags triggersEnabled() {
    return none().triggerOnLiveData().triggerOnTimeElapsed();
  }
  
}

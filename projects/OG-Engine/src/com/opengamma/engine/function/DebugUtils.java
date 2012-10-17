/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;

/**
 * Debugging/profiling utilities for identifying bottlenecks in dependency graph function resolution.
 */
public final class DebugUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(DebugUtils.class);

  private static final class Trap {

    private final String _label;
    private final AtomicLong _calls = new AtomicLong();
    private final AtomicLong _time = new AtomicLong();

    public Trap(final String label) {
      _label = label;
    }

    public void enter() {
      _calls.incrementAndGet();
      _time.addAndGet(-System.nanoTime());
    }

    public void leave() {
      _time.addAndGet(System.nanoTime());
    }

    public String toString() {
      return _label + " " + _calls + " call(s) in " + (_time.doubleValue() / 1e6) + "ms";
    }

    public void log() {
      s_logger.info("Trapped {}", this);
    }

  }

  private static final Trap s_canApplyTo = new Trap("canApplyTo");
  private static final Trap s_getResults1 = new Trap("getResults.1");
  private static final Trap s_getRequirements = new Trap("getRequirements");
  private static final Trap s_getResults2 = new Trap("getResults.2");
  private static final Trap s_getAdditionalRequirements = new Trap("getAdditionalRequirements");

  private DebugUtils() {
  }

  public static void logTraps() {
    s_canApplyTo.log();
    s_getResults1.log();
    s_getRequirements.log();
    s_getResults2.log();
    s_getAdditionalRequirements.log();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#canApplyTo}.
   */
  public static void canApplyTo_enter() { //CSIGNORE
    s_canApplyTo.enter();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#canApplyTo}.
   */
  public static void canApplyTo_leave() { //CSIGNORE
    s_canApplyTo.leave();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget)}.
   */
  public static void getResults1_enter() { //CSIGNORE
    s_getResults1.enter();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget)}.
   */
  public static void getResults1_leave() { //CSIGNORE
    s_getResults1.leave();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getRequirements}.
   */
  public static void getRequirements_enter() { //CSIGNORE
    s_getRequirements.enter();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getRequirements}.
   */
  public static void getRequirements_leave() { //CSIGNORE
    s_getRequirements.leave();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget,Map)}.
   */
  public static void getResults2_enter() { //CSIGNORE
    s_getResults2.enter();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget,Map)}.
   */
  public static void getResults2_leave() { //CSIGNORE
    s_getResults2.leave();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getAdditionalRequirements}.
   */
  public static void getAdditionalRequirements_enter() { //CSIGNORE
    s_getAdditionalRequirements.enter();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getAdditionalRequirements}.
   */
  public static void getAdditionalRequirements_leave() { //CSIGNORE
    s_getAdditionalRequirements.leave();
  }

}

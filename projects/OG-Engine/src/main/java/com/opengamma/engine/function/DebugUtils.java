/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.util.test.Profiler;

/**
 * Debugging/profiling utilities for identifying bottlenecks in dependency graph function resolution.
 */
public final class DebugUtils {

  private static final Profiler s_canApplyTo = Profiler.create(DebugUtils.class, "canApplyTo");
  private static final Profiler s_getResults1 = Profiler.create(DebugUtils.class, "getResults1");
  private static final Profiler s_getRequirements = Profiler.create(DebugUtils.class, "getRequirements");
  private static final Profiler s_getResults2 = Profiler.create(DebugUtils.class, "getResults2");
  private static final Profiler s_getAdditionalRequirements = Profiler.create(DebugUtils.class, "getAdditionalRequirements");

  private DebugUtils() {
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#canApplyTo}.
   */
  public static void canApplyTo_enter() { //CSIGNORE
    s_canApplyTo.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#canApplyTo}.
   */
  public static void canApplyTo_leave() { //CSIGNORE
    s_canApplyTo.end();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget)}.
   */
  public static void getResults1_enter() { //CSIGNORE
    s_getResults1.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget)}.
   */
  public static void getResults1_leave() { //CSIGNORE
    s_getResults1.end();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getRequirements}.
   */
  public static void getRequirements_enter() { //CSIGNORE
    s_getRequirements.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getRequirements}.
   */
  public static void getRequirements_leave() { //CSIGNORE
    s_getRequirements.end();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget,Map)}.
   */
  public static void getResults2_enter() { //CSIGNORE
    s_getResults2.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget,Map)}.
   */
  public static void getResults2_leave() { //CSIGNORE
    s_getResults2.end();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getAdditionalRequirements}.
   */
  public static void getAdditionalRequirements_enter() { //CSIGNORE
    s_getAdditionalRequirements.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getAdditionalRequirements}.
   */
  public static void getAdditionalRequirements_leave() { //CSIGNORE
    s_getAdditionalRequirements.end();
  }

}

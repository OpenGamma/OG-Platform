/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.InstantProvider;

import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DefaultFunctionResolver implements FunctionResolver {

  private final CompiledFunctionService _functionCompilationService;
  private final FunctionPriority _prioritizer;
  private Set<ResolutionRule> _defaultRules;

  /**
   * 
   */
  public static interface FunctionPriority {

    int getPriority(CompiledFunctionDefinition function);

  }

  public DefaultFunctionResolver(final CompiledFunctionService functionCompilationService) {
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    _functionCompilationService = functionCompilationService;
    _prioritizer = null;
  }

  public DefaultFunctionResolver(final CompiledFunctionService functionCompilationService, final FunctionPriority prioritizer) {
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    ArgumentChecker.notNull(prioritizer, "prioritizer");
    _functionCompilationService = functionCompilationService;
    _prioritizer = prioritizer;
  }

  public void addRule(ResolutionRule rule) {
    addRules(Collections.singleton(rule));
  }

  public void addRules(Collection<ResolutionRule> resolutionRules) {
    if (_defaultRules == null) {
      _defaultRules = new HashSet<ResolutionRule>();
    }
    _defaultRules.addAll(resolutionRules);
  }

  @Override
  public CompiledFunctionResolver compile(InstantProvider atInstant) {
    final DefaultCompiledFunctionResolver result = new DefaultCompiledFunctionResolver(_functionCompilationService.compileFunctionRepository(atInstant), _prioritizer);
    if (_defaultRules != null) {
      result.addRules(_defaultRules);
    }
    return result;
  }

}

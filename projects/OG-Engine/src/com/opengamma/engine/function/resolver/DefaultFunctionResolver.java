/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.InstantProvider;

import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DefaultFunctionResolver implements FunctionResolver {

  private final CompiledFunctionService _functionCompilationService;
  private Set<ResolutionRule> _defaultRules;

  public DefaultFunctionResolver(final CompiledFunctionService functionCompilationService) {
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    _functionCompilationService = functionCompilationService;
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
    final DefaultCompiledFunctionResolver result = new DefaultCompiledFunctionResolver(_functionCompilationService.compileFunctionRepository(atInstant));
    if (_defaultRules != null) {
      result.addRules(_defaultRules);
    }
    return result;
  }

}

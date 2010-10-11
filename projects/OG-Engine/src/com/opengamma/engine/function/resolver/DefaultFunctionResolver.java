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

import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionCompilationService;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.FunctionRepositoryCompiler;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DefaultFunctionResolver implements FunctionResolver {

  private final FunctionRepository _repository;
  private final FunctionRepositoryCompiler _compiler;
  private Set<ResolutionRule> _defaultRules;

  public DefaultFunctionResolver() {
    _repository = null;
    _compiler = null;
  }

  public DefaultFunctionResolver(final FunctionCompilationService functionCompilationService) {
    this(functionCompilationService.getFunctionRepository(), functionCompilationService.getFunctionRepositoryCompiler());
  }

  public DefaultFunctionResolver(final FunctionRepository repository, final FunctionRepositoryCompiler compiler) {
    ArgumentChecker.notNull(repository, "repository");
    ArgumentChecker.notNull(compiler, "compiler");
    _repository = repository;
    _compiler = compiler;
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
  public CompiledFunctionResolver compile(FunctionCompilationContext context, InstantProvider atInstant) {
    final DefaultCompiledFunctionResolver result;
    if (_repository != null) {
      result = new DefaultCompiledFunctionResolver(_compiler.compile(context, _repository, atInstant));
    } else {
      result = new DefaultCompiledFunctionResolver(context);
    }
    if (_defaultRules != null) {
      result.addRules(_defaultRules);
    }
    return result;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Instant;

import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of the function resolver.
 * <p>
 * The aim of the resolution is to find functions that are capable of satisfying a requirement. In addition, a priority mechanism is used to return functions in priority order from highest to lowest.
 * Resolution actually occurs in the {@code CompiledFunctionResolver}. This class creates a {@code DefaultCompiledFunctionResolver} instance.
 * <p>
 * This class is not thread-safe.
 */
public class DefaultFunctionResolver implements FunctionResolver {

  /**
   * The provider of compiled functions.
   */
  private final CompiledFunctionService _functionCompilationService;
  /**
   * The provider of the priority of a function.
   */
  private final FunctionPriority _prioritizer;
  /**
   * The default resolution rules.
   */
  private Set<ResolutionRule> _defaultRules;

  /**
   * Creates an instance.
   * 
   * @param functionCompilationService the provider of compiled functions, not null
   */
  public DefaultFunctionResolver(final CompiledFunctionService functionCompilationService) {
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    _functionCompilationService = functionCompilationService;
    _prioritizer = null;
  }

  /**
   * Creates an instance.
   * 
   * @param functionCompilationService the provider of compiled functions, not null
   * @param prioritizer the provider of the priority of a function, not null
   */
  public DefaultFunctionResolver(final CompiledFunctionService functionCompilationService, final FunctionPriority prioritizer) {
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    ArgumentChecker.notNull(prioritizer, "prioritizer");
    _functionCompilationService = functionCompilationService;
    _prioritizer = prioritizer;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single rule to the resolver.
   * 
   * @param resolutionRule the rule to add, not null
   */
  public void addRule(ResolutionRule resolutionRule) {
    addRules(Collections.singleton(resolutionRule));
  }

  /**
   * Adds rules to the resolver.
   * 
   * @param resolutionRules the rules to add, no nulls, not null
   */
  public void addRules(Collection<ResolutionRule> resolutionRules) {
    if (_defaultRules == null) {
      _defaultRules = new HashSet<ResolutionRule>();
    }
    _defaultRules.addAll(resolutionRules);
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts all the compiled function definitions from the repository and converts them to resolution rules.
   * 
   * @param repository the function repository, not null
   * @return the rules, not null
   */
  protected Collection<ResolutionRule> getRepositoryRules(final CompiledFunctionRepository repository) {
    // REVIEW 2011-07-29 SJC: static method?
    final Collection<CompiledFunctionDefinition> functions = repository.getAllFunctions();
    final Collection<ResolutionRule> result = new ArrayList<ResolutionRule>(functions.size());
    for (CompiledFunctionDefinition compiledFnDefn : repository.getAllFunctions()) {
      if (compiledFnDefn.getTargetType() != null) {
        ParameterizedFunction paramFn = new ParameterizedFunction(compiledFnDefn, compiledFnDefn.getFunctionDefinition().getDefaultParameters());
        result.add(new ResolutionRule(paramFn, ApplyToAllTargets.INSTANCE, getPriority(compiledFnDefn)));
      }
    }
    return result;
  }

  /**
   * Gets the priority of a compiled function definition. This uses the stored priority provider.
   * 
   * @param function the function to examine, not null
   * @return the priority, default zero
   */
  protected int getPriority(final CompiledFunctionDefinition function) {
    return (_prioritizer != null) ? _prioritizer.getPriority(function) : 0;
  }

  //-------------------------------------------------------------------------
  @Override
  public CompiledFunctionResolver compile(final Instant atInstant) {
    final DefaultCompiledFunctionResolver result = new DefaultCompiledFunctionResolver(_functionCompilationService.getFunctionCompilationContext());
    result.addRules(getRepositoryRules(_functionCompilationService.compileFunctionRepository(atInstant)));
    if (_defaultRules != null) {
      result.addRules(_defaultRules);
    }
    result.compileRules();
    return result;
  }

  @Override
  public FunctionDefinition getFunction(final String uniqueId) {
    return _functionCompilationService.getFunctionRepository().getFunction(uniqueId);
  }

}

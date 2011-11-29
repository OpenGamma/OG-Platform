/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Service to interrogate the results available on a computation target.
 */
@PublicAPI
public class ComputationTargetResults {

  private static final Logger s_logger = LoggerFactory.getLogger(ComputationTargetResults.class);

  /**
   * The resolution rules to use, in descending priority order.
   */
  private final List<ResolutionRule> _rules;

  /**
   * The compilation context to use for recursive query. Typically this should be
   * the context this service is part of. On construction, that context is cloned
   * so that the service can be removed from the copy. This prevents functions
   * that are defined in terms of the available outputs of others from being caught
   * in an infinite loop of recursive calls.
   */
  private final FunctionCompilationContext _context;

  private final ComputationTargetResolver _targetResolver;

  /**
   * Creates a new instance.
   * 
   * @param rules the resolution rules to use, not null
   * @param context the containing context, not null
   * @param resolver the computation target resolver, not null
   */
  public ComputationTargetResults(final Collection<ResolutionRule> rules, final FunctionCompilationContext context, final ComputationTargetResolver resolver) {
    ArgumentChecker.notNull(rules, "rules");
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(resolver, "resolver");
    _rules = new ArrayList<ResolutionRule>(rules);
    Collections.sort(_rules, new Comparator<ResolutionRule>() {

      @Override
      public int compare(final ResolutionRule o1, final ResolutionRule o2) {
        if (o1.getPriority() > o2.getPriority()) {
          return -1;
        } else if (o1.getPriority() < o2.getPriority()) {
          return 1;
        } else {
          return 0;
        }
      }

    });
    _context = context.clone();
    _context.setComputationTargetResults(null);
    _targetResolver = resolver;
  }

  /**
   * Gets the list of resolution rules, in descending priority order.
   * 
   * @return the rules, not null
   */
  protected List<ResolutionRule> getRules() {
    return _rules;
  }

  /**
   * Gets the function compilation context (lacking this service).
   * 
   * @return the context, not null
   */
  protected FunctionCompilationContext getContext() {
    return _context;
  }

  /**
   * Gets the target resolver to use for partial result resolution.
   * 
   * @return the target resolver, not null
   */
  protected ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }

  /**
   * Returns the maximal result sets from all functions on the given target. The results
   * are presented in the descending priority order of the rules that produced them.
   * 
   * @param target the target to get results for, not null
   * @return the list of maximal results, not null
   */
  public List<ValueSpecification> getMaximalResults(final ComputationTarget target) {
    final Set<ValueSpecification> result = new LinkedHashSet<ValueSpecification>();
    for (ResolutionRule rule : getRules()) {
      if (rule.getFunction().getFunction().getTargetType() == target.getType()) {
        Set<ValueSpecification> results = rule.getResults(target, getContext());
        if (results != null) {
          result.addAll(results);
        }
      }
    }
    s_logger.info("Maximal results for {} = {}", target, result);
    return new ArrayList<ValueSpecification>(result);
  }

  /**
   * Returns the partially resolved result sets from all functions on the given target.
   * The results are presented in the descending priority order of the rules that produced
   * them.
   * <p>
   * This differs from {@link #getMaximalResults} by following the requirements of any
   * functions that produce non-finite properties on their maximal outputs.
   * 
   * @param target the target to get results for, not null
   * @return the list of partially resolved results, not null
   */
  public List<ValueSpecification> getPartialResults(final ComputationTarget target) {
    final Set<ValueSpecification> result = new LinkedHashSet<ValueSpecification>();
    for (ResolutionRule rule : getRules()) {
      final CompiledFunctionDefinition function = rule.getFunction().getFunction();
      if (function.getTargetType() != target.getType()) {
        continue;
      }
      final Set<ValueSpecification> results;
      try {
        results = rule.getResults(target, getContext());
        if (results == null) {
          continue;
        }
      } catch (Throwable t) {
        s_logger.warn("Couldn't call getResults on {} - {}", rule, t);
        s_logger.debug("Caught exception", t);
        continue;
      }
      //CSOFF
      resultsLoop:
      //CSON
      for (ValueSpecification spec : results) {
        if (!spec.getProperties().getProperties().isEmpty()) {
          result.add(spec);
          continue resultsLoop;
        }
        final ValueSpecification resolvedSpec = resolvePartialSpecification(spec, target, function, new HashSet<ValueRequirement>());
        if (resolvedSpec != null) {
          result.add(resolvedSpec);
        }
      }
    }
    s_logger.info("Maximal results for {} = {}", target, result);
    return new ArrayList<ValueSpecification>(result);
  }

  /**
   * Attempts partial resolution of a requirement. Requirement chains are followed until a
   * specification is found with finite properties.
   * 
   * @param requirement requirement to resolve, not null
   * @param visited requirements visited so far, to detect recursion, not null
   * @return the resolved specification, or null if it couldn't be resolved
   */
  protected ValueSpecification resolvePartialRequirement(final ValueRequirement requirement, final Set<ValueRequirement> visited) {
    if (!visited.add(requirement)) {
      return null;
    }
    final ComputationTarget target = getTargetResolver().resolve(requirement.getTargetSpecification());
    if (target == null) {
      s_logger.debug("Couldn't resolve target for {}", requirement);
      visited.remove(requirement);
      return null;
    }
    for (ResolutionRule rule : getRules()) {
      final CompiledFunctionDefinition function = rule.getFunction().getFunction();
      if (function.getTargetType() != target.getType()) {
        continue;
      }
      final ValueSpecification result;
      try {
        result = rule.getResult(requirement, target, getContext());
        if (result == null) {
          continue;
        }
      } catch (Throwable t) {
        s_logger.warn("Couldn't call getResult on {} - {}", rule, t);
        s_logger.debug("Caught exception", t);
        continue;
      }
      if (!result.getProperties().getProperties().isEmpty()) {
        s_logger.debug("Partial resolution of {} to {}", requirement, result);
        visited.remove(requirement);
        return result;
      }
      final ValueSpecification resolvedResult = resolvePartialSpecification(result, target, function, visited);
      if (resolvedResult != null) {
        s_logger.debug("Partial resolution of {} to {}", requirement, resolvedResult);
        visited.remove(requirement);
        return resolvedResult;
      }
    }
    s_logger.debug("Couldn't resolve {}", requirement);
    visited.remove(requirement);
    return null;
  }

  /**
   * Attempts partial resolution of a non-finite specification produced as part of a function's maximal
   * outputs. Requirement chains are followed until a specification is found with finite properties.
   * 
   * @param specification maximal output specification, non-finite properties, not null
   * @param target computation target the function is to operate on, not null
   * @param function function to apply, not null
   * @param visited requirements visited so far, to detect recursion, not null
   * @return the partially resolved specification, or null if resolution is not possible
   */
  protected ValueSpecification resolvePartialSpecification(final ValueSpecification specification, final ComputationTarget target, final CompiledFunctionDefinition function,
      final Set<ValueRequirement> visited) {
    final Set<ValueRequirement> reqs;
    try {
      reqs = function.getRequirements(getContext(), target, new ValueRequirement(specification.getValueName(), specification.getTargetSpecification(), ValueProperties.none()));
      if (reqs == null) {
        return null;
      }
    } catch (Throwable t) {
      s_logger.warn("Couldn't call getRequirements on {} - {}", function, t);
      s_logger.debug("Caught exception", t);
      return null;
    }
    s_logger.debug("Need partial resolution of {} to continue", reqs);
    final Map<ValueSpecification, ValueRequirement> resolved = Maps.newHashMapWithExpectedSize(reqs.size());
    for (ValueRequirement req : reqs) {
      final ValueSpecification resolvedReq = resolvePartialRequirement(req, visited);
      if (resolvedReq == null) {
        return null;
      }
      resolved.put(resolvedReq, req);
    }
    final Set<ValueSpecification> lateResults;
    try {
      lateResults = function.getResults(getContext(), target, resolved);
      if ((lateResults == null) || lateResults.isEmpty()) {
        return null;
      }
    } catch (Throwable t) {
      s_logger.warn("Couldn't call getResults on {} - {}", function, t);
      s_logger.debug("Caught exception", t);
      return null;
    }
    for (ValueSpecification lateSpec : lateResults) {
      if (!lateSpec.getProperties().getProperties().isEmpty()) {
        s_logger.debug("Deep resolution of {} to {}", specification, lateSpec);
        return lateSpec;
      }
    }
    return null;
  }

}

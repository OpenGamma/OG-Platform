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
import java.util.HashMap;
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
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionCompilationContextAware;
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Service to interrogate the results available on a computation target.
 */
@PublicAPI
public class ComputationTargetResults implements FunctionCompilationContextAware {

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
  private FunctionCompilationContext _context;

  /**
   * Creates a new instance.
   *
   * @param rules the resolution rules to use, not null
   */
  public ComputationTargetResults(final Collection<ResolutionRule> rules) {
    ArgumentChecker.notNull(rules, "rules");
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
  }

  @Override
  public void setFunctionCompilationContext(final FunctionCompilationContext context) {
    if (_context == null) {
      _context = context.clone();
      _context.setComputationTargetResults(null);
    }
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
  protected ComputationTargetResolver.AtVersionCorrection getTargetResolver() {
    return getContext().getComputationTargetResolver();
  }

  /**
   * Gets the specification resolver to use for targets specified by external identifier only.
   *
   * @return the
   */
  protected ComputationTargetSpecificationResolver.AtVersionCorrection getTargetSpecificationResolver() {
    return getContext().getComputationTargetResolver().getSpecificationResolver();
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
    for (final ResolutionRule rule : getRules()) {
      if (rule.getParameterizedFunction().getFunction().getTargetType().isCompatible(target.getType())) {
        final Set<ValueSpecification> results = rule.getResults(target, getContext());
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
    final Map<ComputationTargetType, ComputationTarget> adjustedTargetCache = new HashMap<ComputationTargetType, ComputationTarget>();
    final Set<ValueSpecification> result = new LinkedHashSet<ValueSpecification>();
    for (final ResolutionRule rule : getRules()) {
      final CompiledFunctionDefinition function = rule.getParameterizedFunction().getFunction();
      if (!function.getTargetType().isCompatible(target.getType())) {
        continue;
      }
      final ComputationTarget adjustedTarget = rule.adjustTarget(adjustedTargetCache, target);
      final Set<ValueSpecification> results;
      try {
        results = rule.getResults(adjustedTarget, getContext());
        if (results == null) {
          continue;
        }
      } catch (final Throwable t) {
        s_logger.warn("Couldn't call getResults on {} - {}", rule, t);
        s_logger.debug("Caught exception", t);
        continue;
      }
      //CSOFF
      resultsLoop:
      //CSON
      for (final ValueSpecification spec : results) {
        if (!spec.getProperties().getProperties().isEmpty()) {
          result.add(spec);
          continue resultsLoop;
        }
        final ValueSpecification resolvedSpec = resolvePartialSpecification(spec, adjustedTarget, function, new HashSet<ValueRequirement>(), adjustedTargetCache, ValueProperties.none());
        if (resolvedSpec != null) {
          result.add(resolvedSpec);
        }
      }
    }
    s_logger.info("Maximal results for {} = {}", target, result);
    return new ArrayList<ValueSpecification>(result);
  }

  /**
   * Attempts partial resolution of a requirement. Requirement chains are followed until a specification is found with finite properties.
   *
   * @param requirement requirement to resolve, not null
   * @param visited requirements visited so far, to detect recursion, not null
   * @param adjustedTargetCache cache of adjusted targets, keyed by the target function type, not null
   * @return the resolved specification, or null if it couldn't be resolved
   */
  protected ValueSpecification resolvePartialRequirement(final ValueRequirement requirement, final Set<ValueRequirement> visited,
      final Map<ComputationTargetType, ComputationTarget> adjustedTargetCache) {
    if (!visited.add(requirement)) {
      s_logger.debug("Recursive request for {}", requirement);
      return null;
    }
    final ComputationTargetSpecification targetSpec = getTargetSpecificationResolver().getTargetSpecification(
        ComputationTargetResolverUtils.simplifyType(requirement.getTargetReference(), getTargetResolver()));
    final ComputationTarget target = getTargetResolver().resolve(targetSpec);
    if (target == null) {
      s_logger.debug("Couldn't resolve target for {}", requirement);
      visited.remove(requirement);
      return null;
    }
    s_logger.debug("Partially resolving {}", requirement);
    for (final ResolutionRule rule : getRules()) {
      final CompiledFunctionDefinition function = rule.getParameterizedFunction().getFunction();
      if (!function.getTargetType().isCompatible(target.getType())) {
        continue;
      }
      final ComputationTarget adjustedTarget = rule.adjustTarget(adjustedTargetCache, target);
      final ValueSpecification result;
      try {
        result = rule.getResult(requirement.getValueName(), adjustedTarget, requirement.getConstraints(), getContext());
        if (result == null) {
          continue;
        }
      } catch (final Throwable t) {
        s_logger.warn("Couldn't call getResult on {} - {}", rule, t);
        s_logger.debug("Caught exception", t);
        continue;
      }
      if (!result.getProperties().getProperties().isEmpty()) {
        s_logger.debug("Partial resolution of {} to {}", requirement, result);
        visited.remove(requirement);
        return result;
      }
      final ValueSpecification resolvedResult = resolvePartialSpecification(result, adjustedTarget, function, visited, adjustedTargetCache, requirement.getConstraints());
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
   * Attempts partial resolution of a non-finite specification produced as part of a function's maximal outputs. Requirement chains are followed until a specification is found with finite properties.
   *
   * @param specification maximal output specification, non-finite properties, not null
   * @param target computation target the function is to operate on, not null
   * @param function function to apply, not null
   * @param visited requirements visited so far, to detect recursion, not null
   * @param adjustedTarget cache of adjusted targets, keyed by the target function type, not null
   * @param constraints requirement constraints, not null
   * @return the partially resolved specification, or null if resolution is not possible
   */
  protected ValueSpecification resolvePartialSpecification(final ValueSpecification specification, final ComputationTarget target, final CompiledFunctionDefinition function,
      final Set<ValueRequirement> visited, final Map<ComputationTargetType, ComputationTarget> adjustedTarget, final ValueProperties constraints) {
    final Set<ValueRequirement> reqs;
    try {
      reqs = function.getRequirements(getContext(), target, new ValueRequirement(specification.getValueName(), specification.getTargetSpecification(), constraints));
      if (reqs == null) {
        return null;
      }
    } catch (final Throwable t) {
      s_logger.warn("Couldn't call getRequirements on {} - {}", function, t);
      s_logger.debug("Caught exception", t);
      return null;
    }
    s_logger.debug("Need partial resolution of {} to continue", reqs);
    final Map<ValueSpecification, ValueRequirement> resolved = Maps.newHashMapWithExpectedSize(reqs.size());
    for (final ValueRequirement req : reqs) {
      // TODO: need to call "simplify type" on requirement
      final ValueSpecification resolvedReq = resolvePartialRequirement(req, visited, adjustedTarget);
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
    } catch (final Throwable t) {
      s_logger.warn("Couldn't call getResults on {} - {}", function, t);
      s_logger.debug("Caught exception", t);
      return null;
    }
    for (final ValueSpecification lateSpec : lateResults) {
      if (!lateSpec.getProperties().getProperties().isEmpty()) {
        s_logger.debug("Deep resolution of {} to {}", specification, lateSpec);
        return lateSpec;
      }
    }
    return null;
  }

}

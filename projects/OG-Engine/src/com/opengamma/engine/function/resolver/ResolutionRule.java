/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Advertises a function to a {@link CompiledFunctionResolver}.
 */
@PublicAPI
public class ResolutionRule {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolutionRule.class);

  /**
   * The parameterized function.
   */
  private final ParameterizedFunction _parameterizedFunction;
  /**
   * The target filter.
   */
  private final ComputationTargetFilter _computationTargetFilter;
  /**
   * The priority.
   */
  private final int _priority;

  /**
   * Creates an instance.
   * 
   * @param function the function, not null
   * @param computationTargetFilter the filter, not null
   * @param priority the priority
   */
  public ResolutionRule(ParameterizedFunction function, ComputationTargetFilter computationTargetFilter, int priority) {
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(computationTargetFilter, "computationTargetFilter");
    _parameterizedFunction = function;
    _computationTargetFilter = computationTargetFilter;
    _priority = priority;
  }

  /**
   * Gets the parameterized function.
   * 
   * @return the function and behavioral parameters this rule is advertising, not null
   */
  public ParameterizedFunction getParameterizedFunction() {
    return _parameterizedFunction;
  }

  /**
   * Gets the filter that the rule uses.
   * 
   * @return the filter in use, not null
   */
  public ComputationTargetFilter getComputationTargetFilter() {
    return _computationTargetFilter;
  }

  /**
   * Gets the priority of the rule. If multiple rules can produce a given output, the one with the highest priority is chosen.
   * 
   * @return the priority
   */
  public int getPriority() {
    return _priority;
  }

  /**
   * The function advertised by this rule can validly produce the desired output only if:
   * <ol>
   * <li>The function is declared as applying to the target; and
   * <li>The function can produce the output; and
   * <li>This resolution rule applies to the given computation target
   * </ol>
   * <p>
   * The implementation has been split into two accessible components to allow a resolver to cache the intermediate results. This is more efficient than repeated calls to this method.
   * 
   * @param valueName The output value to be produced
   * @param target Computation target
   * @param constraints The constraints that must be satisfied on the produced value
   * @param context Function compilation context
   * @return Null if this the function advertised by this rule cannot produce the desired output, a valid ValueSpecification otherwise - as returned by the function. The specification is not composed
   *         against the requirement constraints.
   */
  public ValueSpecification getResult(String valueName, ComputationTarget target, ValueProperties constraints, FunctionCompilationContext context) {
    final Set<ValueSpecification> resultSpecs = getResults(target, context);
    if (resultSpecs == null) {
      return null;
    }
    return getResult(valueName, target, constraints, resultSpecs);
  }

  /**
   * The first half of the full {@link #getResult(ValueRequirement,ComputationTarget,FunctionCompilationContext)} implementation returning the set of all function outputs for use by
   * {@link #getResult(ValueRequirement,ComputationTarget,FunctionCompilationContext,Set)}.
   * 
   * @param target the computation target
   * @param context Function compilation context
   * @return the set of all value specifications produced by the function, null if none can be produced
   */
  public Set<ValueSpecification> getResults(final ComputationTarget target, final FunctionCompilationContext context) {
    final CompiledFunctionDefinition function = _parameterizedFunction.getFunction();
    // check the function can apply to the target
    //DebugUtils.canApplyTo_enter();
    if (!function.canApplyTo(context, target)) {
      //DebugUtils.canApplyTo_leave();
      return null;
    }
    //DebugUtils.canApplyTo_leave();
    // return the maximal set of results the function can produce for the target
    //DebugUtils.getResults1_enter();
    final Set<ValueSpecification> results = function.getResults(context, target);
    //DebugUtils.getResults1_leave();
    assert isValidResultsOnTarget(target, results) : "Results " + results + " not valid for target " + target;
    return results;
  }

  private boolean isValidResultsOnTarget(final ComputationTarget target, final Set<ValueSpecification> results) {
    if (results == null) {
      return true;
    }
    final UniqueId uid = target.getUniqueId();
    if (uid != null) {
      final List<UniqueId> context = target.getContextIdentifiers();
      for (ValueSpecification result : results) {
        if (!uid.equals(result.getTargetSpecification().getUniqueId())) {
          s_logger.warn("Invalid UID for result {} on target {}", result, target);
          return false;
        }
        if (context == null) {
          if (result.getTargetSpecification().getParent() != null) {
            s_logger.warn("Invalid parent context of result {} on target {}", result, target);
            return false;
          }
        } else {
          ComputationTargetReference ref = result.getTargetSpecification().getParent();
          for (int i = context.size(); --i >= 0;) {
            if (ref == null) {
              s_logger.warn("Missing parent context of result {} on target {}", result, target);
              return false;
            }
            if (!context.get(i).equals(ref.getSpecification().getUniqueId())) {
              s_logger.warn("Parent context mismatch of result {} on target {}", result, target);
              return false;
            }
            ref = ref.getParent();
          }
        }
      }
    } else {
      for (ValueSpecification result : results) {
        if (result.getTargetSpecification().getUniqueId() != null) {
          s_logger.warn("Invalid result {} on null target {}", result, target);
          return false;
        }
      }
    }
    return true;
  }

  /**
   * The second half of the full {@link #getResult(ValueRequirement, ComputationTarget, FunctionCompilationContext)}) implementation taking the set of all function outputs produced by
   * {@link #getResults}.
   * 
   * @param valueName output value name to be produced, not null and interned
   * @param target Computation target, not null
   * @param constraints the constraints that must be satisfied, not null
   * @param resultSpecs The results from {@code getResults()}, not null
   * @return Null if the function advertised by this rule cannot produce the desired output, a valid ValueSpecification otherwise - as returned by the function. The specification is not composed
   *         against the requirement constraints.
   */
  public ValueSpecification getResult(final String valueName, final ComputationTarget target, final ValueProperties constraints, final Collection<ValueSpecification> resultSpecs) {
    // Of the maximal outputs, is one valid for the requirement
    ValueSpecification validSpec = null;
    final UniqueId targetId = target.getUniqueId();
    if (targetId != null) {
      for (ValueSpecification resultSpec : resultSpecs) {
        //s_logger.debug("Considering {} for {}", resultSpec, output);
        if ((valueName == resultSpec.getValueName())
            && targetId.equals(resultSpec.getTargetSpecification().getUniqueId()) // This is not necessary if functions are well behaved or the "isValidResultsOnTarget" check was used 
            && constraints.isSatisfiedBy(resultSpec.getProperties())) {
          validSpec = resultSpec;
          break;
        }
      }
    } else {
      for (ValueSpecification resultSpec : resultSpecs) {
        if ((valueName == resultSpec.getValueName())
            && (resultSpec.getTargetSpecification().getUniqueId() == null) // This is not necessary if functions are well behaved or the "isValidResultsOnTarget" check was used
          && constraints.isSatisfiedBy(resultSpec.getProperties())) {
          validSpec = resultSpec;
          break;
        }
      }
    }
    if (validSpec == null) {
      return null;
    }
    // Apply the target filter for this rule (this is applied last because filters probably rarely exclude compared to the other tests)
    if (!_computationTargetFilter.accept(target)) {
      return null;
    }
    return validSpec;
  }

  @Override
  public String toString() {
    return "ResolutionRule[" + getParameterizedFunction() + " at priority " + getPriority() + "]";
  }

  private static ComputationTargetTypeVisitor<List<ComputationTargetType>, List<ComputationTargetType>> s_adjustNestedTarget =
      new ComputationTargetTypeVisitor<List<ComputationTargetType>, List<ComputationTargetType>>() {

        @Override
        public List<ComputationTargetType> visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final List<ComputationTargetType> data) {
          // Multiple target types will have been removed during resolution
          throw new IllegalStateException();
        }

        @Override
        public List<ComputationTargetType> visitNestedComputationTargetTypes(final List<ComputationTargetType> targetTypes, final List<ComputationTargetType> ruleTypes) {
          if (targetTypes.size() == ruleTypes.size()) {
            return targetTypes;
          } else if (targetTypes.size() > ruleTypes.size()) {
            return targetTypes.subList(targetTypes.size() - ruleTypes.size(), targetTypes.size());
          } else {
            // Target type is not compatible with the rule
            return null;
          }
        }

        @Override
        public List<ComputationTargetType> visitNullComputationTargetType(final List<ComputationTargetType> data) {
          // NULL should not be getting this far
          throw new IllegalStateException();
        }

        @Override
        public List<ComputationTargetType> visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final List<ComputationTargetType> data) {
          // Single class is not compatible with the rule
          return null;
        }

      };

  private static ComputationTargetTypeVisitor<ComputationTarget, ComputationTarget> s_adjustTarget = new ComputationTargetTypeVisitor<ComputationTarget, ComputationTarget>() {

    @Override
    public ComputationTarget visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ComputationTarget target) {
      for (ComputationTargetType type : types) {
        final ComputationTarget adjusted = type.accept(this, target);
        if (adjusted != null) {
          return adjusted;
        }
      }
      return null;
    }

    @Override
    public ComputationTarget visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ComputationTarget target) {
      // Target needs to be a nested type at least as long as the function type. It will be truncated if it is longer.
      final List<ComputationTargetType> adjustedTypes = target.getType().accept(s_adjustNestedTarget, types);
      if (adjustedTypes == null) {
        return null;
      }
      ComputationTargetType type = null;
      for (ComputationTargetType adjustedType : adjustedTypes) {
        if (type == null) {
          type = adjustedType;
        } else {
          type = type.containing(adjustedType);
        }
      }
      List<UniqueId> uids = target.getContextIdentifiers();
      if (uids.size() > adjustedTypes.size()) {
        uids = uids.subList(uids.size() - adjustedTypes.size(), uids.size());
      }
      return new ComputationTarget(type, uids, target.getValue());
    }

    @Override
    public ComputationTarget visitNullComputationTargetType(final ComputationTarget target) {
      return target;
    }

    @Override
    public ComputationTarget visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ComputationTarget target) {
      final Class<? extends UniqueIdentifiable> clazz = target.getValue().getClass();
      if (type.isAssignableFrom(clazz)) {
        if (target.getContextIdentifiers() == null) {
          // Target has no context, so use as is
          return target;
        } else {
          // Target has context - trim this off to leave just the leaf type
          return new ComputationTarget(ComputationTargetType.of(target.getValue().getClass()), target.getValue());
        }
      } else {
        // Target is not compatible with the function type
        return null;
      }
    }

  };

  /**
   * Adjusts the type on the target to match the declared type of the function.
   * <p>
   * Examples:
   * <ul>
   * <li>The function declares SECURITY as its type and a target is of sub-class FinancialSecurity then the target type remains FinancialSecurity
   * <li>The function declares POSITION|TRADE as its type and a target is of PORTFOLIO_NODE/SimplePosition then it is rewritten to SimplePosition (the matching leaf, not the full union type)
   * </ul>
   * 
   * @param type the declared type to reduce the target to, not null
   * @param target the target to reduce, not null
   * @return the reduced target, or null if the reduction is not possible
   */
  private static ComputationTarget adjustTarget(final ComputationTargetType type, final ComputationTarget target) {
    return type.accept(s_adjustTarget, target);
  }

  /**
   * Adjusts the type on the target to match the declared type of the function.
   * <p>
   * Examples:
   * <ul>
   * <li>The function declares SECURITY as its type and a target is of sub-class FinancialSecurity then the target type remains FinancialSecurity
   * <li>The function declares POSITION|TRADE as its type and a target is of PORTFOLIO_NODE/SimplePosition then it is rewritten to SimplePosition (the matching leaf, not the full union type)
   * </ul>
   * 
   * @param target the target to reduce, not null
   * @return the reduced target, or null if the target is not compatible with the rule
   */
  public ComputationTarget adjustTarget(final ComputationTarget target) {
    return adjustTarget(getParameterizedFunction().getFunction().getTargetType(), target);
  }

  /**
   * Adjusts the type on the target to match the declared type of the function.
   * <p>
   * Examples:
   * <ul>
   * <li>The function declares SECURITY as its type and a target is of sub-class FinancialSecurity then the target type remains FinancialSecurity
   * <li>The function declares POSITION|TRADE as its type and a target is of PORTFOLIO_NODE/SimplePosition then it is rewritten to SimplePosition (the matching leaf, not the full union type)
   * </ul>
   * 
   * @param adjustmentCache a cache of targets already adjusted to certain types, not null
   * @param target the target to reduce, not null
   * @return the reduced target, or null if the target is not compatible with the rule
   */
  public ComputationTarget adjustTarget(final Map<ComputationTargetType, ComputationTarget> adjustmentCache, final ComputationTarget target) {
    final ComputationTargetType type = getParameterizedFunction().getFunction().getTargetType();
    if (ComputationTargetType.NULL.equals(type)) {
      // We use NULL to mark failure in the cache, and NULL will never need adjusting
      return target;
    }
    ComputationTarget adjusted = adjustmentCache.get(type);
    if (adjusted == null) {
      adjusted = adjustTarget(type, target);
      if (adjusted != null) {
        adjustmentCache.put(type, adjusted);
      } else {
        // Store the failure
        adjustmentCache.put(type, ComputationTarget.NULL);
      }
    } else {
      if (ComputationTargetType.NULL.equals(adjusted.getType())) {
        // Got a failure from the cache
        adjusted = null;
      }
    }
    return adjusted;
  }

}

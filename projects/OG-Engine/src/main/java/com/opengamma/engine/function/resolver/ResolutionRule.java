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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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

  /**
   * Tests whether two unique identifiers are sufficiently equal. The object identifiers of each must match. Either may omit the version, but if both specify versions then the versions must also
   * match.
   * 
   * @param a the first identifier to compare, not null
   * @param b the second identifier to compare, not null
   */
  private boolean isUidMatch(final UniqueId a, final UniqueId b) {
    if (!a.getScheme().equals(b.getScheme()) || !a.getValue().equals(b.getValue())) {
      // Object identifiers don't match
      return false;
    }
    if ((a.getVersion() == null) || (b.getVersion() == null)) {
      // Loose versioning is okay
      return true;
    }
    // Strict versioning
    return a.getVersion().equals(b.getVersion());
  }

  private boolean isValidResultsOnTarget(final ComputationTarget target, final Set<ValueSpecification> results) {
    if (results == null) {
      return true;
    }
    final UniqueId uid = target.getUniqueId();
    if (uid != null) {
      ComputationTargetSpecification targetSpec = target.toSpecification();
      for (ValueSpecification result : results) {
        if (!isUidMatch(uid, result.getTargetSpecification().getUniqueId())) {
          s_logger.warn("Invalid UID for result {} on target {}", result, target);
          return false;
        }
        ComputationTargetReference a = result.getTargetSpecification().getParent();
        ComputationTargetReference b = targetSpec.getParent();
        while ((a != null) && (b != null)) {
          if (!isUidMatch(a.getSpecification().getUniqueId(), b.getSpecification().getUniqueId())) {
            s_logger.warn("Parent context mismatch of result {} on target {}", result, target);
            return false;
          }
          a = a.getParent();
          b = b.getParent();
        }
        if ((a != null) || (b != null)) {
          s_logger.warn("Invalid parent context of result {} on target {}", result, target);
          return false;
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
            && isUidMatch(targetId, resultSpec.getTargetSpecification().getUniqueId()) // This is not necessary if functions are well behaved or the "isValidResultsOnTarget" check was used 
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

  private static ComputationTargetTypeVisitor<Void, List<ComputationTargetType>> s_getNestedTargetTypes = new ComputationTargetTypeVisitor<Void, List<ComputationTargetType>>() {

    @Override
    public List<ComputationTargetType> visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void unused) {
      // Multiple target types will have been removed during resolution
      throw new IllegalStateException();
    }

    @Override
    public List<ComputationTargetType> visitNestedComputationTargetTypes(final List<ComputationTargetType> targetTypes, final Void unused) {
      return targetTypes;
    }

    @Override
    public List<ComputationTargetType> visitNullComputationTargetType(final Void unused) {
      // NULL should not be getting this far
      throw new IllegalStateException();
    }

    @Override
    public List<ComputationTargetType> visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void unused) {
      // Single class is not compatible with the rule
      return null;
    }

  };

  private static ComputationTargetTypeVisitor<Void, Class<? extends UniqueIdentifiable>> s_getLeafClass = new ComputationTargetTypeVisitor<Void, Class<? extends UniqueIdentifiable>>() {

    @Override
    public Class<? extends UniqueIdentifiable> visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
      // Multiple target types will have been removed during resolution
      throw new IllegalStateException();
    }

    @Override
    public Class<? extends UniqueIdentifiable> visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
      return types.get(types.size() - 1).accept(this, data);
    }

    @Override
    public Class<? extends UniqueIdentifiable> visitNullComputationTargetType(final Void data) {
      // NULL should not be getting this far
      throw new IllegalStateException();
    }

    @Override
    public Class<? extends UniqueIdentifiable> visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
      return type;
    }

  };

  private static ComputationTargetTypeVisitor<Void, ComputationTargetType> s_getLeafType = new ComputationTargetTypeVisitor<Void, ComputationTargetType>() {

    @Override
    public ComputationTargetType visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
      // Multiple target types will have been removed during resolution
      throw new IllegalStateException();
    }

    @Override
    public ComputationTargetType visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
      final ComputationTargetType type = types.get(types.size() - 1);
      final ComputationTargetType leaf = type.accept(this, data);
      if (leaf == null) {
        return type;
      } else {
        return leaf;
      }
    }

    @Override
    public ComputationTargetType visitNullComputationTargetType(final Void data) {
      // NULL should not be getting this far
      throw new IllegalStateException();
    }

    @Override
    public ComputationTargetType visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
      return null;
    }

  };

  private static ComputationTargetTypeVisitor<ComputationTargetType, ComputationTargetType> s_getAdjustedTargetType = new ComputationTargetTypeVisitor<ComputationTargetType, ComputationTargetType>() {

    @Override
    public ComputationTargetType visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ComputationTargetType target) {
      for (ComputationTargetType type : types) {
        final ComputationTargetType adjusted = type.accept(this, target);
        if (adjusted == ComputationTargetType.NULL) {
          return type;
        } else if (adjusted != null) {
          return adjusted;
        }
      }
      // Target not compatible
      return null;
    }

    @Override
    public ComputationTargetType visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ComputationTargetType target) {
      final List<ComputationTargetType> targetTypes = target.accept(s_getNestedTargetTypes, null);
      if (targetTypes == null) {
        // Target not compatible
        return null;
      }
      final int length = targetTypes.size();
      if (length < types.size()) {
        // Target does not match the type - not enough context
        return null;
      }
      final ComputationTargetType adjustedLeaf = types.get(length - 1).accept(this, targetTypes.get(length - 1));
      if (adjustedLeaf == null) {
        // Target not compatible at leaf type
        return null;
      }
      if (adjustedLeaf == ComputationTargetType.NULL) {
        if (length == types.size()) {
          // Exact match
          return ComputationTargetType.NULL;
        }
      }
      int i = length - types.size();
      ComputationTargetType type = targetTypes.get(i);
      while (++i < length - 1) {
        type = type.containing(targetTypes.get(i));
      }
      return type.containing(adjustedLeaf);
    }

    @Override
    public ComputationTargetType visitNullComputationTargetType(final ComputationTargetType target) {
      return ComputationTargetType.NULL;
    }

    @Override
    public ComputationTargetType visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ComputationTargetType target) {
      final Class<? extends UniqueIdentifiable> clazz = target.accept(s_getLeafClass, null);
      if (type.equals(clazz)) {
        // Target type is correct
        return ComputationTargetType.NULL;
      } else if (type.isAssignableFrom(clazz)) {
        // Target type is the target's leaf type
        final ComputationTargetType leaf = target.accept(s_getLeafType, null);
        if (leaf == null) {
          return target;
        } else {
          return leaf;
        }
      } else {
        // Target is not compatible with the function type
        return null;
      }
    }

  };

  private static final ConcurrentMap<ComputationTargetType, ConcurrentMap<ComputationTargetType, ComputationTargetType>> s_adjustCache =
      new ConcurrentHashMap<ComputationTargetType, ConcurrentMap<ComputationTargetType, ComputationTargetType>>();

  private static ComputationTargetType adjustTargetType(final ComputationTargetType type, final ComputationTargetType target) {
    if ((type == ComputationTargetType.NULL) || (type == target)) {
      // We use NULL to mark failure in the cache, and NULL will never need adjusting
      return target;
    }
    ConcurrentMap<ComputationTargetType, ComputationTargetType> functionCache = s_adjustCache.get(type);
    if (functionCache == null) {
      functionCache = new ConcurrentHashMap<ComputationTargetType, ComputationTargetType>();
      final ConcurrentMap<ComputationTargetType, ComputationTargetType> existing = s_adjustCache.putIfAbsent(type, functionCache);
      if (existing != null) {
        functionCache = existing;
      }
    }
    ComputationTargetType adjusted = functionCache.get(target);
    if (adjusted == null) {
      adjusted = type.accept(s_getAdjustedTargetType, target);
      if (adjusted == null) {
        // Not compatible
        functionCache.put(target, ComputationTargetType.NULL);
        return null;
      } else if (adjusted == ComputationTargetType.NULL) {
        // Exact match
        functionCache.put(target, type);
        return type;
      } else {
        // Type replacement
        functionCache.put(target, adjusted);
        return adjusted;
      }
    } else if (adjusted == ComputationTargetType.NULL) {
      // Failure
      return null;
    } else {
      return adjusted;
    }
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
   * @param type the declared type to reduce the target to, not null
   * @param target the target to reduce, not null
   * @return the reduced target, or null if the reduction is not possible
   */
  public static ComputationTarget adjustTarget(final ComputationTargetType type, final ComputationTarget target) {
    final ComputationTargetType newType = adjustTargetType(type, target.getType());
    if (newType == null) {
      return null;
    }
    if (newType == target.getType()) {
      return target;
    }
    return new ComputationTarget(target.toSpecification().replaceType(newType).getSpecification(), target.getValue());
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

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.ComputationTargetResults;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;

/**
 * Abstract function for injecting default properties into the dependency graph.
 */
public abstract class DefaultPropertyFunction extends AbstractFunction.NonCompiledInvoker implements OpenGammaFunctionExclusions {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultPropertyFunction.class);

  /**
   * The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other.
   */
  public static enum PriorityClass {

    /**
     * Must apply before the "normal" properties.
     */
    ABOVE_NORMAL(1),
    /**
     * Normal application.
     */
    NORMAL(0),
    /**
     * Must apply after the "normal" properties.
     */
    BELOW_NORMAL(-1),
    /**
     * Must apply after other properties.
     */
    LOWEST(-2);

    private final int _level;

    private PriorityClass(final int level) {
      assert (level >= MIN_ADJUST) && (level <= MAX_ADJUST);
      _level = level;
    }

    /**
     * Returns the priority level adjuster - an integer MIN_ADJUST .. MAX_ADJUST
     * 
     * @return priority level adjustment
     */
    public int getPriorityAdjust() {
      return _level;
    }

    /**
     * Maximum integer that can be returned by {@link #getPriorityAdjust}.
     */
    public static final int MAX_ADJUST = 1;

    /**
     * Minimum integer that can be returned by {@link #getPriorityAdjust}.
     */
    public static final int MIN_ADJUST = -2;

  };

  private final ComputationTargetType _targetType;
  private final boolean _permitWithout;
  private volatile PropertyDefaults _defaults;

  protected DefaultPropertyFunction(final ComputationTargetType targetType, final boolean permitWithout) {
    _targetType = targetType;
    _permitWithout = permitWithout;
  }

  public boolean isPermitWithout() {
    return _permitWithout;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

  /**
   * Callback object used by the implementation of {@link #getDefaults}.
   */
  public static final class PropertyDefaults {

    private final Map<String, Set<String>> _valueName2PropertyNames;
    private final FunctionCompilationContext _context;
    private final ComputationTarget _target;
    private boolean _targetUsed;

    private PropertyDefaults(final FunctionCompilationContext context, final ComputationTarget target) {
      _valueName2PropertyNames = new HashMap<String, Set<String>>();
      _context = context;
      _target = target;
    }

    private PropertyDefaults(final PropertyDefaults copyFrom) {
      _valueName2PropertyNames = copyFrom._valueName2PropertyNames;
      _context = null;
      _target = null;
    }

    public FunctionCompilationContext getContext() {
      return _context;
    }

    public ComputationTarget getTarget() {
      _targetUsed = true;
      return _target;
    }

    public void addValuePropertyName(final String valueName, final String propertyName) {
      Set<String> propertyNames = _valueName2PropertyNames.get(valueName);
      if (propertyNames == null) {
        propertyNames = new HashSet<String>();
        _valueName2PropertyNames.put(valueName, propertyNames);
      }
      propertyNames.add(propertyName);
    }

    /**
     * Queries all available outputs on the target and adds those values to the default set if property name is defined on their finite outputs.
     * 
     * @param propertyName the property name a default is available for, not null
     */
    public void addAllValuesPropertyName(final String propertyName) {
      final ComputationTargetResults resultsProvider = getContext().getComputationTargetResults();
      if (resultsProvider == null) {
        return;
      }
      for (final ValueSpecification result : resultsProvider.getPartialResults(getTarget())) {
        final Set<String> properties = result.getProperties().getProperties();
        if ((properties != null) && properties.contains(propertyName)) {
          s_logger.debug("Found {} defined on {}", propertyName, result);
          addValuePropertyName(result.getValueName(), propertyName);
        }
      }
    }

    private Map<String, Set<String>> getValueName2PropertyNames() {
      return _valueName2PropertyNames;
    }

    private boolean isTargetUsed() {
      return _targetUsed;
    }

  }

  /**
   * Returns the defaults that are available
   * 
   * @param defaults the callback object to return the property and value names on, not null
   */
  protected abstract void getDefaults(PropertyDefaults defaults);

  private PropertyDefaults getDefaults(final FunctionCompilationContext context, final ComputationTarget target) {
    PropertyDefaults defaults = _defaults;
    if (defaults != null) {
      return defaults;
    }
    defaults = new PropertyDefaults(context, target);
    getDefaults(defaults);
    if (defaults.getValueName2PropertyNames().isEmpty()) {
      s_logger.debug("No default properties for {}", target);
      return null;
    } else {
      s_logger.debug("Found {} value(s) with default properties for {}", defaults.getValueName2PropertyNames().size(), target);
      if (!defaults.isTargetUsed()) {
        s_logger.info("Caching target agnostic default values for {}", getClass());
        defaults = new PropertyDefaults(defaults);
        _defaults = defaults;
      }
      return defaults;
    }
  }

  /**
   * Returns the maximal set of value requirement names that are defined (if the defaults are not dependent on compilation context information or a target)
   * 
   * @return the set of value requirement names, shouldn't be null
   * @throws NullPointerException if the implementation of getDefaults expected a compilation context or target
   * @deprecated this is for debugging only - it is likely to be removed or unavailable in future versions
   */
  @Deprecated
  public final Set<String> getMaximalValueRequirementNames() {
    final PropertyDefaults defaults = new PropertyDefaults(null, null);
    getDefaults(defaults);
    return defaults.getValueName2PropertyNames().keySet();
  }

  /**
   * Returns the default value(s) to set for the property. If a default value is not available, must return null.
   * 
   * @param context the function compilation context, not null
   * @param target the computation target, not null
   * @param desiredValue the initial requirement, lacking the property to be injected, not null
   * @param propertyName the property name to be injected
   * @return the default values or null if there is no default to inject
   */
  protected abstract Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue, String propertyName);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    throw new IllegalStateException("This function should never be executed");
  }

  /**
   * Performs the {@link CompiledFunctionDefinition#canApplyTo} test by checking whether any defaults are returned for the target. If the {@link #getDefaults} cost is high, then consider overloading
   * this method with something cheaper.
   * 
   * @param context the compilation context, not null
   * @param target computation target, not null
   * @return true if applies (i.e. there are defaults available), false otherwise
   */
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return getDefaults(context, target) != null;
  }

  /**
   * Offers sub-classes a chance to modify the constraints that will be ejected, or to abort the application.
   * 
   * @param constraints the constraint set that will be used to form the input requirement, not null
   * @return true to proceed, false to abort the application
   */
  protected boolean verifyConstraints(final ValueProperties.Builder constraints) {
    // No-op
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final PropertyDefaults defaults = getDefaults(context, target);
    final ValueProperties.Builder constraints = desiredValue.getConstraints().copy();
    boolean matched = false;
    for (final String propertyName : defaults.getValueName2PropertyNames().get(desiredValue.getValueName())) {
      final Set<String> existingValues = desiredValue.getConstraints().getValues(propertyName);
      if (isPermitWithout() || (existingValues == null) || desiredValue.getConstraints().isOptional(propertyName)) {
        s_logger.debug("Matched default property {} for {}", propertyName, desiredValue);
        final Set<String> defaultValues = getDefaultValue(context, target, desiredValue, propertyName);
        if (defaultValues != null) {
          if (defaultValues.isEmpty()) {
            if (existingValues == null) {
              s_logger.debug("Default ANY");
              constraints.withAny(propertyName);
              matched = true;
            } else {
              s_logger.debug("Default ANY but already had constraint {}", existingValues);
            }
          } else {
            if (existingValues == null) {
              s_logger.debug("Default {}", defaultValues);
              constraints.with(propertyName, defaultValues);
              matched = true;
            } else {
              if (existingValues.isEmpty()) {
                s_logger.debug("Default {} better than ANY", defaultValues);
                constraints.withoutAny(propertyName).with(propertyName, defaultValues);
                matched = true;
              } else {
                // REVIEW 2013-11-06 Andrew -- This can be quite inefficient; the isEmpty will create iterators on the underlyings, as will then the with operation if the intersection is non-empty
                final Set<String> intersect = Sets.intersection(existingValues, defaultValues);
                if (intersect.isEmpty()) {
                  s_logger.debug("Default {} incompatible with {}", defaultValues, existingValues);
                } else {
                  s_logger.debug("Default {} reduced to {}", defaultValues, intersect);
                  constraints.withoutAny(propertyName).with(propertyName, intersect);
                  matched = true;
                }
              }
            }
          }
        } else {
          s_logger.debug("No default values");
        }
      } else {
        // If we don't permit constraint absence, and there is a mandatory requirement; that requires something deeper
        // down in the graph to make a decision.
        s_logger.debug("Does not match on property {} for {}", propertyName, desiredValue);
      }
    }
    if (!matched || !verifyConstraints(constraints)) {
      // No default values were found
      s_logger.debug("No matched values");
      return null;
    }
    final ValueRequirement reduction = new ValueRequirement(desiredValue.getValueName(), target.toSpecification(), constraints.get());
    s_logger.debug("Reduced to {}", reduction);
    return Collections.singleton(reduction);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final PropertyDefaults defaults = getDefaults(context, target);
    if (defaults == null) {
      // If canApplyTo is overloaded, we can't assert that getDefaults produces something non-empty
      s_logger.debug("No defaults for {}", target);
      return null;
    }
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
    for (final Map.Entry<String, Set<String>> valueName2PropertyNames : defaults.getValueName2PropertyNames().entrySet()) {
      final String valueName = valueName2PropertyNames.getKey();
      if (isPermitWithout()) {
        result.add(new ValueSpecification(valueName, targetSpec, ValueProperties.all()));
      } else {
        for (final String propertyName : valueName2PropertyNames.getValue()) {
          result.add(new ValueSpecification(valueName, targetSpec, ValueProperties.all().withoutAny(propertyName)));
        }
      }
    }
    s_logger.debug("Produced results {} for {}", result, target);
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    // Pass the inputs through unchanged - will cause suppression of this node from the graph
    return inputs.keySet();
  }

  /**
   * Returns a priority adjustment. {@link FunctionPriority} implementations may recognize {@link DefaultPropertyFunction} instances and use this to adjust the priority they would otherwise assign to
   * the function.
   * 
   * @return the priority adjustment, defaults to {@link PriorityClass#NORMAL}
   */
  public PriorityClass getPriority() {
    return PriorityClass.NORMAL;
  }

  /**
   * Returns a mutual function exclusion group name. A {@link FunctionExclusionGroups} implementation may recognize {@link DefaultPropertyFunction} instances and use this to declare application
   * exclusions.
   * 
   * @return the mutual exclusion group, defaults to the function instance's class name so that any given default function is only applied once in the resolution chain
   */
  @Override
  public String getMutualExclusionGroup() {
    return getClass().getName();
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Basic resolution rule transformations that match on a function's short name.
 */
public class SimpleResolutionRuleTransform implements ResolutionRuleTransform {

  private static final Logger s_logger = LoggerFactory.getLogger(SimpleResolutionRuleTransform.class);

  /**
   * Describes an action as part of a rule's transformation. 
   */
  public abstract static class Action {

    protected abstract Action with(Action other);

    protected abstract void apply(final ResolutionRule originalRule, final Collection<ResolutionRule> output);

  }

  /**
   * Describes a rule that should be suppressed.
   */
  public static final class DontUse extends Action {

    private static final Action INSTANCE = new DontUse();

    private DontUse() {
    }

    @Override
    protected Action with(final Action other) {
      throw new OpenGammaRuntimeException("Resolution rule already marked as \"Don't use\"");
    }

    @Override
    protected void apply(final ResolutionRule originalRule, final Collection<ResolutionRule> output) {
      s_logger.debug("Discarding {}", originalRule);
    }

    @Override
    public boolean equals(final Object o) {
      return (o == this) || (o instanceof DontUse);
    }

    @Override
    public int hashCode() {
      // Not destined for hash tables
      return 0;
    }

    @Override
    public String toString() {
      return "Don't use";
    }

  }

  /**
   * Describes a rule that should be adjusted.
   */
  public static final class Adjust extends Action {

    private Integer _priorityAdjustment;
    private FunctionParameters _parameters;
    private ComputationTargetFilter _computationTargetFilter;

    private Adjust(final FunctionParameters parameters, final ComputationTargetFilter filter, final Integer priorityAdjustment) {
      _priorityAdjustment = priorityAdjustment;
      _parameters = parameters;
      _computationTargetFilter = filter;
    }

    public Integer getPriorityAdjustment() {
      return _priorityAdjustment;
    }

    public FunctionParameters getParameters() {
      return _parameters;
    }

    public ComputationTargetFilter getComputationTargetFilter() {
      return _computationTargetFilter;
    }

    @Override
    protected Action with(final Action other) {
      return new MultipleAdjust().with(this).with(other);
    }

    @Override
    protected void apply(final ResolutionRule originalRule, final Collection<ResolutionRule> output) {
      ParameterizedFunction function = originalRule.getFunction();
      if (_parameters != null) {
        function = new ParameterizedFunction(originalRule.getFunction().getFunction(), _parameters);
      }
      final ComputationTargetFilter computationTargetFilter;
      if (_computationTargetFilter != null) {
        computationTargetFilter = _computationTargetFilter;
      } else {
        computationTargetFilter = originalRule.getComputationTargetFilter();
      }
      int priority = originalRule.getPriority();
      if (_priorityAdjustment != null) {
        priority += _priorityAdjustment;
      }
      final ResolutionRule replacement = new ResolutionRule(function, computationTargetFilter, priority);
      s_logger.debug("Publishing {} in place of {}", replacement, originalRule);
      output.add(replacement);
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Adjust)) {
        return false;
      }
      final Adjust other = (Adjust) o;
      return ObjectUtils.equals(_parameters, other._parameters)
          && ObjectUtils.equals(_computationTargetFilter, other._computationTargetFilter)
          && ObjectUtils.equals(_priorityAdjustment, other._priorityAdjustment);
    }

    @Override
    public int hashCode() {
      // Not destined for hash tables
      return 0;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Adjust[");
      boolean comma = false;
      if (_parameters != null) {
        sb.append("Parameters=").append(_parameters);
        comma = true;
      }
      if (_computationTargetFilter != null) {
        if (comma) {
          sb.append(',');
        } else {
          comma = true;
        }
        sb.append("ComputationTargetFilter=").append(_computationTargetFilter);
      }
      if (_priorityAdjustment != null) {
        if (comma) {
          sb.append(',');
        }
        sb.append("PriorityAdjustment=").append(_priorityAdjustment);
      }
      return sb.append(']').toString();
    }

  }

  /**
   * Describes a set of adjustments for a single rule.
   */
  public static final class MultipleAdjust extends Action {

    private final List<Adjust> _adjusts = new ArrayList<Adjust>();

    private MultipleAdjust() {
    }

    @SuppressWarnings("unchecked")
    public List<Adjust> getAdjusts() {
      return Collections.unmodifiableList(_adjusts);
    }

    @Override
    protected Action with(final Action other) {
      if (other instanceof DontUse) {
        throw new OpenGammaRuntimeException("Attempting to mark composite rule as \"Don't Use\"");
      }
      // other can only be Adjust at this point
      _adjusts.add((Adjust) other);
      return this;
    }

    @Override
    protected void apply(final ResolutionRule originalRule, final Collection<ResolutionRule> output) {
      for (Action adjust : _adjusts) {
        adjust.apply(originalRule, output);
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof MultipleAdjust)) {
        return false;
      }
      final MultipleAdjust other = (MultipleAdjust) o;
      if (_adjusts.size() != other._adjusts.size()) {
        return false;
      }
      for (Action adjust : _adjusts) {
        if (!other._adjusts.contains(adjust)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      // Not destined for hash tables
      return 0;
    }

    @Override
    public String toString() {
      return "Multiple" + _adjusts;
    }

  }

  private final Map<String, Action> _functionTransformations = new HashMap<String, Action>();

  /**
   * Returns the set of transformations as a map from short function names to the action(s) applied to
   * matching resolution rules. If multiple actions are applied, the function will be advertised by
   * multiple new rules in place of the original. If a function is omitted from the set, the original
   * rule is preserved.
   * 
   * @return the set of transformations
   */
  @SuppressWarnings("unchecked")
  public Map<String, Action> getFunctionTransformations() {
    return Collections.unmodifiableMap(_functionTransformations);
  }

  private void registerAction(final String shortFunctionName, final Action action) {
    final Action existing = _functionTransformations.get(shortFunctionName);
    if (existing == null) {
      _functionTransformations.put(shortFunctionName, action);
    } else {
      _functionTransformations.put(shortFunctionName, existing.with(action));
    }
  }

  /**
   * Suppress any rules using the given function name.
   * 
   * @param shortFunctionName function to suppress, not {@code null}
   */
  public void suppressRule(final String shortFunctionName) {
    registerAction(shortFunctionName, DontUse.INSTANCE);
  }

  /**
   * Adjust the rules using the given function name.
   * 
   * @param shortFunctionName function to adjust, not {@code null}
   * @param parameters function parameters, or {@code null} to use the original rule default
   * @param priorityAdjustment priority shift, or {@code null} to use the original rule default
   * @param computationTargetFilter computation target filter, or {@code null} to use the original rule default
   */
  public void adjustRule(final String shortFunctionName, final FunctionParameters parameters, final ComputationTargetFilter computationTargetFilter, final Integer priorityAdjustment) {
    registerAction(shortFunctionName, new Adjust(parameters, computationTargetFilter, priorityAdjustment));
  }

  @Override
  public Collection<ResolutionRule> transform(final Collection<ResolutionRule> rules) {
    final Collection<ResolutionRule> result = new ArrayList<ResolutionRule>(rules.size());
    for (ResolutionRule rule : rules) {
      final Action action = _functionTransformations.get(rule.getFunction().getFunction().getFunctionDefinition().getShortName());
      if (action == null) {
        result.add(rule);
      } else {
        action.apply(rule, result);
      }
    }
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof SimpleResolutionRuleTransform)) {
      return false;
    }
    final SimpleResolutionRuleTransform other = (SimpleResolutionRuleTransform) o;
    return _functionTransformations.equals(other._functionTransformations);
  }

  @Override
  public int hashCode() {
    // Not intended to be hashed
    return 0;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + _functionTransformations;
  }

}

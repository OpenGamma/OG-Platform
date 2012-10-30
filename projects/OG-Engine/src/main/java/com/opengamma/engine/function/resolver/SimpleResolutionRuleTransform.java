/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;

/**
 * Resolution rule transform that matches on the short name of the function.
 */
public class SimpleResolutionRuleTransform implements ResolutionRuleTransform {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SimpleResolutionRuleTransform.class);

  /**
   * The function transformations.
   */
  private final Map<String, Action> _functionTransformations = new HashMap<String, Action>();

  /**
   * Gets the map of registered transformations.
   * <p>
   * The map is keyed by short function name, with the value being the the action to be applied.
   * If multiple actions are applied, the function will be advertised by multiple new rules in
   * place of the original. If a function is omitted from the set, the original rule is preserved.
   * 
   * @return the set of transformations, not null
   */
  public Map<String, Action> getFunctionTransformations() {
    return Collections.unmodifiableMap(_functionTransformations);
  }

  //-------------------------------------------------------------------------
  /**
   * Suppress any rules using the given function name.
   * 
   * @param shortFunctionName  the function to suppress, not null
   */
  public void suppressRule(final String shortFunctionName) {
    registerAction(shortFunctionName, DontUse.INSTANCE);
  }

  /**
   * Adjust the rules using the given function name.
   * 
   * @param shortFunctionName  the function to adjust, not null
   * @param parameters  the function parameters, null to use the original rule default
   * @param priorityAdjustment  the priority shift, null to use the original rule default
   * @param computationTargetFilter  the computation target filter, null to use the original rule default
   */
  public void adjustRule(final String shortFunctionName, final FunctionParameters parameters, final ComputationTargetFilter computationTargetFilter, final Integer priorityAdjustment) {
    registerAction(shortFunctionName, new Adjust(parameters, computationTargetFilter, priorityAdjustment));
  }

  private void registerAction(final String shortFunctionName, final Action action) {
    final Action existing = _functionTransformations.get(shortFunctionName);
    if (existing == null) {
      _functionTransformations.put(shortFunctionName, action);
    } else {
      _functionTransformations.put(shortFunctionName, existing.with(action));
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<ResolutionRule> transform(final Collection<ResolutionRule> rules) {
    final Collection<ResolutionRule> result = Lists.newArrayListWithCapacity(rules.size());
    for (ResolutionRule rule : rules) {
      final String function = rule.getParameterizedFunction().getFunction().getFunctionDefinition().getShortName();
      final Action action = _functionTransformations.get(function);
      if (action == null) {
        s_logger.debug("Function {} has no transformation rules", function);
        result.add(rule);
      } else {
        s_logger.debug("Applying transformation rules for function {}", function);
        action.apply(rule, result);
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof SimpleResolutionRuleTransform) {
      final SimpleResolutionRuleTransform other = (SimpleResolutionRuleTransform) obj;
      return _functionTransformations.equals(other._functionTransformations);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 0;  // not intended to be hashed
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + _functionTransformations;
  }

  //-------------------------------------------------------------------------
  /**
   * Describes an action as part of a rule's transformation. 
   */
  public abstract static class Action {

    protected abstract Action with(Action other);

    protected abstract void apply(final ResolutionRule originalRule, final Collection<ResolutionRule> output);
  }

  //-------------------------------------------------------------------------
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

  //-------------------------------------------------------------------------
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
      ParameterizedFunction function = originalRule.getParameterizedFunction();
      if (_parameters != null) {
        function = new ParameterizedFunction(originalRule.getParameterizedFunction().getFunction(), _parameters);
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

  //-------------------------------------------------------------------------
  /**
   * Describes a set of adjustments for a single rule.
   */
  public static final class MultipleAdjust extends Action {

    private final List<Adjust> _adjusts = new ArrayList<Adjust>();

    private MultipleAdjust() {
    }

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

}

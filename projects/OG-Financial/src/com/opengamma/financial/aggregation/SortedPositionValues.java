/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Function for producing a sorted list of {@link ComputedValue} outputs for all positions underneath a node. The values are
 * sorted in ascending {@link Comparable#compareTo} order. The values must implement the comparable interface for the result to
 * be calculated.
 * <p>
 * This is intended as a "back-end" to functions which produce a specific ordering and filtering (e.g. top N or bottom N) but
 * require sorted input in the first place so that the sort operation can be shared among a number. Otherwise splitting a portfolio
 * into 100 percentiles might mean sorting the results 100 times.  
 */
public class SortedPositionValues extends AbstractSortedPositionValues {

  // TODO: instead of flattening the positions, could request the sorted values from any nodes -- it's then a merge operation
  // to combine the lists. This may be quicker if calculating Top-N or something for sub-nodes in the tree as well which require
  // the intermediate sorted results. This requires PLAT-1049 as the same function will be applied multiple times (on different
  // targets) in graph walk.

  /**
   * Value name on the result produced.
   */
  public static final String VALUE_NAME = "SortedPositionValues";

  private static final Comparator<ComputedValue> s_comparator = new Comparator<ComputedValue>() {
    @SuppressWarnings("unchecked")
    @Override
    public int compare(final ComputedValue o1, final ComputedValue o2) {
      return ((Comparable<Object>) o1.getValue()).compareTo(o2.getValue());
    }
  };

  private static void getPositionRequirements(final PortfolioNode node, final String valueName, final ValueProperties requirementConstraints, final Set<ValueRequirement> requirements) {
    for (Position position : node.getPositions()) {
      requirements.add(new ValueRequirement(valueName, ComputationTargetType.POSITION, position.getUniqueId(), requirementConstraints));
    }
    for (PortfolioNode childNode : node.getChildNodes()) {
      getPositionRequirements(childNode, valueName, requirementConstraints, requirements);
    }
  }

  // AbstractSortedPositionValues

  @Override
  protected String getValueName() {
    return VALUE_NAME;
  }

  @Override
  protected Set<ValueRequirement> getRequirements(final String valueName, final ComputationTarget target, final ValueProperties constraints) {
    final ValueProperties.Builder requirementConstraints = ValueProperties.builder();
    final String valueNameDot = valueName + ".";
    for (String constraint : constraints.getProperties()) {
      if (constraint.startsWith(valueNameDot)) {
        final Set<String> values = constraints.getValues(constraint);
        if (values.isEmpty()) {
          requirementConstraints.withAny(constraint.substring(valueNameDot.length()));
        } else {
          requirementConstraints.with(constraint.substring(valueNameDot.length()), values);
        }
      }
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    getPositionRequirements(target.getPortfolioNode(), valueName, requirementConstraints.get(), requirements);
    return requirements;
  }

  @Override
  protected void composeValueProperties(final ValueProperties.Builder builder, final ValueSpecification inputValue) {
    builder.with(VALUE_NAME_PROPERTY, inputValue.getValueName());
    for (String inputProperty : inputValue.getProperties().getProperties()) {
      final Set<String> inputPropertyValues = inputValue.getProperties().getValues(inputProperty);
      if (inputPropertyValues.isEmpty()) {
        builder.withAny(inputValue.getValueName() + "." + inputProperty);
      } else {
        builder.with(inputValue.getValueName() + "." + inputProperty, inputPropertyValues);
      }
    }
  }

  // AbstractFunction

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final List<ComputedValue> values = new ArrayList<ComputedValue>(inputs.getAllValues());
    final ValueProperties.Builder properties = createValueProperties();
    for (ComputedValue value : values) {
      composeValueProperties(properties, value.getSpecification());
    }
    Collections.sort(values, s_comparator);
    return Collections.singleton(new ComputedValue(ValueSpecification.of(getValueName(), ComputationTargetType.PORTFOLIO_NODE, target.getUniqueId(), properties.get()), values));
  }

}

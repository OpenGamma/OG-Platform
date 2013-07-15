/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Function for producing a slice of outputs from all positions underneath a node based on the sorted results. The values are sorted
 * in the "compareTo" order and returned in value descending order (as reported by {@link Comparable#compareTo}. The values must
 * implement the comparable interface for the result to be calculated.
 * <p>
 * See {@link SortedPositionValues} for further details of how the sort takes place.
 */
public abstract class SlicedPositionValues extends AbstractSortedPositionValues {

  /**
   * Property name giving the number of results in the slice.
   */
  public static final String COUNT_PROPERTY = "Count";

  /**
   * Property name identifying the value that was produced from the positions.
   */
  public static final String VALUE_NAME_PROPERTY = "Value";

  protected abstract boolean validateConstraints(ValueProperties constraints);

  protected abstract List<ComputedValue> sliceResults(List<ComputedValue> sortedAscending, ValueProperties constraints, ValueProperties.Builder properties);

  private static void composePrefixedProperties(final ValueProperties.Builder builder, final ValueProperties source, final String prefix) {
    for (String property : source.getProperties()) {
      if (property.startsWith(prefix)) {
        final Set<String> propertyValues = source.getValues(property);
        if (propertyValues.isEmpty()) {
          builder.withAny(property);
        } else {
          builder.with(property, propertyValues);
        }
      }
    }
  }

  // AbstractSortedPositionValues

  @Override
  protected Set<ValueRequirement> getRequirements(final String valueName, final ComputationTarget target, final ValueProperties constraints) {
    if (!validateConstraints(constraints)) {
      return null;
    }
    final ValueProperties.Builder requirementConstraints = ValueProperties.with(VALUE_NAME_PROPERTY, valueName);
    composePrefixedProperties(requirementConstraints, constraints, valueName + ".");
    return Collections.singleton(new ValueRequirement(SortedPositionValues.VALUE_NAME, target.toSpecification(), requirementConstraints.get()));
  }

  @Override
  protected void composeValueProperties(final ValueProperties.Builder builder, final ValueSpecification inputValue) {
    final String valueName = inputValue.getProperty(SortedPositionValues.VALUE_NAME_PROPERTY);
    builder.with(VALUE_NAME_PROPERTY, valueName);
    composePrefixedProperties(builder, inputValue.getProperties(), valueName + ".");
  }

  // AbstractFunction

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue input = inputs.getAllValues().iterator().next();
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final ValueProperties.Builder properties = createValueProperties();
    composeValueProperties(properties, input.getSpecification());
    final List<ComputedValue> values = sliceResults((List<ComputedValue>) input.getValue(), constraints, properties);
    return Collections.singleton(new ComputedValue(ValueSpecification.of(getValueName(), ComputationTargetType.PORTFOLIO_NODE, target.getUniqueId(), properties.get()), values));
  }

}

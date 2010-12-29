/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collection;

import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Able to preserve a set of optional constraints on output values through to function inputs.
 */
public abstract class PropertyPreservingFunction extends AbstractFunction.NonCompiledInvoker {

  protected abstract String[] getPreservedProperties();

  private ValueProperties createInputConstraints(final String[] preserve) {
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (String value : preserve) {
      builder.withOptional(value);
    }
    return builder.get();
  }

  private ValueProperties createResultProperties(final String[] preserve) {
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (String value : preserve) {
      builder.withAny(value);
    }
    return builder.with(ValuePropertyNames.FUNCTION, getUniqueIdentifier()).get();
  }

  private ValueProperties _inputConstraints;
  private ValueProperties _resultProperties;

  @Override
  public void setUniqueIdentifier(final String identifier) {
    super.setUniqueIdentifier(identifier);
    final String[] preserve = getPreservedProperties();
    _resultProperties = createResultProperties(preserve);
    _inputConstraints = createInputConstraints(preserve);
  }

  protected ValueProperties getInputConstraint(final ValueRequirement desiredValue) {
    return getInputConstraints().compose(desiredValue.getConstraints());
  }

  protected ValueProperties getInputConstraints() {
    return _inputConstraints;
  }

  protected ValueProperties getResultProperties() {
    return _resultProperties;
  }

  private ValueProperties getResultProperties(final ValueProperties properties) {
    return properties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueIdentifier()).get();
  }

  /**
   * Produces the input constraints composed against the properties of the input value.
   * 
   * @param inputSpec an input value specification
   * @return the composed properties
   */
  protected ValueProperties getResultProperties(final ValueSpecification inputSpec) {
    return getResultProperties(getInputConstraints().compose(inputSpec.getProperties()));
  }

  /**
   * Produces the input constraints composed against the properties of an input value, asserting that the composition against
   * all input values gives identical results.
   * 
   * @param inputs a set of input value specifications
   * @return the composed properties
   */
  protected ValueProperties getCompositeSpecificationProperties(final Collection<ValueSpecification> inputs) {
    ValueProperties properties = null;
    ValueSpecification previousInput = null;
    for (ValueSpecification input : inputs) {
      final ValueProperties inputProperties = getInputConstraints().compose(input.getProperties());
      if (properties == null) {
        properties = inputProperties;
        previousInput = input;
      } else {
        if (!properties.equals(inputProperties)) {
          throw new IllegalArgumentException("composition of input constraints with " + input + " yields different results to " + previousInput);
        }
      }
    }
    return getResultProperties(properties);
  }

  protected ValueProperties getCompositeValueProperties(final Collection<ComputedValue> inputs) {
    ValueProperties properties = getInputConstraints();
    for (ComputedValue input : inputs) {
      properties = properties.compose(input.getSpecification().getProperties());
      // We only need to consider the first as all input are equal (asserted by getCompositeSpecificationProperties)
      break;
    }
    return getResultProperties(properties);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A base class for functions which need to preserve a set of properties from their inputs to their outputs, and
 * also therefore need to request the appropriate inputs. 
 */
public abstract class PropertyPreservingFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Gets the properties which the function must preserve. If a property in this category occurs on any input then all
   * inputs must declare the same property value, and this will be propagated to the outputs, or the function will
   * fail. If no inputs declare a property then it will not appear on the outputs.
   * 
   * @return the properties which the function is required to preserve, not {@code null}
   */
  protected abstract Collection<String> getPreservedProperties();
  
  /**
   * Gets the properties which the function will attempt to preserve. A property in this category will appear on the
   * the function outputs if the property is present and has the same value across every input, otherwise it will be
   * dropped.
   * 
   * @return the properties which the function will attempt to preserve, not {@code null}
   */
  protected abstract Collection<String> getOptionalPreservedProperties();

  private ValueProperties createInputConstraints(final Collection<String> preserve) {
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (String value : preserve) {
      builder.withOptional(value);
    }
    return builder.get();
  }

  private ValueProperties createResultProperties(final Collection<String> preserve) {
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (String value : preserve) {
      builder.withAny(value);
    }
    applyAdditionalResultProperties(builder);
    return builder.get();
  }

  /**
   * Add additional properties to the results. The default here adds the function identifier; override
   * this to add further information, but call the superclass method if the function identifier is not
   * added.
   * 
   * @param builder to add properties to
   */
  protected void applyAdditionalResultProperties(final ValueProperties.Builder builder) {
    builder.with(ValuePropertyNames.FUNCTION, getUniqueId());
  }

  private ValueProperties _inputConstraints;
  private ValueProperties _resultProperties;
  private ValueProperties _requiredProperties;

  @Override
  public void setUniqueId(final String identifier) {
    super.setUniqueId(identifier);
    final Collection<String> optionalProperties = getOptionalPreservedProperties();
    final Collection<String> requiredProperties = getPreservedProperties();
    final Collection<String> preservationCandidates = new ArrayList<String>(optionalProperties.size() + requiredProperties.size());
    preservationCandidates.addAll(optionalProperties);
    preservationCandidates.addAll(requiredProperties);
    _resultProperties = createResultProperties(preservationCandidates);
    _requiredProperties = createInputConstraints(requiredProperties);
    _inputConstraints = createInputConstraints(preservationCandidates);
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
    final ValueProperties.Builder builder = properties.copy().withoutAny(ValuePropertyNames.FUNCTION);
    applyAdditionalResultProperties(builder);
    return builder.get();
  }

  /**
   * Produces the properties of the input value composed against the input constraints.
   * 
   * @param inputSpec an input value specification
   * @return the composed properties
   */
  protected ValueProperties getResultProperties(final ValueSpecification inputSpec) {
    return getResultProperties(inputSpec.getProperties().compose(getInputConstraints()));
  }

  /**
   * Produces the input constraints composed against the properties of the inputs, ensuring that any required preserved
   * properties are identical if present.
   * 
   * @param inputs a set of input value specifications
   * @return the composed properties
   */
  protected ValueProperties getResultProperties(final Collection<ValueSpecification> inputs) {
    ValueProperties compositeProperties = null;
    ValueProperties referenceRequiredProperties = null;
    for (ValueSpecification input : inputs) {
      if (compositeProperties == null) {
        compositeProperties = input.getProperties().compose(getInputConstraints());
        referenceRequiredProperties = _requiredProperties.compose(input.getProperties());
      } else {
        ValueProperties requiredPropertyComposition = _requiredProperties.compose(input.getProperties());
        if (!requiredPropertyComposition.equals(referenceRequiredProperties)) {
          throw new IllegalArgumentException("Required property composition " + requiredPropertyComposition +
              " produced from input " + input + " differs from current required property composition " + referenceRequiredProperties + " implying incompatible property values among the inputs");
        }
        // Know that the required properties are preserved correctly, so now compose everything 
        compositeProperties = compositeProperties.compose(input.getProperties());
      }
    }
    return getResultProperties(compositeProperties);
  }

  protected ValueProperties getResultPropertiesFromInputs(final Collection<ComputedValue> inputs) {
    Collection<ValueSpecification> specs = new ArrayList<ValueSpecification>(inputs.size());
    for (ComputedValue input : inputs) {
      specs.add(input.getSpecification());
    }
    return getResultProperties(specs);
  }

}

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

  protected static ValueProperties createInputConstraints(final String[] preserve) {
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (String value : preserve) {
      builder.withOptional(value);
    }
    return builder.get();
  }

  protected static ValueProperties createResultProperties(final String[] preserve) {
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (String value : preserve) {
      builder.withAny(value);
    }
    return builder.get();
  }

  protected abstract ValueProperties getInputConstraints();

  protected abstract ValueProperties createResultProperties();

  private ValueProperties _resultProperties;

  @Override
  public void setUniqueId(final String identifier) {
    super.setUniqueId(identifier);
    _resultProperties = createResultProperties().copy().with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
  }

  protected ValueProperties getInputConstraint(final ValueRequirement desiredValue) {
    return getInputConstraints().compose(desiredValue.getConstraints());
  }

  protected ValueProperties getResultProperties() {
    return _resultProperties;
  }

  protected ValueProperties getResultProperties(final ValueSpecification inputSpec) {
    return getInputConstraints().compose(inputSpec.getProperties()).copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
  }

  protected ValueProperties getResultProperties(final Collection<?> inputs) {
    ValueProperties properties = getInputConstraints();
    for (Object input : inputs) {
      if (input instanceof ComputedValue) {
        properties = properties.compose(((ComputedValue) input).getSpecification().getProperties());
      } else if (input instanceof ValueSpecification) {
        properties = properties.compose(((ValueSpecification) input).getProperties());
      }
    }
    return properties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
  }
  
}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.lambdava.streams.Stream;
import com.opengamma.lambdava.streams.StreamI;

public class WithReplacement extends ValuePropertiesModifier {

  private StreamI<String> _propertyValues;

  public WithReplacement(String propertyName, String... propertyValues) {
    super(propertyName);
    _propertyValues = Stream.of(propertyValues);
  }

  public StreamI<String> getPropertyValues() {
    return _propertyValues;
  }

  @Override
  public ValueProperties.Builder modify(ValueProperties.Builder builder) {
    return builder.withoutAny(getPropertyName()).with(getPropertyName(), getPropertyValues().asList());
  }
}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.lambdava.streams.Stream;
import com.opengamma.lambdava.streams.StreamI;

public class With extends ValuePropertiesModifier {

  private StreamI<String> _propertyValues;

  public With(String propertyName, String... propertyValues) {
    super(propertyName);
    _propertyValues = Stream.of(propertyValues);
  }

  public StreamI<String> getPropertyValues() {
    return _propertyValues;
  }

  @Override
  public ValueProperties.Builder modify(ValueProperties.Builder builder) {
    return builder.with(getPropertyName(), getPropertyValues().asList());
  }
}

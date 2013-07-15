/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import com.opengamma.engine.value.ValueProperties;

public abstract class ValuePropertiesModifier {

  private String _propertyName;

  protected ValuePropertiesModifier(String propertyName) {
    this._propertyName = propertyName;
  }

  public final String getPropertyName() {
    return _propertyName;
  }

  public abstract ValueProperties.Builder modify(ValueProperties.Builder builder);

}

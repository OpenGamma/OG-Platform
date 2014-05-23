/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value.properties;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;

/**
 * Implementation of the {@link ValueProperties.Builder} for subtractive composition of properties.
 */
public class SubtractivePropertiesBuilder extends ValueProperties.Builder {

  private Set<String> _properties;

  private boolean _copied;

  /**
   * Creates an instance with initial missing properties.
   * 
   * @param properties the properties absent from this builder. This will not be modified - a copy will be taken where needed
   */
  public SubtractivePropertiesBuilder(final Set<String> properties) {
    _properties = properties;
    _copied = false;
  }

  /**
   * Creates an instance as a deep copy of another.
   * <p>
   * A full copy is performed rather than taking an unowned reference. The latter approach works when referencing the immutable content of an existing value property set, but not when the owner is a
   * builder as that may continue to modify the structure.
   * 
   * @param copyFrom the builder to copy from
   */
  private SubtractivePropertiesBuilder(final SubtractivePropertiesBuilder copyFrom) {
    _properties = new HashSet<String>(copyFrom._properties);
    _copied = true;
  }

  @Override
  public Builder with(final String propertyName, final String propertyValue) {
    if (_properties.contains(propertyName)) {
      throw new UnsupportedOperationException("Can't add arbitrary property values to the nearly infinite set");
    }
    return this;
  }

  @Override
  public Builder with(final String propertyName, final String... propertyValues) {
    if (_properties.contains(propertyName)) {
      throw new UnsupportedOperationException("Can't add arbitrary property values to the nearly infinite set");
    }
    return this;
  }

  @Override
  public Builder with(final String propertyName, final Collection<String> propertyValues) {
    if (_properties.contains(propertyName)) {
      throw new UnsupportedOperationException("Can't add arbitrary property values to the nearly infinite set");
    }
    return this;
  }

  @Override
  public Builder withAny(final String propertyName) {
    if (!_copied) {
      _properties = new HashSet<String>(_properties);
      _copied = true;
    }
    _properties.remove(propertyName);
    return this;
  }

  @Override
  public Builder withOptional(final String propertyName) {
    throw new UnsupportedOperationException("Can't add optional property values to the nearly infinite set");
  }

  @Override
  public Builder notOptional(final String propertyName) {
    // Nothing is ever optional
    return this;
  }

  @Override
  public Builder withoutAny(final String propertyName) {
    if (!_copied) {
      _properties = new HashSet<String>(_properties);
      _copied = true;
    }
    _properties.add(propertyName);
    return this;
  }

  @Override
  public ValueProperties get() {
    if (_properties.isEmpty()) {
      return ValueProperties.all();
    } else {
      _copied = false;
      return createSubtractive(_properties);
    }
  }

  @Override
  public Builder copy() {
    return new SubtractivePropertiesBuilder(this);
  }

}

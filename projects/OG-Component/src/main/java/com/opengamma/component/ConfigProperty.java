/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.Objects;

import com.opengamma.util.ArgumentChecker;

/**
 * A single configuration property that can be hidden.
 * <p>
 * This is used as part of a mechanism to mask sensitive data from debugging.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ConfigProperty {

  /**
   * The property key.
   */
  private final String _key;
  /**
   * The resolved property value. Null indicates no property has been defined.
   */
  private final String _value;
  /**
   * Whether the value is hidden.
   */
  private final boolean _hidden;

  //-------------------------------------------------------------------------
  /**
   * Obtains a property object consisting of a key, value and whether it is hidden.
   * 
   * @param key  the key, not null
   * @param value  the value, not null
   * @param hidden  whether the value is hidden
   * @return the property, not null
   */
  public static ConfigProperty of(String key, String value, boolean hidden) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    return new ConfigProperty(key, value, hidden);
  }

  /**
   * Obtains a property object consisting of a key, missing optional value
   * and whether it is hidden.
   *
   * @param key  the key, not null
   * @param hidden  whether the value is hidden
   * @return the property, not null
   */
  public static ConfigProperty optional(String key, boolean hidden) {
    ArgumentChecker.notNull(key, "key");
    return new ConfigProperty(key, null, hidden);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param key  the key, not null
   * @param value  the value, not null
   * @param hidden  whether the value is hidden
   */
  private ConfigProperty(String key, String value, boolean hidden) {
    _key = key;
    _value = value;
    _hidden = hidden;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the key.
   * 
   * @return the key, not null
   */
  public String getKey() {
    return _key;
  }

  /**
   * Gets the value. If no value has been defined (i.e. an optional
   * property value) and this method is called, an exception will be
   * thrown. Call {@link #isDefined()} first to avoid this.
   * 
   * @return the value, not null
   * @throws IllegalStateException if called when no property value is defined
   */
  public String getValue() {
    return _value;
  }

  /**
   * Whether the value is hidden.
   *
   * @return whether the value is hidden
   */
  public boolean isHidden() {
    return _hidden;
  }

  /**
   * Indicates whether a property value has been defined for this property.
   *
   * @return whether a property value has been defined for this property
   */
  public boolean isDefined() {
    return _value != null;
  }

  /**
   * Returns a copy of this property with a different key.
   *
   * @param key  the new key, not null
   * @return the new property, not null
   */
  public ConfigProperty withKey(String key) {
    return new ConfigProperty(key, _value, _hidden);
  }

  /**
   * Gets the loggable value.
   *
   * @return the loggable value, not null
   */
  public String loggableValue() {
    return (isHidden() ? ConfigProperties.HIDDEN : (isDefined() ? getValue() : ConfigProperties.OPTIONAL));
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ConfigProperty) {
      ConfigProperty other = (ConfigProperty) obj;
      return getKey().equals(other.getKey()) &&
          // Allow for null value
          Objects.equals(getValue(), other.getValue()) &&
          isHidden() == other.isHidden();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getKey(), getValue(), isHidden());
  }

  @Override
  public String toString() {
    return _key + "=" + loggableValue();
  }
}

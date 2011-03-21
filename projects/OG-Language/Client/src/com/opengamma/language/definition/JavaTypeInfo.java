/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import com.opengamma.util.ArgumentChecker;

/**
 * Holds as much information about a Java type as possible. This will drive parameter conversions
 * to/from the wire format.
 */
public final class JavaTypeInfo {

  /**
   * Constructs {@link JavaTypeInfo} instances.
   */
  public static final class Builder {

    private final Class<?> _rawClass;
    private boolean _allowNull;
    private boolean _hasDefaultValue;
    private Object _defaultValue;

    private Builder(final Class<?> rawClass) {
      _rawClass = rawClass;
    }

    public Builder allowNull() {
      if (!_allowNull) {
        throw new IllegalStateException();
      }
      _allowNull = true;
      return this;
    }

    public Builder defaultValue(final Object defaultValue) {
      if (_hasDefaultValue) {
        throw new IllegalStateException();
      }
      if (defaultValue == null) {
        _allowNull = true;
      } else {
        if (!_rawClass.isAssignableFrom(defaultValue.getClass())) {
          throw new IllegalArgumentException();
        }
      }
      _hasDefaultValue = true;
      _defaultValue = defaultValue;
      return this;
    }

    public JavaTypeInfo get() {
      return new JavaTypeInfo(_rawClass, _allowNull, _hasDefaultValue, _defaultValue);
    }

  }

  private final Class<?> _rawClass;
  private final boolean _allowNull;
  private final boolean _hasDefaultValue;
  private final Object _defaultValue;

  private JavaTypeInfo(final Class<?> rawClass, final boolean allowNull, final boolean hasDefaultValue,
      final Object defaultValue) {
    _rawClass = rawClass;
    _allowNull = allowNull;
    _hasDefaultValue = hasDefaultValue;
    _defaultValue = defaultValue;
  }

  public Class<?> getRawClass() {
    return _rawClass;
  }

  public boolean isAllowNull() {
    return _allowNull;
  }

  public boolean isDefaultValue() {
    return _hasDefaultValue;
  }

  public Object getDefaultValue() {
    if (!isDefaultValue()) {
      throw new IllegalStateException();
    }
    return _defaultValue;
  }

  public static Builder builder(final Class<?> rawClass) {
    ArgumentChecker.notNull(rawClass, "rawClass");
    return new Builder(rawClass);
  }

}

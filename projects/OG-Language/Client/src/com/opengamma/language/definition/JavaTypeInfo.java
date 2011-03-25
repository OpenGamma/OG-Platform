/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import org.springframework.util.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Holds as much information about a Java type as possible. This will drive parameter conversions
 * to/from the wire format.
 * 
 * @param <T> the raw Java type
 */
public final class JavaTypeInfo<T> {

  /**
   * Constructs {@link JavaTypeInfo} instances.
   */
  public static final class Builder<T> {

    private final Class<T> _rawClass;
    private boolean _allowNull;
    private boolean _hasDefaultValue;
    private T _defaultValue;

    private Builder(final Class<T> rawClass) {
      _rawClass = rawClass;
    }

    public Builder<T> allowNull() {
      if (!_allowNull) {
        throw new IllegalStateException();
      }
      _allowNull = true;
      return this;
    }

    public Builder<T> defaultValue(final T defaultValue) {
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

    public JavaTypeInfo<T> get() {
      return new JavaTypeInfo<T>(_rawClass, _allowNull, _hasDefaultValue, _defaultValue);
    }

  }

  private final Class<T> _rawClass;
  private final boolean _allowNull;
  private final boolean _hasDefaultValue;
  private final T _defaultValue;

  private JavaTypeInfo(final Class<T> rawClass, final boolean allowNull, final boolean hasDefaultValue,
      final T defaultValue) {
    _rawClass = rawClass;
    _allowNull = allowNull;
    _hasDefaultValue = hasDefaultValue;
    _defaultValue = defaultValue;
  }

  public Class<T> getRawClass() {
    return _rawClass;
  }

  public boolean isAllowNull() {
    return _allowNull;
  }

  public boolean isDefaultValue() {
    return _hasDefaultValue;
  }

  public T getDefaultValue() {
    if (!isDefaultValue()) {
      throw new IllegalStateException();
    }
    return _defaultValue;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof JavaTypeInfo<?>)) {
      return false;
    }
    final JavaTypeInfo<?> other = (JavaTypeInfo<?>) o;
    if (getRawClass() != other.getRawClass()) {
      return false;
    }
    if (isAllowNull() != other.isAllowNull()) {
      return false;
    }
    if (isDefaultValue()) {
      if (other.isDefaultValue()) {
        if (!ObjectUtils.nullSafeEquals(getDefaultValue(), other.getDefaultValue())) {
          return false;
        }
      } else {
        return false;
      }
    } else {
      if (other.isDefaultValue()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hc = getRawClass().hashCode();
    hc += (hc << 4) + (isAllowNull() ? 1 : 0);
    if (isDefaultValue()) {
      hc += (hc << 4) + 1;
      hc += (hc << 4) + ObjectUtils.nullSafeHashCode(getDefaultValue());
    } else {
      hc += (hc << 4);
      hc += (hc << 4);
    }
    return hc;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getRawClass().getName()).append('[');
    if (isAllowNull()) {
      sb.append("allow null");
    } else {
      sb.append("not null");
    }
    if (isDefaultValue()) {
      sb.append(", deafult = ").append(getDefaultValue());
    }
    sb.append(']');
    return sb.toString();
  }

  public static <T> Builder<T> builder(final Class<T> rawClass) {
    ArgumentChecker.notNull(rawClass, "rawClass");
    return new Builder<T>(rawClass);
  }

}

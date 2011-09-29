/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Holds as much information about a Java type as possible. This will drive parameter conversions
 * to/from the wire format.
 * 
 * @param <T> the raw Java type
 */
public final class JavaTypeInfo<T> {

  private static final JavaTypeInfo<Object> OBJECT = builder(Object.class).get();

  /**
   * Constructs {@link JavaTypeInfo} instances.
   */
  public static final class Builder<T> {

    private Class<T> _rawClass;
    private boolean _allowNull;
    private boolean _hasDefaultValue;
    private T _defaultValue;
    private List<JavaTypeInfo<?>> _parameter;

    private Builder(final Class<T> rawClass) {
      _rawClass = rawClass;
    }

    @SuppressWarnings("unchecked")
    public Builder<T[]> arrayOf() {
      if (_hasDefaultValue) {
        throw new IllegalStateException();
      }
      _rawClass = (Class<T>) Array.newInstance(_rawClass, 0).getClass();
      return (Builder<T[]>) this;
    }

    public Builder<T> allowNull() {
      if (_allowNull) {
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

    public Builder<T> parameter(final JavaTypeInfo<?> paramType) {
      if (_rawClass.isArray()) {
        throw new IllegalStateException();
      }
      if (_parameter == null) {
        _parameter = new LinkedList<JavaTypeInfo<?>>();
      }
      _parameter.add(paramType);
      return this;
    }

    public Builder<T> parameter(final Class<?> rawClass) {
      return parameter(builder(rawClass).get());
    }

    public JavaTypeInfo<T> get() {
      final JavaTypeInfo<?>[] parameter;
      if (_rawClass.isArray()) {
        parameter = new JavaTypeInfo<?>[] {builder(_rawClass.getComponentType()).get() };
      } else {
        if (_parameter != null) {
          parameter = _parameter.toArray(new JavaTypeInfo<?>[_parameter.size()]);
        } else {
          parameter = null;
        }
      }
      return new JavaTypeInfo<T>(_rawClass, _allowNull, _hasDefaultValue, _defaultValue, parameter);
    }

  }

  private final Class<T> _rawClass;
  private final boolean _allowNull;
  private final boolean _hasDefaultValue;
  private final T _defaultValue;
  private final JavaTypeInfo<?>[] _parameter;

  private JavaTypeInfo(final Class<T> rawClass, final boolean allowNull, final boolean hasDefaultValue,
      final T defaultValue, final JavaTypeInfo<?>[] parameter) {
    _rawClass = rawClass;
    _allowNull = allowNull;
    _hasDefaultValue = hasDefaultValue;
    _defaultValue = defaultValue;
    _parameter = parameter;
  }

  @SuppressWarnings("unchecked")
  public JavaTypeInfo<?> arrayOf() {
    return new JavaTypeInfo<Object>((Class<Object>) Array.newInstance(_rawClass, 0).getClass(), _allowNull, false, null, new JavaTypeInfo<?>[] {this });
  }

  @SuppressWarnings("unchecked")
  public JavaTypeInfo<?> withAllowNull(final boolean allowNull) {
    return new JavaTypeInfo<Object>((Class<Object>) _rawClass, allowNull, _hasDefaultValue, _defaultValue, _parameter);
  }

  public Class<T> getRawClass() {
    return _rawClass;
  }

  public boolean isAllowNull() {
    return _allowNull;
  }

  public boolean isArray() {
    return _rawClass.isArray();
  }

  public int getArrayDimension() {
    int dimensions = 0;
    JavaTypeInfo<?> t = this;
    while (t.isArray()) {
      dimensions++;
      t = t._parameter[0];
    }
    return dimensions;
  }

  public JavaTypeInfo<?> getArrayElementType() {
    if (!isArray()) {
      throw new IllegalStateException();
    }
    return _parameter[0];
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

  public JavaTypeInfo<?> getParameterizedType(final int index) {
    if ((_parameter != null) && (index < _parameter.length)) {
      return _parameter[index];
    } else {
      return OBJECT;
    }
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
      sb.append(", default = ").append(getDefaultValue());
    }
    sb.append(']');
    return sb.toString();
  }

  /**
   * Return a simplified {@link #toString} that is suitable for the client as it may be shown to a user.
   * The raw {@code toString} should contain sufficient extra information to be useful in a diagnostic
   * log but may not be particularly pretty.
   * 
   * @return the string
   */
  public String toClientString() {
    return getRawClass().getSimpleName();
  }

  public static <T> Builder<T> builder(final Class<T> rawClass) {
    ArgumentChecker.notNull(rawClass, "rawClass");
    return new Builder<T>(rawClass);
  }

}

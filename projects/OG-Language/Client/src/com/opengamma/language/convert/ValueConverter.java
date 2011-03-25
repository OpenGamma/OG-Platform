/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.language.Value;
import com.opengamma.language.ValueUtil;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.InvalidConversionException;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Basic conversions to/from the {@link Value} type.
 */
@SuppressWarnings("unchecked")
public final class ValueConverter implements TypeConverter {

  private static final JavaTypeInfo<?> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<?> BOOLEAN = JavaTypeInfo.builder(Boolean.class).get();
  private static final JavaTypeInfo<?> INTEGER = JavaTypeInfo.builder(Integer.class).get();
  private static final JavaTypeInfo<?> DOUBLE = JavaTypeInfo.builder(Double.class).get();
  private static final JavaTypeInfo<?> STRING = JavaTypeInfo.builder(String.class).get();
  private static final JavaTypeInfo<?> MESSAGE = JavaTypeInfo.builder(FudgeFieldContainer.class).get();

  private static final List<JavaTypeInfo<?>> TO_VALUE = Arrays.asList(BOOLEAN, INTEGER, DOUBLE, STRING, MESSAGE);
  private static final List<JavaTypeInfo<?>> FROM_VALUE = Collections.<JavaTypeInfo<?>> singletonList(VALUE);

  @Override
  public boolean canConvertTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return true;
    } else {
      for (JavaTypeInfo<?> toData : TO_VALUE) {
        if (clazz == toData.getRawClass()) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public List<JavaTypeInfo<?>> getConversionsTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return TO_VALUE;
    } else {
      return FROM_VALUE;
    }
  }

  @Override
  public boolean canConvert(SessionContext sessionContext, Object fromValue, JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      for (JavaTypeInfo<?> toData : TO_VALUE) {
        if (toData.getRawClass().isAssignableFrom(fromValue.getClass())) {
          return true;
        }
      }
    } else {
      if (fromValue instanceof Value) {
        final Value value = (Value) fromValue;
        if (clazz == Boolean.class) {
          return value.getBoolValue() != null;
        } else if (clazz == Integer.class) {
          return value.getIntValue() != null;
        } else if (clazz == Double.class) {
          return value.getDoubleValue() != null;
        } else if (clazz == String.class) {
          return value.getStringValue() != null;
        } else if (clazz == FudgeFieldContainer.class) {
          return value.getMessageValue() != null;
        }
      }
    }
    return false;
  }

  @Override
  public <T> T convert(SessionContext sessionContext, Object fromValue, JavaTypeInfo<T> type) {
    final Class<T> clazz = type.getRawClass();
    if (clazz == Value.class) {
      if (fromValue instanceof Boolean) {
        return (T) ValueUtil.of((Boolean) fromValue);
      } else if (fromValue instanceof Integer) {
        return (T) ValueUtil.of((Integer) fromValue);
      } else if (fromValue instanceof Double) {
        return (T) ValueUtil.of((Double) fromValue);
      } else if (fromValue instanceof String) {
        return (T) ValueUtil.of((String) fromValue);
      } else if (fromValue instanceof FudgeFieldContainer) {
        return (T) ValueUtil.of((FudgeFieldContainer) fromValue);
      }
    } else {
      final Value value = (Value) fromValue;
      if (clazz == Boolean.class) {
        return (T) value.getBoolValue();
      } else if (clazz == Integer.class) {
        return (T) value.getIntValue();
      } else if (clazz == Double.class) {
        return (T) value.getDoubleValue();
      } else if (clazz == String.class) {
        return (T) value.getStringValue();
      } else if (clazz == FudgeFieldContainer.class) {
        return (T) value.getMessageValue();
      }
    }
    throw new InvalidConversionException(fromValue, type);
  }

  @Override
  public String toString() {
    return TypeConverter.class.getSimpleName() + "[to/from " + Value.class.getName() + "]";
  }

}

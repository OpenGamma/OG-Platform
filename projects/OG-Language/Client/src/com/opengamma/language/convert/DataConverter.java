/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.Value;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.InvalidConversionException;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Basic conversions to/from the {@link Data} type.
 */
@SuppressWarnings("unchecked")
public final class DataConverter implements TypeConverter {

  private static final JavaTypeInfo<?> DATA = JavaTypeInfo.builder(Data.class).get();
  private static final JavaTypeInfo<?> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<?> VALUE_1 = JavaTypeInfo.builder(Value[].class).get();
  private static final JavaTypeInfo<?> VALUE_2 = JavaTypeInfo.builder(Value[][].class).get();

  private static final List<JavaTypeInfo<?>> TO_DATA = Arrays.asList(VALUE, VALUE_1, VALUE_2);
  private static final List<JavaTypeInfo<?>> FROM_DATA = Collections.<JavaTypeInfo<?>> singletonList(DATA);

  @Override
  public boolean canConvertTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Data.class) {
      return true;
    } else {
      for (JavaTypeInfo<?> toData : TO_DATA) {
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
    if (clazz == Data.class) {
      return TO_DATA;
    } else {
      return FROM_DATA;
    }
  }

  @Override
  public boolean canConvert(SessionContext sessionContext, Object fromValue, JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Data.class) {
      for (JavaTypeInfo<?> toData : TO_DATA) {
        if (toData.getRawClass().isAssignableFrom(fromValue.getClass())) {
          return true;
        }
      }
    } else {
      if (fromValue instanceof Data) {
        final Data dataValue = (Data) fromValue;
        if (clazz == Value.class) {
          return (dataValue.getSingle() != null);
        } else if (clazz == Value[].class) {
          return (dataValue.getLinear() != null);
        } else if (clazz == Value[][].class) {
          return (dataValue.getMatrix() != null);
        }
      }
    }
    return false;
  }

  @Override
  public <T> T convert(SessionContext sessionContext, Object value, JavaTypeInfo<T> type) {
    final Class<T> clazz = type.getRawClass();
    if (clazz == Data.class) {
      if (value instanceof Value) {
        return (T) DataUtil.of((Value) value);
      } else if (value instanceof Value[]) {
        return (T) DataUtil.of((Value[]) value);
      } else if (value instanceof Value[][]) {
        return (T) DataUtil.of((Value[][]) value);
      }
    } else {
      final Data dataValue = (Data) value;
      if (clazz == Value.class) {
        return (T) dataValue.getSingle();
      } else if (clazz == Value[].class) {
        return (T) dataValue.getLinear();
      } else if (clazz == Value[][].class) {
        return (T) dataValue.getMatrix();
      }
    }
    throw new InvalidConversionException(value, type);
  }

  @Override
  public String toString() {
    return TypeConverter.class.getSimpleName() + "[to/from " + Data.class.getName() + "]";
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.List;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.language.Value;
import com.opengamma.language.ValueUtil;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Basic conversions to/from the {@link Value} type.
 */
public final class ValueConverter implements TypeConverter {

  private static final JavaTypeInfo<Value> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<Boolean> BOOLEAN = JavaTypeInfo.builder(Boolean.class).get();
  private static final JavaTypeInfo<Integer> INTEGER = JavaTypeInfo.builder(Integer.class).get();
  private static final JavaTypeInfo<Double> DOUBLE = JavaTypeInfo.builder(Double.class).get();
  private static final JavaTypeInfo<String> STRING = JavaTypeInfo.builder(String.class).get();
  private static final JavaTypeInfo<FudgeFieldContainer> MESSAGE = JavaTypeInfo.builder(FudgeFieldContainer.class).get();

  private static final List<JavaTypeInfo<?>> TO_VALUE = JavaTypeInfo.asList(BOOLEAN, INTEGER, DOUBLE, STRING, MESSAGE);
  private static final List<JavaTypeInfo<?>> FROM_VALUE = JavaTypeInfo.asList(VALUE);

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
  public String toString() {
    return TypeConverter.class.getSimpleName() + "[to/from " + Value.class.getName() + "]";
  }

  @Override
  public void convertValue(ValueConversionContext conversionContext, Object value, JavaTypeInfo<?> type) {
    final Class<?> clazz = type.getRawClass();
    if (clazz == Value.class) {
      if (value instanceof Boolean) {
        conversionContext.setResult(1, ValueUtil.of((Boolean) value));
      } else if (value instanceof Integer) {
        conversionContext.setResult(1, ValueUtil.of((Integer) value));
      } else if (value instanceof Double) {
        conversionContext.setResult(1, ValueUtil.of((Double) value));
      } else if (value instanceof String) {
        conversionContext.setResult(1, ValueUtil.of((String) value));
      } else if (value instanceof FudgeFieldContainer) {
        conversionContext.setResult(1, ValueUtil.of((FudgeFieldContainer) value));
      } else {
        conversionContext.setFail();
      }
      return;
    } else {
      if (value instanceof Value) {
        final Value valueValue = (Value) value;
        if (clazz == Boolean.class) {
          conversionContext.setResult(1, valueValue.getBoolValue());
        } else if (clazz == Integer.class) {
          conversionContext.setResult(1, valueValue.getIntValue());
        } else if (clazz == Double.class) {
          conversionContext.setResult(1, valueValue.getDoubleValue());
        } else if (clazz == String.class) {
          conversionContext.setResult(1, valueValue.getStringValue());
        } else if (clazz == FudgeFieldContainer.class) {
          conversionContext.setResult(1, valueValue.getMessageValue());
        } else {
          conversionContext.setFail();
        }
        return;
      }
    }
    conversionContext.setFail();
  }

}

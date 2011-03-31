/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Map;

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

  private static final TypeMap TO_VALUE = TypeMap.of(ZERO_LOSS, BOOLEAN, INTEGER, DOUBLE, STRING, MESSAGE);
  private static final TypeMap FROM_VALUE = TypeMap.of(ZERO_LOSS, VALUE);

  @Override
  public boolean canConvertTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return true;
    } else {
      for (JavaTypeInfo<?> toData : TO_VALUE.keySet()) {
        if (clazz == toData.getRawClass()) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(JavaTypeInfo<?> targetType) {
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
        conversionContext.setResult(ValueUtil.of((Boolean) value));
      } else if (value instanceof Integer) {
        conversionContext.setResult(ValueUtil.of((Integer) value));
      } else if (value instanceof Double) {
        conversionContext.setResult(ValueUtil.of((Double) value));
      } else if (value instanceof String) {
        conversionContext.setResult(ValueUtil.of((String) value));
      } else if (value instanceof FudgeFieldContainer) {
        conversionContext.setResult(ValueUtil.of((FudgeFieldContainer) value));
      } else {
        conversionContext.setFail();
      }
      return;
    } else {
      if (value instanceof Value) {
        final Value valueValue = (Value) value;
        if (clazz == Boolean.class) {
          conversionContext.setResult(valueValue.getBoolValue());
        } else if (clazz == Integer.class) {
          conversionContext.setResult(valueValue.getIntValue());
        } else if (clazz == Double.class) {
          conversionContext.setResult(valueValue.getDoubleValue());
        } else if (clazz == String.class) {
          conversionContext.setResult(valueValue.getStringValue());
        } else if (clazz == FudgeFieldContainer.class) {
          conversionContext.setResult(valueValue.getMessageValue());
        } else {
          conversionContext.setFail();
        }
        return;
      }
    }
    conversionContext.setFail();
  }

}

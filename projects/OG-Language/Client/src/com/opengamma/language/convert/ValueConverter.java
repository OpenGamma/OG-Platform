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
  public void convertValue(ValueConversionContext conversionContext, Object valueObject, JavaTypeInfo<?> type) {
    final Class<?> clazz = type.getRawClass();
    if (clazz == Value.class) {
      if (valueObject instanceof Boolean) {
        conversionContext.setResult(ValueUtil.of((Boolean) valueObject));
      } else if (valueObject instanceof Integer) {
        conversionContext.setResult(ValueUtil.of((Integer) valueObject));
      } else if (valueObject instanceof Double) {
        conversionContext.setResult(ValueUtil.of((Double) valueObject));
      } else if (valueObject instanceof String) {
        conversionContext.setResult(ValueUtil.of((String) valueObject));
      } else if (valueObject instanceof FudgeFieldContainer) {
        conversionContext.setResult(ValueUtil.of((FudgeFieldContainer) valueObject));
      } else {
        conversionContext.setFail();
      }
      return;
    } else {
      if (valueObject instanceof Value) {
        final Value value = (Value) valueObject;
        if (clazz == Boolean.class) {
          if (type.isAllowNull() || (value.getBoolValue() != null)) {
            conversionContext.setResult(value.getBoolValue());
          } else {
            conversionContext.setFail();
          }
        } else if (clazz == Integer.class) {
          if (type.isAllowNull() || (value.getIntValue() != null)) {
            conversionContext.setResult(value.getIntValue());
          } else {
            conversionContext.setFail();
          }
        } else if (clazz == Double.class) {
          if (type.isAllowNull() || (value.getDoubleValue() != null)) {
            conversionContext.setResult(value.getDoubleValue());
          } else {
            conversionContext.setFail();
          }
        } else if (clazz == String.class) {
          if (type.isAllowNull() || (value.getStringValue() != null)) {
            conversionContext.setResult(value.getStringValue());
          } else {
            conversionContext.setFail();
          }
        } else if (clazz == FudgeFieldContainer.class) {
          if (type.isAllowNull() || (value.getMessageValue() != null)) {
            conversionContext.setResult(value.getMessageValue());
          } else {
            conversionContext.setFail();
          }
        } else {
          conversionContext.setFail();
        }
        return;
      }
    }
    conversionContext.setFail();
  }

}

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

  private static final JavaTypeInfo<Value> VALUE_NOT_NULL = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<Value> VALUE_NULL = JavaTypeInfo.builder(Value.class).allowNull().get();
  private static final JavaTypeInfo<Boolean> BOOLEAN_NOT_NULL = JavaTypeInfo.builder(Boolean.class).get();
  private static final JavaTypeInfo<Boolean> BOOLEAN_NULL = JavaTypeInfo.builder(Boolean.class).allowNull().get();
  private static final JavaTypeInfo<Integer> INTEGER_NOT_NULL = JavaTypeInfo.builder(Integer.class).get();
  private static final JavaTypeInfo<Integer> INTEGER_NULL = JavaTypeInfo.builder(Integer.class).allowNull().get();
  private static final JavaTypeInfo<Double> DOUBLE_NOT_NULL = JavaTypeInfo.builder(Double.class).get();
  private static final JavaTypeInfo<Double> DOUBLE_NULL = JavaTypeInfo.builder(Double.class).allowNull().get();
  private static final JavaTypeInfo<String> STRING_NOT_NULL = JavaTypeInfo.builder(String.class).get();
  private static final JavaTypeInfo<String> STRING_NULL = JavaTypeInfo.builder(String.class).allowNull().get();
  private static final JavaTypeInfo<FudgeFieldContainer> MESSAGE_NOT_NULL = JavaTypeInfo.builder(FudgeFieldContainer.class).get();
  private static final JavaTypeInfo<FudgeFieldContainer> MESSAGE_NULL = JavaTypeInfo.builder(FudgeFieldContainer.class).allowNull().get();

  private static final TypeMap TO_VALUE_NOT_NULL = TypeMap.of(ZERO_LOSS, BOOLEAN_NOT_NULL, INTEGER_NOT_NULL, DOUBLE_NOT_NULL, STRING_NOT_NULL, MESSAGE_NOT_NULL);
  private static final TypeMap TO_VALUE_ALLOW_NULL = TypeMap.of(ZERO_LOSS, BOOLEAN_NULL, INTEGER_NULL, DOUBLE_NULL, STRING_NULL, MESSAGE_NULL);
  private static final TypeMap FROM_VALUE_NOT_NULL = TypeMap.of(ZERO_LOSS, VALUE_NOT_NULL);
  private static final TypeMap FROM_VALUE_ALLOW_NULL = TypeMap.of(ZERO_LOSS, VALUE_NULL);

  @Override
  public boolean canConvertTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return true;
    } else {
      for (JavaTypeInfo<?> toData : (targetType.isAllowNull() ? TO_VALUE_ALLOW_NULL : TO_VALUE_NOT_NULL).keySet()) {
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
      return targetType.isAllowNull() ? TO_VALUE_ALLOW_NULL : TO_VALUE_NOT_NULL;
    } else {
      return targetType.isAllowNull() ? FROM_VALUE_ALLOW_NULL : FROM_VALUE_NOT_NULL;
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
        if (ValueUtil.isNull(value)) {
          if (type.isAllowNull()) {
            conversionContext.setResult(null);
          } else {
            conversionContext.setFail();
          }
        } else if (clazz == Boolean.class) {
          if (value.getBoolValue() != null) {
            conversionContext.setResult(value.getBoolValue());
          } else {
            conversionContext.setFail();
          }
        } else if (clazz == Integer.class) {
          if (value.getIntValue() != null) {
            conversionContext.setResult(value.getIntValue());
          } else {
            conversionContext.setFail();
          }
        } else if (clazz == Double.class) {
          if (value.getDoubleValue() != null) {
            conversionContext.setResult(value.getDoubleValue());
          } else {
            conversionContext.setFail();
          }
        } else if (clazz == String.class) {
          if (value.getStringValue() != null) {
            conversionContext.setResult(value.getStringValue());
          } else {
            conversionContext.setFail();
          }
        } else if (clazz == FudgeFieldContainer.class) {
          if (value.getMessageValue() != null) {
            conversionContext.setResult(value.getMessageValue());
          } else {
            conversionContext.setFail();
          }
        } else {
          conversionContext.setFail();
        }
        return;
      } else if (valueObject == null) {
        if (type.isAllowNull()) {
          conversionContext.setResult(null);
          return;
        }
      }
    }
    conversionContext.setFail();
  }

}

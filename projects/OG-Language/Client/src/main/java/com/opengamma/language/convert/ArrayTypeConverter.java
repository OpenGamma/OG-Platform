/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.lang.reflect.Array;
import java.util.Map;

import com.opengamma.language.Value;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Converts arrays from X[] to Y[]
 */
public class ArrayTypeConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final ArrayTypeConverter INSTANCE = new ArrayTypeConverter();

  private static final JavaTypeInfo<Object> OBJECT = JavaTypeInfo.builder(Object.class).get();
  private static final JavaTypeInfo<Value[]> VALUE_1 = JavaTypeInfo.builder(Value.class).arrayOf().get();
  private static final JavaTypeInfo<Value[]> VALUE_1_ALLOW_NULL = JavaTypeInfo.builder(Value.class).arrayOf().allowNull().get();
  private static final JavaTypeInfo<Value[][]> VALUE_2 = JavaTypeInfo.builder(Value.class).arrayOf().arrayOf().get();
  private static final JavaTypeInfo<Value[][]> VALUE_2_ALLOW_NULL = JavaTypeInfo.builder(Value.class).arrayOf().arrayOf().allowNull().get();
  private static final Map<JavaTypeInfo<?>, Integer> TO_OBJECT = TypeMap.ofWeighted(ZERO_LOSS, VALUE_1, VALUE_2);
  private static final Map<JavaTypeInfo<?>, Integer> TO_OBJECT_ALLOW_NULL = TypeMap.ofWeighted(ZERO_LOSS, VALUE_1_ALLOW_NULL, VALUE_2_ALLOW_NULL);
  private static final Map<JavaTypeInfo<?>, Integer> TO_VALUE = TypeMap.of(ZERO_LOSS, OBJECT);

  protected ArrayTypeConverter() {
  }

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return targetType.isArray() && (targetType.getArrayDimension() > 1 || !targetType.getArrayElementType().getRawClass().isPrimitive());
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (value == null) {
      if (type.isDefaultValue()) {
        conversionContext.setResult(type.getDefaultValue());
      } else if (type.isAllowNull()) {
        conversionContext.setResult(null);
      } else {
        conversionContext.setFail();
      }
      return;
    }
    if (!value.getClass().isArray()) {
      conversionContext.setFail();
      return;
    }
    final JavaTypeInfo<?> element = type.getArrayElementType();
    final int length = Array.getLength(value);
    final Object result = Array.newInstance(element.getRawClass(), length);
    for (int i = 0; i < length; i++) {
      final Object sourceValue = Array.get(value, i);
      if (sourceValue != null) {
        conversionContext.convertValue(sourceValue, element);
        if (conversionContext.isFailed()) {
          conversionContext.setFail();
          return;
        }
        Array.set(result, i, conversionContext.getResult());
      } else {
        if (!element.isAllowNull()) {
          conversionContext.setFail();
          return;
        }
      }
    }
    conversionContext.setResult(result);
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    if ((targetType.getRawClass() == Value[].class) || (targetType.getRawClass() == Value[][].class)) {
      return TO_VALUE;
    } else {
      return targetType.isAllowNull() ? TO_OBJECT_ALLOW_NULL : TO_OBJECT;
    }
  }

}

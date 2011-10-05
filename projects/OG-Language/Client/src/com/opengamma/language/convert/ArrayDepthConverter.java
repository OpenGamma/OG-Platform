/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.lang.reflect.Array;
import java.util.Map;

import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Converts arrays with a single element from X[] to X, or any value X to the single element array X[]
 */
public class ArrayDepthConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final ArrayDepthConverter INSTANCE = new ArrayDepthConverter();

  protected ArrayDepthConverter() {
  }

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return true;
  }

  private static int getArrayDimension(Class<?> clazz) {
    int dimensions = 0;
    while (clazz.isArray()) {
      dimensions++;
      clazz = clazz.getComponentType();
    }
    return dimensions;
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (value == null) {
      conversionContext.setFail();
      return;
    }
    final int targetDimensions = type.getArrayDimension();
    final int sourceDimensions = getArrayDimension(value.getClass());
    if (targetDimensions == sourceDimensions + 1) {
      final Object result = Array.newInstance(type.getArrayElementType().getRawClass(), 1);
      Array.set(result, 0, value);
      conversionContext.setResult(result);
    } else if (targetDimensions == sourceDimensions - 1) {
      if (Array.getLength(value) == 1) {
        conversionContext.setResult(Array.get(value, 0));
      } else {
        conversionContext.setFail();
      }
    } else {
      conversionContext.setFail();
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    TypeMap conversions = TypeMap.builder();
    if (targetType.isArray()) {
      conversions.with(ZERO_LOSS, targetType.getArrayElementType());
    }
    conversions.with(ZERO_LOSS, targetType.arrayOf());
    return conversions;
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.language.Value;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Converts a map of A->B to Value[][2]
 */
public class MapConverter extends AbstractTypeConverter {

  // TODO: handle nulls e.g. Value[][2]/null -> Map/null or Map/empty if nulls not allowed

  private static final JavaTypeInfo<Value> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<Value[][]> VALUES = JavaTypeInfo.builder(Value[][].class).get();
  @SuppressWarnings("unchecked")
  private static final JavaTypeInfo<Map> MAP = JavaTypeInfo.builder(Map.class).get();

  private static final TypeMap TO_MAP = TypeMap.of(ZERO_LOSS, VALUES);
  private static final TypeMap FROM_MAP = TypeMap.of(ZERO_LOSS, MAP);

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return (targetType.getRawClass() == Map.class) || (targetType.getRawClass() == Value[][].class);
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (type.getRawClass() == Map.class) {
      // Converting from Values[][] to Map
      final Value[][] values = (Value[][]) value;
      final JavaTypeInfo<?> keyType = type.getParameterizedType(0);
      final JavaTypeInfo<?> valueType = type.getParameterizedType(1);
      final Map<Object, Object> result = Maps.newHashMapWithExpectedSize(values.length);
      for (Value[] entry : values) {
        if (entry.length != 2) {
          conversionContext.setFail();
          return;
        }
        conversionContext.convertValue(entry[0], keyType);
        if (conversionContext.isFailed()) {
          conversionContext.setFail();
          return;
        }
        final Object key = conversionContext.getResult();
        conversionContext.convertValue(entry[1], valueType);
        if (conversionContext.isFailed()) {
          conversionContext.setFail();
          return;
        }
        result.put(key, conversionContext.getResult());
      }
      conversionContext.setResult(result);
    } else {
      // Converting from Map to Values[][]
      final Map<?, ?> map = (Map<?, ?>) value;
      final Value[][] result = new Value[map.size()][2];
      int i = 0;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (entry.getKey() == null) {
          result[i][0] = new Value();
        } else {
          conversionContext.convertValue(entry.getKey(), VALUE);
          if (conversionContext.isFailed()) {
            conversionContext.setFail();
            return;
          }
          result[i][0] = conversionContext.getResult();
        }
        if (entry.getValue() == null) {
          result[i++][1] = new Value();
        } else {
          conversionContext.convertValue(entry.getValue(), VALUE);
          if (conversionContext.isFailed()) {
            conversionContext.setFail();
            return;
          }
          result[i++][1] = conversionContext.getResult();
        }
      }
      conversionContext.setResult(result);
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    if (targetType.getRawClass() == Map.class) {
      return TO_MAP;
    } else {
      return FROM_MAP;
    }
  }

}

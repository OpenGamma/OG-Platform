/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.language.Value;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Converts a set of A to Value[]
 */
public class SetConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final SetConverter INSTANCE = new SetConverter();

  private static final JavaTypeInfo<Value> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<Value[]> VALUES = JavaTypeInfo.builder(Value[].class).get();
  private static final JavaTypeInfo<Value[]> VALUES_ALLOW_NULL = JavaTypeInfo.builder(Value[].class).allowNull().get();
  @SuppressWarnings("unchecked")
  private static final JavaTypeInfo<Set> SET = JavaTypeInfo.builder(Set.class).get();
  @SuppressWarnings("unchecked")
  private static final JavaTypeInfo<Set> SET_ALLOW_NULL = JavaTypeInfo.builder(Set.class).allowNull().get();

  private static final TypeMap TO_SET = TypeMap.of(ZERO_LOSS, VALUES);
  private static final TypeMap FROM_SET = TypeMap.of(ZERO_LOSS, SET);
  private static final TypeMap TO_SET_ALLOW_NULL = TypeMap.of(ZERO_LOSS, VALUES_ALLOW_NULL);
  private static final TypeMap FROM_SET_ALLOW_NULL = TypeMap.of(ZERO_LOSS, SET_ALLOW_NULL);

  protected SetConverter() {
  }

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return (targetType.getRawClass() == Set.class) || (targetType.getRawClass() == Value[].class);
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if ((value == null) && type.isAllowNull()) {
      conversionContext.setResult(null);
      return;
    }
    if (type.getRawClass() == Set.class) {
      // Converting from Values[] to Set
      final Value[] values = (Value[]) value;
      final JavaTypeInfo<?> setType = type.getParameterizedType(0);
      final Set<Object> result = Sets.newHashSetWithExpectedSize(values.length);
      for (Value entry : values) {
        conversionContext.convertValue(entry, setType);
        if (conversionContext.isFailed()) {
          conversionContext.setFail();
          return;
        }
        result.add(conversionContext.getResult());
      }
      conversionContext.setResult(result);
    } else {
      // Converting from Set to Values[]
      final Set<?> set = (Set<?>) value;
      final Value[] result = new Value[set.size()];
      int i = 0;
      for (Object entry : set) {
        if (entry == null) {
          result[i++] = new Value();
        } else {
          conversionContext.convertValue(entry, VALUE);
          if (conversionContext.isFailed()) {
            conversionContext.setFail();
            return;
          }
          result[i++] = conversionContext.getResult();
        }
      }
      // TODO: be nice to sort the result
      conversionContext.setResult(result);
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    if (targetType.getRawClass() == Set.class) {
      return targetType.isAllowNull() ? TO_SET_ALLOW_NULL : TO_SET;
    } else {
      return targetType.isAllowNull() ? FROM_SET_ALLOW_NULL : FROM_SET;
    }
  }

}

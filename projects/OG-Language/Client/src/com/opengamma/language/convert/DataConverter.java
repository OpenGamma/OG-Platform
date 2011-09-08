/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Map;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Basic conversions to/from the {@link Data} type.
 */
public final class DataConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final DataConverter INSTANCE = new DataConverter();

  private static final JavaTypeInfo<Data> DATA = JavaTypeInfo.builder(Data.class).get();
  private static final JavaTypeInfo<Value> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<Value[]> VALUE_1 = JavaTypeInfo.builder(Value[].class).get();
  private static final JavaTypeInfo<Value[][]> VALUE_2 = JavaTypeInfo.builder(Value[][].class).get();

  private static final TypeMap TO_DATA = TypeMap.ofWeighted(ZERO_LOSS, VALUE, VALUE_1, VALUE_2);
  private static final TypeMap FROM_DATA = TypeMap.of(ZERO_LOSS, DATA);

  protected DataConverter() {
  }

  @Override
  public boolean canConvertTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Data.class) {
      return true;
    } else {
      for (JavaTypeInfo<?> toData : TO_DATA.keySet()) {
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
    if (clazz == Data.class) {
      return TO_DATA;
    } else {
      return FROM_DATA;
    }
  }

  @Override
  public String toString() {
    return TypeConverter.class.getSimpleName() + "[to/from " + Data.class.getName() + "]";
  }

  @Override
  public void convertValue(ValueConversionContext conversionContext, Object value, JavaTypeInfo<?> type) {
    final Class<?> clazz = type.getRawClass();
    if (clazz == Data.class) {
      if (value instanceof Value) {
        conversionContext.setResult(DataUtils.of((Value) value));
      } else if (value instanceof Value[]) {
        conversionContext.setResult(DataUtils.of((Value[]) value));
      } else if (value instanceof Value[][]) {
        conversionContext.setResult(DataUtils.of((Value[][]) value));
      } else {
        conversionContext.setFail();
      }
      return;
    }
    if (value instanceof Data) {
      final Data dataValue = (Data) value;
      if (dataValue.getSingle() != null) {
        if (clazz == Value.class) {
          conversionContext.setResult(dataValue.getSingle());
          return;
        } else if (type.isAllowNull() && ValueUtils.isNull(dataValue.getSingle())) {
          conversionContext.setResult(null);
          return;
        }
      } else if (dataValue.getLinear() != null) {
        if (clazz == Value[].class) {
          conversionContext.setResult(dataValue.getLinear());
          return;
        }
      } else if (dataValue.getMatrix() != null) {
        if (clazz == Value[][].class) {
          conversionContext.setResult(dataValue.getMatrix());
          return;
        }
      } else {
        if (type.isAllowNull()) {
          conversionContext.setResult(null);
          return;
        }
      }
    }
    conversionContext.setFail();
  }

}

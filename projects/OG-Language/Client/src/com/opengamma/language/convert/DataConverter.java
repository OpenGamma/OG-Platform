/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.Value;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Basic conversions to/from the {@link Data} type.
 */
public final class DataConverter implements TypeConverter {

  private static final JavaTypeInfo<Data> DATA = JavaTypeInfo.builder(Data.class).get();
  private static final JavaTypeInfo<Value> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<Value[]> VALUE_1 = JavaTypeInfo.builder(Value[].class).get();
  private static final JavaTypeInfo<Value[][]> VALUE_2 = JavaTypeInfo.builder(Value[][].class).get();

  private static final List<JavaTypeInfo<?>> TO_DATA = JavaTypeInfo.asList(VALUE, VALUE_1, VALUE_2);
  private static final List<JavaTypeInfo<?>> FROM_DATA = JavaTypeInfo.asList(DATA);

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
  public String toString() {
    return TypeConverter.class.getSimpleName() + "[to/from " + Data.class.getName() + "]";
  }

  @Override
  public void convertValue(ValueConversionContext conversionContext, Object value, JavaTypeInfo<?> type) {
    final Class<?> clazz = type.getRawClass();
    if (clazz == Data.class) {
      if (value instanceof Value) {
        conversionContext.setResult(1, DataUtil.of((Value) value));
      } else if (value instanceof Value[]) {
        conversionContext.setResult(1, DataUtil.of((Value[]) value));
      } else if (value instanceof Value[][]) {
        conversionContext.setResult(1, DataUtil.of((Value[][]) value));
      } else {
        conversionContext.setFail();
      }
      return;
    }
    if (value instanceof Data) {
      final Data dataValue = (Data) value;
      if (clazz == Value.class) {
        if (dataValue.getSingle() != null) {
          conversionContext.setResult(1, dataValue.getSingle());
          return;
        }
      } else if (clazz == Value[].class) {
        if (dataValue.getLinear() != null) {
          conversionContext.setResult(1, dataValue.getLinear());
          return;
        }
      } else if (clazz == Value[][].class) {
        if (dataValue.getMatrix() != null) {
          conversionContext.setResult(1, dataValue.getMatrix());
          return;
        }
      }
    }
    conversionContext.setFail();
  }

}

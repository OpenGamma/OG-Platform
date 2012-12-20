/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Unbox the primitive types.
 */
public class BoxingConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<Boolean> BOOLEAN_PRIMITIVE = JavaTypeInfo.builder(boolean.class).get();
  private static final JavaTypeInfo<Boolean> BOOLEAN_OBJECT = JavaTypeInfo.builder(Boolean.class).get();
  private static final JavaTypeInfo<Byte> BYTE_PRIMITIVE = JavaTypeInfo.builder(byte.class).get();
  private static final JavaTypeInfo<Byte> BYTE_OBJECT = JavaTypeInfo.builder(Byte.class).get();
  private static final JavaTypeInfo<Character> CHARACTER_PRIMITIVE = JavaTypeInfo.builder(char.class).get();
  private static final JavaTypeInfo<Character> CHARACTER_OBJECT = JavaTypeInfo.builder(Character.class).get();
  private static final JavaTypeInfo<Double> DOUBLE_PRIMITIVE = JavaTypeInfo.builder(double.class).get();
  private static final JavaTypeInfo<Double> DOUBLE_OBJECT = JavaTypeInfo.builder(Double.class).get();
  private static final JavaTypeInfo<Float> FLOAT_PRIMITIVE = JavaTypeInfo.builder(float.class).get();
  private static final JavaTypeInfo<Float> FLOAT_OBJECT = JavaTypeInfo.builder(Float.class).get();
  private static final JavaTypeInfo<Integer> INTEGER_PRIMITIVE = JavaTypeInfo.builder(int.class).get();
  private static final JavaTypeInfo<Integer> INTEGER_OBJECT = JavaTypeInfo.builder(Integer.class).get();
  private static final JavaTypeInfo<Long> LONG_PRIMITIVE = JavaTypeInfo.builder(long.class).get();
  private static final JavaTypeInfo<Long> LONG_OBJECT = JavaTypeInfo.builder(Long.class).get();
  private static final JavaTypeInfo<Short> SHORT_PRIMITIVE = JavaTypeInfo.builder(short.class).get();
  private static final JavaTypeInfo<Short> SHORT_OBJECT = JavaTypeInfo.builder(Short.class).get();

  private static final Action<?, ?> s_identity = new Action<Object, Object>() {
    @Override
    protected Object convert(Object value) {
      return value;
    }
  };

  /**
   * Default instance.
   */
  public static final BoxingConverter INSTANCE = new BoxingConverter();

  @SuppressWarnings("unchecked")
  private static <T> Action<T, T> identity() {
    return (Action<T, T>) s_identity;
  }

  protected BoxingConverter() {
    conversion(ZERO_LOSS, BOOLEAN_PRIMITIVE, BOOLEAN_OBJECT, BoxingConverter.<Boolean>identity(), BoxingConverter.<Boolean>identity());
    conversion(ZERO_LOSS, BYTE_PRIMITIVE, BYTE_OBJECT, BoxingConverter.<Byte>identity(), BoxingConverter.<Byte>identity());
    conversion(ZERO_LOSS, CHARACTER_PRIMITIVE, CHARACTER_OBJECT, BoxingConverter.<Character>identity(), BoxingConverter.<Character>identity());
    conversion(ZERO_LOSS, DOUBLE_PRIMITIVE, DOUBLE_OBJECT, BoxingConverter.<Double>identity(), BoxingConverter.<Double>identity());
    conversion(ZERO_LOSS, FLOAT_PRIMITIVE, FLOAT_OBJECT, BoxingConverter.<Float>identity(), BoxingConverter.<Float>identity());
    conversion(ZERO_LOSS, INTEGER_PRIMITIVE, INTEGER_OBJECT, BoxingConverter.<Integer>identity(), BoxingConverter.<Integer>identity());
    conversion(ZERO_LOSS, LONG_PRIMITIVE, LONG_OBJECT, BoxingConverter.<Long>identity(), BoxingConverter.<Long>identity());
    conversion(ZERO_LOSS, SHORT_PRIMITIVE, SHORT_OBJECT, BoxingConverter.<Short>identity(), BoxingConverter.<Short>identity());
  }

}

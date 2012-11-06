/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts primitive arrays to arrays of the boxed objects
 */
public class PrimitiveArrayConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<boolean[]> BOOLEAN_PRIMITIVE = JavaTypeInfo.builder(boolean[].class).get();
  private static final JavaTypeInfo<Boolean[]> BOOLEAN_OBJECT = JavaTypeInfo.builder(Boolean[].class).get();
  private static final JavaTypeInfo<byte[]> BYTE_PRIMITIVE = JavaTypeInfo.builder(byte[].class).get();
  private static final JavaTypeInfo<Byte[]> BYTE_OBJECT = JavaTypeInfo.builder(Byte[].class).get();
  private static final JavaTypeInfo<char[]> CHARACTER_PRIMITIVE = JavaTypeInfo.builder(char[].class).get();
  private static final JavaTypeInfo<Character[]> CHARACTER_OBJECT = JavaTypeInfo.builder(Character[].class).get();
  private static final JavaTypeInfo<double[]> DOUBLE_PRIMITIVE = JavaTypeInfo.builder(double[].class).get();
  private static final JavaTypeInfo<Double[]> DOUBLE_OBJECT = JavaTypeInfo.builder(Double[].class).get();
  private static final JavaTypeInfo<float[]> FLOAT_PRIMITIVE = JavaTypeInfo.builder(float[].class).get();
  private static final JavaTypeInfo<Float[]> FLOAT_OBJECT = JavaTypeInfo.builder(Float[].class).get();
  private static final JavaTypeInfo<int[]> INTEGER_PRIMITIVE = JavaTypeInfo.builder(int[].class).get();
  private static final JavaTypeInfo<Integer[]> INTEGER_OBJECT = JavaTypeInfo.builder(Integer[].class).get();
  private static final JavaTypeInfo<long[]> LONG_PRIMITIVE = JavaTypeInfo.builder(long[].class).get();
  private static final JavaTypeInfo<Long[]> LONG_OBJECT = JavaTypeInfo.builder(Long[].class).get();
  private static final JavaTypeInfo<short[]> SHORT_PRIMITIVE = JavaTypeInfo.builder(short[].class).get();
  private static final JavaTypeInfo<Short[]> SHORT_OBJECT = JavaTypeInfo.builder(Short[].class).get();

  /**
   * Default instance.
   */
  public static final PrimitiveArrayConverter INSTANCE = new PrimitiveArrayConverter();

  protected PrimitiveArrayConverter() {
    conversion(ZERO_LOSS, BOOLEAN_PRIMITIVE, BOOLEAN_OBJECT, new Action<boolean[], Boolean[]>() {
      @Override
      public boolean[] cast(final Object value) {
        return (value instanceof boolean[]) ? (boolean[]) value : null;
      }

      @Override
      public Boolean[] convert(final boolean[] value) {
        final Boolean[] result = new Boolean[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    }, new Action<Boolean[], boolean[]>() {
      @Override
      public Boolean[] cast(final Object value) {
        return (value instanceof Boolean[]) ? (Boolean[]) value : null;
      }

      @Override
      public boolean[] convert(final Boolean[] value) {
        final boolean[] result = new boolean[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    });
    conversion(ZERO_LOSS, BYTE_PRIMITIVE, BYTE_OBJECT, new Action<byte[], Byte[]>() {
      @Override
      public byte[] cast(final Object value) {
        return (value instanceof byte[]) ? (byte[]) value : null;
      }

      @Override
      public Byte[] convert(final byte[] value) {
        final Byte[] result = new Byte[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    }, new Action<Byte[], byte[]>() {
      @Override
      public Byte[] cast(final Object value) {
        return (value instanceof Byte[]) ? (Byte[]) value : null;
      }

      @Override
      public byte[] convert(final Byte[] value) {
        final byte[] result = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    });
    conversion(ZERO_LOSS, CHARACTER_PRIMITIVE, CHARACTER_OBJECT, new Action<char[], Character[]>() {
      @Override
      public char[] cast(final Object value) {
        return (value instanceof char[]) ? (char[]) value : null;
      }

      @Override
      public Character[] convert(final char[] value) {
        final Character[] result = new Character[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    }, new Action<Character[], char[]>() {
      @Override
      public Character[] cast(final Object value) {
        return (value instanceof Character[]) ? (Character[]) value : null;
      }

      @Override
      public char[] convert(final Character[] value) {
        final char[] result = new char[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    });
    conversion(ZERO_LOSS, DOUBLE_PRIMITIVE, DOUBLE_OBJECT, new Action<double[], Double[]>() {
      @Override
      public double[] cast(final Object value) {
        return (value instanceof double[]) ? (double[]) value : null;
      }

      @Override
      public Double[] convert(final double[] value) {
        final Double[] result = new Double[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    }, new Action<Double[], double[]>() {
      @Override
      public Double[] cast(final Object value) {
        return (value instanceof Double[]) ? (Double[]) value : null;
      }

      @Override
      public double[] convert(final Double[] value) {
        final double[] result = new double[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    });
    conversion(ZERO_LOSS, FLOAT_PRIMITIVE, FLOAT_OBJECT, new Action<float[], Float[]>() {
      @Override
      public float[] cast(final Object value) {
        return (value instanceof float[]) ? (float[]) value : null;
      }

      @Override
      public Float[] convert(final float[] value) {
        final Float[] result = new Float[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    }, new Action<Float[], float[]>() {
      @Override
      public Float[] cast(final Object value) {
        return (value instanceof Float[]) ? (Float[]) value : null;
      }

      @Override
      public float[] convert(final Float[] value) {
        final float[] result = new float[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    });
    conversion(ZERO_LOSS, INTEGER_PRIMITIVE, INTEGER_OBJECT, new Action<int[], Integer[]>() {
      @Override
      public int[] cast(final Object value) {
        return (value instanceof int[]) ? (int[]) value : null;
      }

      @Override
      public Integer[] convert(final int[] value) {
        final Integer[] result = new Integer[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    }, new Action<Integer[], int[]>() {
      @Override
      public Integer[] cast(final Object value) {
        return (value instanceof Integer[]) ? (Integer[]) value : null;
      }

      @Override
      public int[] convert(Integer[] value) {
        final int[] result = new int[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    });
    conversion(ZERO_LOSS, LONG_PRIMITIVE, LONG_OBJECT, new Action<long[], Long[]>() {
      @Override
      public long[] cast(final Object value) {
        return (value instanceof long[]) ? (long[]) value : null;
      }

      @Override
      public Long[] convert(final long[] value) {
        final Long[] result = new Long[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    }, new Action<Long[], long[]>() {
      @Override
      public Long[] cast(final Object value) {
        return (value instanceof Long[]) ? (Long[]) value : null;
      }

      @Override
      public long[] convert(final Long[] value) {
        final long[] result = new long[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    });
    conversion(ZERO_LOSS, SHORT_PRIMITIVE, SHORT_OBJECT, new Action<short[], Short[]>() {
      @Override
      public short[] cast(final Object value) {
        return (value instanceof short[]) ? (short[]) value : null;
      }

      @Override
      public Short[] convert(final short[] value) {
        final Short[] result = new Short[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    }, new Action<Short[], short[]>() {
      @Override
      public Short[] cast(final Object value) {
        return (value instanceof Short[]) ? (Short[]) value : null;
      }

      @Override
      public short[] convert(final Short[] value) {
        final short[] result = new short[value.length];
        for (int i = 0; i < value.length; i++) {
          result[i] = value[i];
        }
        return result;
      }
    });
  }

}

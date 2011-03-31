/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.List;

import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Conversions between the basic Java types.
 */
public class PrimitiveConverter implements TypeConverter {

  private static final JavaTypeInfo<Boolean> BOOLEAN = JavaTypeInfo.builder(Boolean.class).get();
  private static final JavaTypeInfo<Byte> BYTE = JavaTypeInfo.builder(Byte.class).get();
  private static final JavaTypeInfo<Character> CHARACTER = JavaTypeInfo.builder(Character.class).get();
  private static final JavaTypeInfo<Double> DOUBLE = JavaTypeInfo.builder(Double.class).get();
  private static final JavaTypeInfo<Float> FLOAT = JavaTypeInfo.builder(Float.class).get();
  private static final JavaTypeInfo<Integer> INTEGER = JavaTypeInfo.builder(Integer.class).get();
  private static final JavaTypeInfo<Long> LONG = JavaTypeInfo.builder(Long.class).get();
  private static final JavaTypeInfo<Short> SHORT = JavaTypeInfo.builder(Short.class).get();
  private static final JavaTypeInfo<String> STRING = JavaTypeInfo.builder(String.class).get();

  /**
   * Major loss of precision.
   */
  private static final Integer MAJOR_LOSS = 5;
  /**
   * Slight loss of precision.
   */
  private static final Integer MINOR_LOSS = 3;
  /**
   * No loss of precision; i.e. F-1 (F (x)) == x, although not always F (F-1 (x)) == x
   */
  private static final Integer ZERO_LOSS = 1;

  private static final List<JavaTypeInfo<?>> TO_BOOLEAN = JavaTypeInfo.asList(BYTE, CHARACTER, DOUBLE, FLOAT, INTEGER, LONG, SHORT, STRING);
  private static final List<JavaTypeInfo<?>> TO_BYTE = JavaTypeInfo.asList(BYTE, DOUBLE, FLOAT, INTEGER, LONG, SHORT, STRING);
  private static final List<JavaTypeInfo<?>> TO_CHARACTER = JavaTypeInfo.asList(BOOLEAN, STRING);
  private static final List<JavaTypeInfo<?>> TO_DOUBLE = JavaTypeInfo.asList(BOOLEAN, FLOAT, BYTE, INTEGER, LONG, SHORT, STRING);
  private static final List<JavaTypeInfo<?>> TO_FLOAT = JavaTypeInfo.asList(BOOLEAN, BYTE, DOUBLE, INTEGER, LONG, SHORT, STRING);
  private static final List<JavaTypeInfo<?>> TO_INTEGER = JavaTypeInfo.asList(BOOLEAN, BYTE, SHORT, LONG, FLOAT, DOUBLE, STRING);
  private static final List<JavaTypeInfo<?>> TO_LONG = JavaTypeInfo.asList(BOOLEAN, BYTE, INTEGER, SHORT, FLOAT, DOUBLE, STRING);
  private static final List<JavaTypeInfo<?>> TO_SHORT = JavaTypeInfo.asList(BOOLEAN, BYTE, INTEGER, LONG, FLOAT, DOUBLE, STRING);
  private static final List<JavaTypeInfo<?>> TO_STRING = JavaTypeInfo.asList(BOOLEAN, BYTE, CHARACTER, DOUBLE, FLOAT, INTEGER, LONG, SHORT);

  @Override
  public boolean canConvertTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Boolean.class) {
      return true;
    } else if (clazz == Byte.class) {
      return true;
    } else if (clazz == Character.class) {
      return true;
    } else if (clazz == Double.class) {
      return true;
    } else if (clazz == Float.class) {
      return true;
    } else if (clazz == Integer.class) {
      return true;
    } else if (clazz == Long.class) {
      return true;
    } else if (clazz == Short.class) {
      return true;
    } else if (clazz == String.class) {
      return true;
    }
    return false;
  }

  @Override
  public List<JavaTypeInfo<?>> getConversionsTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Boolean.class) {
      return TO_BOOLEAN;
    } else if (clazz == Byte.class) {
      return TO_BYTE;
    } else if (clazz == Character.class) {
      return TO_CHARACTER;
    } else if (clazz == Double.class) {
      return TO_DOUBLE;
    } else if (clazz == Float.class) {
      return TO_FLOAT;
    } else if (clazz == Integer.class) {
      return TO_INTEGER;
    } else if (clazz == Long.class) {
      return TO_LONG;
    } else if (clazz == Short.class) {
      return TO_SHORT;
    } else if (clazz == String.class) {
      return TO_STRING;
    }
    return null;
  }

  private static boolean isRealNumber(final Object value) {
    if (value instanceof Double) {
      final Double v = (Double) value;
      return !v.isInfinite() && !v.isNaN();
    } else if (value instanceof Float) {
      final Float v = (Float) value;
      return !v.isInfinite() && !v.isNaN();
    } else if (value instanceof Number) {
      return true;
    }
    return false;
  }

  private static boolean toBoolean(final ValueConversionContext conversionContext, final Object value) {
    if (isRealNumber(value)) {
      if (value instanceof Double) {
        return conversionContext.setResult(MAJOR_LOSS, ((Number) value).doubleValue() != 0.0d);
      } else if (value instanceof Float) {
        return conversionContext.setResult(MAJOR_LOSS, ((Number) value).doubleValue() != 0.0f);
      } else {
        return conversionContext.setResult(MAJOR_LOSS, ((Number) value).longValue() != 0);
      }
    } else if (value instanceof Character) {
      final Character c = (Character) value;
      if (c.equals('T') || c.equals('t')) {
        return conversionContext.setResult(MINOR_LOSS, Boolean.TRUE);
      } else if (c.equals('F') || c.equals('f')) {
        return conversionContext.setResult(MINOR_LOSS, Boolean.FALSE);
      }
    } else if (value instanceof String) {
      final String str = (String) value;
      if ("TRUE".equalsIgnoreCase(str) || "T".equalsIgnoreCase(str)) {
        return conversionContext.setResult(MINOR_LOSS, Boolean.TRUE);
      } else if ("FALSE".equalsIgnoreCase(str) || "F".equalsIgnoreCase(str)) {
        return conversionContext.setResult(MINOR_LOSS, Boolean.FALSE);
      }
    }
    return conversionContext.setFail();
  }

  private static boolean toByte(final ValueConversionContext conversionContext, final Object value) {
    if (isRealNumber(value)) {
      if (value instanceof Double) {
        final double v = ((Double) value).doubleValue();
        if ((v >= (double) Byte.MIN_VALUE) && (v <= (double) Byte.MAX_VALUE)) {
          return conversionContext.setResult(MINOR_LOSS, (byte) v);
        }
      } else if (value instanceof Float) {
        final float v = ((Float) value).floatValue();
        if ((v >= (float) Byte.MIN_VALUE) && (v <= (float) Byte.MAX_VALUE)) {
          return conversionContext.setResult(MINOR_LOSS, (byte) v);
        }
      } else if (value instanceof Integer) {
        final int v = ((Integer) value).intValue();
        if ((v >= (int) Byte.MIN_VALUE) && (v <= (int) Byte.MAX_VALUE)) {
          return conversionContext.setResult(ZERO_LOSS, (byte) v);
        }
      } else if (value instanceof Long) {
        final long v = ((Long) value).longValue();
        if ((v >= (long) Byte.MIN_VALUE) && (v <= (long) Byte.MAX_VALUE)) {
          return conversionContext.setResult(ZERO_LOSS, (byte) v);
        }
      } else if (value instanceof Short) {
        final short v = ((Short) value).shortValue();
        if ((v >= (short) Byte.MIN_VALUE) && (v <= (short) Byte.MAX_VALUE)) {
          return conversionContext.setResult(ZERO_LOSS, (byte) v);
        }
      }
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(ZERO_LOSS, Byte.parseByte((String) value));
      } catch (NumberFormatException e) {
        // Ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(ZERO_LOSS, ((Boolean) value).booleanValue() ? (byte) 1 : (byte) 0);
    }
    return conversionContext.setFail();
  }

  private static boolean toCharacter(final ValueConversionContext conversionContext, final Object value) {
    if (value instanceof Boolean) {
      return conversionContext.setResult(ZERO_LOSS, ((Boolean) value).booleanValue() ? 'T' : 'F');
    } else if (value instanceof String) {
      final String v = (String) value;
      if (v.length() == 1) {
        return conversionContext.setResult(ZERO_LOSS, v.charAt(0));
      }
    }
    return conversionContext.setFail();
  }

  private static boolean toDouble(final ValueConversionContext conversionContext, final Object value) {
    if (value instanceof Float) {
      return conversionContext.setResult(ZERO_LOSS, ((Float) value).doubleValue());
    } else if (value instanceof Number) {
      return conversionContext.setResult(MINOR_LOSS, ((Number) value).doubleValue());
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(ZERO_LOSS, Double.parseDouble((String) value));
      } catch (NumberFormatException e) {
        // ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(ZERO_LOSS, ((Boolean) value).booleanValue() ? 1.0 : 0.0);
    }
    return conversionContext.setFail();
  }

  private static boolean toFloat(final ValueConversionContext conversionContext, final Object value) {
    if (value instanceof Number) {
      return conversionContext.setResult(MINOR_LOSS, ((Number) value).floatValue());
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(ZERO_LOSS, Float.parseFloat((String) value));
      } catch (NumberFormatException e) {
        // ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(ZERO_LOSS, ((Boolean) value).booleanValue() ? 1.0f : 0.0f);
    }
    return conversionContext.setFail();
  }

  private static boolean toInteger(final ValueConversionContext conversionContext, final Object value) {
    if (isRealNumber(value)) {
      if (value instanceof Double) {
        final double v = ((Double) value).doubleValue();
        if ((v >= (double) Integer.MIN_VALUE) && (v <= (double) Integer.MAX_VALUE)) {
          return conversionContext.setResult(MINOR_LOSS, (int) v);
        }
      } else if (value instanceof Float) {
        final float v = ((Float) value).floatValue();
        if ((v >= (float) Integer.MIN_VALUE) && (v <= (float) Integer.MAX_VALUE)) {
          return conversionContext.setResult(MINOR_LOSS, (int) v);
        }
      } else if (value instanceof Long) {
        final long v = ((Long) value).longValue();
        if ((v >= (long) Integer.MIN_VALUE) && (v <= (long) Integer.MAX_VALUE)) {
          return conversionContext.setResult(ZERO_LOSS, (int) v);
        }
      } else {
        return conversionContext.setResult(ZERO_LOSS, ((Number) value).intValue());
      }
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(ZERO_LOSS, Integer.parseInt((String) value));
      } catch (NumberFormatException e) {
        // Ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(ZERO_LOSS, ((Boolean) value).booleanValue() ? 1 : 0);
    }
    return conversionContext.setFail();
  }

  private static boolean toLong(final ValueConversionContext conversionContext, final Object value) {
    if (isRealNumber(value)) {
      if (value instanceof Double) {
        final double v = ((Double) value).doubleValue();
        if ((v >= (double) Long.MIN_VALUE) && (v <= (double) Long.MAX_VALUE)) {
          return conversionContext.setResult(MINOR_LOSS, (long) v);
        }
      } else if (value instanceof Float) {
        final float v = ((Float) value).floatValue();
        if ((v >= (float) Long.MIN_VALUE) && (v <= (float) Long.MAX_VALUE)) {
          return conversionContext.setResult(MINOR_LOSS, (long) v);
        }
      } else {
        return conversionContext.setResult(ZERO_LOSS, ((Number) value).longValue());
      }
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(ZERO_LOSS, Long.parseLong((String) value));
      } catch (NumberFormatException e) {
        // Ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(ZERO_LOSS, ((Boolean) value).booleanValue() ? 1L : 0L);
    }
    return conversionContext.setFail();
  }

  private static boolean toShort(final ValueConversionContext conversionContext, final Object value) {
    if (isRealNumber(value)) {
      if (value instanceof Double) {
        final double v = ((Double) value).doubleValue();
        if ((v >= (double) Short.MIN_VALUE) && (v <= (double) Short.MAX_VALUE)) {
          return conversionContext.setResult(MINOR_LOSS, (short) v);
        }
      } else if (value instanceof Float) {
        final float v = ((Float) value).floatValue();
        if ((v >= (float) Short.MIN_VALUE) && (v <= (float) Short.MAX_VALUE)) {
          return conversionContext.setResult(MINOR_LOSS, (short) v);
        }
      } else if (value instanceof Integer) {
        final int v = ((Integer) value).intValue();
        if ((v >= (int) Short.MIN_VALUE) && (v <= (int) Short.MAX_VALUE)) {
          return conversionContext.setResult(ZERO_LOSS, (short) v);
        }
      } else if (value instanceof Long) {
        final long v = ((Long) value).longValue();
        if ((v >= (long) Short.MIN_VALUE) && (v <= (long) Short.MAX_VALUE)) {
          return conversionContext.setResult(ZERO_LOSS, (short) v);
        }
      } else {
        return conversionContext.setResult(ZERO_LOSS, ((Number) value).shortValue());
      }
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(ZERO_LOSS, Short.parseShort((String) value));
      } catch (NumberFormatException e) {
        // Ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(MINOR_LOSS, ((Boolean) value).booleanValue() ? (short) 1 : (short) 0);
    }
    return conversionContext.setFail();
  }

  private static boolean toString(final ValueConversionContext conversionContext, final Object value) {
    // Probably zero-loss, but the stringize is quite expensive so consider it a bit extreme
    return conversionContext.setResult(MAJOR_LOSS, value.toString());
  }

  @Override
  public void convertValue(ValueConversionContext conversionContext, Object value, JavaTypeInfo<?> type) {
    final Class<?> clazz = type.getRawClass();
    if (clazz == Boolean.class) {
      toBoolean(conversionContext, value);
    } else if (clazz == Byte.class) {
      toByte(conversionContext, value);
    } else if (clazz == Character.class) {
      toCharacter(conversionContext, value);
    } else if (clazz == Double.class) {
      toDouble(conversionContext, value);
    } else if (clazz == Float.class) {
      toFloat(conversionContext, value);
    } else if (clazz == Integer.class) {
      toInteger(conversionContext, value);
    } else if (clazz == Long.class) {
      toLong(conversionContext, value);
    } else if (clazz == Short.class) {
      toShort(conversionContext, value);
    } else if (clazz == String.class) {
      toString(conversionContext, value);
    } else {
      conversionContext.setFail();
    }
  }

}

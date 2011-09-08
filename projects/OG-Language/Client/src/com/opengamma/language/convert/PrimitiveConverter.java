/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.MAJOR_LOSS;
import static com.opengamma.language.convert.TypeMap.MINOR_LOSS;
import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;
import static com.opengamma.language.convert.TypeMap.ZERO_LOSS_NON_PREFERRED;

import java.util.Map;

import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Conversions between the basic Java types.
 */
public class PrimitiveConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final PrimitiveConverter INSTANCE = new PrimitiveConverter();

  private static final JavaTypeInfo<Boolean> BOOLEAN = JavaTypeInfo.builder(Boolean.class).get();
  private static final JavaTypeInfo<Byte> BYTE = JavaTypeInfo.builder(Byte.class).get();
  private static final JavaTypeInfo<Character> CHARACTER = JavaTypeInfo.builder(Character.class).get();
  private static final JavaTypeInfo<Double> DOUBLE = JavaTypeInfo.builder(Double.class).get();
  private static final JavaTypeInfo<Float> FLOAT = JavaTypeInfo.builder(Float.class).get();
  private static final JavaTypeInfo<Integer> INTEGER = JavaTypeInfo.builder(Integer.class).get();
  private static final JavaTypeInfo<Long> LONG = JavaTypeInfo.builder(Long.class).get();
  private static final JavaTypeInfo<Short> SHORT = JavaTypeInfo.builder(Short.class).get();
  private static final JavaTypeInfo<String> STRING = JavaTypeInfo.builder(String.class).get();

  private static final TypeMap TO_BOOLEAN = TypeMap.of(MINOR_LOSS, CHARACTER, STRING).with(MAJOR_LOSS, BYTE, DOUBLE, FLOAT, INTEGER, LONG, SHORT);
  private static final TypeMap TO_BYTE = TypeMap.of(ZERO_LOSS, BOOLEAN, INTEGER, LONG, SHORT, STRING).with(MINOR_LOSS, DOUBLE, FLOAT);
  private static final TypeMap TO_CHARACTER = TypeMap.of(ZERO_LOSS, BOOLEAN, STRING);
  private static final TypeMap TO_DOUBLE = TypeMap.of(ZERO_LOSS, BOOLEAN, FLOAT, STRING).with(MINOR_LOSS, BYTE, INTEGER, LONG, SHORT);
  private static final TypeMap TO_FLOAT = TypeMap.of(ZERO_LOSS, BOOLEAN, STRING).with(MINOR_LOSS, DOUBLE, BYTE, INTEGER, LONG, SHORT);
  private static final TypeMap TO_INTEGER = TypeMap.of(ZERO_LOSS, BOOLEAN, BYTE, LONG, SHORT, STRING).with(MINOR_LOSS, FLOAT, DOUBLE);
  private static final TypeMap TO_LONG = TypeMap.of(ZERO_LOSS, BOOLEAN, BYTE, INTEGER, SHORT, STRING).with(MINOR_LOSS, FLOAT, DOUBLE);
  private static final TypeMap TO_SHORT = TypeMap.of(ZERO_LOSS, BOOLEAN, BYTE, INTEGER, LONG, STRING).with(MINOR_LOSS, FLOAT, DOUBLE);
  private static final TypeMap TO_STRING = TypeMap.of(ZERO_LOSS_NON_PREFERRED, BOOLEAN, BYTE, CHARACTER, DOUBLE, FLOAT, INTEGER, LONG, SHORT);

  protected PrimitiveConverter() {
  }

  @Override
  public boolean canConvertTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if ((clazz == Boolean.class) || (clazz == Boolean.TYPE)) {
      return true;
    } else if ((clazz == Byte.class) || (clazz == Byte.TYPE)) {
      return true;
    } else if ((clazz == Character.class) || (clazz == Character.TYPE)) {
      return true;
    } else if ((clazz == Double.class) || (clazz == Double.TYPE)) {
      return true;
    } else if ((clazz == Float.class) || (clazz == Float.TYPE)) {
      return true;
    } else if ((clazz == Integer.class) || (clazz == Integer.TYPE)) {
      return true;
    } else if ((clazz == Long.class) || (clazz == Long.TYPE)) {
      return true;
    } else if ((clazz == Short.class) || (clazz == Short.TYPE)) {
      return true;
    } else if (clazz == String.class) {
      return true;
    }
    return false;
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if ((clazz == Boolean.class) || (clazz == Boolean.TYPE)) {
      return TO_BOOLEAN;
    } else if ((clazz == Byte.class) || (clazz == Byte.TYPE)) {
      return TO_BYTE;
    } else if ((clazz == Character.class) || (clazz == Character.TYPE)) {
      return TO_CHARACTER;
    } else if ((clazz == Double.class) || (clazz == Double.TYPE)) {
      return TO_DOUBLE;
    } else if ((clazz == Float.class) || (clazz == Float.TYPE)) {
      return TO_FLOAT;
    } else if ((clazz == Integer.class) || (clazz == Integer.TYPE)) {
      return TO_INTEGER;
    } else if ((clazz == Long.class) || (clazz == Long.TYPE)) {
      return TO_LONG;
    } else if ((clazz == Short.class) || (clazz == Short.TYPE)) {
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
        return conversionContext.setResult(((Number) value).doubleValue() != 0.0d);
      } else if (value instanceof Float) {
        return conversionContext.setResult(((Number) value).doubleValue() != 0.0f);
      } else {
        return conversionContext.setResult(((Number) value).longValue() != 0);
      }
    } else if (value instanceof Character) {
      final Character c = (Character) value;
      if (c.equals('T') || c.equals('t')) {
        return conversionContext.setResult(Boolean.TRUE);
      } else if (c.equals('F') || c.equals('f')) {
        return conversionContext.setResult(Boolean.FALSE);
      }
    } else if (value instanceof String) {
      final String str = (String) value;
      if ("TRUE".equalsIgnoreCase(str) || "T".equalsIgnoreCase(str)) {
        return conversionContext.setResult(Boolean.TRUE);
      } else if ("FALSE".equalsIgnoreCase(str) || "F".equalsIgnoreCase(str)) {
        return conversionContext.setResult(Boolean.FALSE);
      }
    }
    return conversionContext.setFail();
  }

  private static boolean toByte(final ValueConversionContext conversionContext, final Object value) {
    if (isRealNumber(value)) {
      if (value instanceof Double) {
        final double v = ((Double) value).doubleValue();
        if ((v >= (double) Byte.MIN_VALUE) && (v <= (double) Byte.MAX_VALUE)) {
          return conversionContext.setResult((byte) v);
        }
      } else if (value instanceof Float) {
        final float v = ((Float) value).floatValue();
        if ((v >= (float) Byte.MIN_VALUE) && (v <= (float) Byte.MAX_VALUE)) {
          return conversionContext.setResult((byte) v);
        }
      } else if (value instanceof Integer) {
        final int v = ((Integer) value).intValue();
        if ((v >= (int) Byte.MIN_VALUE) && (v <= (int) Byte.MAX_VALUE)) {
          return conversionContext.setResult((byte) v);
        }
      } else if (value instanceof Long) {
        final long v = ((Long) value).longValue();
        if ((v >= (long) Byte.MIN_VALUE) && (v <= (long) Byte.MAX_VALUE)) {
          return conversionContext.setResult((byte) v);
        }
      } else if (value instanceof Short) {
        final short v = ((Short) value).shortValue();
        if ((v >= (short) Byte.MIN_VALUE) && (v <= (short) Byte.MAX_VALUE)) {
          return conversionContext.setResult((byte) v);
        }
      } else {
        assert value instanceof Byte;
        return conversionContext.setResult(value);
      }
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(Byte.parseByte((String) value));
      } catch (NumberFormatException e) {
        // Ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(((Boolean) value).booleanValue() ? (byte) 1 : (byte) 0);
    }
    return conversionContext.setFail();
  }

  private static boolean toCharacter(final ValueConversionContext conversionContext, final Object value) {
    if (value instanceof Boolean) {
      return conversionContext.setResult(((Boolean) value).booleanValue() ? 'T' : 'F');
    } else if (value instanceof String) {
      final String v = (String) value;
      if (v.length() == 1) {
        return conversionContext.setResult(v.charAt(0));
      }
    } else if (value instanceof Character) {
      return conversionContext.setResult(value);
    }
    return conversionContext.setFail();
  }

  private static boolean toDouble(final ValueConversionContext conversionContext, final Object value) {
    if (value instanceof Number) {
      return conversionContext.setResult(((Number) value).doubleValue());
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(Double.parseDouble((String) value));
      } catch (NumberFormatException e) {
        // ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(((Boolean) value).booleanValue() ? 1.0 : 0.0);
    }
    return conversionContext.setFail();
  }

  private static boolean toFloat(final ValueConversionContext conversionContext, final Object value) {
    if (value instanceof Number) {
      return conversionContext.setResult(((Number) value).floatValue());
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(Float.parseFloat((String) value));
      } catch (NumberFormatException e) {
        // ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(((Boolean) value).booleanValue() ? 1.0f : 0.0f);
    }
    return conversionContext.setFail();
  }

  private static boolean toInteger(final ValueConversionContext conversionContext, final Object value) {
    if (isRealNumber(value)) {
      if (value instanceof Double) {
        final double v = ((Double) value).doubleValue();
        if ((v >= (double) Integer.MIN_VALUE) && (v <= (double) Integer.MAX_VALUE)) {
          return conversionContext.setResult((int) v);
        }
      } else if (value instanceof Float) {
        final float v = ((Float) value).floatValue();
        if ((v >= (float) Integer.MIN_VALUE) && (v <= (float) Integer.MAX_VALUE)) {
          return conversionContext.setResult((int) v);
        }
      } else if (value instanceof Long) {
        final long v = ((Long) value).longValue();
        if ((v >= (long) Integer.MIN_VALUE) && (v <= (long) Integer.MAX_VALUE)) {
          return conversionContext.setResult((int) v);
        }
      } else {
        return conversionContext.setResult(((Number) value).intValue());
      }
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(Integer.parseInt((String) value));
      } catch (NumberFormatException e) {
        // Ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(((Boolean) value).booleanValue() ? 1 : 0);
    }
    return conversionContext.setFail();
  }

  private static boolean toLong(final ValueConversionContext conversionContext, final Object value) {
    if (isRealNumber(value)) {
      if (value instanceof Double) {
        final double v = ((Double) value).doubleValue();
        if ((v >= (double) Long.MIN_VALUE) && (v <= (double) Long.MAX_VALUE)) {
          return conversionContext.setResult((long) v);
        }
      } else if (value instanceof Float) {
        final float v = ((Float) value).floatValue();
        if ((v >= (float) Long.MIN_VALUE) && (v <= (float) Long.MAX_VALUE)) {
          return conversionContext.setResult((long) v);
        }
      } else {
        return conversionContext.setResult(((Number) value).longValue());
      }
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(Long.parseLong((String) value));
      } catch (NumberFormatException e) {
        // Ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(((Boolean) value).booleanValue() ? 1L : 0L);
    }
    return conversionContext.setFail();
  }

  private static boolean toShort(final ValueConversionContext conversionContext, final Object value) {
    if (isRealNumber(value)) {
      if (value instanceof Double) {
        final double v = ((Double) value).doubleValue();
        if ((v >= (double) Short.MIN_VALUE) && (v <= (double) Short.MAX_VALUE)) {
          return conversionContext.setResult((short) v);
        }
      } else if (value instanceof Float) {
        final float v = ((Float) value).floatValue();
        if ((v >= (float) Short.MIN_VALUE) && (v <= (float) Short.MAX_VALUE)) {
          return conversionContext.setResult((short) v);
        }
      } else if (value instanceof Integer) {
        final int v = ((Integer) value).intValue();
        if ((v >= (int) Short.MIN_VALUE) && (v <= (int) Short.MAX_VALUE)) {
          return conversionContext.setResult((short) v);
        }
      } else if (value instanceof Long) {
        final long v = ((Long) value).longValue();
        if ((v >= (long) Short.MIN_VALUE) && (v <= (long) Short.MAX_VALUE)) {
          return conversionContext.setResult((short) v);
        }
      } else {
        return conversionContext.setResult(((Number) value).shortValue());
      }
    } else if (value instanceof String) {
      try {
        return conversionContext.setResult(Short.parseShort((String) value));
      } catch (NumberFormatException e) {
        // Ignore
      }
    } else if (value instanceof Boolean) {
      return conversionContext.setResult(((Boolean) value).booleanValue() ? (short) 1 : (short) 0);
    }
    return conversionContext.setFail();
  }

  private static boolean toString(final ValueConversionContext conversionContext, final Object value) {
    return conversionContext.setResult(value.toString());
  }

  @Override
  public void convertValue(ValueConversionContext conversionContext, Object value, JavaTypeInfo<?> type) {
    final Class<?> clazz = type.getRawClass();
    if ((clazz == Boolean.class) || (clazz == Boolean.TYPE)) {
      toBoolean(conversionContext, value);
    } else if ((clazz == Byte.class) || (clazz == Byte.TYPE)) {
      toByte(conversionContext, value);
    } else if ((clazz == Character.class) || (clazz == Character.TYPE)) {
      toCharacter(conversionContext, value);
    } else if ((clazz == Double.class) || (clazz == Double.TYPE)) {
      toDouble(conversionContext, value);
    } else if ((clazz == Float.class) || (clazz == Float.TYPE)) {
      toFloat(conversionContext, value);
    } else if ((clazz == Integer.class) || (clazz == Integer.TYPE)) {
      toInteger(conversionContext, value);
    } else if ((clazz == Long.class) || (clazz == Long.TYPE)) {
      toLong(conversionContext, value);
    } else if ((clazz == Short.class) || (clazz == Short.TYPE)) {
      toShort(conversionContext, value);
    } else if (clazz == String.class) {
      toString(conversionContext, value);
    } else {
      conversionContext.setFail();
    }
  }

}

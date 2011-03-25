/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.Arrays;
import java.util.List;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.InvalidConversionException;
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

  // TODO: test this class

  @Override
  public boolean canConvert(SessionContext sessionContext, Object fromValue, JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Boolean.class) {
      return toBoolean(fromValue) != null;
    } else if (clazz == Byte.class) {
      return toByte(fromValue) != null;
    } else if (clazz == Character.class) {
      return toCharacter(fromValue) != null;
    } else if (clazz == Double.class) {
      return toDouble(fromValue) != null;
    } else if (clazz == Float.class) {
      return toFloat(fromValue) != null;
    } else if (clazz == Integer.class) {
      return toInteger(fromValue) != null;
    } else if (clazz == Long.class) {
      return toLong(fromValue) != null;
    } else if (clazz == Short.class) {
      return toShort(fromValue) != null;
    } else if (clazz == String.class) {
      return toString(fromValue) != null;
    }
    return false;
  }

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
  public <T> T convert(SessionContext sessionContext, Object fromValue, JavaTypeInfo<T> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Boolean.class) {
      return (T) toBoolean(fromValue);
    } else if (clazz == Byte.class) {
      return (T) toByte(fromValue);
    } else if (clazz == Character.class) {
      return (T) toCharacter(fromValue);
    } else if (clazz == Double.class) {
      return (T) toDouble(fromValue);
    } else if (clazz == Float.class) {
      return (T) toFloat(fromValue);
    } else if (clazz == Integer.class) {
      return (T) toInteger(fromValue);
    } else if (clazz == Long.class) {
      return (T) toLong(fromValue);
    } else if (clazz == Short.class) {
      return (T) toShort(fromValue);
    } else if (clazz == String.class) {
      return (T) toString(fromValue);
    }
    throw new InvalidConversionException(fromValue, targetType);
  }

  @Override
  public List<JavaTypeInfo<?>> getConversionsTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Boolean.class) {
      return Arrays.<JavaTypeInfo<?>> asList(BYTE, CHARACTER, DOUBLE, FLOAT, INTEGER, LONG, SHORT, STRING);
    } else if (clazz == Byte.class) {
      return Arrays.<JavaTypeInfo<?>> asList(BOOLEAN, CHARACTER, DOUBLE, FLOAT, INTEGER, LONG, SHORT, STRING);
    } else if (clazz == Character.class) {
      return Arrays.<JavaTypeInfo<?>> asList(BOOLEAN, BYTE, INTEGER, LONG, SHORT, STRING);
    } else if (clazz == Double.class) {
      return Arrays.<JavaTypeInfo<?>> asList(BOOLEAN, BYTE, CHARACTER, FLOAT, INTEGER, LONG, SHORT, STRING);
    } else if (clazz == Float.class) {
      return Arrays.<JavaTypeInfo<?>> asList(BOOLEAN, BYTE, CHARACTER, DOUBLE, INTEGER, LONG, SHORT, STRING);
    } else if (clazz == Integer.class) {
      return Arrays.<JavaTypeInfo<?>> asList(BOOLEAN, BYTE, CHARACTER, DOUBLE, FLOAT, LONG, SHORT, STRING);
    } else if (clazz == Long.class) {
      return Arrays.<JavaTypeInfo<?>> asList(BOOLEAN, BYTE, CHARACTER, DOUBLE, FLOAT, INTEGER, SHORT, STRING);
    } else if (clazz == Short.class) {
      return Arrays.<JavaTypeInfo<?>> asList(BOOLEAN, BYTE, CHARACTER, DOUBLE, FLOAT, INTEGER, LONG, STRING);
    } else if (clazz == String.class) {
      return Arrays.<JavaTypeInfo<?>> asList(BOOLEAN, BYTE, CHARACTER, DOUBLE, FLOAT, INTEGER, LONG, SHORT);
    }
    return null;
  }

  private Boolean toBoolean(final Object value) {
    if (value instanceof Double) {
      return ((Double) value).doubleValue() != 0;
    } else if (value instanceof Float) {
      return ((Float) value).floatValue() != 0;
    } else if (value instanceof Number) {
      return ((Number) value).intValue() != 0;
    } else if (value instanceof Character) {
      // TODO: check for T or F characters
    } else if (value instanceof String) {
      // TODO: checker for T/F/true/false etc
    }
    return null;
  }

  private Byte toByte(final Object value) {
    if (value instanceof Number) {
      return ((Number) value).byteValue();
    } else if (value instanceof Character) {
      // TODO: ?
    } else if (value instanceof String) {
      // TODO: parse
    } else if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue() ? (byte) 1 : (byte) 0;
    }
    return null;
  }

  private Character toCharacter(final Object value) {
    if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue() ? 'T' : 'F';
    } else if (value instanceof Number) {
      // TODO
    } else if (value instanceof String) {
      // TODO
    }
    return null;
  }

  private Double toDouble(final Object value) {
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    } else if (value instanceof Character) {
      // TODO
    } else if (value instanceof String) {
      // TODO
    } else if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue() ? 1.0 : 0.0;
    }
    return null;
  }

  private Float toFloat(final Object value) {
    if (value instanceof Number) {
      return ((Number) value).floatValue();
    } else if (value instanceof Character) {
      // TODO
    } else if (value instanceof String) {
      // TODO
    } else if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue() ? 1.0f : 0.0f;
    }
    return null;
  }

  private Integer toInteger(final Object value) {
    if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof Character) {
      // TODO
    } else if (value instanceof String) {
      // TODO
    } else if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue() ? 1 : 0;
    }
    return null;
  }

  private Long toLong(final Object value) {
    if (value instanceof Number) {
      return ((Number) value).longValue();
    } else if (value instanceof Character) {
      // TODO
    } else if (value instanceof String) {
      // TODO
    } else if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue() ? 1L : 0L;
    }
    return null;
  }

  private Short toShort(final Object value) {
    if (value instanceof Number) {
      return ((Number) value).shortValue();
    } else if (value instanceof Character) {
      // TODO
    } else if (value instanceof String) {
      // TODO
    } else if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue() ? (short) 1 : (short) 0;
    }
    return null;
  }

  private String toString(final Object value) {
    return value.toString();
  }

}

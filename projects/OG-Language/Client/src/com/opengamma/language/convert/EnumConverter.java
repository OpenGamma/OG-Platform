/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.MINOR_LOSS;
import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Converts enums to/from strings.  
 */
public final class EnumConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final EnumConverter INSTANCE = new EnumConverter();

  @SuppressWarnings("unchecked")
  private static final JavaTypeInfo<Enum> ENUM = JavaTypeInfo.builder(Enum.class).get();
  @SuppressWarnings("unchecked")
  private static final JavaTypeInfo<Enum> ENUM_NULL = JavaTypeInfo.builder(Enum.class).allowNull().get();
  private static final JavaTypeInfo<String> STRING = JavaTypeInfo.builder(String.class).get();
  private static final JavaTypeInfo<String> STRING_NULL = JavaTypeInfo.builder(String.class).allowNull().get();
  private static final Map<JavaTypeInfo<?>, Integer> TO_STRING = TypeMap.of(ZERO_LOSS, ENUM);
  private static final Map<JavaTypeInfo<?>, Integer> TO_STRING_NULL = TypeMap.of(ZERO_LOSS, ENUM_NULL);
  private static final Map<JavaTypeInfo<?>, Integer> TO_ENUM = TypeMap.of(MINOR_LOSS, STRING);
  private static final Map<JavaTypeInfo<?>, Integer> TO_ENUM_NULL = TypeMap.of(MINOR_LOSS, STRING_NULL);

  private final ConcurrentMap<Class<?>, Map<String, Enum<?>>> _enumValues = new ConcurrentHashMap<Class<?>, Map<String, Enum<?>>>();

  protected EnumConverter() {
  }

  @Override
  public synchronized boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return targetType.getRawClass().isEnum() || (targetType.getRawClass() == String.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (value == null) {
      if (type.isAllowNull()) {
        conversionContext.setResult(null);
      } else if (type.isDefaultValue()) {
        conversionContext.setResult(type.getDefaultValue());
      } else {
        conversionContext.setFail();
      }
      return;
    }
    if (type.getRawClass() == String.class) {
      conversionContext.setResult(value.toString());
    } else {
      final String str = (String) value;
      final Class<? extends Enum> enumClass = (Class<? extends Enum>) type.getRawClass();
      Enum<?> enumValue;
      try {
        enumValue = Enum.valueOf(enumClass, str);
      } catch (IllegalArgumentException e) {
        Map<String, Enum<?>> cache = _enumValues.get(type.getRawClass());
        if (cache == null) {
          cache = new HashMap<String, Enum<?>>();
          for (Enum<?> enumEntry : enumClass.getEnumConstants()) {
            cacheAlternativeEnumValue(cache, enumEntry);
          }
          _enumValues.putIfAbsent(type.getRawClass(), cache);
        }
        enumValue = cache.get(getAlternativeString(str));
        if (enumValue == null) {
          conversionContext.setFail();
          return;
        }
      }
      conversionContext.setResult(enumValue);
    }
  }

  /**
   * Converts the raw string so that it may match one of the alternative enum values. The default implementation here converts
   * it to lower case to match the behavior of {@link #cacheAlternativeEnumValue}.
   * 
   * @param str original string
   * @return the alternative form of the string
   */
  protected String getAlternativeString(final String str) {
    return str.trim().toLowerCase();
  }

  /**
   * Inserts alternative string representations of the enum value into a cache. The default implementation here converts it to
   * lower case to match the behavior of {@link #getAlternativeString} and inserts a form with underscores converted to spaces.
   * 
   * @param cache cache to update, mapping alternative strings to enum values
   * @param enumEntry the enum value to cache
   */
  protected void cacheAlternativeEnumValue(final Map<String, Enum<?>> cache, final Enum<?> enumEntry) {
    final String name = enumEntry.name().toLowerCase();
    cache.put(name, enumEntry);
    if (name.indexOf('_') >= 0) {
      final String withSpaces = name.replace('_', ' ').trim();
      cache.put(withSpaces, enumEntry);
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    if (targetType.getRawClass() == String.class) {
      return (targetType.isAllowNull() || targetType.isDefaultValue()) ? TO_STRING_NULL : TO_STRING;
    } else {
      return (targetType.isAllowNull() || targetType.isDefaultValue()) ? TO_ENUM_NULL : TO_ENUM;
    }
  }

}

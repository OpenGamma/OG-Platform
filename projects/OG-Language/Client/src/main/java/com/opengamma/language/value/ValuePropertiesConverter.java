/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts {@link ValueProperties} instances to/from their string representation.
 */
public class ValuePropertiesConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<ValueProperties> VALUE_PROPERTIES = JavaTypeInfo.builder(ValueProperties.class).get();
  private static final JavaTypeInfo<String> STRING = JavaTypeInfo.builder(String.class).get();

  /**
   * Default instance.
   */
  public static final ValuePropertiesConverter INSTANCE = new ValuePropertiesConverter();

  protected ValuePropertiesConverter() {
    conversion(TypeMap.ZERO_LOSS, VALUE_PROPERTIES, STRING, new Action<ValueProperties, String>() {
      @Override
      protected String convert(final ValueProperties value) {
        return value.toString();
      }
    }, new Action<String, ValueProperties>() {
      @Override
      protected ValueProperties convert(final String value) {
        return ValueProperties.parse(value);
      }
    });
  }

}

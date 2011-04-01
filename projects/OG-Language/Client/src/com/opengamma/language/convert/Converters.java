/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.List;

import com.opengamma.language.invoke.TypeConverter;

/**
 * Constructs instances of the standard converters.
 */
public final class Converters {

  private Converters() {
  }

  public static void populateList(final List<TypeConverter> converters) {
    converters.add(new DataConverter());
    converters.add(new PrimitiveConverter());
    converters.add(new ValueConverter());
  }

}

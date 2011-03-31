/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.language.invoke.TypeConverter;
import com.opengamma.language.invoke.TypeConverterProvider;

/**
 * Constructs instances of the standard converters.
 */
public final class Converters implements TypeConverterProvider {

  public Converters() {
  }

  public static void populate(final Collection<TypeConverter> converters) {
    converters.add(new DataConverter());
    converters.add(new PrimitiveConverter());
    converters.add(new ValueConverter());
  }

  @Override
  public Set<TypeConverter> getTypeConverters() {
    final HashSet<TypeConverter> converters = new HashSet<TypeConverter>();
    populate(converters);
    return Collections.unmodifiableSet(converters);
  }

}

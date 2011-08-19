/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Partial implementation of a {@link TypeConverterProvider}.
 */
public abstract class AbstractTypeConverterProvider implements TypeConverterProvider {

  protected abstract void loadTypeConverters(Collection<TypeConverter> converters);

  @Override
  public Set<TypeConverter> getTypeConverters() {
    final Set<TypeConverter> result = new HashSet<TypeConverter>();
    loadTypeConverters(result);
    return Collections.unmodifiableSet(result);
  }

}

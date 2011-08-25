/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Partial implementation of a {@link TypeConverterProvider}.
 */
public abstract class AbstractTypeConverterProvider implements TypeConverterProvider {

  protected abstract void loadTypeConverters(Collection<TypeConverter> converters);

  @Override
  public List<TypeConverter> getTypeConverters() {
    final List<TypeConverter> result = new LinkedList<TypeConverter>();
    loadTypeConverters(result);
    return Collections.unmodifiableList(result);
  }

}

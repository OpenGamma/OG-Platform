/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.Collection;

/**
 * Bean implementation of a {@link TypeConverterProvider}.
 */
public class TypeConverterProviderBean extends AbstractTypeConverterProvider {

  private Collection<TypeConverter> _converters;

  public void setConverters(final Collection<TypeConverter> converters) {
    _converters = converters;
  }

  public Collection<TypeConverter> getConverters() {
    return _converters;
  }

  @Override
  protected void loadTypeConverters(Collection<TypeConverter> converters) {
    converters.addAll(getTypeConverters());
  }

}

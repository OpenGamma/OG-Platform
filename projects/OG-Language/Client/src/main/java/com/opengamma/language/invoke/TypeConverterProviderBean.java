/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.Arrays;
import java.util.Collection;

/**
 * Bean implementation of a {@link TypeConverterProvider}.
 */
public class TypeConverterProviderBean extends AbstractTypeConverterProvider {

  private Collection<? extends TypeConverter> _converters;

  public TypeConverterProviderBean(final Collection<TypeConverter> converters) {
    setConverters(converters);
  }

  public TypeConverterProviderBean(final TypeConverter... converters) {
    setConverters(Arrays.asList(converters));
  }

  public TypeConverterProviderBean() {
  }

  public void setConverters(final Collection<? extends TypeConverter> converters) {
    _converters = converters;
  }

  public Collection<? extends TypeConverter> getConverters() {
    return _converters;
  }

  @Override
  protected void loadTypeConverters(Collection<TypeConverter> converters) {
    converters.addAll(getConverters());
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import java.util.Collection;

import com.opengamma.language.invoke.AbstractTypeConverterProvider;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Supplies the identifier based conversions.
 */
public class Converters extends AbstractTypeConverterProvider {

  @Override
  protected void loadTypeConverters(final Collection<TypeConverter> converters) {
    converters.add(ExternalSchemeRankConverter.INSTANCE);
    converters.add(IdentifierConverter.INSTANCE);
  }

}

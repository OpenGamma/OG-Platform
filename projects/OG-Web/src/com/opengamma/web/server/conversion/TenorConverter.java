/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.time.Tenor;

/**
 * Converter for {@link Tenor} objects which uses the period as the display value.
 */
public class TenorConverter implements ResultConverter<Tenor> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, Tenor value, ConversionMode mode) {
    return value.getPeriod().toString();
  }

  @Override
  public String getFormatterName() {
    return "TENOR";
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, Tenor value) {
    return null;
  }

}

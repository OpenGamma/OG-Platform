/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class TenorConverter implements ResultConverter<Tenor> {

  @Override
  public Object convert(ResultConverterCache context, Tenor value, ConversionMode mode) {
    return value.getPeriod().toString();
  }

  @Override
  public String getResultTypeName() {
    return "TENOR";
  }
 
}

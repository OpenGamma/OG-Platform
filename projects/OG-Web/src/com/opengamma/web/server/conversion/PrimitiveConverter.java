/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

/**
 * A special converter for results which 
 */
public class PrimitiveConverter implements ResultConverter<Object> {

  @Override
  public Object convert(ResultConverterCache context, Object value, ConversionMode mode) {
    return value;
  }

  @Override
  public String getResultTypeName() {
    return "PRIMITIVE";
  }

}

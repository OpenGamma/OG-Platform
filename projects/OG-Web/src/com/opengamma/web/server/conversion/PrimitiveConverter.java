/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

/**
 * Converter for primitives that map directly to JSON and require no transformation.
 */
public class PrimitiveConverter implements ResultConverter<Object> {

  @Override
  public Object convert(ResultConverterCache context, String valueName, Object value, ConversionMode mode) {
    return value;
  }

  @Override
  public String getResultTypeName() {
    return "PRIMITIVE";
  }

}

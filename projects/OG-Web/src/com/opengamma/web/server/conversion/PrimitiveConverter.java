/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Converter for primitives that map directly to JSON and require no transformation.
 */
public class PrimitiveConverter implements ResultConverter<Object> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, Object value, ConversionMode mode) {
    return value;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, Object value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, Object value) {
    return value.toString();
  }
  
  @Override
  public String getFormatterName() {
    return "PRIMITIVE";
  }
  
}

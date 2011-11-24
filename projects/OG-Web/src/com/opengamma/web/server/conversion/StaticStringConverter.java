/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Converter that always returns a fixed string.
 */
public class StaticStringConverter implements ResultConverter<Object> {

  private final String _result;
  
  public StaticStringConverter(String result) {
    _result = result;
  }
    
  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, Object value, ConversionMode mode) {
    return _result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, Object value) {
    return _result;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, Object value) {
    return _result;
  }

  @Override
  public String getFormatterName() {
    return "PRIMITIVE";
  } 
  
}

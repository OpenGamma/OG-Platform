/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Converts a result value into an object which can serialize its Fudge representation as JSON.
 */
public class FudgeBasedJsonGeneratorConverter implements ResultConverter<Object> {

  private final FudgeContext _fudgeContext;
  
  public FudgeBasedJsonGeneratorConverter(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, Object value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("name", value.getClass().getSimpleName());
    
    if (mode == ConversionMode.FULL) {
      result.put("detail", new FudgeBasedJsonGenerator(_fudgeContext, value));
    }
    
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, Object value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, Object value) {
    return value.getClass().getSimpleName();
  }
  
  @Override
  public String getFormatterName() {
    return "GENERIC";
  }

}

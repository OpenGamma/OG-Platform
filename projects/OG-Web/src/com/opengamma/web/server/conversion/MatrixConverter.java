/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.Collection;

/**
 * Converter for matrices
 */
public class MatrixConverter implements ResultConverter<Collection<?>> {

  @Override
  public Object convert(ResultConverterCache context, Collection<?> value, ConversionMode mode) {
    // Actually a JSON primitive
    return value;
  }

  @Override
  public String getResultTypeName() {
    return "MATRIX";
  }
  
}

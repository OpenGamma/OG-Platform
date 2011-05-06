/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Converts from a function result into a JSON-friendly object suitable for consumption by the web client.
 * 
 * @param <T>  the type of function result handled by the converter
 */
public interface ResultConverter<T> {

  /**
   * Converts from a function result into an object suitable for consumption by the web client as JSON.
   * 
   * @param context  the converter context
   * @param valueSpec  the value specification if applicable, may be {@code null}
   * @param value  a function result, not {@code null}
   * @param mode  the mode in which the value should be converted
   * @return  the converted, JSON-friendly value
   */
  Object convert(ResultConverterCache context, ValueSpecification valueSpec, T value, ConversionMode mode);
  
  /**
   * A unique name which indicates to a client both
   * <ul>
   *   <li>how it should interpret the converted result</li>
   *   <li>how it should be rendered</li>
   * </ul>
   * 
   * @return a unique name, not {@code null}
   */
  String getFormatterName();
  
}

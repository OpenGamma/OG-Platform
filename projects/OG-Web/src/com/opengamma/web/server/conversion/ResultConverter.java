/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

/**
 * Converts from a function result into a JSON-friendly object suitable for consumption by the web client.
 * 
 * @param <T>  the type of function result handled by the converter
 */
public interface ResultConverter<T> {

  /**
   * Converts from a function result into an object suitable for consumption by the web client as JSON.
   * 
   * @param value  a function result, not null
   * @param mode  the mode in which the value should be converted
   * @param context  the converter context
   * @return  the converted, JSON-friendly value
   */
  Object convert(ResultConverterCache context, T value, ConversionMode mode);
  
  /**
   * A unique name which indicates to a client both
   * <ul>
   *   <li>how it should interpret the converted result</li>
   *   <li>how it should be rendered</li>
   * </ul>
   * 
   * @return a unique name
   */
  String getResultTypeName();
  
}

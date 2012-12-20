/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.Map;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Converts from a function result into a String/Double map suitable for writing into a database.
 * 
 * @param <T>  the type of function result handled by the converter
 */
public interface ResultConverter<T> {
  
  /**
   * Converts from a function result into a String/Double map suitable for writing into a database.
   * To guarantee uniqueness of risk names, the Strings should be of form valueName + {something}.
   *
   * @param valueName Name of {@link ValueRequirement} that is being converted
   * @param value  a function result, not null
   * @return  the converted value. 
   */
  Map<String, Double> convert(String valueName, T value);
  
  /**
   * @return the type of function result handled by the converter
   */
  Class<?> getConvertedClass();

}

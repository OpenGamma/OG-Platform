/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.List;

import com.opengamma.engine.value.AnalyticValueDefinition;
import com.opengamma.engine.value.ComputedValue;


/**
 * 
 *
 * @author kirk
 */
public interface FunctionInputs {

  Collection<ComputedValue<?>> getAllValues();

  <T> T getValue(Class<T> valueObjectClass);

  <T> Collection<T> getValues(Class<T> valueObjectClass);

  Object getValue(String definitionKey, Object definitionValue);

  // REVIEW jim 18-Sep-09 -- Very yucky use of varargs, but nice to use... 
  Object getValue(String definitionKey, Object definitionValue,
      Object... params);

  List<?> getValues(String definitionKey, Object definitionValue);

  // REVIEW jim 18-Sep-09 -- Very yucky use of varargs, but nice to use...
  List<?> getValues(String definitionKey, Object definitionValue,
      Object... params);

  Object getValue(AnalyticValueDefinition<?> definition);

}

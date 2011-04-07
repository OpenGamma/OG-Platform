/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.Set;

/**
 * A source of type converters
 */
public interface TypeConverterProvider {

  /**
   * Returns a set of type converters.
   * 
   * @return a set of type converters
   */
  Set<TypeConverter> getTypeConverters();

}

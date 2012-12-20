/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.List;


/**
 * A source of type converters
 */
public interface TypeConverterProvider {

  /**
   * Returns a list of type converters
   * 
   * @return a list of type converters
   */
  List<TypeConverter> getTypeConverters();

}

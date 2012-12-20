/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

/**
 * Partial implementation of {@link TypeConverter}.
 */
public abstract class AbstractTypeConverter implements TypeConverter {

  @Override
  public String getTypeConverterKey() {
    return getClass().getSimpleName();
  }

}

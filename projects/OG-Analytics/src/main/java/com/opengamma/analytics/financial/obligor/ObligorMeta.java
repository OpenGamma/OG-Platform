/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.obligor;

import com.opengamma.util.i18n.Country;

/**
 * Classes that pull out a specific field from an {@link Obligor}. As the is a bundle of
 * various data types, these classes are necessary to allow the correct values to be
 * extracted in a general way.
 * <p>
 * Implementing classes can also manipulate data in fields if required - e.g. performing
 * a union of the {@link Region} and {@link Country}.
 * @param <T> The type of the field
 */
public interface ObligorMeta<T> {

  /**
   * Gets the desired field for an obligor
   * @param obligor The obligor.
   * @return The meta data
   */
  T getMetaData(Obligor obligor);
}

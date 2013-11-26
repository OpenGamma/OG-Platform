/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.obligor;

import java.util.Comparator;

import com.opengamma.util.i18n.Country;

/**
 * Classes that pull out a specific field from an {@link Obligor}. As the is a bundle of
 * various data types, these classes are necessary to allow the correct values to be
 * extracted in a general way. Implementing classes of this interface also implement
 * {@link Comparator}, which allows sorting by field of instruments with obligors.
 * <p>
 * Implementing classes can also manipulate data in fields if required - e.g. performing
 * a union of the {@link Region} and {@link Country}.
 * @param <S> The type of the obligor
 * @param <T> The type of the field
 */
public interface ObligorMeta<S extends Obligor, T> extends Comparator<S> {

  /**
   * Gets the desired field for an obligor.
   * @param obligor The obligor, not null
   * @return The meta data
   */
  T getMetaData(S obligor);

  /**
   * Gets the desired single value of a field for an obligor.
   * @param obligor The obligor, not null
   * @param element The element
   * @return The meta data
   * @throws IllegalStateException If the element is not found in the field(s)
   */
  T getMetaData(S obligor, T element);
}

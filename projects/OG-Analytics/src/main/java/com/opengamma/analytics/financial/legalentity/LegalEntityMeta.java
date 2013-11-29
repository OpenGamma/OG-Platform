/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import com.opengamma.util.i18n.Country;

/**
 * Classes that pull out a specific field from an {@link LegalEntity}. As the legal entity is a bundle of
 * various data types, these classes are necessary to allow the correct values to be
 * extracted in a general way.
 * <p>
 * Implementing classes can also manipulate data in fields if required - e.g. performing
 * a union of the {@link Region} and {@link Country}.
 * @param <S> The type of the obligor
 */
public interface LegalEntityMeta<S extends LegalEntity> {

  /**
   * Gets the desired field (e.g. the sector) for an obligor.
   * @param obligor The obligor, not null
   * @return The meta data
   */
  Object getMetaData(S obligor);

}

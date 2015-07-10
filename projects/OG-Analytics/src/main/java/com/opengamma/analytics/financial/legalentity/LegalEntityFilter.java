/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Classes that pull out a specific field from an {@link LegalEntity}. As the legal entity is a bundle of various data types, these classes are necessary to allow the correct values to be extracted in
 * a general way.
 * 
 * @param <S> The type of the legal entity
 */
public interface LegalEntityFilter<S extends LegalEntity> extends Serializable {

  /**
   * Gets the desired field (e.g. the sector) for an legal entity.
   * 
   * @param legalEntity The legal entity, not null
   * @return The meta data
   */
  Object getFilteredData(S legalEntity);

  /**
   * Indicates the type of object returned by a call to {@link #getFilteredData}.
   * 
   * @return the type of object this filter will return
   */
  Type getFilteredDataType();

}

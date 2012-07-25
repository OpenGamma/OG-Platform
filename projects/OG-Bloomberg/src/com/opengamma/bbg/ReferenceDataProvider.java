/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Set;

/**
 * Provider of reference data for one or more securities from a data provider like Bloomberg.
 * <p>
 * The reference data provider is a service for searching security fields from Bloomberg.
 */
public interface ReferenceDataProvider {

  /**
   * Gets the reference data for a set of securities.
   * <p>
   * This retrieves the field-level information for each of a set of securities.
   * 
   * @param securityKeys  the security keys, not empty, not null
   * @param fields  the fields to retrieve, not empty, not null
   * @return the result, not null
   * @throws RuntimeException if an error occurs
   */
  ReferenceDataResult getFields(Set<String> securityKeys, Set<String> fields);

}

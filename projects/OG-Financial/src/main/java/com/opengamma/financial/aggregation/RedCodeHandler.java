/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

/**
 * Processes a RED code, returning data specified by the generic type.
 *
 * @param <T> the type of data to be returned by an extract
 */
public interface RedCodeHandler<T> {

  /**
   * Extract data from the RED code. If data can't be found then null should be returned.
   *
   * @param redCode the RED code to extract the data from
   * @return the data if found, null otherwise
   */
  T extract(String redCode);

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata;

import java.util.Map;
import java.util.Set;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundleWithDates;

/**
 * Provider for loading all available identifiers with date ranges if available.
 * <p>
 * This is used to find the different security identifiers over time.
 */
public interface IdentifierProvider {

  /**
   * Get all available identifiers with dates if available.
   * <p>
   * Some identifiers are only valid for a limited period of time.
   * After the identifier becomes invalid, it may be re-used for something else.
   * The {@code IdentifierBundleWithDates} concept captures the valid dates for identifiers.
   * 
   * @param identifiers  the identifiers that need to be resolved, not empty
   * @return a map of requested identifier to IdentifierBundleWithDates if available
   */
  Map<Identifier, IdentifierBundleWithDates> getIdentifiers(Set<Identifier> identifiers);

}

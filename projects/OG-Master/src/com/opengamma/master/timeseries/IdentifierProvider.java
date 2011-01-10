/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

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
   * 
   * @param unresolvedIdentifiers  the identifiers that need to be resolved, not empty
   * @return a map of requested identifier to IdentifierBundleWithDates if available
   */
  Map<Identifier, IdentifierBundleWithDates> getIdentifiers(Set<Identifier> unresolvedIdentifiers);

}

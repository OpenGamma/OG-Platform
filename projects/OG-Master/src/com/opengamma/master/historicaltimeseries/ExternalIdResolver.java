/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import java.util.Map;
import java.util.Set;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;

/**
 * Resolves a single external identifier to a bundle with validity dates.
 * <p>
 * This is used to find the different security identifiers over time.
 */
public interface ExternalIdResolver {

  /**
   * Get all available identifiers with dates if available.
   * <p>
   * Some identifiers are only valid for a limited period of time.
   * After the identifier becomes invalid, it may be re-used for something else.
   * The {@code ExternalIdBundleWithDates} concept captures the valid dates for identifiers.
   * 
   * @param externalIds  the identifiers that need to be resolved, not empty
   * @return a map of requested identifier to bundle, with dates if available
   */
  Map<ExternalId, ExternalIdBundleWithDates> getExternalIds(Set<ExternalId> externalIds);

}

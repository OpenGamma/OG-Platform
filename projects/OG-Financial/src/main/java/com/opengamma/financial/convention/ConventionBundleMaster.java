/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Repository for rates and associated metadata - e.g. LIBOR/EURIBOR etc...
 */
public interface ConventionBundleMaster {

  /**
   * Search the master for matching convention bundles
   * @param searchRequest a request object containing the query parameters
   * @return a search result object containing the resulting matches plus metadata
   */
  ConventionBundleSearchResult searchConventionBundle(ConventionBundleSearchRequest searchRequest);

  /**
   * Search the master for matching convention bundles in the history
   * @param searchRequest a request object containing the historic query parameters
   * @return a search result object containing the resulting matches plus metadata
   */  
  ConventionBundleSearchResult searchHistoricConventionBundle(ConventionBundleSearchHistoricRequest searchRequest);

  /**
   * A direct look-up of a convention bundle using a UniqueId
   * @param uniqueId the unique identifier
   * @return the matching convention bundle, wrapped in a metadata document
   */
  ConventionBundleDocument getConventionBundle(UniqueId uniqueId);
  
  /**
   * Add a new convention bundle to the master
   * @param bundle The id bundle
   * @param convention The conventions
   * @return the UniqueId of the convention bundle
   */
  UniqueId add(ExternalIdBundle bundle, ConventionBundleImpl convention);

}

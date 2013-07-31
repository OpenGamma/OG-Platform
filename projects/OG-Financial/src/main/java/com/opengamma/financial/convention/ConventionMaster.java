/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractChangeProvidingMaster;

/**
 * Repository for conventions and associated metadata.
 */
public interface ConventionMaster extends AbstractChangeProvidingMaster<ConventionDocument> {

  /**
   * Search the master for matching conventions.
   * @param searchRequest a request object containing the query parameters
   * @return a search result object containing the resulting matches plus metadata
   */
  ConventionSearchResult searchConvention(ConventionSearchRequest searchRequest);

  /**
   * Search the master for matching conventions in the history.
   * @param searchRequest a request object containing the query parameters
   * @return a search result object containing the resulting matches plus metadata
   */
  ConventionSearchResult searchHistoricalConvention(ConventionSearchHistoricRequest searchRequest);

  /**
   * A direct look-up of a convention using a {@link UniqueId}.
   * @param uniqueId The unique identifier
   * @return the matching convention wrapped in a metadata document
   */
  ConventionDocument getConvention(UniqueId uniqueId);

  /**
   * Add a new convention to the master using the id bundle of the convention to generate its unique id.
   * @param convention The convention
   * @return the UniqueId of the convention
   */
  UniqueId add(Convention convention);
}

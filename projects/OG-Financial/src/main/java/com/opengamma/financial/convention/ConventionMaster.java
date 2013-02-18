/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.UniqueId;

/**
 * 
 */
public interface ConventionMaster {

  ConventionSearchResult searchConvention(ConventionSearchRequest searchRequest);

  ConventionSearchResult searchHistoricalConvention(ConventionSearchHistoricRequest searchRequest);

  ConventionDocument getConvention(UniqueId uniqueId);

  UniqueId add(Convention convention);
}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

import java.util.Collection;

import com.opengamma.id.IdentifierBundle;

/**
 * Resolves a given security with the appropriate timeseries metadata
 * 
 * <p>
 * Meta data includes DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME
 * It will be used to lookup timeseries in OG-TimeSeries Storage and 
 * load timeseries from data sources like BLOOMBERG
 */
public interface TimeSeriesMetaDataResolver {
  /**
   * Returns the default metadata for a security.
   * <p>
   * Looks up security in security master and returns default metadata based on security type
   * 
   * @param identifiers the identifier bundle, not-null
   * @return the default metadata, null if security cannot be found in security master
   */
  TimeSeriesMetaData getDefaultMetaData(IdentifierBundle identifiers);
}

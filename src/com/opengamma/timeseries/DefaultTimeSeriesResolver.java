/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import com.opengamma.id.IdentifierBundle;

/**
 * Resolves the timeseries metadata based on instrument type
 */
public class DefaultTimeSeriesResolver implements TimeSeriesResolver {
  private static final String DEFAULT_DATA_SOURCE = "BLOOMBERG";
  private static final String DEFAULT_DATA_PROVIDER = "CMPL";
  private static final String DEFAULT_DATA_FIELD = "PX_LAST";
  private static final String DEFAULT_OBSERVATION_TIME = "LONDON_CLOSE";

  @Override
  public TimeSeriesMetaData resolve(IdentifierBundle identifiers) {
    TimeSeriesMetaData result = new TimeSeriesMetaData();
    result.setDataField(DEFAULT_DATA_FIELD);
    result.setDataProvider(DEFAULT_DATA_PROVIDER);
    result.setDataSource(DEFAULT_DATA_SOURCE);
    result.setIdentifiers(identifiers);
    result.setObservationTime(DEFAULT_OBSERVATION_TIME);
    return result;
  }

}

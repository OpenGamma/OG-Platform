/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import com.opengamma.id.IdentifierBundle;

/**
 * Resolves a given security with the appropriate timeseries meta
 * 
 * <p>
 * Meta data includes DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME
 */
public interface TimeSeriesResolver {

  TimeSeriesMetaData resolve(IdentifierBundle identifiers);
}

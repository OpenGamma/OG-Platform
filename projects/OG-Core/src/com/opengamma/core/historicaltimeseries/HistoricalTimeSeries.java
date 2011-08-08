/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A historical time-series providing a value for a series of dates.
 * <p>
 * This provides a time-series on a daily basis that is associated with a unique identifier.
 */
@PublicSPI
public interface HistoricalTimeSeries extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the time-series.
   * 
   * @return the unique identifier, not null
   */
  UniqueId getUniqueId();

  /**
   * Gets the time-series data.
   * 
   * @return the series, not null
   */
  LocalDateDoubleTimeSeries getTimeSeries();

}

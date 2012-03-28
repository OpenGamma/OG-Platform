/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A historical time-series providing a value for a series of dates.
 * <p>
 * This provides a time-series on a daily basis that is associated with a unique identifier.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface HistoricalTimeSeries extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the historical time-series.
   * <p>
   * This specifies a single version-correction of the time-series.
   * 
   * @return the unique identifier for this series, not null within the engine
   */
  UniqueId getUniqueId();

  /**
   * Gets the time-series data.
   * 
   * @return the series, not null
   */
  LocalDateDoubleTimeSeries getTimeSeries();

}

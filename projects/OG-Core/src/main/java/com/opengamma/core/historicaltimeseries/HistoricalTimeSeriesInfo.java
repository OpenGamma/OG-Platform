/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;

/**
 * The information about a historical time-series.
 * <p>
 * This is used to hold the information about a time-series in the master. The actual time-series is held separately.
 */
@PublicSPI
public interface HistoricalTimeSeriesInfo extends UniqueIdentifiable {

  ExternalIdBundleWithDates getExternalIdBundle();

  String getName();

  String getDataField();

  String getDataSource();

  String getDataProvider();

  String getObservationTime();

  ObjectId getTimeSeriesObjectId();

}

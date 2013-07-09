/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.historical;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderResult;
import com.opengamma.master.historicaltimeseries.impl.AbstractHistoricalTimeSeriesLoader;

/**
 * Mock time-series loader to get the example engine server running.
 * 
 * For fully supported implementations supporting major data vendors like Bloomberg and
 * Thomson-Reuters, please contact sales@opengamma.com
 */
public class MockTimeSeriesLoader extends AbstractHistoricalTimeSeriesLoader {

  private static final String MESSAGE = "This is a placeholder time-series loader." +
      "\nFor fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters," +
      "\nPlease contact sales@opengamma.com.";

  @Override
  protected HistoricalTimeSeriesLoaderResult doBulkLoad(HistoricalTimeSeriesLoaderRequest request) {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

  @Override
  public boolean updateTimeSeries(UniqueId uniqueIdentifier) {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

}

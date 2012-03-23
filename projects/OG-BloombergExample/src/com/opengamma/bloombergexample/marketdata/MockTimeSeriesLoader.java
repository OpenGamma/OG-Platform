/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.marketdata;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;

/**
 * Mock time-series loader to get the example engine server running.
 * 
 * For fully supported implementations supporting major data vendors like Bloomberg and
 * Thomson-Reuters, please contact sales@opengamma.com
 */
public class MockTimeSeriesLoader implements HistoricalTimeSeriesLoader {

  private static final String MESSAGE = "This is a placeholder time-series loader." +
      "\nFor fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters," +
      "\nPlease contact sales@opengamma.com.";

  @Override
  public Map<ExternalId, UniqueId> addTimeSeries(Set<ExternalId> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate) {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

  @Override
  public boolean updateTimeSeries(UniqueId uniqueIdentifier) {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

}

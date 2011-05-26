/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.livedata;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.timeseries.TimeSeriesLoader;
import com.opengamma.master.timeseries.TimeSeriesMaster;

/**
 * Mock timeseries loader to get the example engine server running
 * 
 * For fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, please contact sales@opengamma.com
 */
public class MockTimeSeriesLoader implements TimeSeriesLoader {

  private static final String MESSAGE = "This is a placeholder timeseries loader." +
      "\nFor fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters," +
      "\nPlease contact sales@opengamma.com.";
  
  @Override
  public Map<Identifier, UniqueIdentifier> addTimeSeries(Set<Identifier> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate) {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

  @Override
  public boolean updateTimeSeries(UniqueIdentifier uniqueIdentifier) {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

  @Override
  public TimeSeriesMaster<?> getTimeSeriesMaster() {
    return null;
  }

}

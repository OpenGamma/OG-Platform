/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.junit.Test;

import com.opengamma.financial.timeseries.TimeSeriesDocument;
import com.opengamma.financial.timeseries.TimeSeriesMaster;
import com.opengamma.financial.timeseries.TimeSeriesSearchRequest;
import com.opengamma.financial.timeseries.TimeSeriesSearchResult;
import com.opengamma.financial.timeseries.db.LocalDateRowStoreTimeSeriesMaster;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * 
 */
public class LocalDateTimeSeriesMasterTest extends TimeSeriesMasterTest<LocalDate> {
  
  public LocalDateTimeSeriesMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }
  
  @Override
  protected TimeSeriesMaster<LocalDate> getTimeSeriesMaster(Map<String, String> namedSQLMap) {
    return new LocalDateRowStoreTimeSeriesMaster(
        getTransactionManager(), 
        namedSQLMap,
        false);
  }

  @Override
  protected DoubleTimeSeries<LocalDate> getTimeSeries(MapLocalDateDoubleTimeSeries tsMap) {
    return new ArrayLocalDateDoubleTimeSeries(tsMap);
  }

  @Override
  protected DoubleTimeSeries<LocalDate> getEmptyTimeSeries() {
    return new ArrayLocalDateDoubleTimeSeries();
  }

  @Override
  protected DoubleTimeSeries<LocalDate> getTimeSeries(List<LocalDate> dates, List<Double> values) {
    return new ArrayLocalDateDoubleTimeSeries(dates, values);
  }

  @Override
  protected LocalDate convert(LocalDate date) {
    return date;
  }

  @Override
  protected String print(LocalDate date) {
    return DateUtil.printYYYYMMDD(date);
  }
  
  @Test
  public void getTimeSeriesWithDateRange() throws Exception {
    List<TimeSeriesDocument<LocalDate>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<LocalDate> tsDoc : tsList) {
      DoubleTimeSeries<LocalDate> timeSeries = tsDoc.getTimeSeries();
      
      TimeSeriesDocument<LocalDate> searchDoc = getHistoricalTimeSeries(tsDoc.getIdentifiers(),  tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField(), null, null);
      assertNotNull(searchDoc);
      assertEquals(tsDoc.getUniqueIdentifier(), searchDoc.getUniqueIdentifier());
      assertEquals(timeSeries, searchDoc.getTimeSeries());
      
      // test end dates
      LocalDate earliestDate = timeSeries.getEarliestTime();
      LocalDate latestDate = timeSeries.getLatestTime();
      
      searchDoc = getHistoricalTimeSeries(tsDoc.getIdentifiers(),  tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField(), earliestDate, latestDate);
      assertNotNull(searchDoc);
      assertEquals(tsDoc.getUniqueIdentifier(), searchDoc.getUniqueIdentifier());
      assertEquals(timeSeries, searchDoc.getTimeSeries());

      // test subSeries
      LocalDate start = DateUtil.nextWeekDay(earliestDate);
      LocalDate end = DateUtil.previousWeekDay(latestDate);
      if (start.isBefore(end) || start.equals(end)) {
        searchDoc = getHistoricalTimeSeries(tsDoc.getIdentifiers(),  tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField(), start, end);
        assertNotNull(searchDoc);
        assertEquals(tsDoc.getUniqueIdentifier(), searchDoc.getUniqueIdentifier());
        assertEquals(start, searchDoc.getTimeSeries().getEarliestTime());
        assertEquals(end, searchDoc.getTimeSeries().getLatestTime());
      }
    }
  }
  
  private TimeSeriesDocument<LocalDate> getHistoricalTimeSeries(IdentifierBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate earliestDate, LocalDate latestDate) {
    TimeSeriesSearchRequest<LocalDate> request = new TimeSeriesSearchRequest<LocalDate>();
    request.getIdentifiers().addAll(identifierBundle.getIdentifiers());
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    request.setStart(earliestDate);
    request.setEnd(latestDate);
    request.setLoadTimeSeries(true);
    
    TimeSeriesSearchResult<LocalDate> searchResult = getTsMaster().searchTimeSeries(request);
    return searchResult.getDocuments().get(0);
  }
  
}

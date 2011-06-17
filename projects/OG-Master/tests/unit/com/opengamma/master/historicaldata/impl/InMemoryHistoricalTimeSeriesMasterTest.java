/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchResult;
import com.opengamma.master.historicaldata.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Test InMemoryHistoricalTimeSeriesMaster.
 */
@Test
public class InMemoryHistoricalTimeSeriesMasterTest extends AbstractInMemoryHistoricalTimeSeriesMasterTest {

  @Override
  protected HistoricalTimeSeriesMaster createTimeSeriesMaster() {
    return new InMemoryHistoricalTimeSeriesMaster();
  }

  @Override
  protected LocalDateDoubleTimeSeries getTimeSeries(MapLocalDateDoubleTimeSeries tsMap) {
    return new ArrayLocalDateDoubleTimeSeries(tsMap);
  }

  @Override
  protected LocalDateDoubleTimeSeries getEmptyTimeSeries() {
    return new ArrayLocalDateDoubleTimeSeries();
  }

  @Override
  protected LocalDateDoubleTimeSeries getTimeSeries(List<LocalDate> dates, List<Double> values) {
    return new ArrayLocalDateDoubleTimeSeries(dates, values);
  }

  @Override
  protected String print(LocalDate date) {
    return DateUtil.printYYYYMMDD(date);
  }
  
  public void getTimeSeriesWithDateRange() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (HistoricalTimeSeriesDocument tsDoc : tsList) {
      LocalDateDoubleTimeSeries timeSeries = tsDoc.getTimeSeries();
      
      HistoricalTimeSeriesDocument searchDoc = getHistoricalTimeSeries(tsDoc.getIdentifiers(),  tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField(), null, null);
      assertNotNull(searchDoc);
      assertEquals(tsDoc.getUniqueId(), searchDoc.getUniqueId());
      assertEquals(timeSeries, searchDoc.getTimeSeries());
      
      // test end dates
      LocalDate earliestDate = timeSeries.getEarliestTime();
      LocalDate latestDate = timeSeries.getLatestTime();
      
      searchDoc = getHistoricalTimeSeries(tsDoc.getIdentifiers(),  tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField(), earliestDate, latestDate);
      assertNotNull(searchDoc);
      assertEquals(tsDoc.getUniqueId(), searchDoc.getUniqueId());
      assertEquals(timeSeries, searchDoc.getTimeSeries());

      // test subSeries
      LocalDate start = DateUtil.nextWeekDay(earliestDate);
      LocalDate end = DateUtil.previousWeekDay(latestDate);
      if (start.isBefore(end) || start.equals(end)) {
        searchDoc = getHistoricalTimeSeries(tsDoc.getIdentifiers(),  tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField(), start, end);
        assertNotNull(searchDoc);
        assertEquals(tsDoc.getUniqueId(), searchDoc.getUniqueId());
        assertEquals(start, searchDoc.getTimeSeries().getEarliestTime());
        assertEquals(end, searchDoc.getTimeSeries().getLatestTime());
      }
    }
  }
  
  private HistoricalTimeSeriesDocument getHistoricalTimeSeries(IdentifierBundleWithDates identifierBundleWithDates, String dataSource, String dataProvider, String dataField, LocalDate earliestDate, LocalDate latestDate) {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setIdentifiers(identifierBundleWithDates.asIdentifierBundle());
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    request.setStart(earliestDate);
    request.setEnd(latestDate);
    request.setLoadTimeSeries(true);
    
    HistoricalTimeSeriesSearchResult searchResult = getTsMaster().search(request);
    return searchResult.getDocuments().get(0);
  }

}

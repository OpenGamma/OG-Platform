/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Test {@link MasterHistoricalTimeSeriesSource}.
 * Ensure it makes the right method calls to the underlying master and resolver.
 */
@Test
public class MasterHistoricalTimeSeriesSourceTest {

  private static final String TEST_CONFIG = "TEST_CONFIG";
  private static final UniqueId UID = UniqueId.of("A", "1");
  private static final String CLOSE_DATA_FIELD = "CLOSE";
  private static final String CMPL_DATA_PROVIDER = "CMPL";
  private static final String BBG_DATA_SOURCE = "BLOOMBERG";
  private static final ExternalIdBundle IDENTIFIERS = ExternalIdBundle.of("A", "B");
  
  private HistoricalTimeSeriesMaster _mockMaster;
  private HistoricalTimeSeriesResolver _mockResolver;
  private MasterHistoricalTimeSeriesSource _tsSource;

  @BeforeMethod
  public void setUp() throws Exception {
    _mockMaster = mock(HistoricalTimeSeriesMaster.class);
    _mockResolver = mock(HistoricalTimeSeriesResolver.class);
    _tsSource = new MasterHistoricalTimeSeriesSource(_mockMaster, _mockResolver);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _tsSource = null;
    _mockResolver = null;
    _mockMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructorWith1ArgNull() throws Exception {
    HistoricalTimeSeriesResolver mock = mock(HistoricalTimeSeriesResolver.class);
    new MasterHistoricalTimeSeriesSource(null, mock);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructorWith2ArgNull() throws Exception {
    HistoricalTimeSeriesMaster mock = mock(HistoricalTimeSeriesMaster.class);
    new MasterHistoricalTimeSeriesSource(mock, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructorWithNull() throws Exception {
    new MasterHistoricalTimeSeriesSource(null, null);
  }

  public void getHistoricalTimeSeriesByExternalIdWithMetaData() throws Exception {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    request.setValidityDate(LocalDate.now());
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);
    
    HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID);
    doc.getInfo().setTimeSeriesObjectId(UID.getObjectId());
    searchResult.getDocuments().add(doc);
    when(_mockMaster.search(request)).thenReturn(searchResult);
    
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID);
    hts.setTimeSeries(randomTimeSeries());
    when(_mockMaster.getTimeSeries(UID.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    
    HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    verify(_mockMaster, times(1)).search(request);
    verify(_mockMaster, times(1)).getTimeSeries(UID.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    
    assertEquals(UID, test.getUniqueId());
  }

  public void getHistoricalTimeSeriesByExternalIdWithoutMetaData() throws Exception {
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID);
    hts.setTimeSeries(randomTimeSeries());
    when(_mockMaster.getTimeSeries(UID, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    when(_mockResolver.resolve(HistoricalTimeSeriesFields.LAST_PRICE, IDENTIFIERS, LocalDate.now(), TEST_CONFIG)).thenReturn(UID);
    
    HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(HistoricalTimeSeriesFields.LAST_PRICE, IDENTIFIERS, TEST_CONFIG);
    verify(_mockMaster, times(1)).getTimeSeries(UID, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    
    assertEquals(UID, test.getUniqueId());
    assertEquals(hts.getTimeSeries().times(), test.getTimeSeries().times());
    assertEquals(hts.getTimeSeries().values(), test.getTimeSeries().values());
  }

  public void getHistoricalWithInclusiveExclusiveDates() throws Exception {
    LocalDate end = DateUtils.previousWeekDay();
    LocalDate start = end.minusDays(7);
    
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    request.setValidityDate(LocalDate.now());
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);
    LocalDateDoubleTimeSeries timeSeries = randomTimeSeries();
    
    HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID);
    doc.getInfo().setTimeSeriesObjectId(UID.getObjectId());
    searchResult.getDocuments().add(doc);
    
    for (boolean includeStart : new boolean[] {true, false})  {
      for (boolean includeEnd : new boolean[] {true, false}) {
        // Also test max points limit for various values
        for (Integer maxPoints : new Integer[] {null, -10, -1, 1, 0, -2, 2, 10} ) {
          LocalDate startInput = start;
          LocalDate endInput = end;
          if (!includeStart) {
            startInput = start.plusDays(1);
          }
          if (!includeEnd) {
            endInput = end.minusDays(1);
          }
          
          ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
          LocalDateDoubleTimeSeries lddts = 
              (maxPoints == null) || (Math.abs(maxPoints) >= timeSeries.subSeries(start, includeStart, end, includeEnd).size())
              ? timeSeries.subSeries(start, includeStart, end, includeEnd)
              : (maxPoints >= 0)
                ? timeSeries.subSeries(start, includeStart, end, includeEnd).head(maxPoints)
                : timeSeries.subSeries(start, includeStart, end, includeEnd).tail(-maxPoints);
          hts.setUniqueId(UID);
          hts.setTimeSeries(lddts.toLocalDateDoubleTimeSeries());
          when(_mockMaster.getTimeSeries(UID.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(startInput, endInput, maxPoints))).thenReturn(hts);
          when(_mockMaster.search(request)).thenReturn(searchResult);
          
          HistoricalTimeSeries test = (maxPoints == null)
              ? _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, end, includeEnd)
              : _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, end, includeEnd, maxPoints);
          
          assertEquals(UID, test.getUniqueId());
          assertEquals(hts.getTimeSeries(), test.getTimeSeries());
        }
      }
    }
  }

  public void getHistoricalTimeSeriesByUID() throws Exception {
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID);
    hts.setTimeSeries(randomTimeSeries());
    when(_mockMaster.getTimeSeries(UID, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    
    HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(UID);
    verify(_mockMaster, times(1)).getTimeSeries(UID, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    
    assertEquals(UID, test.getUniqueId());
    assertEquals(hts.getTimeSeries().times(), test.getTimeSeries().times());
    assertEquals(hts.getTimeSeries().values(), test.getTimeSeries().values());
  }

  private LocalDateDoubleTimeSeries randomTimeSeries() {
    return RandomTimeSeriesGenerator.makeRandomTimeSeries(200);
  }

}

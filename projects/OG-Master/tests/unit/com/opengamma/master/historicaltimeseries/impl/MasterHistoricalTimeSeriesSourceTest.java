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

import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.core.historicaldata.HistoricalTimeSeriesFields;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Test MasterHistoricalTimeSeriesSource.
 * Ensure it makes the right method calls to the underlying master and resolver.
 */
@Test
public class MasterHistoricalTimeSeriesSourceTest {

  private static final String TEST_CONFIG = "TEST_CONFIG";
  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "1");
  private static final String CLOSE_DATA_FIELD = "CLOSE";
  private static final String CMPL_DATA_PROVIDER = "CMPL";
  private static final String BBG_DATA_SOURCE = "BLOOMBERG";
  private static final IdentifierBundle IDENTIFIERS = IdentifierBundle.of(Identifier.of("A", "B"));
  
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

  public void getHistoricalTimeSeriesByIdentifierWithMetaData() throws Exception {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
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
    when(_mockMaster.getTimeSeries(UID.getObjectId(), VersionCorrection.LATEST, null, null)).thenReturn(hts);
    
    HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    verify(_mockMaster, times(1)).search(request);
    verify(_mockMaster, times(1)).getTimeSeries(UID.getObjectId(), VersionCorrection.LATEST, null, null);
    
    assertEquals(UID, test.getUniqueId());
  }

  public void getHistoricalTimeSeriesByIdentifierWithoutMetaData() throws Exception {
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID);
    hts.setTimeSeries(randomTimeSeries());
    when(_mockMaster.getTimeSeries(UID, null, null)).thenReturn(hts);
    when(_mockResolver.resolve(HistoricalTimeSeriesFields.LAST_PRICE, IDENTIFIERS, TEST_CONFIG)).thenReturn(UID);
    
    HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(HistoricalTimeSeriesFields.LAST_PRICE, IDENTIFIERS, TEST_CONFIG);
    verify(_mockMaster, times(1)).getTimeSeries(UID, null, null);
    
    assertEquals(UID, test.getUniqueId());
    assertEquals(hts.getTimeSeries().times(), test.getTimeSeries().times());
    assertEquals(hts.getTimeSeries().values(), test.getTimeSeries().values());
  }

  public void getHistoricalWithInclusiveExclusiveDates() throws Exception {
    LocalDate end = DateUtil.previousWeekDay();
    LocalDate start = end.minusDays(7);
    
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);
    LocalDateDoubleTimeSeries timeSeries = randomTimeSeries();
    
    HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID);
    doc.getInfo().setTimeSeriesObjectId(UID.getObjectId());
    searchResult.getDocuments().add(doc);
    
    for (boolean startIncluded : new boolean[] {true, false})  {
      for (boolean endExcluded : new boolean[] {true, false}) {
        LocalDate startInput = start;
        LocalDate endInput = end;
        if (!startIncluded) {
          startInput = start.plusDays(1);
        }
        if (endExcluded) {
          endInput = end.minusDays(1);
        }
        
        ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
        hts.setUniqueId(UID);
        hts.setTimeSeries(timeSeries.subSeries(start, startIncluded, end, endExcluded).toLocalDateDoubleTimeSeries());
        when(_mockMaster.getTimeSeries(UID.getObjectId(), VersionCorrection.LATEST, startInput, endInput)).thenReturn(hts);
        when(_mockMaster.search(request)).thenReturn(searchResult);
        
        HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, startIncluded, end, endExcluded);
        assertEquals(UID, test.getUniqueId());
        assertEquals(hts.getTimeSeries(), test.getTimeSeries());
      }
    }
  }

  public void getHistoricalTimeSeriesByUID() throws Exception {
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID);
    hts.setTimeSeries(randomTimeSeries());
    when(_mockMaster.getTimeSeries(UID, null, null)).thenReturn(hts);
    
    HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(UID);
    verify(_mockMaster, times(1)).getTimeSeries(UID, null, null);
    
    assertEquals(UID, test.getUniqueId());
    assertEquals(hts.getTimeSeries().times(), test.getTimeSeries().times());
    assertEquals(hts.getTimeSeries().values(), test.getTimeSeries().values());
  }

  private LocalDateDoubleTimeSeries randomTimeSeries() {
    return RandomTimeSeriesGenerator.makeRandomTimeSeries(200);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.bloomberglp.blpapi.SessionOptions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.masterdb.historicaltimeseries.DbHistoricalTimeSeriesMaster;
import com.opengamma.masterdb.position.DbPositionMaster;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Test BloombergHistoricalLoader.
 */
public class BloombergHistoricalLoaderTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(BloombergHistoricalLoaderTest.class);
  private static final String SECURITIES_FILE_NAME = "test-securities.txt";

  private static final String[] DATA_FIELDS = new String[] {"PX_LAST", "VOLUME"};
  private static final String[] DATA_PROVIDERS = new String[] {"UNKNOWN", "CMPL", "CMPT"};
  private static final String[] DATA_SOURCES = new String[] {BLOOMBERG_DATA_SOURCE_NAME, "REUTERS", "JPM"};
  private static final int TS_DATASET_SIZE = 2;
  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";

  private HistoricalTimeSeriesMaster _master;
  private BloombergHistoricalLoader _loader;
  private TestBulkHistoricalTimeSeriesProvider _historicalTimeSeriesSource;
  private PositionMaster _positionMaster;

  /**
   * Creates an instance specifying the database to run.
   * @param databaseType  the database type
   * @param databaseVersion  the database version
   */
  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public BloombergHistoricalLoaderTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.debug("running test for database = {} version = {}", databaseType, databaseVersion);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    DataSourceTransactionManager transactionManager = getTransactionManager();
    
    _master = setUpTimeSeriesMaster(transactionManager);
    _historicalTimeSeriesSource = new TestBulkHistoricalTimeSeriesProvider();
    PositionMaster positionMaster = setUpPositionMaster(transactionManager);
    _positionMaster = positionMaster;
    
    SessionOptions options = BloombergTestUtils.getSessionOptions();
    BloombergReferenceDataProvider dataProvider = new BloombergReferenceDataProvider(options);
    dataProvider.start();
    
    BloombergIdentifierProvider idProvider = new BloombergIdentifierProvider(dataProvider);
    
    BloombergHistoricalLoader loader = new BloombergHistoricalLoader(_master, _historicalTimeSeriesSource, idProvider);
    loader.setPositionMaster(_positionMaster);
    _loader = loader;
  }

  private PositionMaster setUpPositionMaster(DataSourceTransactionManager transactionManager) {
    DbConnector dbConnector = getDbConnector();
    DbPositionMaster positionMaster = new DbPositionMaster(dbConnector);
    return positionMaster;
  }

  private HistoricalTimeSeriesMaster setUpTimeSeriesMaster(DataSourceTransactionManager transactionManager) {
    HistoricalTimeSeriesMaster ts = new DbHistoricalTimeSeriesMaster(getDbConnector());
    return ts;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _master = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  @Test
  public void updateDB() throws Exception {
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> timeseriesDocs = addAndTestTimeSeries();
    _loader.setUpdateDb(true);
    _loader.run();
    LocalDate previousWeekDay = DateUtils.previousWeekDay();
    for (Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries> original : timeseriesDocs) {
      HistoricalTimeSeriesInfoDocument latestDoc = _master.get(original.getFirst().getObjectId(), VersionCorrection.LATEST);
      assertEquals(original.getFirst().getUniqueId(), latestDoc.getUniqueId());  // document not changed
      HistoricalTimeSeries latestHTS = _master.getTimeSeries(original.getSecond().getUniqueId(), VersionCorrection.LATEST);
      assertNotNull(latestHTS);
      //will update only for Bloomberg dataSource
      if (original.getFirst().getInfo().getDataSource().equals(BLOOMBERG_DATA_SOURCE_NAME)) {
        assertEquals(previousWeekDay, latestHTS.getTimeSeries().getLatestTime());
        LocalDateDoubleTimeSeries originalTS = original.getSecond().getTimeSeries();
        LocalDate earliest = originalTS.getEarliestTime();
        LocalDate latest = originalTS.getLatestTime();
        assertEquals(originalTS, latestHTS.getTimeSeries().subSeries(earliest, true, latest, true));
      } else {
        assertEquals(original.getSecond().getTimeSeries(), latestHTS.getTimeSeries());
      }
    }
  }

  @Test
  public void loadGivenDates() throws Exception {
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> timeseriesDocs = addAndTestTimeSeries();
    _loader.setUpdateDb(true);
    LocalDate previousWeekDay = DateUtils.previousWeekDay();
    _loader.setStartDate(previousWeekDay);
    LocalDate endDate = previousWeekDay.plusDays(7);
    _loader.setEndDate(endDate);
    _loader.run();
    for (Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries> original : timeseriesDocs) {
      HistoricalTimeSeries latestHTS = _master.getTimeSeries(original.getSecond().getUniqueId(), VersionCorrection.LATEST);
      assertNotNull(latestHTS);
      //will update only for Bloomberg dataSource
      if (original.getFirst().getInfo().getDataSource().equals(BLOOMBERG_DATA_SOURCE_NAME)) {
        assertEquals(endDate, latestHTS.getTimeSeries().getLatestTime());
      } else {
        assertEquals(original.getSecond().getTimeSeries().getLatestTime(), latestHTS.getTimeSeries().getLatestTime());
      }
    }
  }

  @Test
  public void updateTimeSeries() throws Exception {
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> timeseriesDocs = addAndTestTimeSeries();
    assertNotNull(timeseriesDocs);
    assertFalse(timeseriesDocs.isEmpty());
    HistoricalTimeSeries previousTS = _master.getTimeSeries(timeseriesDocs.get(0).getSecond().getUniqueId());
    previousTS.getUniqueId();
    assertTrue(_loader.updateTimeSeries(previousTS.getUniqueId()));
    HistoricalTimeSeries updatedTS = _master.getTimeSeries(previousTS.getUniqueId(), VersionCorrection.LATEST);
    assertTrue(!previousTS.getUniqueId().equals(updatedTS.getUniqueId()));
    assertTrue(!previousTS.getTimeSeries().equals(updatedTS.getTimeSeries()));
  }

  @Test
  public void reload() throws Exception {
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> timeseriesDocs = addAndTestTimeSeries();
    _loader.setReload(true);
    LocalDate previousWeekDay = DateUtils.previousWeekDay();
    // dont need to set startDate for reload, but setting it for testing
    _loader.setStartDate(previousWeekDay);
    LocalDate endDate = previousWeekDay.plusDays(7);
    // do not need to set endDate for reload, but setting it for testing
    _loader.setEndDate(endDate);
    _loader.run();
    for (Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries> original : timeseriesDocs) {
      HistoricalTimeSeries latestHTS = _master.getTimeSeries(original.getSecond().getUniqueId(), VersionCorrection.LATEST);
      assertNotNull(latestHTS);
      // after reload time series start date should be same, because the test provider returns
      // time-series with previous start date, but this may be different in production
      assertEquals(original.getSecond().getTimeSeries().getEarliestTime(), latestHTS.getTimeSeries().getEarliestTime());
      // will reload only for Bloomberg dataSource
      if (original.getFirst().getInfo().getDataSource().equals(BLOOMBERG_DATA_SOURCE_NAME)) {
        assertTrue(!original.getSecond().getTimeSeries().equals(latestHTS.getTimeSeries()));
      } else {
        assertEquals(original.getSecond().getTimeSeries(), latestHTS.getTimeSeries());
      }
    }
  }

  @Test
  public void loadInputFile() throws Exception {
    String inputFile = BloombergHistoricalLoaderTest.class.getResource(SECURITIES_FILE_NAME).getPath();
    _loader.setFiles(Collections.singletonList(inputFile));
    String[] dataFields = new String[] {"PX_LAST", "VOLUME"};
    _loader.setDataFields(Arrays.asList(dataFields));
    _loader.setDataProviders(Collections.singleton("CMPL"));
    LocalDate end = DateUtils.previousWeekDay();
    LocalDate start = end.minusDays(7);
    //set dates
    _loader.setStartDate(start);
    _loader.setEndDate(end);
    _loader.run();

    List<String> readLines = FileUtils.readLines(new File(inputFile));
    List<ExternalId> identifiers = new ArrayList<ExternalId>();
    for (String line : readLines) {
      if (_loader.isBbgUniqueId()) {
        identifiers.add(SecurityUtils.bloombergBuidSecurityId(line.trim()));
      } else {
        identifiers.add(SecurityUtils.bloombergTickerSecurityId(line.trim()));
      }
    }

    for (String dataProvider : _loader.getDataProviders()) {
      for (String dataField : _loader.getDataFields()) {
        for (ExternalId identifier : identifiers) {
          HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(identifier);
          request.setDataField(dataField);
          request.setDataProvider(dataProvider);
          request.setDataSource(BLOOMBERG_DATA_SOURCE_NAME);
          HistoricalTimeSeriesInfoSearchResult searchResult = _master.search(request);
          assertNotNull(searchResult);
          List<HistoricalTimeSeriesInfoDocument> documents = searchResult.getDocuments();
          assertNotNull(documents);
          assertTrue(documents.size() == 1);
          HistoricalTimeSeries hts = _master.getTimeSeries(documents.get(0).getInfo().getTimeSeriesObjectId(), VersionCorrection.LATEST, 
              HistoricalTimeSeriesGetFilter.ofRange(start, end));
          assertNotNull(hts);
          assertNotNull(hts.getTimeSeries());
          assertEquals(start, hts.getTimeSeries().getEarliestTime());
          assertEquals(end, hts.getTimeSeries().getLatestTime());
        }
      }
    }
  }

  private class TestBulkHistoricalTimeSeriesProvider implements HistoricalTimeSeriesSource {
    public TestBulkHistoricalTimeSeriesProvider() {
    }

    //keep track of start date to use the same for reloading
    Map<ExternalIdBundle, LocalDate> _startDateMap = new HashMap<ExternalIdBundle, LocalDate>();

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uid) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uid, LocalDate start, boolean includeStart,
        LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
      throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifiers,
        String dataSource, String dataProvider, String dataField) {
      return new SimpleHistoricalTimeSeries(UniqueId.of("A", "1"),
          makeRandomTimeSeries(DateUtils.previousWeekDay().minusWeeks(1), DateUtils.previousWeekDay()));
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifiers,
        LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifiers,
        String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
        LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifiers,
        LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
        boolean includeStart, LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
        boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
        LocalDate end, boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
        boolean includeStart, LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
        LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(
        String dataField, ExternalIdBundle identifiers, String resolutionKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(
        String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(
        String dataField, ExternalIdBundle identifiers, String resolutionKey,
        LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(
        String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey,
        LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
        boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
        boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
        boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
        boolean includeStart, LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    @Override
    public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
        final Set<ExternalIdBundle> identifierSet, final String dataSource, final String dataProvider, final String dataField, final LocalDate start, final boolean includeStart, final LocalDate end, boolean includeEnd) {
      Map<ExternalIdBundle, HistoricalTimeSeries> tsMap = new HashMap<ExternalIdBundle, HistoricalTimeSeries>();
      s_logger.debug("producing TS for startDate={} endDate={}", start, end);
      for (ExternalIdBundle identifiers : identifierSet) {
        LocalDate cachedStart = _startDateMap.get(identifiers);
        if (cachedStart == null) {
          cachedStart = start;
          _startDateMap.put(identifiers, cachedStart);
        }
        LocalDate startToUse = start;
        if (start.isBefore(cachedStart)) {
          startToUse = cachedStart;
        }
        LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(startToUse, end);
        tsMap.put(identifiers, new SimpleHistoricalTimeSeries(UniqueId.of("A", "1"), timeSeries));
      }
      return tsMap;
    }

  }

  private List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> addAndTestTimeSeries() {
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> result = Lists.newArrayList();
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      Set<ExternalIdWithDates> identifierWithDatesSet = Sets.newHashSet(
          ExternalIdWithDates.of(SecurityUtils.bloombergTickerSecurityId("ticker" + i), null, null),
          ExternalIdWithDates.of(SecurityUtils.bloombergBuidSecurityId("buid" + i), null, null)
        );
      ExternalIdBundleWithDates identifiersWithDates = new ExternalIdBundleWithDates(identifierWithDatesSet);
      LocalDate previousWeekDay = DateUtils.previousWeekDay();
      LocalDate start = previousWeekDay.minusDays(10);
      LocalDate end = previousWeekDay.minusDays(1);
      
      for (String dataSource : DATA_SOURCES) {
        for (String dataProvider : DATA_PROVIDERS) {
          for (String dataField : DATA_FIELDS) {
            ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
            info.setName(dataField + " " + dataSource);
            info.setDataField(dataField);
            info.setDataProvider(dataProvider);
            info.setDataSource(dataSource);
            info.setObservationTime(LCLOSE_OBSERVATION_TIME);
            info.setExternalIdBundle(identifiersWithDates);
            HistoricalTimeSeriesInfoDocument added = _master.add(new HistoricalTimeSeriesInfoDocument(info));
            assertNotNull(added);
            assertNotNull(added.getUniqueId());
            
            ExternalIdBundle identifiers = identifiersWithDates.toBundle();
            Map<ExternalIdBundle, HistoricalTimeSeries> resultMap = _historicalTimeSeriesSource.getHistoricalTimeSeries(
                Collections.singleton(identifiers), BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME, dataProvider, dataField, start, true, end, true);
            LocalDateDoubleTimeSeries timeSeries = resultMap.get(identifiers).getTimeSeries();
            //            assertEquals(start, timeSeries.getEarliestTime());
            //            assertEquals(end, timeSeries.getLatestTime());
            UniqueId tsUid = _master.updateTimeSeriesDataPoints(added.getInfo().getTimeSeriesObjectId(), timeSeries);
            
            HistoricalTimeSeries hts = _master.getTimeSeries(tsUid);
            assertNotNull(hts);
            assertEquals(timeSeries, hts.getTimeSeries());
            result.add(Pair.of(added, hts));
          }
        }
      }
    }
    return result;
  }

  private LocalDateDoubleTimeSeries makeRandomTimeSeries(LocalDate start, LocalDate end) {
    MapLocalDateDoubleTimeSeries tsMap = new MapLocalDateDoubleTimeSeries();
    LocalDate current = start;
    tsMap.putDataPoint(current, Math.random());
    while (current.isBefore(end)) {
      current = current.plusDays(1);
      if (isWeekday(current)) {
        tsMap.putDataPoint(current, Math.random());
      }
    }
    return new ArrayLocalDateDoubleTimeSeries(tsMap);
  }

  private boolean isWeekday(LocalDate day) {
    return (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY);
  }

}

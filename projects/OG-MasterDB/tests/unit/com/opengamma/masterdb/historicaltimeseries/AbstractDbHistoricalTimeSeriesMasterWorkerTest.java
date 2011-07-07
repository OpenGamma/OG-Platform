/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import javax.time.Instant;
import javax.time.TimeSource;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.test.DBTest;

/**
 * Base tests for DbHistoricalTimeSeriesMasterWorker via DbHistoricalTimeSeriesMaster.
 */
public abstract class AbstractDbHistoricalTimeSeriesMasterWorkerTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbHistoricalTimeSeriesMasterWorkerTest.class);

  protected DbHistoricalTimeSeriesMaster _htsMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalPortfolios;
  protected int _totalHistoricalTimeSeries;
  protected OffsetDateTime _now;

  public AbstractDbHistoricalTimeSeriesMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _htsMaster = (DbHistoricalTimeSeriesMaster) context.getBean(getDatabaseType() + "DbHistoricalTimeSeriesMaster");
    
    _now = OffsetDateTime.now();
    _htsMaster.setTimeSource(TimeSource.fixed(_now.toInstant()));
    _version1Instant = _now.toInstant().minusSeconds(100);
    _version2Instant = _now.toInstant().minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _htsMaster.getDbSource().getJdbcTemplate();
    template.update("INSERT INTO hts_data_field VALUES (?,?)",
        11, "DF11");
    template.update("INSERT INTO hts_data_field VALUES (?,?)",
        12, "DF12");
    template.update("INSERT INTO hts_data_source VALUES (?,?)",
        21, "DS21");
    template.update("INSERT INTO hts_data_source VALUES (?,?)",
        22, "DS22");
    template.update("INSERT INTO hts_data_provider VALUES (?,?)",
        31, "DP31");
    template.update("INSERT INTO hts_data_provider VALUES (?,?)",
        32, "DP32");
    template.update("INSERT INTO hts_observation_time VALUES (?,?)",
        41, "OT41");
    template.update("INSERT INTO hts_observation_time VALUES (?,?)",
        42, "OT42");
    
    template.update("INSERT INTO hts_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "N101", 11, 21, 31, 41);
    template.update("INSERT INTO hts_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "N102", 12, 22, 32, 42);
    template.update("INSERT INTO hts_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "N201", 11, 21, 31, 41);
    template.update("INSERT INTO hts_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "N202", 11, 21, 31, 42);
    _totalHistoricalTimeSeries = 3;
    
    template.update("INSERT INTO hts_idkey VALUES (?,?,?)",
        501, "TICKER", "V501");
    template.update("INSERT INTO hts_idkey VALUES (?,?,?)",
        502, "NASDAQ", "V502");
    template.update("INSERT INTO hts_idkey VALUES (?,?,?)",
        503, "TICKER", "V503");
    template.update("INSERT INTO hts_idkey VALUES (?,?,?)",
        504, "NASDAQ", "V504");
    template.update("INSERT INTO hts_idkey VALUES (?,?,?)",
        505, "TICKER", "V505");
    template.update("INSERT INTO hts_idkey VALUES (?,?,?)",
        506, "NASDAQ", "V506");
    
    template.update("INSERT INTO hts_doc2idkey VALUES (?,?,?,?)", 101, 501, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey VALUES (?,?,?,?)", 101, 502, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey VALUES (?,?,?,?)", 102, 503, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey VALUES (?,?,?,?)", 102, 504, DbDateUtils.toSqlDate(LocalDate.of(2011, 6, 30)), DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey VALUES (?,?,?,?)", 201, 505, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey VALUES (?,?,?,?)", 201, 506, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey VALUES (?,?,?,?)", 202, 505, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey VALUES (?,?,?,?)", 202, 506, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _htsMaster = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final HistoricalTimeSeriesDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "101", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeries series = test.getSeries();
    assertNotNull(series);
    assertEquals(uid, series.getUniqueId());
    assertEquals("N101", series.getName());
    assertEquals("DF11", series.getDataField());
    assertEquals("DS21", series.getDataSource());
    assertEquals("DP31", series.getDataProvider());
    assertEquals("OT41", series.getObservationTime());
    IdentifierBundleWithDates key = series.getIdentifiers();
    assertNotNull(key);
    assertEquals(2, key.size());
    assertEquals(true, key.getIdentifiers().contains(
        IdentifierWithDates.of(Identifier.of("TICKER", "V501"), null, null)));
    assertEquals(true, key.getIdentifiers().contains(
        IdentifierWithDates.of(Identifier.of("NASDAQ", "V502"), null, null)));
  }

  protected void assert102(final HistoricalTimeSeriesDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "102", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeries series = test.getSeries();
    assertNotNull(series);
    assertEquals(uid, series.getUniqueId());
    assertEquals("N102", series.getName());
    assertEquals("DF12", series.getDataField());
    assertEquals("DS22", series.getDataSource());
    assertEquals("DP32", series.getDataProvider());
    assertEquals("OT42", series.getObservationTime());
    IdentifierBundleWithDates key = series.getIdentifiers();
    assertNotNull(key);
    assertEquals(2, key.size());
    assertEquals(true, key.getIdentifiers().contains(
        IdentifierWithDates.of(Identifier.of("TICKER", "V503"), null, null)));
    assertEquals(true, key.getIdentifiers().contains(
        IdentifierWithDates.of(Identifier.of("NASDAQ", "V504"), LocalDate.of(2011, 6, 30), null)));
  }

  protected void assert201(final HistoricalTimeSeriesDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "201", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeries series = test.getSeries();
    assertNotNull(series);
    assertEquals(uid, series.getUniqueId());
    assertEquals("N201", series.getName());
    assertEquals("DF11", series.getDataField());
    assertEquals("DS21", series.getDataSource());
    assertEquals("DP31", series.getDataProvider());
    assertEquals("OT41", series.getObservationTime());
    IdentifierBundleWithDates key = series.getIdentifiers();
    assertNotNull(key);
    assertEquals(2, key.size());
    assertEquals(true, key.getIdentifiers().contains(
        IdentifierWithDates.of(Identifier.of("TICKER", "V505"), null, null)));
    assertEquals(true, key.getIdentifiers().contains(
        IdentifierWithDates.of(Identifier.of("NASDAQ", "V506"), null, null)));
  }

  protected void assert202(final HistoricalTimeSeriesDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "201", "1");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeries series = test.getSeries();
    assertNotNull(series);
    assertEquals(uid, series.getUniqueId());
    assertEquals("N202", series.getName());
    assertEquals("DF11", series.getDataField());
    assertEquals("DS21", series.getDataSource());
    assertEquals("DP31", series.getDataProvider());
    assertEquals("OT42", series.getObservationTime());
    IdentifierBundleWithDates key = series.getIdentifiers();
    assertNotNull(key);
    assertEquals(2, key.size());
    assertEquals(true, key.getIdentifiers().contains(
        IdentifierWithDates.of(Identifier.of("TICKER", "V505"), null, null)));
    assertEquals(true, key.getIdentifiers().contains(
        IdentifierWithDates.of(Identifier.of("NASDAQ", "V506"), null, null)));
  }

}

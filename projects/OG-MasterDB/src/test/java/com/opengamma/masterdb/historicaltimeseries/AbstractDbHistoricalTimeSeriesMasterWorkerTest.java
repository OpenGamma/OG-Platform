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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Base tests for DbHistoricalTimeSeriesMasterWorker via DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public abstract class AbstractDbHistoricalTimeSeriesMasterWorkerTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbHistoricalTimeSeriesMasterWorkerTest.class);

  protected DbHistoricalTimeSeriesMaster _htsMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected Instant _version3Instant;
  protected Instant _version4Instant;
  protected int _totalPortfolios;
  protected int _totalHistoricalTimeSeries;
  protected OffsetDateTime _now;

  public AbstractDbHistoricalTimeSeriesMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _htsMaster = new DbHistoricalTimeSeriesMaster(getDbConnector());
    
    _now = OffsetDateTime.now();
    _htsMaster.setClock(Clock.fixed(_now.toInstant(), ZoneOffset.UTC));
    _version1Instant = _now.toInstant().minusSeconds(100);
    _version2Instant = _now.toInstant().minusSeconds(50);
    _version3Instant = _now.toInstant().minusSeconds(40);
    _version4Instant = _now.toInstant().minusSeconds(30);
    s_logger.debug("test data now:   {}", _now);
    s_logger.debug("test data 1: {}", _version1Instant);
    s_logger.debug("test data 2: {}", _version2Instant);
    s_logger.debug("test data 3: {}", _version3Instant);
    s_logger.debug("test data 4: {}", _version4Instant);
    final JdbcOperations template = _htsMaster.getDbConnector().getJdbcOperations();
    template.update("INSERT INTO hts_name VALUES (?,?)",
        1, "N101");
    template.update("INSERT INTO hts_name VALUES (?,?)",
        2, "N102");
    template.update("INSERT INTO hts_name VALUES (?,?)",
        3, "N201");
    template.update("INSERT INTO hts_name VALUES (?,?)",
        4, "N202");
    template.update("INSERT INTO hts_name VALUES (?,?)",
        5, "N203");
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
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, 1, 11, 21, 31, 41);
    template.update("INSERT INTO hts_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, 2, 12, 22, 32, 42);
    template.update("INSERT INTO hts_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, 3, 11, 21, 31, 41);
    template.update("INSERT INTO hts_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), toSqlTimestamp(_version3Instant), 4, 11, 21, 31, 42);
    template.update("INSERT INTO hts_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        203, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version3Instant), MAX_SQL_TIMESTAMP, 5, 11, 21, 31, 42);
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
    
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        1, 101, 501, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        2, 101, 502, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        3, 102, 503, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        4, 102, 504, DbDateUtils.toSqlDate(LocalDate.of(2011, 6, 30)), DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        5, 201, 505, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        6, 201, 506, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        7, 202, 505, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        8, 202, 506, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        9, 203, 505, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    template.update("INSERT INTO hts_doc2idkey (id, doc_id, idkey_id, valid_from, valid_to) VALUES (?,?,?,?,?)",
        10, 203, 506, DbDateUtils.MIN_SQL_DATE, DbDateUtils.MAX_SQL_DATE);
    
    template.update("INSERT INTO hts_point VALUES (?,?,?,?,?)",
        101, DbDateUtils.toSqlDate(LocalDate.of(2011, 1, 1)), toSqlTimestamp(_version1Instant), toSqlTimestamp(_version1Instant), 3.1d);
    template.update("INSERT INTO hts_point VALUES (?,?,?,?,?)",
        101, DbDateUtils.toSqlDate(LocalDate.of(2011, 1, 2)), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version2Instant), 3.2d);
    template.update("INSERT INTO hts_point VALUES (?,?,?,?,?)",
        101, DbDateUtils.toSqlDate(LocalDate.of(2011, 1, 3)), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version2Instant), 3.3d);
    template.update("INSERT INTO hts_point VALUES (?,?,?,?,?)",
        101, DbDateUtils.toSqlDate(LocalDate.of(2011, 1, 2)), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version3Instant), 3.21d);
    template.update("INSERT INTO hts_point VALUES (?,?,?,?,?)",
        101, DbDateUtils.toSqlDate(LocalDate.of(2011, 1, 2)), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version4Instant), 3.22d);
    template.update("INSERT INTO hts_point VALUES (?,?,?,?,?)",
        101, DbDateUtils.toSqlDate(LocalDate.of(2011, 1, 3)), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version4Instant), 3.33d);
  }

  @Override
  protected void doTearDown() {
    _htsMaster = null;
  }

  //-------------------------------------------------------------------------
  protected void assert101(final HistoricalTimeSeriesInfoDocument test) {
    UniqueId uniqueId = UniqueId.of("DbHts", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeriesInfo info = test.getInfo();
    assertNotNull(info);
    assertEquals(uniqueId, info.getUniqueId());
    assertEquals("N101", info.getName());
    assertEquals("DF11", info.getDataField());
    assertEquals("DS21", info.getDataSource());
    assertEquals("DP31", info.getDataProvider());
    assertEquals("OT41", info.getObservationTime());
    ExternalIdBundleWithDates key = info.getExternalIdBundle();
    assertNotNull(key);
    assertEquals(2, key.size());
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("TICKER", "V501"), null, null)));
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("NASDAQ", "V502"), null, null)));
  }

  protected void assert102(final HistoricalTimeSeriesInfoDocument test) {
    UniqueId uniqueId = UniqueId.of("DbHts", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeriesInfo info = test.getInfo();
    assertNotNull(info);
    assertEquals(uniqueId, info.getUniqueId());
    assertEquals("N102", info.getName());
    assertEquals("DF12", info.getDataField());
    assertEquals("DS22", info.getDataSource());
    assertEquals("DP32", info.getDataProvider());
    assertEquals("OT42", info.getObservationTime());
    ExternalIdBundleWithDates key = info.getExternalIdBundle();
    assertNotNull(key);
    assertEquals(2, key.size());
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("TICKER", "V503"), null, null)));
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("NASDAQ", "V504"), LocalDate.of(2011, 6, 30), null)));
  }

  protected void assert201(final HistoricalTimeSeriesInfoDocument test) {
    UniqueId uniqueId = UniqueId.of("DbHts", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeriesInfo info = test.getInfo();
    assertNotNull(info);
    assertEquals(uniqueId, info.getUniqueId());
    assertEquals("N201", info.getName());
    assertEquals("DF11", info.getDataField());
    assertEquals("DS21", info.getDataSource());
    assertEquals("DP31", info.getDataProvider());
    assertEquals("OT41", info.getObservationTime());
    ExternalIdBundleWithDates key = info.getExternalIdBundle();
    assertNotNull(key);
    assertEquals(2, key.size());
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("TICKER", "V505"), null, null)));
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("NASDAQ", "V506"), null, null)));
  }

  protected void assert202(final HistoricalTimeSeriesInfoDocument test) {
    UniqueId uniqueId = UniqueId.of("DbHts", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(_version3Instant, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeriesInfo info = test.getInfo();
    assertNotNull(info);
    assertEquals(uniqueId, info.getUniqueId());
    assertEquals("N202", info.getName());
    assertEquals("DF11", info.getDataField());
    assertEquals("DS21", info.getDataSource());
    assertEquals("DP31", info.getDataProvider());
    assertEquals("OT42", info.getObservationTime());
    ExternalIdBundleWithDates key = info.getExternalIdBundle();
    assertNotNull(key);
    assertEquals(2, key.size());
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("TICKER", "V505"), null, null)));
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("NASDAQ", "V506"), null, null)));
  }

  protected void assert203(final HistoricalTimeSeriesInfoDocument test) {
    UniqueId uniqueId = UniqueId.of("DbHts", "201", "2");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version3Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeriesInfo info = test.getInfo();
    assertNotNull(info);
    assertEquals(uniqueId, info.getUniqueId());
    assertEquals("N203", info.getName());
    assertEquals("DF11", info.getDataField());
    assertEquals("DS21", info.getDataSource());
    assertEquals("DP31", info.getDataProvider());
    assertEquals("OT42", info.getObservationTime());
    ExternalIdBundleWithDates key = info.getExternalIdBundle();
    assertNotNull(key);
    assertEquals(2, key.size());
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("TICKER", "V505"), null, null)));
    assertEquals(true, key.getExternalIds().contains(
        ExternalIdWithDates.of(ExternalId.of("NASDAQ", "V506"), null, null)));
  }

}

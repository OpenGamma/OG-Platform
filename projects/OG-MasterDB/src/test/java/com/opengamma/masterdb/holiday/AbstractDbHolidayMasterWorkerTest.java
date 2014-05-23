/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlDate;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Base tests for DbHolidayMasterWorker via DbHolidayMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public abstract class AbstractDbHolidayMasterWorkerTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbHolidayMasterWorkerTest.class);

  protected DbHolidayMaster _holMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalHolidays;

  public AbstractDbHolidayMasterWorkerTest(String databaseType, String databaseVersion, boolean readOnly) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    init();
  }

  @Override
  protected void doTearDown() {
    _holMaster = null;
  }

  @Override
  protected void doTearDownClass() {
    _holMaster = null;
  }

  //-------------------------------------------------------------------------
  private void init() {
    _holMaster = new DbHolidayMaster(getDbConnector());
    
//    id bigint NOT NULL,
//    oid bigint NOT NULL,
//    ver_from_instant timestamp without time zone NOT NULL,
//    ver_to_instant timestamp without time zone NOT NULL,
//    corr_from_instant timestamp without time zone NOT NULL,
//    corr_to_instant timestamp without time zone NOT NULL,
//    name varchar(255) NOT NULL,
//    provider_scheme varchar(255),
//    provider_value varchar(255),
//    hol_type varchar(255) NOT NULL,
//    region_scheme varchar(255),
//    region_value varchar(255),
//    exchange_scheme varchar(255),
//    exchange_value varchar(255),
//    custom_scheme varchar(255),
//    custom_value varchar(255),
//    currency_iso varchar(255),
    Instant now = Instant.now();
    _holMaster.setClock(Clock.fixed(now, ZoneOffset.UTC));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final JdbcOperations template = _holMaster.getDbConnector().getJdbcOperations();
    template.update("INSERT INTO hol_holiday VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestHoliday101", "COPP_CLARK", "1", "CURRENCY", null, null, null, null, null, null, "GBP");
    template.update("INSERT INTO hol_holiday VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestHoliday102", "COPP_CLARK", "2", "CURRENCY", null, null, null, null, null, null, "EUR");
    template.update("INSERT INTO hol_holiday VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestHoliday201", "COPP_CLARK", "3", "CURRENCY", null, null, null, null, null, null, "GBP");
    template.update("INSERT INTO hol_holiday VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP,
        "TestHoliday202", "COPP_CLARK", "3", "CURRENCY", null, null, null, null, null, null, "GBP");
    _totalHolidays = 3;
//    holiday_id bigint not null,
//    hol_date date not null,
    template.update("INSERT INTO hol_date VALUES (?,?)",
        101, toSqlDate(LocalDate.of(2010, 1, 1)));
    template.update("INSERT INTO hol_date VALUES (?,?)",
        102, toSqlDate(LocalDate.of(2010, 1, 2)));
    template.update("INSERT INTO hol_date VALUES (?,?)",
        102, toSqlDate(LocalDate.of(2010, 1, 3)));
    template.update("INSERT INTO hol_date VALUES (?,?)",
        201, toSqlDate(LocalDate.of(2010, 2, 1)));
    template.update("INSERT INTO hol_date VALUES (?,?)",
        202, toSqlDate(LocalDate.of(2010, 2, 1)));
  }

  //-------------------------------------------------------------------------
  protected void assert101(final HolidayDocument test) {
    UniqueId uniqueId = UniqueId.of("DbHol", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uniqueId, holiday.getUniqueId());
    assertEquals(ExternalId.of("COPP_CLARK", "1"), test.getProviderId());
    assertEquals("TestHoliday101", test.getName());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals(null, holiday.getRegionExternalId());
    assertEquals(null, holiday.getExchangeExternalId());
    assertEquals("GBP", holiday.getCurrency().getCode());
    assertEquals(Arrays.asList(LocalDate.of(2010, 1, 1)), holiday.getHolidayDates());
  }

  protected void assert102(final HolidayDocument test) {
    UniqueId uniqueId = UniqueId.of("DbHol", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uniqueId, holiday.getUniqueId());
    assertEquals("TestHoliday102", test.getName());
    assertEquals(ExternalId.of("COPP_CLARK", "2"), test.getProviderId());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals(null, holiday.getRegionExternalId());
    assertEquals(null, holiday.getExchangeExternalId());
    assertEquals("EUR", holiday.getCurrency().getCode());
    assertEquals(Arrays.asList(LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 3)), holiday.getHolidayDates());
  }

  protected void assert201(final HolidayDocument test) {
    UniqueId uniqueId = UniqueId.of("DbHol", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uniqueId, holiday.getUniqueId());
    assertEquals("TestHoliday201", test.getName());
    assertEquals(ExternalId.of("COPP_CLARK", "3"), test.getProviderId());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals(null, holiday.getRegionExternalId());
    assertEquals(null, holiday.getExchangeExternalId());
    assertEquals("GBP", holiday.getCurrency().getCode());
    assertEquals(Arrays.asList(LocalDate.of(2010, 2, 1)), holiday.getHolidayDates());
  }

  protected void assert202(final HolidayDocument test) {
    UniqueId uniqueId = UniqueId.of("DbHol", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uniqueId, holiday.getUniqueId());
    assertEquals("TestHoliday202", test.getName());
    assertEquals(ExternalId.of("COPP_CLARK", "3"), test.getProviderId());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals(null, holiday.getRegionExternalId());
    assertEquals(null, holiday.getExchangeExternalId());
    assertEquals("GBP", holiday.getCurrency().getCode());
    assertEquals(Arrays.asList(LocalDate.of(2010, 2, 1)), holiday.getHolidayDates());
  }

}

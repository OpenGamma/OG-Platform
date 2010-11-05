/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master.db;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static com.opengamma.util.db.DbDateUtils.toSqlDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.TimeSource;
import javax.time.calendar.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.opengamma.financial.master.db.DbMasterTestUtils;
import com.opengamma.financial.world.holiday.HolidayType;
import com.opengamma.financial.world.holiday.master.HolidayDocument;
import com.opengamma.financial.world.holiday.master.ManageableHoliday;
import com.opengamma.financial.world.holiday.master.db.DbHolidayMaster;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.test.DBTest;

/**
 * Base tests for DbHolidayMasterWorker via DbHolidayMaster.
 */
@Ignore
public abstract class AbstractDbHolidayMasterWorkerTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbHolidayMasterWorkerTest.class);

  protected DbHolidayMaster _holMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalHolidays;

  public AbstractDbHolidayMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _holMaster = (DbHolidayMaster) context.getBean(getDatabaseType() + "DbHolidayMaster");
    
//    id bigint not null,
//    oid bigint not null,
//    ver_from_instant timestamp not null,
//    ver_to_instant timestamp not null,
//    name varchar(255) not null,
//    hol_type varchar(255) not null,
//    provider_scheme varchar(255),
//    provider_value varchar(255),
//    region_scheme varchar(255),
//    region_value varchar(255),
//    exchange_scheme varchar(255),
//    exchange_value varchar(255),
//    currency_iso varchar(255),
    Instant now = Instant.nowSystemClock();
    _holMaster.setTimeSource(TimeSource.fixed(now));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _holMaster.getDbSource().getJdbcTemplate();
    template.update("INSERT INTO hol_holiday VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestHoliday101", "COPP_CLARK", "1", "CURRENCY", null, null, null, null, "GBP");
    template.update("INSERT INTO hol_holiday VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestHoliday102", "COPP_CLARK", "2", "CURRENCY", null, null, null, null, "EUR");
    template.update("INSERT INTO hol_holiday VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestHoliday201", "COPP_CLARK", "3", "CURRENCY", null, null, null, null, "GBP");
    template.update("INSERT INTO hol_holiday VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP,
        "TestHoliday202", "COPP_CLARK", "3", "CURRENCY", null, null, null, null, "GBP");
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

  @After
  public void tearDown() throws Exception {
    _holMaster = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final HolidayDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "0");
    assertNotNull(test);
    assertEquals(uid, test.getHolidayId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uid, holiday.getUniqueIdentifier());
    assertEquals(Identifier.of("COPP_CLARK", "1"), test.getProviderId());
    assertEquals("TestHoliday101", test.getName());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals(null, holiday.getRegionId());
    assertEquals(null, holiday.getExchangeId());
    assertEquals("GBP", holiday.getCurrency().getISOCode());
    assertEquals(Arrays.asList(LocalDate.of(2010, 1, 1)), holiday.getHolidayDates());
  }

  protected void assert102(final HolidayDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "102", "0");
    assertNotNull(test);
    assertEquals(uid, test.getHolidayId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uid, holiday.getUniqueIdentifier());
    assertEquals("TestHoliday102", test.getName());
    assertEquals(Identifier.of("COPP_CLARK", "2"), test.getProviderId());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals(null, holiday.getRegionId());
    assertEquals(null, holiday.getExchangeId());
    assertEquals("EUR", holiday.getCurrency().getISOCode());
    assertEquals(Arrays.asList(LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 3)), holiday.getHolidayDates());
  }

  protected void assert201(final HolidayDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "201", "0");
    assertNotNull(test);
    assertEquals(uid, test.getHolidayId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uid, holiday.getUniqueIdentifier());
    assertEquals("TestHoliday201", test.getName());
    assertEquals(Identifier.of("COPP_CLARK", "3"), test.getProviderId());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals(null, holiday.getRegionId());
    assertEquals(null, holiday.getExchangeId());
    assertEquals("GBP", holiday.getCurrency().getISOCode());
    assertEquals(Arrays.asList(LocalDate.of(2010, 2, 1)), holiday.getHolidayDates());
  }

  protected void assert202(final HolidayDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "201", "1");
    assertNotNull(test);
    assertEquals(uid, test.getHolidayId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uid, holiday.getUniqueIdentifier());
    assertEquals("TestHoliday202", test.getName());
    assertEquals(Identifier.of("COPP_CLARK", "3"), test.getProviderId());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals(null, holiday.getRegionId());
    assertEquals(null, holiday.getExchangeId());
    assertEquals("GBP", holiday.getCurrency().getISOCode());
    assertEquals(Arrays.asList(LocalDate.of(2010, 2, 1)), holiday.getHolidayDates());
  }

}

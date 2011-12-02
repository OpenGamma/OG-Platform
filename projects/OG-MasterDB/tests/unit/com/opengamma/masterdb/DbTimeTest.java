/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.springframework.jdbc.core.RowMapper;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.test.DbTest;

/**
 * Tests time in the database.
 */
public class DbTimeTest extends DbTest {
  // TIMESTAMP WITHOUT TIME ZONE is consistent across Postgres and HSQL
  // it stores the visible field values from Timestamp (ignoring the Java and DB time zones)
  // TIMESTAMP WITH TIME ZONE is inconsistent across Postgres and HSQL
  // Postgres stores the UTC instant, with the DB time zone altering viewing
  // HSQL stores the offset, but can double apply it
  // need to use special PreparedStatement.setTimestamp(int,Timestamp,Calendar) to push the time zone
  // and ResultSet.getTimestamp(int,Calendar) to retrieve it

  // thus we use TIMESTAMP WITHOUT TIME ZONE, storing everything as UTC fields
  // the DbDateUtils methods will work fine, so long as the Java time zone is a fixed offset (no DST)
  // with DST, the spring 'Gap' will cause it to go wrong

//  private static final TimeZone  ORIGINAL_ZONE = TimeZone.getDefault();
//  private static final javax.time.calendar.TimeZone TZ_LONDON = javax.time.calendar.TimeZone.of("Europe/London");
  private static final Instant INSTANT1 = OffsetDateTime.of(2011, 1, 1, 12, 30, 40, 567123000, ZoneOffset.UTC).toInstant();  // winter
  private static final Instant INSTANT2 = OffsetDateTime.of(2011, 7, 1, 12, 30, 40, 567123000, ZoneOffset.UTC).toInstant();  // summer
  private static final Instant INSTANT3 = OffsetDateTime.of(2011, 3, 27, 1, 30, 40, 567123000, ZoneOffset.UTC).toInstant();  // Europe spring gap
  private static final DateTimeFormatter FORMAT = DateTimeFormatters.pattern("yyyy-MM-dd HH:mm:ssfnnnnnn");

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbTimeTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @Test
  public void test_writeRead_timestamp() {
    // create test table
    String drop = "DROP TABLE IF EXISTS tst_times";
    getDbConnector().getJdbcTemplate().update(drop);
    String create = "CREATE TABLE tst_times ( id bigint not null, ver timestamp without time zone not null )";
    getDbConnector().getJdbcTemplate().update(create);
    
    // insert data
    String insert = "INSERT INTO tst_times VALUES (?,?)";
    final Timestamp tsOut1 = DbDateUtils.toSqlTimestamp(INSTANT1);
    final Timestamp tsOut2 = DbDateUtils.toSqlTimestamp(INSTANT2);
    final Timestamp tsOut3 = DbDateUtils.toSqlTimestamp(INSTANT3);
    
    getDbConnector().getJdbcTemplate().update(insert, 1, tsOut1);
    getDbConnector().getJdbcTemplate().update(insert, 2, tsOut2);
    getDbConnector().getJdbcTemplate().update(insert, 3, tsOut3);
    
    // pull back to check roundtripping
    String select1 = "SELECT ver FROM tst_times WHERE id = ?";
    
    Map<String, Object> result1 = getDbConnector().getJdbcTemplate().queryForMap(select1, 1);
    Map<String, Object> result2 = getDbConnector().getJdbcTemplate().queryForMap(select1, 2);
    Map<String, Object> result3 = getDbConnector().getJdbcTemplate().queryForMap(select1, 3);
    Timestamp tsIn1 = (Timestamp) result1.get("ver");
    Timestamp tsIn2 = (Timestamp) result2.get("ver");
    Timestamp tsIn3 = (Timestamp) result3.get("ver");
    Instant retrieved1 = DbDateUtils.fromSqlTimestamp(tsIn1);
    Instant retrieved2 = DbDateUtils.fromSqlTimestamp(tsIn2);
    Instant retrieved3 = DbDateUtils.fromSqlTimestamp(tsIn3);
    assertEquals(super.toString() + " Instant " + retrieved1, INSTANT1, retrieved1);
    assertEquals(super.toString() + " Instant " + retrieved2, INSTANT2, retrieved2);
    assertEquals(super.toString() + " Instant " + retrieved3, INSTANT3, retrieved3);
    
    // pull back the raw DB string form to ensure it actually stored UTC field values
    String retrievedText1 = getDbConnector().getJdbcTemplate().queryForObject(select1, new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("ver");
      }
    }, 1);
    String retrievedText2 = getDbConnector().getJdbcTemplate().queryForObject(select1, new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("ver");
      }
    }, 2);
    String retrievedText3 = getDbConnector().getJdbcTemplate().queryForObject(select1, new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("ver");
      }
    }, 3);
    assertEquals(super.toString() + " Instant " + retrieved1, OffsetDateTime.ofInstant(INSTANT1, ZoneOffset.UTC).toString(FORMAT), retrievedText1);
    assertEquals(super.toString() + " Instant " + retrieved2, OffsetDateTime.ofInstant(INSTANT2, ZoneOffset.UTC).toString(FORMAT), retrievedText2);
    assertEquals(super.toString() + " Instant " + retrieved2, OffsetDateTime.ofInstant(INSTANT3, ZoneOffset.UTC).toString(FORMAT), retrievedText3);
    
    // tidy up
    getDbConnector().getJdbcTemplate().update(drop);
  }

//  @Test
//  public void test_experiment() {
////    TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
////    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
//    
//    String setupUTC = "SET TIME ZONE INTERVAL '+0:00' HOUR TO MINUTE";
//    getDbConnector().getJdbcTemplate().update(setupUTC);
//    
//    String create = "CREATE TABLE tst_times ( id bigint not null, ver1 timestamp without time zone not null, ver2 timestamp with time zone not null )";
//    getDbConnector().getJdbcTemplate().update(create);
//    
//    String insert = "INSERT INTO tst_times VALUES (?,?,?)";
//    final Timestamp tsOut1 = DbDateUtils.toSqlTimestamp(INSTANT1);
//    final Timestamp tsOut2 = DbDateUtils.toSqlTimestamp(INSTANT2);
//    
//    getDbConnector().getJdbcTemplate().update(insert, 1, tsOut1, tsOut1);
//    getDbConnector().getJdbcTemplate().update(insert, 2, tsOut2, tsOut2);
//    getDbConnector().getJdbcTemplate().getJdbcOperations().execute(insert, new PreparedStatementCallback<Void>() {
//      @Override
//      public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
//        ps.setInt(1, 3);
//        ps.setTimestamp(2, tsOut1, new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        ps.setTimestamp(3, tsOut1, new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        ps.execute();
//        return null;
//      }
//    });
//    getDbConnector().getJdbcTemplate().getJdbcOperations().execute(insert, new PreparedStatementCallback<Void>() {
//      @Override
//      public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
//        ps.setInt(1, 4);
//        ps.setTimestamp(2, tsOut2, new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        ps.setTimestamp(3, tsOut2, new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        ps.execute();
//        return null;
//      }
//    });
//    
////    String setup = "SET TIME ZONE 'UTC'";
////    String setup = "SET TIME ZONE 'America/Los_Angeles'";
//    String setup = "SET TIME ZONE INTERVAL '+5:00' HOUR TO MINUTE";
//    getDbConnector().getJdbcTemplate().update(setup);
//    
//    getDbConnector().getJdbcTemplate().update(insert, 5, tsOut1, tsOut1);
//    getDbConnector().getJdbcTemplate().update(insert, 6, tsOut2, tsOut2);
//    getDbConnector().getJdbcTemplate().getJdbcOperations().execute(insert, new PreparedStatementCallback<Void>() {
//      @Override
//      public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
//        ps.setInt(1, 7);
//        ps.setTimestamp(2, tsOut1, new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        ps.setTimestamp(3, tsOut1, new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        ps.execute();
//        return null;
//      }
//    });
//    getDbConnector().getJdbcTemplate().getJdbcOperations().execute(insert, new PreparedStatementCallback<Void>() {
//      @Override
//      public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
//        ps.setInt(1, 8);
//        ps.setTimestamp(2, tsOut2, new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        ps.setTimestamp(3, tsOut2, new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        ps.execute();
//        return null;
//      }
//    });
//    
//    getDbConnector().getJdbcTemplate().getJdbcOperations().execute(insert, new PreparedStatementCallback<Void>() {
//      @Override
//      public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
//        ps.setInt(1, 9);
//        ps.setTimestamp(2, tsOut1, new GregorianCalendar(TimeZone.getTimeZone("GMT-11:00")));
//        ps.setTimestamp(3, tsOut1, new GregorianCalendar(TimeZone.getTimeZone("GMT-11:00")));
//        ps.execute();
//        return null;
//      }
//    });
//    Calendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("GMT-06:00"));
//    cal1.setTimeInMillis(INSTANT1.toEpochMillisLong());
//    getDbConnector().getJdbcTemplate().update(insert, 10, cal1, cal1);
//    Calendar cal11 = new GregorianCalendar(TimeZone.getTimeZone("GMT-06:00"));
//    cal11.setTimeInMillis(INSTANT2.toEpochMillisLong());
//    getDbConnector().getJdbcTemplate().update(insert, 11, cal11, cal11);
//    Calendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("GMT+06:00"));
//    cal2.setTimeInMillis(INSTANT1.toEpochMillisLong());
//    getDbConnector().getJdbcTemplate().update(insert, 12, cal2, cal2);
//    Calendar cal21 = new GregorianCalendar(TimeZone.getTimeZone("GMT+06:00"));
//    cal21.setTimeInMillis(INSTANT2.toEpochMillisLong());
//    getDbConnector().getJdbcTemplate().update(insert, 13, cal21, cal21);
//    Calendar cal3 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
//    cal3.setTimeInMillis(INSTANT1.toEpochMillisLong());
//    getDbConnector().getJdbcTemplate().update(insert, 14, cal3, cal3);
//    Calendar cal31 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
//    cal31.setTimeInMillis(INSTANT2.toEpochMillisLong());
//    getDbConnector().getJdbcTemplate().update(insert, 15, cal31, cal31);
//    
//    
////    String select1 = "SELECT NOW() FROM tst_times";
////    Map<String, Object> result1 = getDbConnector().getJdbcTemplate().queryForMap(select1);
////    System.out.println(result1);
//    
//    String select1 = "SELECT ver1, ver2, EXTRACT(TIMEZONE_HOUR FROM ver2) AS offsethr FROM tst_times WHERE id = 1";
//    String select2 = "SELECT ver1, ver2, EXTRACT(TIMEZONE_HOUR FROM ver2) AS offsethr FROM tst_times WHERE id = 2";
//    
//    Map<String, Object> result = getDbConnector().getJdbcTemplate().queryForMap(select1);
//    Number offset = (Number) result.get("offsethr");
//    Timestamp tsIn1 = (Timestamp) result.get("ver1");
//    Timestamp tsIn2 = (Timestamp) result.get("ver2");
//    Instant retrieved1 = DbDateUtils.fromSqlTimestamp(tsIn1);
//    Instant retrieved2 = DbDateUtils.fromSqlTimestamp(tsIn2);
//    Instant retrieved1b = getDbConnector().getJdbcTemplate().query(select1, new RowMapper<Instant>() {
//      @Override
//      public Instant mapRow(ResultSet rs, int rowNum) throws SQLException {
//        Timestamp tsIn = (Timestamp) rs.getTimestamp("ver1", new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        Instant retrieved = DbDateUtils.fromSqlTimestamp(tsIn);
//        return retrieved;
//      }
//    }).get(0);
//    Instant retrieved2b = getDbConnector().getJdbcTemplate().query(select1, new RowMapper<Instant>() {
//      @Override
//      public Instant mapRow(ResultSet rs, int rowNum) throws SQLException {
//        Timestamp tsIn = (Timestamp) rs.getTimestamp("ver2", new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        Instant retrieved = DbDateUtils.fromSqlTimestamp(tsIn);
//        return retrieved;
//      }
//    }).get(0);
//    assertEquals("Offset " + offset, 0, offset.intValue());
//    assertEquals("Instant " + retrieved2 + " " + retrieved2b, INSTANT1, retrieved2);
//    assertEquals("Instant " + retrieved1 + " " + retrieved1b, INSTANT1, retrieved1);
//    
////    String drop = "DROP TABLE tst_times";
////    getDbConnector().getJdbcTemplate().update(drop);
//  }

}

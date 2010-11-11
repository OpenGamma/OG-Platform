/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * Holiday master worker to modify a holiday.
 */
public class ModifyHolidayDbHolidayMasterWorker extends DbHolidayMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ModifyHolidayDbHolidayMasterWorker.class);

  /**
   * Creates an instance.
   */
  public ModifyHolidayDbHolidayMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected HolidayDocument add(final HolidayDocument document) {
    s_logger.debug("addHoliday {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // insert new row
        final Instant now = Instant.now(getTimeSource());
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setHolidayId(null);
        insertHoliday(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected HolidayDocument update(final HolidayDocument document) {
    final UniqueIdentifier uid = document.getHolidayId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("updateHoliday {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final HolidayDocument oldDoc = getHolidayCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setHolidayId(oldDoc.getHolidayId().toLatest());
        insertHoliday(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void remove(final UniqueIdentifier uid) {
    s_logger.debug("removeHoliday {}", uid);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final HolidayDocument oldDoc = getHolidayCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected HolidayDocument correct(final HolidayDocument document) {
    final UniqueIdentifier uid = document.getHolidayId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("correctHoliday {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final HolidayDocument oldDoc = getHolidayCheckLatestCorrection(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setCorrectionToInstant(now);
        updateCorrectionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(oldDoc.getVersionFromInstant());
        document.setVersionToInstant(oldDoc.getVersionToInstant());
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setHolidayId(oldDoc.getHolidayId().toLatest());
        insertHoliday(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the next database id.
   * @param sequenceName  the name of the sequence to query, not null
   * @return the next database id
   */
  protected long nextId(String sequenceName) {
    return getJdbcTemplate().queryForLong(getDbHelper().sqlNextSequenceValueSelect(sequenceName));
  }

  /**
   * Inserts a holiday.
   * @param document  the document, not null
   */
  protected void insertHoliday(final HolidayDocument document) {
    final long holidayId = nextId("hol_holiday_seq");
    final long holidayOid = (document.getHolidayId() != null ? extractOid(document.getHolidayId()) : holidayId);
    // the arguments for inserting into the holiday table
    ManageableHoliday holiday = document.getHoliday();
    final DbMapSqlParameterSource holidayArgs = new DbMapSqlParameterSource()
      .addValue("holiday_id", holidayId)
      .addValue("holiday_oid", holidayOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getName())
      .addValue("provider_scheme", (document.getProviderId() != null ? document.getProviderId().getScheme().getName() : null))
      .addValue("provider_value", (document.getProviderId() != null ? document.getProviderId().getValue() : null))
      .addValue("hol_type", holiday.getType() != null ? holiday.getType().name() : null)
      .addValue("region_scheme", (holiday.getRegionId() != null ? holiday.getRegionId().getScheme().getName() : null))
      .addValue("region_value", (holiday.getRegionId() != null ? holiday.getRegionId().getValue() : null))
      .addValue("exchange_scheme", (holiday.getExchangeId() != null ? holiday.getExchangeId().getScheme().getName() : null))
      .addValue("exchange_value", (holiday.getExchangeId() != null ? holiday.getExchangeId().getValue() : null))
      .addValue("currency_iso", (holiday.getCurrency() != null ? holiday.getCurrency().getISOCode() : null));
    // the arguments for inserting into the date table
    final List<DbMapSqlParameterSource> dateList = new ArrayList<DbMapSqlParameterSource>();
    for (LocalDate date : holiday.getHolidayDates()) {
      final DbMapSqlParameterSource dateArgs = new DbMapSqlParameterSource()
        .addValue("holiday_id", holidayId)
        .addDate("hol_date", date);
      dateList.add(dateArgs);
    }
    getJdbcTemplate().update(sqlInsertHoliday(), holidayArgs);
    getJdbcTemplate().batchUpdate(sqlInsertDate(), dateList.toArray(new DbMapSqlParameterSource[dateList.size()]));
    // set the uid
    final UniqueIdentifier uid = createUniqueIdentifier(holidayOid, holidayId);
    holiday.setUniqueIdentifier(uid);
    document.setHolidayId(uid);
  }

  /**
   * Gets the SQL for inserting a holiday.
   * @return the SQL, not null
   */
  protected String sqlInsertHoliday() {
    return "INSERT INTO hol_holiday " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name, hol_type," +
              "provider_scheme, provider_value, region_scheme, region_value, exchange_scheme, exchange_value, currency_iso) " +
            "VALUES " +
              "(:holiday_id, :holiday_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name, :hol_type," +
              ":provider_scheme, :provider_value, :region_scheme, :region_value, :exchange_scheme, :exchange_value, :currency_iso)";
  }

  /**
   * Gets the SQL for inserting a date.
   * @return the SQL, not null
   */
  protected String sqlInsertDate() {
    return "INSERT INTO hol_date (holiday_id, hol_date) " +
            "VALUES (:holiday_id, :hol_date)";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the holiday document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected HolidayDocument getHolidayCheckLatestVersion(final UniqueIdentifier uid) {
    final HolidayDocument oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the version as ended.
   * @param document  the document to update, not null
   */
  protected void updateVersionToInstant(final HolidayDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("holiday_id", extractRowId(document.getHolidayId()))
      .addTimestamp("ver_to_instant", document.getVersionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateVersionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end version instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end version of a holiday.
   * @return the SQL, not null
   */
  protected String sqlUpdateVersionToInstant() {
    return "UPDATE hol_holiday " +
              "SET ver_to_instant = :ver_to_instant " +
            "WHERE id = :holiday_id " +
              "AND ver_to_instant = :max_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the holiday document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected HolidayDocument getHolidayCheckLatestCorrection(final UniqueIdentifier uid) {
    final HolidayDocument oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getCorrectionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest correction: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the correction as ended.
   * @param document  the document to update, not null
   */
  protected void updateCorrectionToInstant(final HolidayDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("holiday_id", extractRowId(document.getHolidayId()))
      .addTimestamp("corr_to_instant", document.getCorrectionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateCorrectionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end correction instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end correction of a holiday.
   * @return the SQL, not null
   */
  protected String sqlUpdateCorrectionToInstant() {
    return "UPDATE hol_holiday " +
              "SET corr_to_instant = :corr_to_instant " +
            "WHERE id = :holiday_id " +
              "AND corr_to_instant = :max_instant ";
  }

}

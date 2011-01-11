/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import javax.time.Instant;

import org.fudgemsg.FudgeMsgEnvelope;
import org.hsqldb.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * Config master worker to modify a configuration document.
 * 
 * @param <T>  the configuration element type
 */
public class ModifyConfigDbConfigTypeMasterWorker<T> extends DbConfigTypeMasterWorker<T> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigTypeMasterWorker.class);

  /**
   * Creates an instance.
   */
  public ModifyConfigDbConfigTypeMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected ConfigDocument<T> add(final ConfigDocument<T> document) {
    s_logger.debug("addConfig {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // insert new row
        final Instant now = Instant.now(getTimeSource());
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setUniqueId(null);
        insertConfig(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected ConfigDocument<T> update(final ConfigDocument<T> document) {
    final UniqueIdentifier uid = document.getUniqueId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("updateConfig {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final ConfigDocument<T> oldDoc = getConfigCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setUniqueId(oldDoc.getUniqueId().toLatest());
        insertConfig(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void remove(final UniqueIdentifier uid) {
    s_logger.debug("removeConfig {}", uid);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final ConfigDocument<T> oldDoc = getConfigCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected ConfigDocument<T> correct(final ConfigDocument<T> document) {
    final UniqueIdentifier uid = document.getUniqueId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("correctConfig {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final ConfigDocument<T> oldDoc = getConfigCheckLatestCorrection(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setCorrectionToInstant(now);
        updateCorrectionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(oldDoc.getVersionFromInstant());
        document.setVersionToInstant(oldDoc.getVersionToInstant());
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setUniqueId(oldDoc.getUniqueId().toLatest());
        insertConfig(document);
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
   * Inserts a config.
   * @param document  the document, not null
   */
  protected void insertConfig(final ConfigDocument<T> document) {
    final T value = document.getValue();
    final long configId = nextId("cfg_config_seq");
    final long configOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : configId);
    // set the uid
    final UniqueIdentifier uid = createUniqueIdentifier(configOid, configId);
    document.setUniqueId(uid);
    if (value instanceof MutableUniqueIdentifiable) {
      ((MutableUniqueIdentifiable) value).setUniqueId(uid);
    }
    // serialize the configuration value
    FudgeMsgEnvelope env = FUDGE_CONTEXT.toFudgeMsg(value);
    // REVIEW 2011-01-06 Andrew -- the serialization should only add headers for anything subclass to the reified type to match the deserialization call, reduce payload size and allow easier
    // refactoring of stored objects following an upgrade through database operations.
    byte[] bytes = FUDGE_CONTEXT.toByteArray(env.getMessage());
    // the arguments for inserting into the config table
    final MapSqlParameterSource configArgs = new DbMapSqlParameterSource()
      .addValue("config_id", configId)
      .addValue("config_oid", configOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getName())
      .addValue("config_type", getMaster().getReifiedType().getName())
      .addValue("config", new SqlLobValue(bytes, getDbHelper().getLobHandler()), Types.BLOB);
    getJdbcTemplate().update(sqlInsertConfig(), configArgs);
  }

  /**
   * Gets the SQL for inserting a config.
   * @return the SQL, not null
   */
  protected String sqlInsertConfig() {
    return "INSERT INTO cfg_config " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name, config_type, config) " +
            "VALUES " +
              "(:config_id, :config_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name, :config_type, :config)";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected ConfigDocument<T> getConfigCheckLatestVersion(final UniqueIdentifier uid) {
    final ConfigDocument<T> oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the version as ended.
   * @param document  the document to update, not null
   */
  protected void updateVersionToInstant(final ConfigDocument<T> document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("config_id", extractRowId(document.getUniqueId()))
      .addTimestamp("ver_to_instant", document.getVersionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateVersionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end version instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end version of a config.
   * @return the SQL, not null
   */
  protected String sqlUpdateVersionToInstant() {
    return "UPDATE cfg_config " +
              "SET ver_to_instant = :ver_to_instant " +
            "WHERE id = :config_id " +
              "AND ver_to_instant = :max_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected ConfigDocument<T> getConfigCheckLatestCorrection(final UniqueIdentifier uid) {
    final ConfigDocument<T> oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getCorrectionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest correction: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the correction as ended.
   * @param document  the document to update, not null
   */
  protected void updateCorrectionToInstant(final ConfigDocument<T> document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("config_id", extractRowId(document.getUniqueId()))
      .addTimestamp("corr_to_instant", document.getCorrectionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateCorrectionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end correction instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end correction of a config.
   * @return the SQL, not null
   */
  protected String sqlUpdateCorrectionToInstant() {
    return "UPDATE cfg_config " +
              "SET corr_to_instant = :corr_to_instant " +
            "WHERE id = :config_id " +
              "AND corr_to_instant = :max_instant ";
  }

}

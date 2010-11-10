/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master.db;

import java.util.ArrayList;
import java.util.List;

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

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * Exchange master worker to modify a exchange.
 */
public class ModifyExchangeDbExchangeMasterWorker extends DbExchangeMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorker.class);

  /**
   * Creates an instance.
   */
  public ModifyExchangeDbExchangeMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected ExchangeDocument add(final ExchangeDocument document) {
    s_logger.debug("addExchange {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // insert new row
        final Instant now = Instant.now(getTimeSource());
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setExchangeId(null);
        insertExchange(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected ExchangeDocument update(final ExchangeDocument document) {
    final UniqueIdentifier uid = document.getExchangeId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("updateExchange {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final ExchangeDocument oldDoc = getExchangeCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setExchangeId(oldDoc.getExchangeId().toLatest());
        insertExchange(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void remove(final UniqueIdentifier uid) {
    s_logger.debug("removeExchange {}", uid);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final ExchangeDocument oldDoc = getExchangeCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected ExchangeDocument correct(final ExchangeDocument document) {
    final UniqueIdentifier uid = document.getExchangeId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("correctExchange {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final ExchangeDocument oldDoc = getExchangeCheckLatestCorrection(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setCorrectionToInstant(now);
        updateCorrectionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(oldDoc.getVersionFromInstant());
        document.setVersionToInstant(oldDoc.getVersionToInstant());
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setExchangeId(oldDoc.getExchangeId().toLatest());
        insertExchange(document);
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
   * Inserts a exchange.
   * @param document  the document, not null
   */
  protected void insertExchange(final ExchangeDocument document) {
    ManageableExchange exchange = document.getExchange();
    final long exchangeId = nextId("exg_exchange_seq");
    final long exchangeOid = (document.getExchangeId() != null ? extractOid(document.getExchangeId()) : exchangeId);
    // set the uid (needs to go in Fudge message)
    final UniqueIdentifier uid = createUniqueIdentifier(exchangeOid, exchangeId);
    exchange.setUniqueIdentifier(uid);
    document.setExchangeId(uid);
    // the arguments for inserting into the exchange table
    FudgeMsgEnvelope env = FUDGE_CONTEXT.toFudgeMsg(exchange);
    byte[] bytes = FUDGE_CONTEXT.toByteArray(env.getMessage());
    final MapSqlParameterSource exchangeArgs = new DbMapSqlParameterSource()
      .addValue("exchange_id", exchangeId)
      .addValue("exchange_oid", exchangeOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getName())
      .addValue("time_zone", exchange.getTimeZone() != null ? exchange.getTimeZone().getID() : null)
      .addValue("detail", new SqlLobValue(bytes, getDbHelper().getLobHandler()), Types.BLOB);
    // the arguments for inserting into the idkey tables
    final List<DbMapSqlParameterSource> assocList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
    for (Identifier id : exchange.getIdentifiers()) {
      final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
        .addValue("exchange_id", exchangeId)
        .addValue("key_scheme", id.getScheme().getName())
        .addValue("key_value", id.getValue());
      assocList.add(assocArgs);
      if (getJdbcTemplate().queryForList(sqlSelectIdKey(), assocArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long idKeyId = nextId("sec_idkey_seq");
        final DbMapSqlParameterSource idkeyArgs = new DbMapSqlParameterSource()
          .addValue("idkey_id", idKeyId)
          .addValue("key_scheme", id.getScheme().getName())
          .addValue("key_value", id.getValue());
        idKeyList.add(idkeyArgs);
      }
    }
    getJdbcTemplate().update(sqlInsertExchange(), exchangeArgs);
    getJdbcTemplate().batchUpdate(sqlInsertIdKey(), (DbMapSqlParameterSource[]) idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertSecurityIdKey(), (DbMapSqlParameterSource[]) assocList.toArray(new DbMapSqlParameterSource[assocList.size()]));
  }

  /**
   * Gets the SQL for inserting an exchange.
   * @return the SQL, not null
   */
  protected String sqlInsertExchange() {
    return "INSERT INTO exg_exchange " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name, time_zone, detail) " +
            "VALUES " +
              "(:exchange_id, :exchange_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name, :time_zone, :detail)";
  }

  /**
   * Gets the SQL for inserting an exchange-idkey association.
   * @return the SQL, not null
   */
  protected String sqlInsertSecurityIdKey() {
    return "INSERT INTO exg_exchange2idkey " +
              "(exchange_id, idkey_id) " +
            "VALUES " +
              "(:exchange_id, (" + sqlSelectIdKey() + "))";
  }

  /**
   * Gets the SQL for selecting an idkey.
   * @return the SQL, not null
   */
  protected String sqlSelectIdKey() {
    return "SELECT id FROM exg_idkey WHERE key_scheme = :key_scheme AND key_value = :key_value";
  }

  /**
   * Gets the SQL for inserting an idkey.
   * @return the SQL, not null
   */
  protected String sqlInsertIdKey() {
    return "INSERT INTO exg_idkey (id, key_scheme, key_value) " +
            "VALUES (:idkey_id, :key_scheme, :key_value)";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected ExchangeDocument getExchangeCheckLatestVersion(final UniqueIdentifier uid) {
    final ExchangeDocument oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the version as ended.
   * @param document  the document to update, not null
   */
  protected void updateVersionToInstant(final ExchangeDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("exchange_id", extractRowId(document.getExchangeId()))
      .addTimestamp("ver_to_instant", document.getVersionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateVersionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end version instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end version of a exchange.
   * @return the SQL, not null
   */
  protected String sqlUpdateVersionToInstant() {
    return "UPDATE exg_exchange " +
              "SET ver_to_instant = :ver_to_instant " +
            "WHERE id = :exchange_id " +
              "AND ver_to_instant = :max_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected ExchangeDocument getExchangeCheckLatestCorrection(final UniqueIdentifier uid) {
    final ExchangeDocument oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getCorrectionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest correction: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the correction as ended.
   * @param document  the document to update, not null
   */
  protected void updateCorrectionToInstant(final ExchangeDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("exchange_id", extractRowId(document.getExchangeId()))
      .addTimestamp("corr_to_instant", document.getCorrectionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateCorrectionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end correction instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end correction of a exchange.
   * @return the SQL, not null
   */
  protected String sqlUpdateCorrectionToInstant() {
    return "UPDATE exg_exchange " +
              "SET corr_to_instant = :corr_to_instant " +
            "WHERE id = :exchange_id " +
              "AND corr_to_instant = :max_instant ";
  }

}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.time.DateUtil;

/**
 * Security master worker to modify a security.
 */
public class ModifySecurityDbSecurityMasterWorker extends DbSecurityMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorker.class);

  /**
   * Creates an instance.
   */
  public ModifySecurityDbSecurityMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityDocument add(final SecurityDocument document) {
    s_logger.debug("addSecurity {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // insert new row
        final Instant now = Instant.now(getTimeSource());
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setSecurityId(null);
        insertSecurity(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityDocument update(final SecurityDocument document) {
    final UniqueIdentifier uid = document.getSecurityId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("updateSecurity {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final SecurityDocument oldDoc = getSecurityCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setSecurityId(oldDoc.getSecurityId().toLatest());
        insertSecurity(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void remove(final UniqueIdentifier uid) {
    s_logger.debug("removeSecurity {}", uid);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final SecurityDocument oldDoc = getSecurityCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityDocument correct(final SecurityDocument document) {
    final UniqueIdentifier uid = document.getSecurityId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("correctSecurity {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final SecurityDocument oldDoc = getSecurityCheckLatestCorrection(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setCorrectionToInstant(now);
        updateCorrectionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(oldDoc.getVersionFromInstant());
        document.setVersionToInstant(oldDoc.getVersionToInstant());
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setSecurityId(oldDoc.getSecurityId().toLatest());
        insertSecurity(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a security.
   * @param document  the document, not null
   */
  protected void insertSecurity(final SecurityDocument document) {
    final long securityId = nextId();
    final long securityOid = (document.getSecurityId() != null ? extractOid(document.getSecurityId()) : securityId);
    // the arguments for inserting into the security table
    final DbMapSqlParameterSource securityArgs = new DbMapSqlParameterSource()
      .addValue("security_id", securityId)
      .addValue("security_oid", securityOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getSecurity().getName())
      .addValue("sec_type", document.getSecurity().getSecurityType());
    // the arguments for inserting into the idkey table
    final List<DbMapSqlParameterSource> secKeyList = new ArrayList<DbMapSqlParameterSource>();
    for (Identifier id : document.getSecurity().getIdentifiers()) {
      final long secKeyId = nextId();
      final DbMapSqlParameterSource treeArgs = new DbMapSqlParameterSource()
        .addValue("idkey_id", secKeyId)
        .addValue("security_id", securityId)
        .addValue("id_scheme", id.getScheme().getName())
        .addValue("id_value", id.getValue());
      secKeyList.add(treeArgs);
    }
    getJdbcTemplate().update(sqlInsertSecurity(), securityArgs);
    getJdbcTemplate().batchUpdate(sqlInsertSecurityKey(), (DbMapSqlParameterSource[]) secKeyList.toArray(new DbMapSqlParameterSource[secKeyList.size()]));
    // set the uid
    final UniqueIdentifier uid = createUniqueIdentifier(securityOid, securityId, null);
    UniqueIdentifiables.setInto(document.getSecurity(), uid);
    document.setSecurityId(uid);
  }

  /**
   * Gets the SQL for inserting a security.
   * @return the SQL, not null
   */
  protected String sqlInsertSecurity() {
    return "INSERT INTO sec_security " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name, sec_type) " +
            "VALUES " +
              "(:security_id, :security_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name, :sec_type)";
  }

  /**
   * Gets the SQL for inserting a security key.
   * @return the SQL, not null
   */
  protected String sqlInsertSecurityKey() {
    return "INSERT INTO sec_identitykey " +
              "(id, security_id, id_scheme, id_value) " +
            "VALUES " +
              "(:idkey_id, :security_id, :id_scheme, :id_value)";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected SecurityDocument getSecurityCheckLatestVersion(final UniqueIdentifier uid) {
    final SecurityDocument oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the version as ended.
   * @param document  the document to update, not null
   */
  protected void updateVersionToInstant(final SecurityDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("security_id", extractRowId(document.getSecurityId()))
      .addTimestamp("ver_to_instant", document.getVersionToInstant())
      .addValue("max_instant", DateUtil.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateVersionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end version instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end version of a security.
   * @return the SQL, not null
   */
  protected String sqlUpdateVersionToInstant() {
    return "UPDATE sec_security " +
              "SET ver_to_instant = :ver_to_instant " +
            "WHERE id = :security_id " +
              "AND ver_to_instant = :max_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected SecurityDocument getSecurityCheckLatestCorrection(final UniqueIdentifier uid) {
    final SecurityDocument oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getCorrectionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest correction: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the correction as ended.
   * @param document  the document to update, not null
   */
  protected void updateCorrectionToInstant(final SecurityDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("security_id", extractRowId(document.getSecurityId()))
      .addTimestamp("corr_to_instant", document.getCorrectionToInstant())
      .addValue("max_instant", DateUtil.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateCorrectionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end correction instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end correction of a security.
   * @return the SQL, not null
   */
  protected String sqlUpdateCorrectionToInstant() {
    return "UPDATE sec_security " +
              "SET corr_to_instant = :corr_to_instant " +
            "WHERE id = :security_id " +
              "AND corr_to_instant = :max_instant ";
  }

}

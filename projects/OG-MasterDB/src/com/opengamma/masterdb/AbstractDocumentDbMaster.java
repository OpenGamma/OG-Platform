/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import java.util.List;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.AbstractMaster;
import com.opengamma.master.listener.BasicMasterChangeManager;
import com.opengamma.master.listener.MasterChangeManager;
import com.opengamma.master.listener.MasterChangedType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

/**
 * An abstract master for rapid implementation of a standard version-correction
 * document database backed master.
 * <p>
 * This provides common implementations of methods in a standard {@link AbstractMaster}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 * 
 * @param <D>  the type of the document
 */
public abstract class AbstractDocumentDbMaster<D extends AbstractDocument> extends AbstractDbMaster implements AbstractMaster<D> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDocumentDbMaster.class);

  /**
   * The change manager.
   */
  private MasterChangeManager _changeManager = new BasicMasterChangeManager();
  /**
   * The maximum number of retries.
   */
  private int _maxRetries = 10;

  /**
   * Creates an instance.
   * 
   * @param dbSource  the database source combining all configuration, not null
   * @param defaultScheme  the default scheme for unique identifier, not null
   */
  public AbstractDocumentDbMaster(final DbSource dbSource, final String defaultScheme) {
    super(dbSource, defaultScheme);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the maximum number of retries.
   * The default is ten.
   * 
   * @return the maximum number of retries, not null
   */
  public int getMaxRetries() {
    return _maxRetries;
  }

  /**
   * Sets the maximum number of retries.
   * The default is ten.
   * 
   * @param maxRetries  the maximum number of retries, not negative
   */
  public void setMaxRetries(final int maxRetries) {
    ArgumentChecker.notNegative(maxRetries, "maxRetries");
    _maxRetries = maxRetries;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the change manager.
   * 
   * @return the change manager, not null
   */
  public MasterChangeManager getChangeManager() {
    return _changeManager;
  }

  /**
   * Sets the change manager.
   * 
   * @param changeManager  the change manager, not null
   */
  public void setChangeManager(final MasterChangeManager changeManager) {
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the change manager that handles events.
   * 
   * @return the change manager, not null if in use
   */
  public MasterChangeManager changeManager() {
    return getChangeManager();
  }

  //-------------------------------------------------------------------------
  /**
   * Performs a standard get by unique identifier, handling exact version or latest.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param extractor  the extractor to use, not null
   * @param masterName  a name describing the contents of the master for an error message, not null
   * @return the document, null if not found
   */
  public D doGet(final UniqueIdentifier uniqueId, final ResultSetExtractor<List<D>> extractor, final String masterName) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    checkScheme(uniqueId);
    
    if (uniqueId.isVersioned()) {
      return doGetById(uniqueId, extractor, masterName);
    } else {
      return doGetByOidInstants(uniqueId, VersionCorrection.LATEST, extractor, masterName);
    }
  }

  /**
   * Performs a standard get by object identifier at instants.
   * 
   * @param objectId  the object identifier, not null
   * @param versionCorrection  the version-correction locator, not null
   * @param extractor  the extractor to use, not null
   * @param masterName  a name describing the contents of the master for an error message, not null
   * @return the document, null if not found
   */
  protected D doGetByOidInstants(
      final ObjectIdentifiable objectId, final VersionCorrection versionCorrection,
      final ResultSetExtractor<List<D>> extractor, final String masterName) {
    ArgumentChecker.notNull(objectId, "oid");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(extractor, "extractor");
    s_logger.debug("getByOidInstants {}", objectId);
    
    final VersionCorrection vc = versionCorrection.withLatestFixed(Instant.now(getTimeSource()));
    final DbMapSqlParameterSource args = argsGetByOidInstants(objectId, vc);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<D> docs = namedJdbc.query(sqlGetByOidInstants(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException(masterName + " not found: " + objectId);
    }
    return docs.get(0);
  }

  /**
   * Gets the SQL arguments to use for a standard get by object identifier at instants.
   * 
   * @param objectId  the object identifier, not null
   * @param versionCorrection  the version-correction locator with instants fixed, not null
   * @return the SQL arguments, not null
   */
  protected DbMapSqlParameterSource argsGetByOidInstants(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    final long docOid = extractOid(objectId);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_oid", docOid)
      .addTimestamp("version_as_of", versionCorrection.getVersionAsOf())
      .addTimestamp("corrected_to", versionCorrection.getCorrectedTo());
    return args;
  }

  /**
   * Gets the SQL for a standard get by object identifier at instants.
   * 
   * @return the SQL, not null
   */
  protected String sqlGetByOidInstants() {
    return sqlSelectFrom() +
      "WHERE main.oid = :doc_oid " +
        "AND main.ver_from_instant <= :version_as_of AND main.ver_to_instant > :version_as_of " +
        "AND main.corr_from_instant <= :corrected_to AND main.corr_to_instant > :corrected_to " +
      sqlAdditionalWhere() +
      sqlAdditionalOrderBy(true);
  }

  /**
   * Performs a standard get by versioned unique identifier.
   * 
   * @param uniqueId  the versioned unique identifier, not null
   * @param extractor  the extractor to use, not null
   * @param masterName  a name describing the contents of the master for an error message, not null
   * @return the document, null if not found
   */
  protected D doGetById(final UniqueIdentifier uniqueId, final ResultSetExtractor<List<D>> extractor, final String masterName) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(extractor, "extractor");
    s_logger.debug("getById {}", uniqueId);
    
    final DbMapSqlParameterSource args = argsGetById(uniqueId);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<D> docs = namedJdbc.query(sqlGetById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException(masterName + " not found: " + uniqueId);
    }
    return docs.get(0);
  }

  /**
   * Gets the SQL arguments to use for a standard get by versioned unique identifier.
   * 
   * @param uniqueId  the versioned unique identifier, not null
   * @return the SQL arguments, not null
   */
  protected DbMapSqlParameterSource argsGetById(final UniqueIdentifier uniqueId) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_id", extractRowId(uniqueId));
    return args;
  }

  /**
   * Gets the SQL for a standard get by versioned unique identifier.
   * 
   * @return the SQL, not null
   */
  protected String sqlGetById() {
    return sqlSelectFrom() + "WHERE main.id = :doc_id " + sqlAdditionalWhere() + sqlAdditionalOrderBy(true);
  }

  //-------------------------------------------------------------------------
  /**
   * Performs a standard history search.
   * 
   * @param <R>  the document result type
   * @param request  the request, not null
   * @param result  the result to populate, not null
   * @param extractor  the extractor to use, not null
   * @return the populated result, not null
   */
  protected <R extends AbstractHistoryResult<D>> R doHistory(
      final AbstractHistoryRequest request, final R result,
      final ResultSetExtractor<List<D>> extractor) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(result, "result");
    ArgumentChecker.notNull(extractor, "extractor");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    checkScheme(request.getObjectId());
    s_logger.debug("history {}", request);
    
    final DbMapSqlParameterSource args = argsHistory(request);
    searchWithPaging(request.getPagingRequest(), sqlHistory(request), args, extractor, result);
    return result;
  }

  /**
   * Gets the SQL arguments to use for searching the history of a document.
   * 
   * @param request  the request, not null
   * @return the SQL arguments, not null
   */
  protected DbMapSqlParameterSource argsHistory(final AbstractHistoryRequest request) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_oid", extractOid(request.getObjectId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant());
    return args;
  }

  /**
   * Gets the SQL for searching the history of a document.
   * 
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlHistory(final AbstractHistoryRequest request) {
    String where = sqlHistoryWhere(request);
    String selectFromWhereInner = "SELECT id FROM " + mainTableName() + " " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = sqlSelectFrom() + "WHERE main.id IN (" + inner + ") ORDER BY main.ver_from_instant DESC, main.corr_from_instant DESC" + sqlAdditionalOrderBy(false);
    String count = "SELECT COUNT(*) FROM " + mainTableName() + " " + where;
    return new String[] {search, count};
  }

  /**
   * Gets the SQL where clause for searching the history of a document.
   * 
   * @param request  the request, not null
   * @return the SQL where clause, not null
   */
  protected String sqlHistoryWhere(final AbstractHistoryRequest request) {
    String where = "WHERE oid = :doc_oid ";
    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
      where += "AND ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant ";
    } else {
      if (request.getVersionsFromInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant) " +
                            "OR ver_from_instant >= :versions_from_instant) ";
      }
      if (request.getVersionsToInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_to_instant AND ver_to_instant > :versions_to_instant) " +
                            "OR ver_to_instant < :versions_to_instant) ";
      }
    }
    if (request.getCorrectionsFromInstant() != null && request.getCorrectionsFromInstant().equals(request.getCorrectionsToInstant())) {
      where += "AND corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant ";
    } else {
      if (request.getCorrectionsFromInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant) " +
                            "OR corr_from_instant >= :corrections_from_instant) ";
      }
      if (request.getCorrectionsToInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_to_instant AND ver_to_instant > :corrections_to_instant) " +
                            "OR corr_to_instant < :corrections_to_instant) ";
      }
    }
    where += sqlAdditionalWhere();
    return where;
  }

  //-------------------------------------------------------------------------
  /**
   * Searches for documents with paging.
   * 
   * @param pagingRequest  the paging request, not null
   * @param sql  the array of SQL, query and count, not null
   * @param args  the query arguments, not null
   * @param extractor  the extractor of results, not null
   * @param result  the object to populate, not null
   */
  protected void searchWithPaging(
      final PagingRequest pagingRequest, final String[] sql, final DbMapSqlParameterSource args,
      final ResultSetExtractor<List<D>> extractor, final AbstractDocumentsResult<D> result) {
    
    s_logger.debug("with args {}", args);
    final NamedParameterJdbcOperations namedJdbc = getDbSource().getJdbcTemplate().getNamedParameterJdbcOperations();
    if (pagingRequest.equals(PagingRequest.ALL)) {
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      result.setPaging(Paging.of(result.getDocuments(), pagingRequest));
    } else {
      s_logger.debug("executing sql {}", sql[1]);
      final int count = namedJdbc.queryForInt(sql[1], args);
      result.setPaging(new Paging(pagingRequest, count));
      if (count > 0) {
        s_logger.debug("executing sql {}", sql[0]);
        result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the next database id.
   * 
   * @param sequenceName  the name of the sequence to query, not null
   * @return the next database id
   */
  protected long nextId(String sequenceName) {
    return getJdbcTemplate().queryForLong(getDbHelper().sqlNextSequenceValueSelect(sequenceName));
  }

  //-------------------------------------------------------------------------
  @Override
  public D add(final D document) {
    ArgumentChecker.notNull(document, "document");
    s_logger.debug("add {}", document);
    
    // retry to handle concurrent conflicting inserts into unique content tables
    for (int retry = 0; true; retry++) {
      try {
        D result = getTransactionTemplate().execute(new TransactionCallback<D>() {
          @Override
          public D doInTransaction(final TransactionStatus status) {
            // insert new row
            final Instant now = Instant.now(getTimeSource());
            document.setVersionFromInstant(now);
            document.setVersionToInstant(null);
            document.setCorrectionFromInstant(now);
            document.setCorrectionToInstant(null);
            document.setUniqueId(null);
            insert(document);
            return document;
          }
        });
        changeManager().masterChanged(MasterChangedType.ADDED, null, result.getUniqueId(), result.getVersionFromInstant());
        return result;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public D update(final D document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    checkScheme(document.getUniqueId());
    s_logger.debug("update {}", document);
    
    // retry to handle concurrent conflicting inserts into unique content tables
    for (int retry = 0; true; retry++) {
      try {
        final UniqueIdentifier beforeId = document.getUniqueId();
        ArgumentChecker.isTrue(beforeId.isVersioned(), "UniqueIdentifier must be versioned");
        D result = getTransactionTemplate().execute(new TransactionCallback<D>() {
          @Override
          public D doInTransaction(final TransactionStatus status) {
            // load old row
            final D oldDoc = getCheckLatestVersion(beforeId);
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
            insert(document);
            return document;
          }
        });
        changeManager().masterChanged(MasterChangedType.UPDATED, beforeId, result.getUniqueId(), result.getVersionFromInstant());
        return result;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    checkScheme(uniqueId);
    s_logger.debug("remove {}", uniqueId);
    
    // retry to handle concurrent conflicting inserts into unique content tables
    for (int retry = 0; true; retry++) {
      try {
        D result = getTransactionTemplate().execute(new TransactionCallback<D>() {
          @Override
          public D doInTransaction(final TransactionStatus status) {
            // load old row
            final D oldDoc = getCheckLatestVersion(uniqueId);
            // update old row
            final Instant now = Instant.now(getTimeSource());
            oldDoc.setVersionToInstant(now);
            updateVersionToInstant(oldDoc);
            return oldDoc;
          }
        });
        changeManager().masterChanged(MasterChangedType.REMOVED, result.getUniqueId(), null, result.getVersionToInstant());
        return;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  public D correct(final D document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    checkScheme(document.getUniqueId());
    s_logger.debug("correct {}", document);
    
    // retry to handle concurrent conflicting inserts into unique content tables
    for (int retry = 0; true; retry++) {
      try {
        final UniqueIdentifier beforeId = document.getUniqueId();
        ArgumentChecker.isTrue(beforeId.isVersioned(), "UniqueIdentifier must be versioned");
        D result = getTransactionTemplate().execute(new TransactionCallback<D>() {
          @Override
          public D doInTransaction(final TransactionStatus status) {
            // load old row
            final D oldDoc = getCheckLatestCorrection(beforeId);
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
            insert(document);
            return document;
          }
        });
        changeManager().masterChanged(MasterChangedType.CORRECTED, beforeId, result.getUniqueId(), result.getVersionFromInstant());
        return result;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document to insert, not null
   * @return the new document, not null
   */
  protected abstract D insert(D document);

  //-------------------------------------------------------------------------
  /**
   * Gets the document ensuring that it is the latest version.
   * 
   * @param uniqueId  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected D getCheckLatestVersion(final UniqueIdentifier uniqueId) {
    final D oldDoc = get(uniqueId);  // checks uniqueId exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uniqueId);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the version as ended.
   * 
   * @param document  the document to update, not null
   */
  protected void updateVersionToInstant(final D document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_id", extractRowId(document.getUniqueId()))
      .addTimestamp("ver_to_instant", document.getVersionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateVersionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end version instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end version of a document.
   * 
   * @return the SQL, not null
   */
  protected String sqlUpdateVersionToInstant() {
    return "UPDATE " + mainTableName() + " " +
              "SET ver_to_instant = :ver_to_instant " +
            "WHERE id = :doc_id " +
              "AND ver_to_instant = :max_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the document ensuring that it is the latest version.
   * 
   * @param uniqueId  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected D getCheckLatestCorrection(final UniqueIdentifier uniqueId) {
    final D oldDoc = get(uniqueId);  // checks uniqueId exists
    if (oldDoc.getCorrectionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest correction: " + uniqueId);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the correction as ended.
   * 
   * @param document  the document to update, not null
   */
  protected void updateCorrectionToInstant(final D document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_id", extractRowId(document.getUniqueId()))
      .addTimestamp("corr_to_instant", document.getCorrectionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateCorrectionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end correction instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end correction of a document.
   * 
   * @return the SQL, not null
   */
  protected String sqlUpdateCorrectionToInstant() {
    return "UPDATE " + mainTableName() + " " +
              "SET corr_to_instant = :corr_to_instant " +
            "WHERE id = :doc_id " +
              "AND corr_to_instant = :max_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the SQL select-from clause.
   * The main table must be aliased to 'main'.
   * 
   * @return the SQL select-from, not null
   */
  protected abstract String sqlSelectFrom();

  /**
   * Gets the additional SQL where clause added at the end.
   * This is used to refine the search space, and must start with 'AND' and end
   * with a space. It may be an empty string.
   * <p>
   * The default value is an empty string.
   * 
   * @return the SQL where, not null
   */
  protected String sqlAdditionalWhere() {
    return "";
  }

  /**
   * Gets the additional SQL order-by clause added at the end.
   * This is used to sort joined tables, and must start with 'ORDER BY' or ', ' depending
   * on the flag, and end with a space. It may be a single space.
   * <p>
   * The default value is a single space.
   * 
   * @param orderByPrefix  true to prefix by 'ORDER BY', false to prefix by comma ', '.
   * @return the SQL order by, not null
   */
  protected String sqlAdditionalOrderBy(final boolean orderByPrefix) {
    return " ";
  }

  /**
   * Gets the main table.
   * This table must have the columns id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant.
   * 
   * @return the main table, not null
   */
  protected abstract String mainTableName();

}

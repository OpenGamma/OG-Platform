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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.extsql.ExtSqlBundle;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.AbstractMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;

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
   * External SQL bundle.
   */
  private ExtSqlBundle _externalSqlBundle;
  /**
   * The maximum number of retries.
   */
  private int _maxRetries = 10;
  /**
   * The change manager.
   */
  private ChangeManager _changeManager = new BasicChangeManager();

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   * @param defaultScheme  the default scheme for unique identifier, not null
   */
  public AbstractDocumentDbMaster(final DbConnector dbConnector, final String defaultScheme) {
    super(dbConnector, defaultScheme);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the external SQL bundle.
   * 
   * @return the external SQL bundle, not null
   */
  public ExtSqlBundle getExtSqlBundle() {
    return _externalSqlBundle;
  }

  /**
   * Sets the external SQL bundle.
   * 
   * @param bundle  the external SQL bundle, not null
   */
  public void setExtSqlBundle(ExtSqlBundle bundle) {
    _externalSqlBundle = bundle;
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
  public ChangeManager getChangeManager() {
    return _changeManager;
  }

  /**
   * Sets the change manager.
   * 
   * @param changeManager  the change manager, not null
   */
  public void setChangeManager(final ChangeManager changeManager) {
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the change manager that handles events.
   * 
   * @return the change manager, not null if in use
   */
  public ChangeManager changeManager() {
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
  protected D doGet(final UniqueId uniqueId, final ResultSetExtractor<List<D>> extractor, final String masterName) {
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
    
    final VersionCorrection vc = (versionCorrection.containsLatest() ? versionCorrection.withLatestFixed(now()) : versionCorrection);
    final DbMapSqlParameterSource args = argsGetByOidInstants(objectId, vc);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final String sql = getExtSqlBundle().getSql("GetByOidInstants", args);
    final List<D> docs = namedJdbc.query(sql, args, extractor);
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
   * Performs a standard get by versioned unique identifier.
   * 
   * @param uniqueId  the versioned unique identifier, not null
   * @param extractor  the extractor to use, not null
   * @param masterName  a name describing the contents of the master for an error message, not null
   * @return the document, null if not found
   */
  protected D doGetById(final UniqueId uniqueId, final ResultSetExtractor<List<D>> extractor, final String masterName) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(extractor, "extractor");
    s_logger.debug("getById {}", uniqueId);
    
    final DbMapSqlParameterSource args = argsGetById(uniqueId);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final String sql = getExtSqlBundle().getSql("GetById", args);
    final List<D> docs = namedJdbc.query(sql, args, extractor);
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
  protected DbMapSqlParameterSource argsGetById(final UniqueId uniqueId) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_oid", extractOid(uniqueId))
      .addValue("doc_id", extractRowId(uniqueId));
    return args;
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
    final String[] sql = {getExtSqlBundle().getSql("History", args), getExtSqlBundle().getSql("HistoryCount", args)};
    searchWithPaging(request.getPagingRequest(), sql, args, extractor, result);
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
    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
      args.addValue("sql_history_versions", "Point");
    } else {
      args.addValue("sql_history_versions", "Range");
    }
    if (request.getCorrectionsFromInstant() != null && request.getCorrectionsFromInstant().equals(request.getCorrectionsToInstant())) {
      args.addValue("sql_history_corrections", "Point");
    } else {
      args.addValue("sql_history_corrections", "Range");
    }
    args.addValue("paging_offset", request.getPagingRequest().getFirstItem());
    args.addValue("paging_fetch", request.getPagingRequest().getPagingSize());
    return args;
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
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate().getNamedParameterJdbcOperations();
    if (pagingRequest.equals(PagingRequest.ALL)) {
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      result.setPaging(Paging.of(pagingRequest, result.getDocuments()));
    } else {
      s_logger.debug("executing sql {}", sql[1]);
      final int count = namedJdbc.queryForInt(sql[1], args);
      result.setPaging(Paging.of(pagingRequest, count));
      if (count > 0 && pagingRequest.equals(PagingRequest.NONE) == false) {
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
    return getJdbcTemplate().queryForLong(getDialect().sqlNextSequenceValueSelect(sequenceName));
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
            return doAddInTransaction(document);
          }
        });
        changeManager().entityChanged(ChangeType.ADDED, null, result.getUniqueId(), result.getVersionFromInstant());
        return result;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Processes the document add, within a retrying transaction.
   * 
   * @param document  the document to add, not null
   * @return the added document, not null
   */
  protected D doAddInTransaction(final D document) {
    // insert new row
    final Instant now = now();
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    document.setUniqueId(null);
    insert(document);
    return document;
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
        final UniqueId beforeId = document.getUniqueId();
        ArgumentChecker.isTrue(beforeId.isVersioned(), "UniqueId must be versioned");
        D result = getTransactionTemplate().execute(new TransactionCallback<D>() {
          @Override
          public D doInTransaction(final TransactionStatus status) {
            return doUpdateInTransaction(document);
          }
        });
        changeManager().entityChanged(ChangeType.UPDATED, beforeId, result.getUniqueId(), result.getVersionFromInstant());
        return result;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Processes the document update, within a retrying transaction.
   * 
   * @param document  the document to update, not null
   * @return the updated document, not null
   */
  protected D doUpdateInTransaction(final D document) {
    // load old row
    final D oldDoc = getCheckLatestVersion(document.getUniqueId());
    // update old row
    final Instant now = now();
    oldDoc.setVersionToInstant(now);
    updateVersionToInstant(oldDoc);
    // insert new row
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    document.setUniqueId(oldDoc.getUniqueId().toLatest());
    mergeNonUpdatedFields(document, oldDoc);
    insert(document);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    checkScheme(uniqueId);
    s_logger.debug("remove {}", uniqueId);
    
    // retry to handle concurrent conflicting inserts into unique content tables
    for (int retry = 0; true; retry++) {
      try {
        D result = getTransactionTemplate().execute(new TransactionCallback<D>() {
          @Override
          public D doInTransaction(final TransactionStatus status) {
            return doRemoveInTransaction(uniqueId);
          }
        });
        changeManager().entityChanged(ChangeType.REMOVED, result.getUniqueId(), null, result.getVersionToInstant());
        return;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Processes the document update, within a retrying transaction.
   * 
   * @param uniqueId  the unique identifier to remove, not null
   * @return the updated document, not null
   */
  protected D doRemoveInTransaction(final UniqueId uniqueId) {
    // load old row
    final D oldDoc = getCheckLatestVersion(uniqueId);
    // update old row
    final Instant now = now();
    oldDoc.setVersionToInstant(now);
    updateVersionToInstant(oldDoc);
    return oldDoc;
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
        final UniqueId beforeId = document.getUniqueId();
        ArgumentChecker.isTrue(beforeId.isVersioned(), "UniqueId must be versioned");
        D result = getTransactionTemplate().execute(new TransactionCallback<D>() {
          @Override
          public D doInTransaction(final TransactionStatus status) {
            return doCorrectInTransaction(document);
          }
        });
        changeManager().entityChanged(ChangeType.CORRECTED, beforeId, result.getUniqueId(), result.getVersionFromInstant());
        return result;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Processes the document correction, within a retrying transaction.
   * 
   * @param document  the document to correct, not null
   * @return the corrected document, not null
   */
  protected D doCorrectInTransaction(final D document) {
    // load old row
    final D oldDoc = getCheckLatestCorrection(document.getUniqueId());
    // update old row
    final Instant now = now();
    oldDoc.setCorrectionToInstant(now);
    updateCorrectionToInstant(oldDoc);
    // insert new row
    document.setVersionFromInstant(oldDoc.getVersionFromInstant());
    document.setVersionToInstant(oldDoc.getVersionToInstant());
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    document.setUniqueId(oldDoc.getUniqueId().toLatest());
    mergeNonUpdatedFields(document, oldDoc);
    insert(document);
    return document;
  }

  //-------------------------------------------------------------------------
  /**
   * Merges any fields from the old document that have not been updated.
   * <p>
   * Masters can choose to accept a null value for a field to mean
   * 
   * @param newDocument  the new document to merge into, not null
   * @param oldDocument  the old document to merge from, not null
   */
  protected void mergeNonUpdatedFields(D newDocument, D oldDocument) {
    // do nothing (override in subclass)
    // the following code would merge all null fields, but not sure if that makes sense
//    for (MetaProperty<Object> prop : newDocument.metaBean().metaPropertyIterable()) {
//      if (prop.get(newDocument) == null) {
//        prop.set(newDocument, prop.get(oldDocument));
//      }
//    }
  }

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
  protected D getCheckLatestVersion(final UniqueId uniqueId) {
    final D oldDoc = get(uniqueId);  // checks uniqueId exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueId is not latest version: " + uniqueId);
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
    final String sql = getExtSqlBundle().getSql("UpdateVersionToInstant", args);
    int rowsUpdated = getJdbcTemplate().update(sql, args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end version instant failed, rows updated: " + rowsUpdated);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the document ensuring that it is the latest version.
   * 
   * @param uniqueId  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected D getCheckLatestCorrection(final UniqueId uniqueId) {
    final D oldDoc = get(uniqueId);  // checks uniqueId exists
    if (oldDoc.getCorrectionToInstant() != null) {
      throw new IllegalArgumentException("UniqueId is not latest correction: " + uniqueId);
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
    final String sql = getExtSqlBundle().getSql("UpdateCorrectionToInstant", args);
    int rowsUpdated = getJdbcTemplate().update(sql, args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end correction instant failed, rows updated: " + rowsUpdated);
    }
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.threeten.bp.Instant;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.AbstractMaster;
import com.opengamma.master.MasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.metric.MetricProducer;
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
public abstract class AbstractDocumentDbMaster<D extends AbstractDocument>
    extends AbstractDbMaster
    implements AbstractMaster<D>, MetricProducer, ConfigurableDbChangeProvidingMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDocumentDbMaster.class);

  /**
   * The change manager.
   */
  private ChangeManager _changeManager = new BasicChangeManager();
  // -----------------------------------------------------------------
  // TIMERS FOR METRICS GATHERING
  // By default these do nothing. Registration will replace them
  // so that they actually do something.
  // -----------------------------------------------------------------
  private Timer _getByOidInstantsTimer = new Timer();
  private Timer _getByIdTimer = new Timer();
  private Timer _historyTimer = new Timer();
  private Timer _searchTimer = new Timer();
  private Timer _addTimer = new Timer();
  private Timer _updateTimer = new Timer();
  private Timer _removeTimer = new Timer();
  private Timer _correctTimer = new Timer();
  private Timer _replaceVersionTimer = new Timer();
  private Timer _replaceVersionsTimer = new Timer();
  private Timer _replaceAllVersionsTimer = new Timer();

  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   * @param defaultScheme  the default scheme for unique identifier, not null
   */
  public AbstractDocumentDbMaster(final DbConnector dbConnector, final String defaultScheme) {
    super(dbConnector, defaultScheme);
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    _getByOidInstantsTimer = summaryRegistry.timer(namePrefix + ".getByOidInstants");
    _getByIdTimer = summaryRegistry.timer(namePrefix + ".getById");
    _historyTimer = summaryRegistry.timer(namePrefix + ".history");
    _searchTimer = summaryRegistry.timer(namePrefix + ".search");
    _addTimer = summaryRegistry.timer(namePrefix + ".add");
    _updateTimer = summaryRegistry.timer(namePrefix + ".update");
    _updateTimer = summaryRegistry.timer(namePrefix + ".remove");
    _correctTimer = summaryRegistry.timer(namePrefix + ".correct");
    _replaceVersionTimer = summaryRegistry.timer(namePrefix + ".replaceVersion");
    _replaceVersionsTimer = summaryRegistry.timer(namePrefix + ".replaceVersions");
    _replaceAllVersionsTimer = summaryRegistry.timer(namePrefix + ".replaceAllVersions");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the change manager.
   *
   * @return the change manager, not null
   */
  @Override
  public ChangeManager getChangeManager() {
    return _changeManager;
  }

  /**
   * Sets the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  @Override
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

    Timer.Context context = _getByOidInstantsTimer.time();
    try {
      final VersionCorrection vc = (versionCorrection.containsLatest() ? versionCorrection.withLatestFixed(now()) : versionCorrection);
      final DbMapSqlParameterSource args = argsGetByOidInstants(objectId, vc);
      final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
      final String sql = getElSqlBundle().getSql("GetByOidInstants", args);
      final List<D> docs = namedJdbc.query(sql, args, extractor);
      if (docs.isEmpty()) {
        throw new DataNotFoundException(masterName + " not found: " + objectId);
      }
      return docs.get(0);
    } finally {
      context.stop();
    }
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
    final DbMapSqlParameterSource args = createParameterSource()
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

    Timer.Context context = _getByIdTimer.time();
    try {
      final DbMapSqlParameterSource args = argsGetById(uniqueId);
      final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
      final String sql = getElSqlBundle().getSql("GetById", args);
      final List<D> docs = namedJdbc.query(sql, args, extractor);
      if (docs.isEmpty()) {
        throw new DataNotFoundException(masterName + " not found: " + uniqueId);
      }
      return docs.get(0);
    } finally {
      context.stop();
    }
  }

  /**
   * Gets the SQL arguments to use for a standard get by versioned unique identifier.
   *
   * @param uniqueId  the versioned unique identifier, not null
   * @return the SQL arguments, not null
   */
  protected DbMapSqlParameterSource argsGetById(final UniqueId uniqueId) {
    final DbMapSqlParameterSource args = createParameterSource()
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
    
    Timer.Context context = _historyTimer.time();
    try {
      final DbMapSqlParameterSource args = argsHistory(request);
      final String[] sql = {getElSqlBundle().getSql("History", args), getElSqlBundle().getSql("HistoryCount", args)};
      searchWithPaging(request.getPagingRequest(), sql, args, extractor, result);
      return result;
    } finally {
      context.stop();
    }
  }

  /**
   * Gets the SQL arguments to use for searching the history of a document.
   *
   * @param request  the request, not null
   * @return the SQL arguments, not null
   */
  protected DbMapSqlParameterSource argsHistory(final AbstractHistoryRequest request) {
    final DbMapSqlParameterSource args = createParameterSource()
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
   * @param <T>  the type of the document
   * @param pagingRequest  the paging request, not null
   * @param sql  the array of SQL, query and count, not null
   * @param args  the query arguments, not null
   * @param extractor  the extractor of results, not null
   * @param result  the object to populate, not null
   */
  protected <T extends AbstractDocument> void doSearch(
      final PagingRequest pagingRequest, final String[] sql, final DbMapSqlParameterSource args,
      final ResultSetExtractor<List<T>> extractor, final AbstractDocumentsResult<T> result) {
    
    Timer.Context context = _searchTimer.time();
    try {
      searchWithPaging(pagingRequest, sql, args, extractor, result);
    } finally {
      context.stop();
    }
  }

  /**
   * Searches for documents with paging.
   *
   * @param <T>  the type of the document
   * @param pagingRequest  the paging request, not null
   * @param sql  the array of SQL, query and count, not null
   * @param args  the query arguments, not null
   * @param extractor  the extractor of results, not null
   * @param result  the object to populate, not null
   */
  protected <T extends AbstractDocument> void searchWithPaging(
      final PagingRequest pagingRequest, final String[] sql, final DbMapSqlParameterSource args,
      final ResultSetExtractor<List<T>> extractor, final AbstractDocumentsResult<T> result) {
    s_logger.debug("with args {}", args);
    
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate();
    if (pagingRequest.equals(PagingRequest.ALL)) {
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      result.setPaging(Paging.of(pagingRequest, result.getDocuments()));
    } else {
      s_logger.debug("executing sql {}", sql[1]);
      final int count = namedJdbc.queryForObject(sql[1], args, Integer.class);
      result.setPaging(Paging.of(pagingRequest, count));
      if (count > 0 && pagingRequest.equals(PagingRequest.NONE) == false) {
        s_logger.debug("executing sql {}", sql[0]);
        result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public D add(final D document) {
    ArgumentChecker.notNull(document, "document");
    s_logger.debug("add {}", document);
    
    Timer.Context context = _addTimer.time();
    try {
      final D added = getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<D>() {
        @Override
        public D doInTransaction(final TransactionStatus status) {
          return doAddInTransaction(document);
        }
      });
      changeManager().entityChanged(ChangeType.ADDED, added.getObjectId(), added.getVersionFromInstant(), added.getVersionToInstant(), now());
      return added;
    } finally {
      context.stop();
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
    
    Timer.Context context = _updateTimer.time();
    try {
      final UniqueId beforeId = document.getUniqueId();
      ArgumentChecker.isTrue(beforeId.isVersioned(), "UniqueId must be versioned");
      final D updated = getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<D>() {
        @Override
        public D doInTransaction(final TransactionStatus status) {
          return doUpdateInTransaction(beforeId, document);
        }
      });
      changeManager().entityChanged(ChangeType.CHANGED, updated.getObjectId(), updated.getVersionFromInstant(), updated.getVersionToInstant(), now());
      return updated;
    } finally {
      context.stop();
    }
  }

  /**
   * Processes the document update, within a retrying transaction.
   *
   * @param beforeId the original identifier of the document, not null
   * @param document the document to update, not null
   * @return the updated document, not null
   */
  protected D doUpdateInTransaction(final UniqueId beforeId, final D document) {
    // load old row
    final D oldDoc = getCheckLatestVersion(beforeId);
    // update old row
    final Instant now = now();
    oldDoc.setVersionToInstant(now);
    oldDoc.setCorrectionToInstant(now);
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
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    checkScheme(objectIdentifiable);
    s_logger.debug("remove {}", objectIdentifiable);
    
    Timer.Context context = _removeTimer.time();
    try {
      final D removed = getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<D>() {
        @Override
        public D doInTransaction(final TransactionStatus status) {
          return doRemoveInTransaction(objectIdentifiable);
        }
      });
      changeManager().entityChanged(ChangeType.REMOVED, removed.getObjectId(), removed.getVersionToInstant(), null, removed.getVersionToInstant());
    } finally {
      context.stop();
    }
  }

  /**
   * Processes the document update, within a retrying transaction.
   *
   * @param objectIdentifiable the objectIdentifiable to remove, not null
   * @return the updated document, not null
   */
  protected D doRemoveInTransaction(final ObjectIdentifiable objectIdentifiable) {
    // load old row
    final D oldDoc = get(objectIdentifiable.getObjectId(), VersionCorrection.LATEST);

    if (oldDoc == null) {
      throw new DataNotFoundException("There is no document with oid:" + objectIdentifiable.getObjectId());
    }
    // update old row
    final Instant now = now();
    oldDoc.setVersionToInstant(now);
    updateVersionToInstant(oldDoc);
    return oldDoc;
  }

  //-------------------------------------------------------------------------
  @Override
  public D correct(final D document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    checkScheme(document.getUniqueId());
    s_logger.debug("correct {}", document);
    
    Timer.Context context = _correctTimer.time();
    try {
      final UniqueId beforeId = document.getUniqueId();
      ArgumentChecker.isTrue(beforeId.isVersioned(), "UniqueId must be versioned");
      final D corrected = getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<D>() {
        @Override
        public D doInTransaction(final TransactionStatus status) {
          return doCorrectInTransaction(beforeId, document);
        }
      });
      changeManager().entityChanged(ChangeType.CHANGED, corrected.getObjectId(), corrected.getVersionFromInstant(), corrected.getVersionToInstant(), now());
      return corrected;
    } finally {
      context.stop();
    }
  }

  /**
   * Processes the document correction, within a retrying transaction.
   *
   * @param beforeId  the ID before
   * @param document  the document to correct, not null
   * @return the corrected document, not null
   */
  protected D doCorrectInTransaction(final UniqueId beforeId, final D document) {
    // load old row
    final D oldDoc = getCheckLatestCorrection(beforeId);
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

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<D> replacementDocuments) {
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    for (final D replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
    }
    final Instant now = now();
    ArgumentChecker.isTrue(MasterUtils.checkUniqueVersionsFrom(replacementDocuments), "No two versioned documents may have the same \"version from\" instant");
    
    Timer.Context context = _replaceVersionTimer.time();
    try {
      return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<List<UniqueId>>() {
        @Override
        public List<UniqueId> doInTransaction(final TransactionStatus status) {
          final D storedDocument = get(uniqueId);
          if (storedDocument == null) {
            throw new DataNotFoundException("Document not found: " + uniqueId.getObjectId());
          }
          ArgumentChecker.isTrue(storedDocument.getCorrectionToInstant() == null, "we can replace only current document. The " + storedDocument.getUniqueId() + " is not current.");

          final Instant storedVersionFrom = storedDocument.getVersionFromInstant();
          final Instant storedVersionTo = storedDocument.getVersionToInstant();

          ArgumentChecker.isTrue(
            MasterUtils.checkVersionInstantsWithinRange(storedVersionFrom, storedVersionFrom, storedVersionTo, replacementDocuments, true),
            "The versions must exactly match the version range of the original version being replaced.");

          // we terminate the stored docuemnt (correction)
          storedDocument.setCorrectionToInstant(now);
          updateCorrectionToInstant(storedDocument);

          final List<D> orderedReplacementDocuments = MasterUtils.adjustVersionInstants(now, storedVersionFrom, storedVersionTo, replacementDocuments);
          final List<D> newVersions = newArrayList();
          if (orderedReplacementDocuments.isEmpty()) {
            // since we don't have replacement documents we rather act as versionRemove than versionReplace
            final D previousDocument = getPreviousDocument(uniqueId.getObjectId(), now, storedVersionFrom);
            if (previousDocument != null) {
              // we terminate the previous docuemnt (correction)
              previousDocument.setCorrectionToInstant(now);
              updateCorrectionToInstant(previousDocument);
              // and create new copy of it extending versionTo instant to storedDocument's versionFrom instant
              previousDocument.setCorrectionFromInstant(now);
              previousDocument.setCorrectionToInstant(null);
              previousDocument.setVersionToInstant(storedVersionFrom);
              previousDocument.setUniqueId(uniqueId.getUniqueId().toLatest());
              insert(previousDocument);
              newVersions.add(previousDocument);
              changeManager().entityChanged(ChangeType.CHANGED, storedDocument.getObjectId(), storedVersionFrom, storedVersionTo, now);
            } else {
              changeManager().entityChanged(ChangeType.REMOVED, storedDocument.getObjectId(), null, null, now);
            }
          } else {
            for (final D replacementDocument : orderedReplacementDocuments) {
              replacementDocument.setUniqueId(uniqueId.getUniqueId().toLatest());
              insert(replacementDocument);
              newVersions.add(replacementDocument);
            }
            changeManager().entityChanged(ChangeType.CHANGED, storedDocument.getObjectId(), storedVersionFrom, storedVersionTo, now);
          }
          return MasterUtils.mapToUniqueIDs(newVersions);
        }
      });
    } finally {
      context.stop();
    }
  }

  private D getPreviousDocument(final ObjectId oid, final Instant now, final Instant thisVersionFrom) {
    return historyByVersionsCorrections(new AbstractHistoryRequest() {
      @Override
      public Instant getCorrectionsFromInstant() {
        return now;
      }

      @Override
      public Instant getCorrectionsToInstant() {
        return now;
      }

      @Override
      public ObjectId getObjectId() {
        return oid;
      }

      @Override
      public PagingRequest getPagingRequest() {
        return PagingRequest.ONE;
      }

      @Override
      public Instant getVersionsFromInstant() {
        return thisVersionFrom.minusMillis(1);
      }

      @Override
      public Instant getVersionsToInstant() {
        return thisVersionFrom.minusMillis(1);
      }
    }).getFirstDocument();
  }

  private List<D> getAllCurrentDocuments(final ObjectId oid, final Instant now) {
    return historyByVersionsCorrections(new AbstractHistoryRequest() {
      @Override
      public Instant getCorrectionsFromInstant() {
        return now;
      }

      @Override
      public Instant getCorrectionsToInstant() {
        return now;
      }

      @Override
      public ObjectId getObjectId() {
        return oid;
      }

      @Override
      public PagingRequest getPagingRequest() {
        return PagingRequest.ALL;
      }

      @Override
      public Instant getVersionsFromInstant() {
        return null;
      }

      @Override
      public Instant getVersionsToInstant() {
        return null;
      }
    }).getDocuments();
  }

  private List<D> getCurrentDocumentsInRange(final ObjectId oid, final Instant now, final Instant from, final Instant to) {
    return historyByVersionsCorrections(new AbstractHistoryRequest() {
      @Override
      public Instant getCorrectionsFromInstant() {
        return now;
      }

      @Override
      public Instant getCorrectionsToInstant() {
        return now;
      }

      @Override
      public ObjectId getObjectId() {
        return oid;
      }

      @Override
      public PagingRequest getPagingRequest() {
        return PagingRequest.ALL;
      }

      @Override
      public Instant getVersionsFromInstant() {
        return from;
      }

      @Override
      public Instant getVersionsToInstant() {
        return to;
      }
    }).getDocuments();
  }

//  private D getNextDocument(final ObjectId oid, final Instant now, final Instant thisVersionTo) {
//    return historyByVersionsCorrections(new AbstractHistoryRequest() {
//      @Override
//      public Instant getCorrectionsFromInstant() {
//        return now;
//      }
//
//      @Override
//      public Instant getCorrectionsToInstant() {
//        return now;
//      }
//
//      @Override
//      public ObjectId getObjectId() {
//        return oid;
//      }
//
//      @Override
//      public PagingRequest getPagingRequest() {
//        return PagingRequest.ONE;
//      }
//
//      @Override
//      public Instant getVersionsFromInstant() {
//        return thisVersionTo;
//      }
//
//      @Override
//      public Instant getVersionsToInstant() {
//        return thisVersionTo;
//      }
//    }).getFirstDocument();
//  }


  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    final Instant now = now();

    for (final D replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument.getVersionFromInstant(), "Each replacement document must have version from defined.");
    }
    
    Timer.Context context = _replaceAllVersionsTimer.time();
    try {
      return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<List<UniqueId>>() {
        @Override
        public List<UniqueId> doInTransaction(final TransactionStatus status) {

          boolean terminatedAny = false;

          final List<D> storedDocuments = getAllCurrentDocuments(objectId.getObjectId(), now);

          for (final D storedDocument : storedDocuments) {
            ArgumentChecker.isTrue(storedDocument.getCorrectionToInstant() == null, "we can replace only current documents. The " + storedDocument.getUniqueId() + " is not current.");
          }
          // terminating all current documents
          for (final D storedDocument : storedDocuments) {
            storedDocument.setCorrectionToInstant(now);
            updateCorrectionToInstant(storedDocument);
            terminatedAny = true;
          }

          if (terminatedAny && replacementDocuments.isEmpty()) {
            changeManager().entityChanged(ChangeType.REMOVED, objectId.getObjectId(), null, null, now);
            return Collections.emptyList();
          } else {
            final List<D> orderedReplacementDocuments = MasterUtils.adjustVersionInstants(now, null, null, replacementDocuments);
            for (final D replacementDocument : orderedReplacementDocuments) {
              replacementDocument.setUniqueId(objectId.getObjectId().atLatestVersion());
              insert(replacementDocument);
            }
            final Instant versionFromInstant = functional(orderedReplacementDocuments).first().getVersionFromInstant();
            final Instant versionToInstant = functional(orderedReplacementDocuments).last().getVersionToInstant();
            changeManager().entityChanged(ChangeType.CHANGED, objectId.getObjectId(), versionFromInstant, versionToInstant, now);
            return MasterUtils.mapToUniqueIDs(orderedReplacementDocuments);
          }
        }
      });
    } finally {
      context.stop();
    }
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    final Instant now = now();

    if (!replacementDocuments.isEmpty()) {
      for (final D replacementDocument : replacementDocuments) {
        ArgumentChecker.notNull(replacementDocument.getVersionFromInstant(), "Each replacement document must have version from defined.");
      }

      final List<D> orderedReplacementDocuments = MasterUtils.adjustVersionInstants(now, null, null, replacementDocuments);
      final Instant lowestVersionFrom = orderedReplacementDocuments.get(0).getVersionFromInstant();
      final Instant highestVersionTo = orderedReplacementDocuments.get(orderedReplacementDocuments.size() - 1).getVersionToInstant();
      
      Timer.Context context = _replaceVersionsTimer.time();
      try {
        return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<List<UniqueId>>() {
          @Override
          public List<UniqueId> doInTransaction(final TransactionStatus status) {
            boolean terminatedAny = false;
            final List<D> storedDocuments = getCurrentDocumentsInRange(objectId.getObjectId(), now, lowestVersionFrom, highestVersionTo);

            if (!storedDocuments.isEmpty()) {
              for (final D storedDocument : storedDocuments) {
                ArgumentChecker.isTrue(storedDocument.getCorrectionToInstant() == null, "we can replace only current documents. The " + storedDocument.getUniqueId() + " is not current.");
              }

              final D earliestStoredDocument = storedDocuments.get(storedDocuments.size() - 1);
              final D latestStoredDocument = storedDocuments.get(0);


              // terminating all current documents
              for (final D storedDocument : storedDocuments) {
                storedDocument.setCorrectionToInstant(now);
                updateCorrectionToInstant(storedDocument);
                terminatedAny = true;
              }

              if (earliestStoredDocument != null && earliestStoredDocument.getVersionFromInstant().isBefore(lowestVersionFrom)) {
                // we need to make copy of the earliestStoredDocument
                earliestStoredDocument.setVersionToInstant(lowestVersionFrom);
                earliestStoredDocument.setCorrectionFromInstant(now);
                earliestStoredDocument.setCorrectionToInstant(null);
                earliestStoredDocument.setUniqueId(objectId.getObjectId().atLatestVersion());
                insert(earliestStoredDocument);
              }
              if (latestStoredDocument != null && latestStoredDocument.getVersionToInstant() != null &&
                  highestVersionTo != null && latestStoredDocument.getVersionToInstant().isAfter(highestVersionTo)) {
                // we need to make copy of the latestStoredDocument
                latestStoredDocument.setVersionFromInstant(highestVersionTo);
                latestStoredDocument.setCorrectionFromInstant(now);
                latestStoredDocument.setCorrectionToInstant(null);
                latestStoredDocument.setUniqueId(objectId.getObjectId().atLatestVersion());
                insert(latestStoredDocument);
              }
            }
            if (terminatedAny && replacementDocuments.isEmpty()) {
              changeManager().entityChanged(ChangeType.REMOVED, objectId.getObjectId(), null, null, now);
              return Collections.emptyList();
            } else {
              for (final D replacementDocument : orderedReplacementDocuments) {
                replacementDocument.setUniqueId(objectId.getObjectId().atLatestVersion());
                insert(replacementDocument);
              }
              final Instant versionFromInstant = functional(orderedReplacementDocuments).first().getVersionFromInstant();
              final Instant versionToInstant = functional(orderedReplacementDocuments).last().getVersionToInstant();
              changeManager().entityChanged(ChangeType.CHANGED, objectId.getObjectId(), versionFromInstant, versionToInstant, now);
              return MasterUtils.mapToUniqueIDs(orderedReplacementDocuments);
            }

          }
        });
        
      } finally {
        context.stop();
      }
    }
    // nothing to replace with
    return Collections.emptyList();
  }

  @Override
  public final void removeVersion(final UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<D>emptyList());
  }

  @Override
  public final UniqueId replaceVersion(final D replacementDocument) {
    ArgumentChecker.notNull(replacementDocument, "replacementDocument");
    final List<UniqueId> result = replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public final UniqueId addVersion(final ObjectIdentifiable objectId, final D documentToAdd) {
    final List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
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
  protected void mergeNonUpdatedFields(final D newDocument, final D oldDocument) {
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
    final DbMapSqlParameterSource args = createParameterSource()
      .addValue("doc_id", extractRowId(document.getUniqueId()))
      .addTimestamp("ver_to_instant", document.getVersionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    final String sql = getElSqlBundle().getSql("UpdateVersionToInstant", args);
    final int rowsUpdated = getJdbcTemplate().update(sql, args);
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
  protected void updateCorrectionToInstant(final AbstractDocument document) {
    final DbMapSqlParameterSource args = createParameterSource()
      .addValue("doc_id", extractRowId(document.getUniqueId()))
      .addTimestamp("corr_to_instant", document.getCorrectionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    final String sql = getElSqlBundle().getSql("UpdateCorrectionToInstant", args);
    final int rowsUpdated = getJdbcTemplate().update(sql, args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end correction instant failed, rows updated: " + rowsUpdated);
    }
  }

  @Override
  public abstract D get(ObjectIdentifiable objectId, VersionCorrection versionCorrection);

  /**
   * Queries the history of an object.
   * <p>
   * The request must contain an object identifier to identify the object.
   *
   * @param request  the history request, not null
   * @return the object history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  protected abstract AbstractHistoryResult<D> historyByVersionsCorrections(AbstractHistoryRequest request);

  @Override
  public Map<UniqueId, D> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, D> map = newHashMap();
    for (final UniqueId uniqueId : uniqueIds) {
      map.put(uniqueId, get(uniqueId));
    }
    return map;
  }

}

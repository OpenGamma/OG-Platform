/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.util.db.DbConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Set;

/**
 * A batch master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the batch master using an SQL database.
 * This implementation uses Hibernate to write all static data, including LiveData snapshots.
 * Risk itself is written using direct JDBC.
 * <p>
 * Full details of the API are in {@link com.opengamma.masterdb.batch.document.BatchMaster}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbBatchWriter extends InTransactionDbBatchWriter implements BatchRunWriter {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchWriter.class);


  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   */
  public DbBatchWriter(final DbConnector dbConnector) {
    super(dbConnector);
  }


  @Override
  public void addValuesToSnapshot(final UniqueId marketDataSnapshotUniqueId, final Set<LiveDataValue> values) {
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        addValuesToSnapshotInTransaction(marketDataSnapshotUniqueId, values);
        return null;
      }
    });
  }

  @Override
  public void startBatch(final Batch batch, final RunCreationMode runCreationMode, final SnapshotMode snapshotMode) {
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        startBatchInTransaction(batch, runCreationMode, snapshotMode);
        return null;
      }
    });
  }

  @Override
  public void deleteBatch(final UniqueId batchUniqueId) {
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        deleteBatchInTransaction(batchUniqueId);
        return null;
      }
    });
  }

  @Override
  public void endBatch(final UniqueId batchUniqueId) {
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        endBatchInTransaction(batchUniqueId);
        return null;
      }
    });
  }

  @Override
  public LiveDataSnapshot createLiveDataSnapshot(final UniqueId marketDataSnapshotUniqueId) {
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<LiveDataSnapshot>() {
      @Override
      public LiveDataSnapshot doInTransaction(final TransactionStatus status) {
        return createOrGetLiveDataSnapshotInTransaction(marketDataSnapshotUniqueId);
      }
    });
  }

  @Override
  public Set<LiveDataValue> getSnapshotValues(final UniqueId snapshotUniqueId) {
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Set<LiveDataValue>>() {
      @Override
      public Set<LiveDataValue> doInTransaction(final TransactionStatus status) {
        return getSnapshotValuesInTransaction(snapshotUniqueId);
      }
    });
  }

  @Override
  public VersionCorrection createVersionCorrection(final com.opengamma.id.VersionCorrection versionCorrection) {
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<VersionCorrection>() {
      @Override
      public VersionCorrection doInTransaction(final TransactionStatus status) {
        return createVersionCorrectionInTransaction(versionCorrection);
      }
    });
  }

  // -------------------------------------------------------------------------------------------------------------------

  @Override
  public void addJobResults(final UniqueId batchUniqueId, final ViewResultModel result) {
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        addJobResultsInTransaction(batchUniqueId, result);
        return null;
      }
    });
  }
}

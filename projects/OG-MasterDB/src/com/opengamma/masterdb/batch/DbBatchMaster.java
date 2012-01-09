/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.master.batch.*;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.opengamma.util.db.DbUtil.eqOrIsNull;

public class DbBatchMaster extends AbstractDbMaster implements BatchMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchMaster.class);

  
  final private DbBatchWriter _dbBatchWriter; 
  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   */
  public DbBatchMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    _dbBatchWriter = new DbBatchWriter(dbConnector);
  }  


  @Override
  public BatchDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    s_logger.info("Getting BatchDocument by unique id: ", uniqueId);
    final Long id = extractOid(uniqueId);
    return getHibernateTransactionTemplate().execute(new HibernateCallback<BatchDocument>() {
      @Override
      public BatchDocument doInHibernate(Session session) throws HibernateException, SQLException {
        RiskRun run = _dbBatchWriter.getRiskRunById(id);
        if (run != null) {
          return new BatchDocument(
            UniqueId.of(BatchMaster.IDENTIFIER_SCHEME_DEFAULT, Long.toString(run.getId())),
            UniqueId.parse(run.getViewDefinition().getViewDefinitionUid()),
            UniqueId.parse(run.getLiveDataSnapshot().getMarketDataSnapshotUid()),
            run.getValuationTime(),
            com.opengamma.id.VersionCorrection.of(run.getVersionCorrection().getAsOf(), run.getVersionCorrection().getCorrectedTo()),
            (run.isComplete() ? BatchStatus.COMPLETE : BatchStatus.RUNNING),
            run.getCreateInstant(),
            run.getStartInstant(),
            run.getEndInstant(),
            run.getNumRestarts()
          );
        } else {
          throw new DataNotFoundException("Batch run not found: " + id);
        }
      }
    });
  }

  //--------------------------------------------------------------------------------------------------------------------


  @Override
  @SuppressWarnings("unchecked")
  public BatchSearchResult search(final BatchSearchRequest request) {
    s_logger.info("Searching BatchDocuments: ", request);

    final DetachedCriteria criteria = DetachedCriteria.forClass(RiskRun.class);


    if (request.getValuationTime() != null) {
      criteria.add(
        Restrictions.eq("valuationTime", request.getValuationTime()));
    }

    if (request.getVersionCorrection() != null) {
      criteria.createCriteria("versionCorrection")
        .add(eqOrIsNull("asOf", request.getVersionCorrection().getVersionAsOf()))
        .add(eqOrIsNull("correctedTo", request.getVersionCorrection().getCorrectedTo()));
    }


    if (request.getMarketDataSnapshotUid() != null) {
      criteria.createCriteria("liveDataSnapshot")
        .add(Restrictions.eq("marketDataSnapshotUid", request.getMarketDataSnapshotUid().toString()))
        .addOrder(Order.asc("marketDataSnapshotUid"));
    }

    if (request.getViewDefinitionUid() != null) {
      criteria.createCriteria("viewDefinition")
        .add(Restrictions.eq("viewDefinitionUid", request.getViewDefinitionUid().toString()))
        .addOrder(Order.asc("viewDefinitionUid"));
    }

    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<BatchSearchResult>() {
      @Override
      public BatchSearchResult doInTransaction(final TransactionStatus status) {
        BatchSearchResult result = new BatchSearchResult();
        //
        if (request.getPagingRequest().equals(PagingRequest.ALL)) {
          result.setPaging(Paging.of(request.getPagingRequest(), result.getDocuments()));
        } else {
          criteria.setProjection(Projections.rowCount());
          Long totalCount = (Long) getHibernateTemplate().findByCriteria(criteria).get(0);
          result.setPaging(Paging.of(request.getPagingRequest(), totalCount.intValue()));
          criteria.setProjection(null);
          criteria.setResultTransformer(Criteria.ROOT_ENTITY);
        }
        criteria.addOrder(Order.asc("valuationTime"));
        List<RiskRun> runs = Collections.emptyList();
        if (!request.getPagingRequest().equals(PagingRequest.NONE)) {
          runs = getHibernateTemplate().findByCriteria(
            criteria,
            request.getPagingRequest().getFirstItem(),
            request.getPagingRequest().getPagingSize());
        }

        for (RiskRun run : runs) {
          BatchDocument doc = new BatchDocument(
            UniqueId.of(BatchMaster.IDENTIFIER_SCHEME_DEFAULT, Long.toString(run.getId())),
            UniqueId.parse(run.getViewDefinition().getViewDefinitionUid()),
            UniqueId.parse(run.getLiveDataSnapshot().getMarketDataSnapshotUid()),
            run.getValuationTime(),
            com.opengamma.id.VersionCorrection.of(run.getVersionCorrection().getAsOf(), run.getVersionCorrection().getCorrectedTo()),
            (run.isComplete() ? BatchStatus.COMPLETE : BatchStatus.RUNNING),
            run.getCreateInstant(),
            run.getStartInstant(),
            run.getEndInstant(),
            run.getNumRestarts()
          );
          result.getDocuments().add(doc);
        }

        return result;
      }
    });
  }


  @Override
  public int delete(final UniqueId uniqueId) {
    s_logger.info("Deleting Batch by unique id: ", uniqueId);
    final Long id = extractOid(uniqueId);

    return getHibernateTransactionTemplateRetrying(getMaxRetries()).execute(new HibernateCallback<Integer>() {
          @Override
          public Integer doInHibernate(Session session) throws HibernateException, SQLException {
            Query query = session.getNamedQuery("RiskRun.delete.byId");
            query.setLong("id", id);
            return query.executeUpdate();
          }
        });
      }

  //--------------------------------------------------------------------------------------------------------------------

  @Override
    public void addValuesToSnapshot(final UniqueId marketDataSnapshotUniqueId, final Set<LiveDataValue> values) {
      getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
        @Override
        public Void doInTransaction(final TransactionStatus status) {
          _dbBatchWriter.addValuesToSnapshotInTransaction(marketDataSnapshotUniqueId, values);
          return null;
        }
      });
    }
  
    @Override
    public void startBatch(final Batch batch, final RunCreationMode runCreationMode, final SnapshotMode snapshotMode) {
      getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
        @Override
        public Void doInTransaction(final TransactionStatus status) {
          _dbBatchWriter.startBatchInTransaction(batch, runCreationMode, snapshotMode);
          return null;
        }
      });
    }
  
    @Override
    public void deleteBatch(final UniqueId batchUniqueId) {
      getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
        @Override
        public Void doInTransaction(final TransactionStatus status) {
          _dbBatchWriter.deleteBatchInTransaction(batchUniqueId);
          return null;
        }
      });
    }
  
    @Override
    public void endBatch(final UniqueId batchUniqueId) {
      getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
        @Override
        public Void doInTransaction(final TransactionStatus status) {
          _dbBatchWriter.endBatchInTransaction(batchUniqueId);
          return null;
        }
      });
    }
  
    //@Override
    public LiveDataSnapshot createLiveDataSnapshot(final UniqueId marketDataSnapshotUniqueId) {
      return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<LiveDataSnapshot>() {
        @Override
        public LiveDataSnapshot doInTransaction(final TransactionStatus status) {
          return _dbBatchWriter.createOrGetLiveDataSnapshotInTransaction(marketDataSnapshotUniqueId);
        }
      });
    }
  
    @Override
    public Set<LiveDataValue> getSnapshotValues(final UniqueId snapshotUniqueId) {
      return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Set<LiveDataValue>>() {
        @Override
        public Set<LiveDataValue> doInTransaction(final TransactionStatus status) {
          return _dbBatchWriter.getSnapshotValuesInTransaction(snapshotUniqueId);
        }
      });
    }
  
    //@Override
    public VersionCorrection createVersionCorrection(final com.opengamma.id.VersionCorrection versionCorrection) {
      return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<VersionCorrection>() {
        @Override
        public VersionCorrection doInTransaction(final TransactionStatus status) {
          return _dbBatchWriter.createVersionCorrectionInTransaction(versionCorrection);
        }
      });
    }
  
    // -------------------------------------------------------------------------------------------------------------------
  
    @Override
    public void addJobResults(final UniqueId batchUniqueId, final ViewComputationResultModel result) {
      getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
        @Override
        public Void doInTransaction(final TransactionStatus status) {
          _dbBatchWriter.addJobResultsInTransaction(batchUniqueId, result);
          return null;
        }
      });
    }
}

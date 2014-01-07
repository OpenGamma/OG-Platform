/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static com.google.common.collect.Lists.newArrayList;
import static com.opengamma.util.db.HibernateDbUtils.eqOrIsNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.opengamma.DataNotFoundException;
import com.opengamma.batch.BatchMasterWriter;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.domain.RiskValueProperties;
import com.opengamma.batch.rest.BatchRunSearchRequest;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A batch master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the batch master using an SQL database.
 * Full details of the API are in {@link BatchMasterWriter}.
 * <p>
 * The SQL is stored externally in {@code DbBatchMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbBatchMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbBatchMaster extends AbstractDbMaster implements BatchMasterWriter {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchMaster.class);

  /**
   * The batch writer.
   */
  private final DbBatchWriter _dbBatchWriter;

  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   * @param computationTargetResolver  the resolver
   */
  public DbBatchMaster(final DbConnector dbConnector, final ComputationTargetResolver computationTargetResolver) {
    super(dbConnector, BATCH_IDENTIFIER_SCHEME);
    _dbBatchWriter = new DbBatchWriter(dbConnector, computationTargetResolver);
    setElSqlBundle(_dbBatchWriter.getElSqlBundle());
  }

  //-------------------------------------------------------------------------
  @Override
  public RiskRun getRiskRun(final ObjectId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    s_logger.info("Getting BatchDocument by unique id: ", uniqueId);
    final Long id = extractOid(uniqueId);
    return getHibernateTransactionTemplate().execute(new HibernateCallback<RiskRun>() {
      @Override
      public RiskRun doInHibernate(final Session session) throws HibernateException, SQLException {
        final RiskRun run = _dbBatchWriter.getRiskRunById(id);
        if (run != null) {
          return run;
        } else {
          throw new DataNotFoundException("Batch run not found: " + id);
        }
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public Pair<List<MarketData>, Paging> getMarketData(final PagingRequest pagingRequest) {
    s_logger.info("Getting markte datas: ", pagingRequest);

    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Pair<List<MarketData>, Paging>>() {
      @Override
      public Pair<List<MarketData>, Paging> doInTransaction(final TransactionStatus status) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(MarketData.class);

        List<MarketData> results = Collections.emptyList();
        if (!pagingRequest.equals(PagingRequest.NONE)) {
          results = getHibernateTemplate().findByCriteria(
            criteria,
            pagingRequest.getFirstItem(),
            pagingRequest.getPagingSize());
        }
        //
        Paging paging;
        if (pagingRequest.equals(PagingRequest.ALL)) {
          paging = Paging.of(pagingRequest, results);
        } else {
          criteria.setProjection(Projections.rowCount());
          final Long totalCount = (Long) getHibernateTemplate().findByCriteria(criteria).get(0);
          paging = Paging.of(pagingRequest, totalCount.intValue());
        }
        //     
        return Pairs.of(results, paging);
      }
    });
  }

  @Override
  public MarketData getMarketDataById(final ObjectId batchSnapshotId) {
    s_logger.info("Getting the batch data snapshot: {}", batchSnapshotId);

    final Long marketDataPK = extractOid(batchSnapshotId);

    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<MarketData>() {
      @Override
      public MarketData doInTransaction(final TransactionStatus status) {
        return getHibernateTemplate().get(MarketData.class, marketDataPK);
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public Pair<List<MarketDataValue>, Paging> getMarketDataValues(final ObjectId marketDataId, final PagingRequest pagingRequest) {
    s_logger.info("Getting the batch data snapshot: {}", marketDataId);

    final Long marketDataPK = extractOid(marketDataId);

    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Pair<List<MarketDataValue>, Paging>>() {
      @Override
      public Pair<List<MarketDataValue>, Paging> doInTransaction(final TransactionStatus status) {

        final DetachedCriteria criteria = DetachedCriteria.forClass(MarketDataValue.class);
        criteria.add(Restrictions.eq("marketDataId", marketDataPK));
        //
        List<MarketDataValue> results = Collections.emptyList();
        if (!pagingRequest.equals(PagingRequest.NONE)) {
          results = getHibernateTemplate().findByCriteria(
            criteria,
            pagingRequest.getFirstItem(),
            pagingRequest.getPagingSize());
        }
        //
        Paging paging;
        if (pagingRequest.equals(PagingRequest.ALL)) {
          paging = Paging.of(pagingRequest, results);
        } else {
          criteria.setProjection(Projections.rowCount());
          final Long totalCount = (Long) getHibernateTemplate().findByCriteria(criteria).get(0);
          paging = Paging.of(pagingRequest, totalCount.intValue());
        }
        //
        return Pairs.of(results, paging);
      }
    });
  }

  @Override
  public void deleteMarketData(final ObjectId batchSnapshotId) {
    s_logger.info("Deleting market data snapshot: ", batchSnapshotId);
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        _dbBatchWriter.deleteSnapshotInTransaction(batchSnapshotId);
        return null;
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  @SuppressWarnings("unchecked")
  public Pair<List<RiskRun>, Paging> searchRiskRun(final BatchRunSearchRequest request) {
    s_logger.info("Searching BatchDocuments: ", request);
    
    final DetachedCriteria criteria = DetachedCriteria.forClass(RiskRun.class);
    
    if (request.getValuationTime() != null) {
      criteria.add(
        Restrictions.eq("valuationTime", request.getValuationTime()));
    }
    if (request.getVersionCorrection() != null) {
      criteria.add(
        Restrictions.eq("versionCorrection", request.getVersionCorrection()));
    }
    
    if (request.getMarketDataUid() != null) {
      criteria.createCriteria("marketData")
        .add(Restrictions.eq("baseUidScheme", request.getMarketDataUid().getScheme()))
        .add(Restrictions.eq("baseUidValue", request.getMarketDataUid().getValue()))
        .add(eqOrIsNull("baseUidVersion", request.getMarketDataUid().getVersion()));
      //.addOrder(Order.asc("baseUid"));
    }
    
    if (request.getViewDefinitionUid() != null) {
      criteria.add(Restrictions.eq("viewDefinitionUidScheme", request.getViewDefinitionUid().getScheme()))
        .add(Restrictions.eq("viewDefinitionUidValue", request.getViewDefinitionUid().getValue()))
        .add(eqOrIsNull("viewDefinitionUidVersion", request.getViewDefinitionUid().getVersion()));
      //.addOrder(Order.asc("viewDefinitionUid"));
    }
    
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Pair<List<RiskRun>, Paging>>() {
      @Override
      public Pair<List<RiskRun>, Paging> doInTransaction(final TransactionStatus status) {
        //
        final PagingRequest pagingRequest = request.getPagingRequest();
        List<RiskRun> results = Collections.emptyList();
        Paging paging;
        if (!pagingRequest.equals(PagingRequest.NONE)) {
          if (pagingRequest.equals(PagingRequest.ALL)) {
            criteria.addOrder(Order.asc("valuationTime"));
            results = getHibernateTemplate().findByCriteria(
              criteria,
              pagingRequest.getFirstItem(),
              pagingRequest.getPagingSize());
            //
            paging = Paging.of(pagingRequest, results);
          } else {
            criteria.setProjection(Projections.rowCount());
            final Long totalCount = (Long) getHibernateTemplate().findByCriteria(criteria).get(0);
            paging = Paging.of(pagingRequest, totalCount.intValue());
            //
            criteria.setProjection(null);
            criteria.setResultTransformer(Criteria.ROOT_ENTITY);
            criteria.addOrder(Order.asc("valuationTime"));
            results = getHibernateTemplate().findByCriteria(
              criteria,
              pagingRequest.getFirstItem(),
              pagingRequest.getPagingSize());
          }
        } else {
          paging = Paging.of(PagingRequest.NONE, 0);
        }
        return Pairs.of(results, paging);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  public void addValuesToMarketData(final ObjectId marketDataId, final Set<MarketDataValue> values) {
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        _dbBatchWriter.addValuesToMarketDataInTransaction(marketDataId, values);
        return null;
      }
    });
  }

  @Override
  public RiskRun startRiskRun(final ViewCycleMetadata cycleMetadata,
                              final Map<String, String> batchParameters,
                              final RunCreationMode runCreationMode,
                              final SnapshotMode snapshotMode) {
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<RiskRun>() {
      @Override
      public RiskRun doInTransaction(final TransactionStatus status) {
        return _dbBatchWriter.startBatchInTransaction(cycleMetadata, batchParameters, runCreationMode, snapshotMode);
      }
    });
  }

  @Override
  public void deleteRiskRun(final ObjectId batchUniqueId) {
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        _dbBatchWriter.deleteBatchInTransaction(batchUniqueId);
        return null;
      }
    });
  }

  /**
   * Searches for documents with paging.
   *
   * @param <D>  the  list type
   * @param pagingRequest  the paging request, not null
   * @param sql  the array of SQL, query and count, not null
   * @param args  the query arguments, not null
   * @param extractor  the extractor of results, not null
   * @return values with its paging descriptor
   */
  protected <D> Pair<List<D>, Paging> searchWithPaging(
    final PagingRequest pagingRequest, final String[] sql, final DbMapSqlParameterSource args,
    final ResultSetExtractor<List<D>> extractor) {
    
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Pair<List<D>, Paging>>() {
      @Override
      public Pair<List<D>, Paging> doInTransaction(final TransactionStatus status) {
        final List<D> result = newArrayList();
        Paging paging;
        s_logger.debug("with args {}", args);
        final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
        if (pagingRequest.equals(PagingRequest.ALL)) {
          result.addAll(namedJdbc.query(sql[0], args, extractor));
          paging = Paging.of(pagingRequest, result);
        } else {
          s_logger.debug("executing sql {}", sql[1]);
          final int count = namedJdbc.queryForObject(sql[1], args, Integer.class);
          paging = Paging.of(pagingRequest, count);
          if (count > 0 && !pagingRequest.equals(PagingRequest.NONE)) {
            s_logger.debug("executing sql {}", sql[0]);
            result.addAll(namedJdbc.query(sql[0], args, extractor));
          }
        }
        return Pairs.of(result, paging);
      }
    });
  }

  @Override
  public Pair<List<ViewResultEntry>, Paging> getBatchValues(final ObjectId batchId, final PagingRequest pagingRequest) {
    s_logger.info("Getting Batch values: ", pagingRequest);
    
    final Long runId = extractOid(batchId);
    final DbMapSqlParameterSource args = createParameterSource();
    args.addValue("run_id", runId);
    if (pagingRequest != null) {
      args.addValue("paging_offset", pagingRequest.getFirstItem());
      args.addValue("paging_fetch", pagingRequest.getPagingSize());
    }
    
    final String[] sql = {getElSqlBundle().getSql("GetBatchValues", args), getElSqlBundle().getSql("BatchValuesCount", args)};
    return searchWithPaging(pagingRequest, sql, args, new BatchValuesExtractor());
  }

  @Override
  public void endRiskRun(final ObjectId batchUniqueId) {
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        _dbBatchWriter.endBatchInTransaction(batchUniqueId);
        return null;
      }
    });
  }

  @Override
  public MarketData createMarketData(final UniqueId marketDataSnapshotUniqueId) {
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<MarketData>() {
      @Override
      public MarketData doInTransaction(final TransactionStatus status) {
        return _dbBatchWriter.createOrGetMarketDataInTransaction(marketDataSnapshotUniqueId);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  public void addJobResults(final ObjectId riskRunId, final ViewComputationResultModel result) {
    getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        _dbBatchWriter.addJobResultsInTransaction(status, riskRunId, result);
        return null;
      }
    });
  }

  /**
   * Mapper from SQL rows to a ViewResultEntries.
   */
  protected final class BatchValuesExtractor implements ResultSetExtractor<List<ViewResultEntry>> {

    @Override
    public List<ViewResultEntry> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      final List<ViewResultEntry> data = newArrayList();
      while (rs.next()) {
        data.add(buildBatchValue(rs));
      }
      return data;
    }

    private ViewResultEntry buildBatchValue(final ResultSet rs) throws SQLException {
//      final long id = rs.getLong("ID");
//      final long calculationConfigurationId = rs.getLong("calculation_configuration_id");
//      final long valueSpecificationId = rs.getLong("value_specification_id");
//      final long functionUniqueId = rs.getLong("function_unique_id");
//      final long computationTargetId = rs.getLong("computation_target_id");
//      final long runId = rs.getLong("run_id");
      final double value = rs.getDouble("value");
      final String valueName = rs.getString("name");
//      final Timestamp evalInstant = rs.getTimestamp("eval_instant");
//      final long computeNodeId = rs.getLong("compute_node_id");
      final ComputationTargetType computationTargetType = ComputationTargetType.parse(rs.getString("target_type"));
      final String valueRequirementsSyntheticForm = rs.getString("synthetic_form");
      final String targetTypeIdScheme = rs.getString("target_type_id_scheme");
      final String targetTypeIdValue = rs.getString("target_type_id_value");
      final String targetTypeIdVersion = rs.getString("target_type_id_version");
      final UniqueId targetId = UniqueId.of(targetTypeIdScheme, targetTypeIdValue, targetTypeIdVersion);
      final ValueProperties valueProperties = RiskValueProperties.parseJson(valueRequirementsSyntheticForm);
      final String configurationName = rs.getString("config_name");
      final ValueSpecification valueSpecification = ValueSpecification.of(valueName, computationTargetType, targetId, valueProperties);
      final ComputedValueResult computedValue = new ComputedValueResult(valueSpecification, value, AggregatedExecutionLog.EMPTY);
      final ViewResultEntry viewResultEntry = new ViewResultEntry(configurationName, computedValue);

      return viewResultEntry;
    }
  }

}

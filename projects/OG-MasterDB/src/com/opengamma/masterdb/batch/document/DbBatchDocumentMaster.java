/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch.document;

import com.opengamma.id.UniqueId;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.masterdb.batch.RiskRun;
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

import static com.opengamma.util.db.DbUtil.eqOrIsNull;

public class DbBatchDocumentMaster extends AbstractDbMaster implements BatchMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchDocumentMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbBat";

  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   */
  public DbBatchDocumentMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
  }

  public RiskRun getRiskRunById(final Long id) {
    return getHibernateTransactionTemplate().execute(new HibernateCallback<RiskRun>() {
      @Override
      public RiskRun doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("RiskRun.one.byId");
        query.setLong("id", id);
        return (RiskRun) query.uniqueResult();
      }
    });
  }


  @Override
  public BatchDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    s_logger.info("Getting BatchDocument by unique id: ", uniqueId);
    final Long id = extractOid(uniqueId);
    return getHibernateTransactionTemplate().execute(new HibernateCallback<BatchDocument>() {
      @Override
      public BatchDocument doInHibernate(Session session) throws HibernateException, SQLException {
        RiskRun run = getRiskRunById(id);
        if (run != null) {
          return new BatchDocument(run);
        } else {
          return null;
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

        for (RiskRun riskRun : runs) {
          BatchDocument doc = new BatchDocument(riskRun);
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
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Integer>() {
      @Override
      public Integer doInTransaction(final TransactionStatus status) {
        return getHibernateTemplate().execute(new HibernateCallback<Integer>() {
          @Override
          public Integer doInHibernate(Session session) throws HibernateException, SQLException {
            Query query = session.getNamedQuery("RiskRun.delete.byId");
            query.setLong("id", id);
            return query.executeUpdate();
          }
        });
      }
    });
  }

  //--------------------------------------------------------------------------------------------------------------------

}

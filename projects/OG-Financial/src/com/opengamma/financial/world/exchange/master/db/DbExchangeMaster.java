/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.master.db.AbstractDbMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;

/**
 * An exchange master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the exchange master using an SQL database.
 * Full details of the API are in {@link ExchangeMaster}.
 * <p>
 * This class uses SQL via JDBC to store the data via a set of workers.
 * The workers may be replaced by configuration to allow different SQL on different databases.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbExchangeMaster extends AbstractDbMaster implements ExchangeMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbExchangeMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbExg";

  /**
   * The workers.
   */
  private DbExchangeMasterWorkers _workers;

  /**
   * Creates an instance.
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbExchangeMaster(final DbSource dbSource) {
    super(dbSource, IDENTIFIER_SCHEME_DEFAULT);
    setWorkers(new DbExchangeMasterWorkers());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the configured set of workers.
   * @return the configured workers, not null
   */
  public DbExchangeMasterWorkers getWorkers() {
    return _workers;
  }

  /**
   * Sets the configured workers to use.
   * <p>
   * The workers will be {@link DbExchangeMasterWorkers#init initialized} as part of this method call.
   * @param workers  the configured workers, not null
   */
  public void setWorkers(final DbExchangeMasterWorkers workers) {
    ArgumentChecker.notNull(workers, "workers");
    workers.init(this);
    s_logger.debug("installed DbExchangeMasterWorkers: {}", workers);
    _workers = workers;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeSearchResult search(final ExchangeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    return getWorkers().getSearchWorker().search(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    return getWorkers().getGetWorker().get(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument add(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    ArgumentChecker.notNull(document.getName(), "document.name");
    
    return getWorkers().getAddWorker().add(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument update(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    ArgumentChecker.notNull(document.getExchangeId(), "document.exchangeId");
    ArgumentChecker.notNull(document.getName(), "document.name");
    checkScheme(document.getExchangeId());
    
    return getWorkers().getUpdateWorker().update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    getWorkers().getRemoveWorker().remove(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeHistoryResult history(final ExchangeHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getExchangeId(), "request.exchangeId");
    checkScheme(request.getExchangeId());
    
    return getWorkers().getHistoryWorker().history(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument correct(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    ArgumentChecker.notNull(document.getExchangeId(), "document.exchangeId");
    checkScheme(document.getExchangeId());
    
    return getWorkers().getCorrectWorker().correct(document);
  }

}

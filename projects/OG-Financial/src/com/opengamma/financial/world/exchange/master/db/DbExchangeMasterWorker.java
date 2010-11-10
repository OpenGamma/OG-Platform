/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master.db;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.master.db.AbstractDbMasterWorker;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Base worker class for the exchange master.
 * <p>
 * This is designed to allow each exchange master method to be implemented by a
 * different class and easily replaced by an alternative.
 * Implementations are registered using {@link DbExchangeMasterWorkers}.
 * <p>
 * The API of this class follows {@link ExchangeMaster}.
 * Each of the methods should be implemented as per the documentation on the master.
 * The parameters to the methods will be pre-checked for nulls before the worker is called,
 * including any internal required values in request or document objects.
 * <p>
 * This base implementation throws {@code UnsupportedOperationException} from each method.
 * As a result, subclasses only need to implement those methods they want to.
 */
public class DbExchangeMasterWorker extends AbstractDbMasterWorker<DbExchangeMaster> {

  /**
   * The Fudge context.
   */
  protected static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  /**
   * Creates an instance.
   */
  protected DbExchangeMasterWorker() {
  }

  /**
   * Initializes the instance.
   * @param master  the exchange master, not null
   */
  protected void init(final DbExchangeMaster master) {
    super.init(master);
  }

  //-------------------------------------------------------------------------
  protected ExchangeSearchResult search(ExchangeSearchRequest request) {
    throw new UnsupportedOperationException();
  }

  protected ExchangeDocument get(final UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected ExchangeDocument add(ExchangeDocument document) {
    throw new UnsupportedOperationException();
  }

  protected ExchangeDocument update(ExchangeDocument document) {
    throw new UnsupportedOperationException();
  }

  protected void remove(UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected ExchangeHistoryResult history(ExchangeHistoryRequest request) {
    throw new UnsupportedOperationException();
  }

  protected ExchangeDocument correct(ExchangeDocument document) {
    throw new UnsupportedOperationException();
  }

}

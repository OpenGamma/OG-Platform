/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.language.context.SessionContext;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;

/**
 * Combines the masters from session, user and global contexts into a single
 * entity for searching and querying. A map of schemes to actual masters is
 * built on the fly based on error returns from the underlying masters. 
 * 
 * @param <D> the document type of the underlying master(s)
 * @param <M> the type of the master(s)
 * @param <Master> the combined master
 */
public abstract class CombiningMaster<D extends AbstractDocument, M extends AbstractMaster<D>, Master extends CombinedMaster<D, M>> {

  /**
   * Misleading name constant for the "session" store. Should be called "session" but that might confuse people until
   * we have the per-user store implemented correctly.
   */
  public static final String SESSION_MASTER_DISPLAY_NAME = "user";

  /**
   * Misleading name constant for the "user" store. Should be called "user" but that might confuse people given that
   * we currently use it for the per-session store.
   */
  public static final String USER_MASTER_DISPLAY_NAME = "local";

  /**
   * Name constant for the "global" store shared with all other users and anything else using the view processor
   * configuration the stack was initialized from.
   */
  public static final String GLOBAL_MASTER_DISPLAY_NAME = "shared";

  /**
   * Singleton instance for MarketDataSnapshotMaster.
   */
  public static final CombiningMaster<MarketDataSnapshotDocument, MarketDataSnapshotMaster, CombinedMarketDataSnapshotMaster> MARKET_DATA_SNAPSHOT =
      new CombiningMaster<MarketDataSnapshotDocument, MarketDataSnapshotMaster, CombinedMarketDataSnapshotMaster>() {

        @Override
        protected MarketDataSnapshotMaster getMasterImpl(final RemoteClient client) {
          return client.getMarketDataSnapshotMaster();
        }

        @Override
        protected CombinedMarketDataSnapshotMaster createCombinedMaster(final MarketDataSnapshotMaster sessionMaster, final MarketDataSnapshotMaster userMaster,
            final MarketDataSnapshotMaster globalMaster) {
          return new CombinedMarketDataSnapshotMaster(this, sessionMaster, userMaster, globalMaster);
        }

      };

  /**
   * Singleton instance for PortfolioMaster.
   */
  public static final CombiningMaster<PortfolioDocument, PortfolioMaster, CombinedPortfolioMaster> PORTFOLIO = new CombiningMaster<PortfolioDocument, PortfolioMaster, CombinedPortfolioMaster>() {

    @Override
    protected PortfolioMaster getMasterImpl(final RemoteClient client) {
      return client.getPortfolioMaster();
    }

    @Override
    protected CombinedPortfolioMaster createCombinedMaster(final PortfolioMaster sessionMaster, final PortfolioMaster userMaster, final PortfolioMaster globalMaster) {
      return new CombinedPortfolioMaster(this, sessionMaster, userMaster, globalMaster);
    }

  };

  /**
   * Singleton instance for PositionMaster.
   */
  public static final CombiningMaster<PositionDocument, PositionMaster, CombinedPositionMaster> POSITION = new CombiningMaster<PositionDocument, PositionMaster, CombinedPositionMaster>() {

    @Override
    protected PositionMaster getMasterImpl(final RemoteClient client) {
      return client.getPositionMaster();
    }

    @Override
    protected CombinedPositionMaster createCombinedMaster(final PositionMaster sessionMaster, final PositionMaster userMaster, final PositionMaster globalMaster) {
      return new CombinedPositionMaster(this, sessionMaster, userMaster, globalMaster);
    }

  };

  /**
   * Singleton instance for SecurityMaster.
   */
  public static final CombiningMaster<SecurityDocument, SecurityMaster, CombinedSecurityMaster> SECURITY = new CombiningMaster<SecurityDocument, SecurityMaster, CombinedSecurityMaster>() {

    @Override
    protected SecurityMaster getMasterImpl(final RemoteClient client) {
      return client.getSecurityMaster();
    }

    @Override
    protected CombinedSecurityMaster createCombinedMaster(final SecurityMaster sessionMaster, final SecurityMaster userMaster, final SecurityMaster globalMaster) {
      return new CombinedSecurityMaster(this, sessionMaster, userMaster, globalMaster);
    }

  };

  private final ConcurrentMap<String, MasterID> _mastersByScheme = new ConcurrentHashMap<String, MasterID>();

  private CombiningMaster() {
  }

  protected abstract M getMasterImpl(final RemoteClient client);

  protected MasterID getSchemeMasterID(final String scheme) {
    return _mastersByScheme.get(scheme);
  }

  protected void setSchemeMasterID(final String scheme, final MasterID masterID) {
    _mastersByScheme.putIfAbsent(scheme, masterID);
  }

  protected abstract Master createCombinedMaster(final M sessionMaster, final M userMaster, final M globalMaster);

  private M getMaster(final RemoteClient client) {
    if (client != null) {
      return getMasterImpl(client);
    } else {
      return null;
    }
  }

  public Master get(final SessionContext sessionContext) {
    return createCombinedMaster(getMaster(sessionContext.getClient()), getMaster(sessionContext.getUserContext().getClient()), getMaster(sessionContext.getGlobalContext().getClient()));
  }

}

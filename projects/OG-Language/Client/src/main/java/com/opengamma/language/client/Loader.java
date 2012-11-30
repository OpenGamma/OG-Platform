/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.client;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.financial.user.rest.RemoteClient.ExternalTargetProvider;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.connector.StashMessage;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with {@link RemoteClient} instances.
 */
public class Loader extends ContextInitializationBean {

  private static final String CLIENTID_STASH_FIELD = "clientId";

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private Configuration _configuration;
  private String _configMaster = "configMaster";
  private String _positionMaster = "positionMaster";
  private String _portfolioMaster = "portfolioMaster";
  private String _securityMaster = "securityMaster";
  private String _marketDataSnapshotMaster = "marketDataSnapshotMaster";
  private String _userData = "userData";
  private String _historicalTimeSeriesMaster = "historicalTimeSeriesMaster";
  private ScheduledExecutorService _housekeepingScheduler;
  private int _clientHeartbeat = 5;

  public void setConfiguration(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }

  public void setConfigMaster(final String configMaster) {
    _configMaster = configMaster;
  }

  public String getConfigMaster() {
    return _configMaster;
  }

  public void setPositionMaster(final String positionMaster) {
    _positionMaster = positionMaster;
  }

  public String getPositionMaster() {
    return _positionMaster;
  }

  public void setPortfolioMaster(final String portfolioMaster) {
    _portfolioMaster = portfolioMaster;
  }

  public String getPortfolioMaster() {
    return _portfolioMaster;
  }

  public void setSecurityMaster(final String securityMaster) {
    _securityMaster = securityMaster;
  }

  public String getSecurityMaster() {
    return _securityMaster;
  }

  public void setMarketDataSnapshotMaster(final String marketDataSnapshotMaster) {
    _marketDataSnapshotMaster = marketDataSnapshotMaster;
  }

  public String getMarketDataSnapshotMaster() {
    return _marketDataSnapshotMaster;
  }

  public void setUserData(final String userData) {
    _userData = userData;
  }

  public String getUserData() {
    return _userData;
  }

  public void setHousekeepingScheduler(final ScheduledExecutorService housekeepingScheduler) {
    _housekeepingScheduler = housekeepingScheduler;
  }

  public ScheduledExecutorService getHousekeepingScheduler() {
    return _housekeepingScheduler;
  }

  public void setClientHeartbeatPeriod(final int minutes) {
    _clientHeartbeat = minutes;
  }

  public int getClientHeartbeatPeriod() {
    return _clientHeartbeat;
  }

  public void setHistoricalTimeSeriesMaster(final String historicalTimeSeriesMaster) {
    _historicalTimeSeriesMaster = historicalTimeSeriesMaster;
  }

  public String getHistoricalTimeSeriesMaster() {
    return _historicalTimeSeriesMaster;
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
    ArgumentChecker.notNull(getGlobalContextFactory(), "globalContextFactory");
    ArgumentChecker.notNull(getUserContextFactory(), "userContextFactory");
    ArgumentChecker.notNull(getSessionContextFactory(), "sessionContextFactory");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    s_logger.info("Configuring \"shared\" remote client support");
    final ExternalTargetProvider targets = new ExternalTargetProvider();
    targets.setConfigMaster(getConfiguration().getURIConfiguration(getConfigMaster()));
    targets.setPositionMaster(getConfiguration().getURIConfiguration(getPositionMaster()));
    targets.setPortfolioMaster(getConfiguration().getURIConfiguration(getPortfolioMaster()));
    targets.setSecurityMaster(getConfiguration().getURIConfiguration(getSecurityMaster()));
    targets.setMarketDataSnapshotMaster(getConfiguration().getURIConfiguration(getMarketDataSnapshotMaster()));
    targets.setHistoricalTimeSeriesMaster(getConfiguration().getURIConfiguration(getHistoricalTimeSeriesMaster()));
    globalContext.setClient(new RemoteClient(null, getConfiguration().getFudgeContext(), targets));
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    // TODO: do we have a "user" one shared among all of their sessions?
  }

  @Override
  protected void doneContext(final MutableUserContext userContext) {
    final ScheduledFuture<?> heartbeat = userContext.getClientHeartbeat();
    if (heartbeat != null) {
      s_logger.debug("Cancelling user client heartbeat");
      heartbeat.cancel(true);
    }
  }

  protected void initClient(final MutableSessionContext sessionContext, final RemoteClient client) {
    if (getHousekeepingScheduler() != null) {
      sessionContext.setClientHeartbeat(getHousekeepingScheduler().scheduleWithFixedDelay(client.createHeartbeatSender(), getClientHeartbeatPeriod(), getClientHeartbeatPeriod(), TimeUnit.MINUTES));
    } else {
      s_logger.warn("No housekeeping scheduler set so no heartbeats will be sent; client may timeout");
    }
    sessionContext.setClient(client);
  }

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    final URI uri = getConfiguration().getURIConfiguration(getUserData());
    if (uri == null) {
      s_logger.warn("Per-user remote engine clients not available");
      return;
    }
    final StashMessage stash = sessionContext.getStashMessage();
    if (stash != null) {
      final FudgeMsg msg = stash.get();
      if (msg != null) {
        final String clientId = msg.getString(CLIENTID_STASH_FIELD);
        if (clientId != null) {
          s_logger.info("Recovering old remote engine client {}", clientId);
          initClient(sessionContext, RemoteClient.forClient(getConfiguration().getFudgeContext(), uri, sessionContext.getUserContext().getUserName(), clientId));
          return;
        }
      }
    }
    s_logger.info("Creating new remote engine client");
    final RemoteClient client = RemoteClient.forNewClient(getConfiguration().getFudgeContext(), uri, sessionContext.getUserContext().getUserName());
    if (stash != null) {
      final MutableFudgeMsg msgStash = FudgeContext.GLOBAL_DEFAULT.newMessage();
      msgStash.add(CLIENTID_STASH_FIELD, client.getClientId());
      stash.put(msgStash);
    } else {
      s_logger.warn("Message stash not available - cannot resume client if JVM abends");
    }
    initClient(sessionContext, client);
  }

  @Override
  protected void doneContext(final MutableSessionContext sessionContext) {
    final ScheduledFuture<?> heartbeat = sessionContext.getClientHeartbeat();
    if (heartbeat != null) {
      s_logger.debug("Cancelling session client heartbeat");
      heartbeat.cancel(true);
    }
  }

}

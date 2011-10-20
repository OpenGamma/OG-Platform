/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.client;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.financial.user.rest.RemoteClient.ExternalTargetProvider;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with {@link RemoteClient} instances.
 */
public class Loader extends ContextInitializationBean {

  private static final String CLIENTID_STASH_FIELD = "clientId";

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private Configuration _configuration;
  private String _positionMaster = "positionMaster";
  private String _portfolioMaster = "portfolioMaster";
  private String _securityMaster = "securityMaster";
  private String _marketDataSnapshotMaster = "marketDataSnapshotMaster";
  private String _userData = "userData";
  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;

  public void setConfiguration(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public Configuration getConfiguration() {
    return _configuration;
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

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
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
    targets.setPositionMaster(getConfiguration().getURIConfiguration(getPositionMaster()));
    targets.setPortfolioMaster(getConfiguration().getURIConfiguration(getPortfolioMaster()));
    targets.setSecurityMaster(getConfiguration().getRestTargetConfiguration(getSecurityMaster()));
    targets.setMarketDataSnapshotMaster(getConfiguration().getRestTargetConfiguration(getMarketDataSnapshotMaster()));
    globalContext.setClient(new RemoteClient(null, getFudgeContext(), targets));
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    // TODO: do we have a "user" one shared among all of their sessions?
  }

  protected void initClient(final MutableSessionContext sessionContext, final RemoteClient client) {
    // TODO: heartbeat sender
    sessionContext.setClient(client);
  }

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    final RestTarget target = getConfiguration().getRestTargetConfiguration(getUserData());
    if (target == null) {
      s_logger.warn("Per-user remote engine clients not available");
      return;
    }
    final FudgeMsg msg = sessionContext.getStashMessage().get();
    if (msg != null) {
      final String clientId = msg.getString(CLIENTID_STASH_FIELD);
      if (clientId != null) {
        s_logger.info("Recovering old remote engine client {}", clientId);
        initClient(sessionContext, RemoteClient.forClient(getFudgeContext(), target, sessionContext.getUserContext().getUserName(), clientId));
        return;
      }
    }
    s_logger.info("Creating new remote engine client");
    final RemoteClient client = RemoteClient.forNewClient(getFudgeContext(), target, sessionContext.getUserContext().getUserName());
    final MutableFudgeMsg stash = getFudgeContext().newMessage();
    stash.add(CLIENTID_STASH_FIELD, client.getClientId());
    sessionContext.getStashMessage().put(stash);
    initClient(sessionContext, client);
  }

  @Override
  protected void doneContext(final MutableSessionContext sessionContext) {
    // TODO: stop the heartbeat sender
  }

}

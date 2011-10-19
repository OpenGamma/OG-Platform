/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.client;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.financial.user.rest.RemoteClient.ExternalTargetProvider;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the global context with the "shared" {@link RemoteClient} instance.
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private Configuration _configuration;
  private String _positionMaster = "positionMaster";
  private String _portfolioMaster = "portfolioMaster";
  private String _securityMaster = "securityMaster";
  private String _marketDataSnapshotMaster = "marketDataSnapshotMaster";
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
    // TODO: should set the user's client instance
  }

}

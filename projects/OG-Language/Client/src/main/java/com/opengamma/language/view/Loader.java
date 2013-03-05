/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.language.invoke.TypeConverterProviderBean;
import com.opengamma.language.procedure.ProcedureProviderBean;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;

/**
 * Extends the global context with view processor support (if available).
 */
public class Loader extends ContextInitializationBean {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "viewProcessor";
  private Configuration _configuration;
  private JmsConnector _jmsConnector;
  private ScheduledExecutorService _housekeepingScheduler;
  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;

  public void setConfiguration(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }

  public void setConfigurationEntry(final String configurationEntry) {
    ArgumentChecker.notNull(configurationEntry, "configurationEntry");
    _configurationEntry = configurationEntry;
  }

  public String getConfigurationEntry() {
    return _configurationEntry;
  }

  public void setJmsConnector(final JmsConnector jmsConnector) {
    ArgumentChecker.notNull(jmsConnector, "jmsConnector");
    _jmsConnector = jmsConnector;
  }

  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  public void setHousekeepingScheduler(final ScheduledExecutorService housekeepingScheduler) {
    _housekeepingScheduler = housekeepingScheduler;
  }

  public ScheduledExecutorService getHousekeepingScheduler() {
    return _housekeepingScheduler;
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
    ArgumentChecker.notNull(getJmsConnector(), "jmsConnector");
    ArgumentChecker.notNull(getHousekeepingScheduler(), "housekeepingScheduler");
    ArgumentChecker.notNull(getGlobalContextFactory(), "globalContextFactory");
    ArgumentChecker.notNull(getUserContextFactory(), "userContextFactory");
    ArgumentChecker.notNull(getSessionContextFactory(), "sessionContextFactory");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    // TODO: Still not the right place to set this (was in the OG-Excel project only), but the ViewProcessor is loaded after all of the other sources
    globalContext.setComputationTargetResolver(new DefaultComputationTargetResolver(globalContext.getSecuritySource(), globalContext.getPositionSource()));
    final URI uri = getConfiguration().getURIConfiguration(getConfigurationEntry());
    if (uri == null) {
      s_logger.warn("View processor support not available");
      return;
    }
    s_logger.info("Configuring view processor support");
    globalContext.setViewProcessor(new RemoteViewProcessor(uri, getJmsConnector(), getHousekeepingScheduler()));
    globalContext.getFunctionProvider().addProvider(new FunctionProviderBean(
        FetchViewDefinitionFunction.INSTANCE,
        GetViewPortfolioFunction.INSTANCE,
        GetViewResultFunction.INSTANCE,
        HistoricalExecutionSequenceFunction.INSTANCE,
        SetViewClientExecutionFlagFunction.INSTANCE,
        ViewClientDescriptorFunction.HISTORICAL_MARKET_DATA,
        ViewClientDescriptorFunction.STATIC_MARKET_DATA,
        ViewClientDescriptorFunction.STATIC_SNAPSHOT,
        ViewClientDescriptorFunction.TICKING_MARKET_DATA,
        ViewClientDescriptorFunction.TICKING_SNAPSHOT,
        ViewClientFunction.INSTANCE,
        ViewDefinitionFunction.INSTANCE,
        ViewsFunction.INSTANCE,
        ViewIdFunction.INSTANCE,
        ViewPrimitiveCycleValueFunction.INSTANCE));
    globalContext.getProcedureProvider().addProvider(new ProcedureProviderBean(
        ConfigureViewClientProcedure.INSTANCE,
        StoreViewDefinitionProcedure.INSTANCE,
        TriggerViewCycleProcedure.INSTANCE));
    globalContext.getTypeConverterProvider().addTypeConverterProvider(new TypeConverterProviderBean(
        UserViewClientConverter.INSTANCE,
        ViewClientDescriptorConverter.INSTANCE));
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    userContext.setViewClients(new UserViewClients(userContext));
  }

  @Override
  protected void doneContext(final MutableUserContext userContext) {
    userContext.getViewClients().destroyAll();
  }

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    sessionContext.setViewClients(new SessionViewClients(sessionContext));
  }

  @Override
  protected void doneContext(final MutableSessionContext sessionContext) {
    sessionContext.getViewClients().destroyAll();
  }

}

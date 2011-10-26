/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import javax.jms.ConnectionFactory;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Extends the global context with view processor support (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "viewProcessor";
  private Configuration _configuration;
  private ConnectionFactory _connectionFactory;
  private ScheduledExecutorService _scheduler;
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

  public void setConnectionFactory(final ConnectionFactory connectionFactory) {
    ArgumentChecker.notNull(connectionFactory, "connectionFactory");
    _connectionFactory = connectionFactory;
  }

  public ConnectionFactory getConnectionFactory() {
    return _connectionFactory;
  }

  public void setScheduler(final ScheduledExecutorService scheduler) {
    _scheduler = scheduler;
  }

  public ScheduledExecutorService getScheduler() {
    return _scheduler;
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
    ArgumentChecker.notNull(getConnectionFactory(), "connectionFactory");
    ArgumentChecker.notNull(getScheduler(), "scheduler");
    ArgumentChecker.notNull(getGlobalContextFactory(), "globalContextFactory");
    ArgumentChecker.notNull(getUserContextFactory(), "userContextFactory");
    ArgumentChecker.notNull(getSessionContextFactory(), "sessionContextFactory");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    final URI uri = getConfiguration().getURIConfiguration(getConfigurationEntry());
    if (uri == null) {
      s_logger.warn("View processor support not available");
      return;
    }
    s_logger.info("Configuring view processor support");
    globalContext.setViewProcessor(new RemoteViewProcessor(uri, getConnectionFactory(), getScheduler()));
    globalContext.getFunctionProvider().addProvider(new FunctionProviderBean(
        GetViewResultFunction.INSTANCE,
        ViewClientDescriptorFunction.HISTORICAL_MARKET_DATA,
        ViewClientDescriptorFunction.STATIC_MARKET_DATA,
        ViewClientDescriptorFunction.STATIC_SNAPSHOT,
        ViewClientDescriptorFunction.TICKING_MARKET_DATA,
        ViewClientDescriptorFunction.TICKING_SNAPSHOT,
        ViewClientFunction.INSTANCE,
        ViewsFunction.INSTANCE));
    globalContext.getProcedureProvider().addProvider(new ProcedureProviderBean(
        ConfigureViewClientProcedure.INSTANCE,
        TriggerViewCycleProcedure.INSTANCE));
    globalContext.getTypeConverterProvider().addTypeConverterProvider(new TypeConverterProviderBean(
        UserViewClientConverter.INSTANCE));
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

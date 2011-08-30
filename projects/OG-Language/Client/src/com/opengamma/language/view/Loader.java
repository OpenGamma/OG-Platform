/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import javax.jms.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with view processor support (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "viewProcessor";
  private Configuration _configuration;
  private ConnectionFactory _connectionFactory;
  private ScheduledExecutorService _scheduler;

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

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
    ArgumentChecker.notNull(getConnectionFactory(), "connectionFactory");
    ArgumentChecker.notNull(getScheduler(), "scheduler");
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
    // TODO: add function provider
    // TODO: add type converter provider
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    userContext.setViewClients(new ViewClients(userContext));
  }

  @Override
  protected void doneContext(final MutableUserContext userContext) {
    userContext.getViewClients().destroyAll();
  }

}

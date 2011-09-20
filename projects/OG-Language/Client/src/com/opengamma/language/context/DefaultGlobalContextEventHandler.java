/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * The default global context event handler.
 */
public final class DefaultGlobalContextEventHandler implements InitializingBean, GlobalContextEventHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultGlobalContextEventHandler.class);

  private Properties _systemSettings;
  private ExecutorService _saturatingExecutor;

  public DefaultGlobalContextEventHandler() {
    final int cores = Runtime.getRuntime().availableProcessors();
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(cores, cores, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    executor.allowCoreThreadTimeOut(true);
    executor.setThreadFactory(new NamedThreadPoolFactory("S-Worker"));
    setSaturatingExecutor(executor);
  }

  public void setSystemSettings(final Properties systemSettings) {
    ArgumentChecker.notNull(systemSettings, "systemSettings");
    _systemSettings = systemSettings;
  }

  public Properties getSystemSettings() {
    return _systemSettings;
  }

  public void setSaturatingExecutor(final ExecutorService saturatingExecutor) {
    ArgumentChecker.notNull(saturatingExecutor, "saturatingExecutor");
    _saturatingExecutor = saturatingExecutor;
  }

  public ExecutorService getSaturatingExecutor() {
    return _saturatingExecutor;
  }

  // GlobalContextEventHandler

  @Override
  public void initContext(final MutableGlobalContext context) {
    s_logger.info("Initialising global context {}", context);
    context.setSystemSettings(getSystemSettings());
    context.setSaturatingExecutor(getSaturatingExecutor());
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getSystemSettings(), "systemSettings");
    ArgumentChecker.notNull(getSaturatingExecutor(), "saturatingExecutor");
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.Enumeration;
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

  private ExecutorService _saturatingExecutor;

  public DefaultGlobalContextEventHandler() {
    final int cores = Runtime.getRuntime().availableProcessors();
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(cores, cores, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    executor.allowCoreThreadTimeOut(true);
    executor.setThreadFactory(new NamedThreadPoolFactory("S-Worker"));
    setSaturatingExecutor(executor);
  }

  /**
   * Merges user specified settings into the "system" settings. Typically, settings
   * will be provided by the O/S launcher (e.g. from the registry on Windows). A
   * properties file can be used for a fallback if the properties aren't defined in
   * the registry. For example to provide defaults.
   * 
   * @param systemSettings the default settings to use
   */
  public void setSystemSettings(final Properties systemSettings) {
    ArgumentChecker.notNull(systemSettings, "systemSettings");
    final Properties existingSettings = System.getProperties();
    final Enumeration<Object> keys = systemSettings.keys();
    while (keys.hasMoreElements()) {
      final Object keyObject = keys.nextElement();
      if (existingSettings.containsKey(keyObject)) {
        s_logger.debug("Ignoring {} in favour of system property", keyObject);
      } else {
        if (keyObject instanceof String) {
          final String key = (String) keyObject;
          final String value = systemSettings.getProperty(key);
          existingSettings.setProperty(key, value);
          s_logger.debug("Using {}={}", key, value);
        }
      }
    }
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
    context.setSaturatingExecutor(getSaturatingExecutor());
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getSaturatingExecutor(), "saturatingExecutor");
  }

}

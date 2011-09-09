/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.exchange;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.exchange.rest.RemoteExchangeSource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.master.exchange.impl.EHCachingExchangeSource;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with exchange support (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "exchangeSource";
  private Configuration _configuration;
  private CacheManager _cacheManager;
  
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

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    final RestTarget restTarget = getConfiguration().getRestTargetConfiguration(getConfigurationEntry());
    if (restTarget == null) {
      s_logger.warn("Exchange support not available");
      return;
    }
    s_logger.info("Configuring exchange support");
    RemoteExchangeSource remote = new RemoteExchangeSource(getConfiguration().getFudgeContext(), restTarget);
    EHCachingExchangeSource caching = new EHCachingExchangeSource(remote, getCacheManager());
    globalContext.setExchangeSource(caching);
    // TODO:
  }

  private CacheManager getCacheManager() {
    return _cacheManager;
  }

  public void setCacheManager(CacheManager cacheManager) {
    _cacheManager = cacheManager;
  }
}

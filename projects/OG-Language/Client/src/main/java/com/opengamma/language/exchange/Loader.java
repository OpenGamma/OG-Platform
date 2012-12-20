/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.exchange;

import java.net.URI;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.exchange.impl.RemoteExchangeSource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.master.exchange.impl.EHCachingExchangeSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with exchange support (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "exchangeSource";
  private Configuration _configuration;
  private CacheManager _cacheManager = CacheManager.getInstance();
  
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

  public void setCacheManager(final CacheManager cacheManager) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
  }

  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    final URI uri = getConfiguration().getURIConfiguration(getConfigurationEntry());
    if (uri == null) {
      s_logger.warn("Exchange support not available");
      return;
    }
    s_logger.info("Configuring exchange support");
    globalContext.setExchangeSource(new EHCachingExchangeSource(new RemoteExchangeSource(uri), getCacheManager()));
    // TODO:
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.impl.EHCachingPositionSource;
import com.opengamma.engine.position.rest.RemotePositionSource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with support for positions, portfolios and trades (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "positionSource";
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
    final RestTarget restTarget = getConfiguration().getRestTargetConfiguration(getConfigurationEntry());
    if (restTarget == null) {
      s_logger.warn("Position support not available");
      return;
    }
    s_logger.info("Configuring position support");
    globalContext.setPositionSource(new EHCachingPositionSource(new RemotePositionSource(getConfiguration().getFudgeContext(), restTarget), getCacheManager()));
    // TODO: type, function and procedure providers
  }

}

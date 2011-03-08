/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.util.ArgumentChecker;

/**
 * The default global context event handler.
 */
public final class DefaultGlobalContextEventHandler implements InitializingBean, GlobalContextEventHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultGlobalContextEventHandler.class);

  private Properties _systemSettings;

  public void setSystemSettings(final Properties systemSettings) {
    ArgumentChecker.notNull(systemSettings, "systemSettings");
    _systemSettings = systemSettings;
  }

  public Properties getSystemSettings() {
    return _systemSettings;
  }

  // GlobalContextEventHandler

  @Override
  public void initContext(final MutableGlobalContext context) {
    s_logger.info("Initialising global context {}", context);
    context.setSystemSettings(getSystemSettings());
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getSystemSettings(), "systemSettings");
  }

}

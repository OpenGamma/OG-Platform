/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A no-op global context event handler.
 */
public final class NullGlobalContextEventHandler implements GlobalContextEventHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(NullGlobalContextEventHandler.class);

  @Override
  public void initContext(MutableGlobalContext context) {
    s_logger.info("Initialising global context {}", context);
    // No-op
  }

}

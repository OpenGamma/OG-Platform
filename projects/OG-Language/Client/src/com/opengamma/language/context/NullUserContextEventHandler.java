/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op handler for lifetime events on a user context
 */
public class NullUserContextEventHandler implements UserContextEventHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(NullUserContextEventHandler.class);

  @Override
  public void initContext(final MutableUserContext context) {
    s_logger.info("Initialising user context {}", context);
    // No-op
  }

  @Override
  public void doneContext(final MutableUserContext context) {
    // No-op
    s_logger.info("Destroyed user context {}", context);
  }

}

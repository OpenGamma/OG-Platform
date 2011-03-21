/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default user context event handler.  
 */
public final class DefaultUserContextEventHandler implements UserContextEventHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultUserContextEventHandler.class);

  // UserContextEventHandler

  @Override
  public void doneContext(MutableUserContext context) {
    s_logger.info("Destroying user context {}", context);
    // No-op
  }

  @Override
  public void initContext(MutableUserContext context) {
    s_logger.info("Initialising user context {}", context);
    final GlobalContext parent = context.getGlobalContext();
    context.getFunctionProvider().addProvider(parent.getFunctionProvider());
    context.getLiveDataProvider().addProvider(parent.getLiveDataProvider());
    context.getProcedureProvider().addProvider(parent.getProcedureProvider());
  }

}

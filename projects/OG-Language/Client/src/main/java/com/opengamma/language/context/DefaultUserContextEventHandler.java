/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.UserPrincipal;

/**
 * The default user context event handler.  
 */
public final class DefaultUserContextEventHandler implements UserContextEventHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultUserContextEventHandler.class);

  // UserContextEventHandler

  @Override
  public void doneContext(final MutableUserContext context) {
    s_logger.info("Destroying user context {}", context);
    // No-op
  }

  @Override
  public void initContext(final MutableUserContext context) {
    s_logger.info("Initialising user context {}", context);
    final GlobalContext parent = context.getGlobalContext();
    context.getFunctionProvider().addProvider(parent.getFunctionProvider());
    context.getLiveDataProvider().addProvider(parent.getLiveDataProvider());
    context.getProcedureProvider().addProvider(parent.getProcedureProvider());
    context.setLiveDataUser(UserPrincipal.getLocalUser(context.getUserName()));
  }

}

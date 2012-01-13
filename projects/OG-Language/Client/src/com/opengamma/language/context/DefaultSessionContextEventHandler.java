/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default session context event handler.  
 */
public final class DefaultSessionContextEventHandler implements SessionContextEventHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultSessionContextEventHandler.class);

  // SessionContextEventHandler

  @Override
  public void doneContext(final MutableSessionContext context) {
    s_logger.info("Destroying session context {}", context);
    context.getConnections().cancelAll();
  }

  @Override
  public void initContext(final MutableSessionContext context) {
    s_logger.info("Initialising session context {}", context);
    final UserContext parent = context.getUserContext();
    context.getFunctionProvider().addProvider(parent.getFunctionProvider());
    context.getLiveDataProvider().addProvider(parent.getLiveDataProvider());
    context.getProcedureProvider().addProvider(parent.getProcedureProvider());
  }

  @Override
  public void initContextWithStash(final MutableSessionContext context, final FudgeMsg stash) {
    initContext(context);
  }

}

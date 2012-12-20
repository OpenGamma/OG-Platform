/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A no-op handler for session context lifetime events
 */
public final class NullSessionContextEventHandler implements SessionContextEventHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(NullSessionContextEventHandler.class);

  @Override
  public void initContext(final MutableSessionContext context) {
    s_logger.info("Initialising session context {}", context);
    // No-op
  }

  @Override
  public void initContextWithStash(final MutableSessionContext context, final FudgeMsg stash) {
    s_logger.info("Initialising session context {} with stash {}", context, stash);
    // No-op
  }

  @Override
  public void doneContext(final MutableSessionContext context) {
    // No-op
    s_logger.info("Destroyed session context {}", context);
  }

}

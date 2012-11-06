/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeMsg;

/**
 * Lifetime events on a session context
 */
public interface SessionContextInitializationEventHandler {

  void initContext(MutableSessionContext context);

  void initContextWithStash(MutableSessionContext context, FudgeMsg stash);

}

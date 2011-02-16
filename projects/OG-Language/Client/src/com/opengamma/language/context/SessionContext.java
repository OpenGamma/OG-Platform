/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.language.connector.MessageSender;

/**
 * An information context specific to a given client instance. The external client process may
 * be longer lived than the Java framework. In these cases, any session state that must remain
 * for the lifetime of the actual client must be "stashed" and the session context initialized
 * based on the result of the stash.
 */
public abstract class SessionContext extends AbstractContext {

  private final UserContext _userContext;

  /* package */SessionContext(final UserContext userContext) {
    _userContext = userContext;
  }

  public GlobalContext getGlobalContext() {
    return getUserContext().getGlobalContext();
  }

  public UserContext getUserContext() {
    return _userContext;
  }

  // Context control

  public abstract void initContext(SessionContextInitializationEventHandler preInitialize);

  public abstract void initContextWithStash(SessionContextInitializationEventHandler preInitialize,
      FudgeFieldContainer stash);

  public abstract void doneContext();

  // Standard context members

  public abstract MessageSender getMessageSender();

}

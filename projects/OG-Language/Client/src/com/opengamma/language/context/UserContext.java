/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.procedure.AggregatingProcedureProvider;

/**
 * An information context shared by any client instances running for a given user. 
 */
public abstract class UserContext extends AbstractContext<GlobalContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(UserContext.class);

  private final Set<SessionContext> _sessionContexts = new HashSet<SessionContext>();
  private final String _userName;

  /* package */UserContext(final GlobalContext globalContext, final String userName) {
    super(globalContext);
    _userName = userName;
    setValue(FUNCTION_PROVIDER, AggregatingFunctionProvider.cachingInstance());
    setValue(LIVEDATA_PROVIDER, AggregatingLiveDataProvider.cachingInstance());
    setValue(PROCEDURE_PROVIDER, AggregatingProcedureProvider.cachingInstance());
  }

  public GlobalContext getGlobalContext() {
    return getParentContext();
  }

  public String getUserName() {
    return _userName;
  }

  protected abstract void doneContext();

  /* package */synchronized void addSessionContext(final SessionContext sessionContext) {
    _sessionContexts.add(sessionContext);
    s_logger.info("Session created for user {}", getUserName());
  }

  /* package */synchronized void removeSessionContext(final SessionContext sessionContext) {
    if (!_sessionContexts.remove(sessionContext)) {
      throw new IllegalStateException("Session context " + sessionContext + " was not in the active session set");
    }
    s_logger.info("Session destroyed for user {}", getUserName());
    if (_sessionContexts.isEmpty()) {
      getGlobalContext().removeUserContext(this);
      doneContext();
      s_logger.info("User {} disconnected", getUserName());
    } else {
      s_logger.debug("{} sessions remaining for user {}", _sessionContexts.size(), getUserName());
    }
  }

  // TODO

  @Override
  public synchronized String toString() {
    return "UserContext[user=" + getUserName() + ", sessions=" + _sessionContexts.size() + "]";
  }

}

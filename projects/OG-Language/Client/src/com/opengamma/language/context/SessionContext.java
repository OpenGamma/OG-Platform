/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.language.connector.MessageSender;
import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.procedure.AggregatingProcedureProvider;

/**
 * An information context specific to a given client instance. The external client process may
 * be longer lived than the Java framework. In these cases, any session state that must remain
 * for the lifetime of the actual client must be "stashed" and the session context initialized
 * based on the result of the stash.
 */
public abstract class SessionContext extends AbstractContext {

  /**
   * Whether the client for the session is in debug mode.
   */
  protected static final String DEBUG = "debug";
  /**
   * The {@link MessageSender} for posting asynchronously to the client.
   */
  protected static final String MESSAGE_SENDER = "messageSender";

  private final UserContext _userContext;

  /* package */SessionContext(final UserContext userContext) {
    _userContext = userContext;
    setValue(FUNCTION_PROVIDER, AggregatingFunctionProvider.nonCachingInstance());
    setValue(LIVEDATA_PROVIDER, AggregatingLiveDataProvider.nonCachingInstance());
    setValue(PROCEDURE_PROVIDER, AggregatingProcedureProvider.nonCachingInstance());
  }

  public GlobalContext getGlobalContext() {
    return getUserContext().getGlobalContext();
  }

  public UserContext getUserContext() {
    return _userContext;
  }

  // Context initialization

  public abstract void initContext(SessionContextInitializationEventHandler preInitialize);

  public abstract void initContextWithStash(SessionContextInitializationEventHandler preInitialize,
      FudgeFieldContainer stash);

  public abstract void doneContext();

  // Standard context members

  public MessageSender getMessageSender() {
    return getValue(MESSAGE_SENDER);
  }

  public boolean isDebug() {
    return getValue(DEBUG) != null;
  }

}

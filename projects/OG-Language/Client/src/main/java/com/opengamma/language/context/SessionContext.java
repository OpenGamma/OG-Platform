/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeMsg;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.language.connector.MessageSender;
import com.opengamma.language.connector.StashMessage;
import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.function.FunctionRepository;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.livedata.LiveDataRepository;
import com.opengamma.language.livedata.SessionConnections;
import com.opengamma.language.procedure.AggregatingProcedureProvider;
import com.opengamma.language.procedure.ProcedureRepository;
import com.opengamma.language.view.SessionViewClients;

/**
 * An information context specific to a given client instance. The external client process may
 * be longer lived than the Java framework. In these cases, any session state that must remain
 * for the lifetime of the actual client must be "stashed" and the session context initialized
 * based on the result of the stash.
 */
public abstract class SessionContext extends AbstractContext<UserContext> {

  /**
   * Name under which the user's session engine client is bound.
   */
  protected static final String CLIENT = "client";

  /**
   * Whether the client for the session is in debug mode.
   */
  protected static final String DEBUG = "debug";

  /**
   * The {@link MessageSender} for posting asynchronously to the client.
   */
  protected static final String MESSAGE_SENDER = "messageSender";

  /**
   * The stash message.
   */
  protected static final String STASH_MESSAGE = "stashMessage";

  /**
   * The view clients detached into this session.
   */
  protected static final String VIEW_CLIENTS = "viewClients";

  private final FunctionRepository _functionRepository = new FunctionRepository();
  private final LiveDataRepository _liveDataRepository = new LiveDataRepository();
  private final ProcedureRepository _procedureRepository = new ProcedureRepository();
  private final SessionConnections _connections = new SessionConnections(this);

  /* package */SessionContext(final UserContext userContext) {
    super(userContext);
    // Providers
    setValue(FUNCTION_PROVIDER, AggregatingFunctionProvider.nonCachingInstance());
    setValue(LIVEDATA_PROVIDER, AggregatingLiveDataProvider.nonCachingInstance());
    setValue(PROCEDURE_PROVIDER, AggregatingProcedureProvider.nonCachingInstance());
  }

  public GlobalContext getGlobalContext() {
    return getUserContext().getGlobalContext();
  }

  public UserContext getUserContext() {
    return getParentContext();
  }

  // Context initialization

  public abstract void initContext(SessionContextInitializationEventHandler preInitialize);

  public abstract void initContextWithStash(SessionContextInitializationEventHandler preInitialize,
      FudgeMsg stash);

  public abstract void doneContext();

  // Core members

  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  public LiveDataRepository getLiveDataRepository() {
    return _liveDataRepository;
  }

  public ProcedureRepository getProcedureRepository() {
    return _procedureRepository;
  }

  public SessionConnections getConnections() {
    return _connections;
  }

  // Standard context members

  public RemoteClient getClient() {
    return getValue(CLIENT);
  }

  public boolean isDebug() {
    return getValue(DEBUG) != null;
  }

  public MessageSender getMessageSender() {
    return getValue(MESSAGE_SENDER);
  }

  public StashMessage getStashMessage() {
    return getValue(STASH_MESSAGE);
  }

  public SessionViewClients getViewClients() {
    return getValue(VIEW_CLIENTS);
  }

}

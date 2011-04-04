/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeMsg;

import com.opengamma.language.connector.MessageSender;
import com.opengamma.language.definition.DefinitionRepository;
import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.function.FunctionRepository;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.livedata.LiveDataRepository;
import com.opengamma.language.procedure.AggregatingProcedureProvider;
import com.opengamma.language.procedure.ProcedureRepository;

/**
 * An information context specific to a given client instance. The external client process may
 * be longer lived than the Java framework. In these cases, any session state that must remain
 * for the lifetime of the actual client must be "stashed" and the session context initialized
 * based on the result of the stash.
 */
public abstract class SessionContext extends AbstractContext<UserContext> {

  /**
   * Whether the client for the session is in debug mode.
   */
  protected static final String DEBUG = "debug";
  /**
   * The {@link MessageSender} for posting asynchronously to the client.
   */
  protected static final String MESSAGE_SENDER = "messageSender";
  /**
   * The repository of published functions.
   */
  protected static final String FUNCTION_REPOSITORY = "functionRepository";
  /**
   * The repository of published live data.
   */
  protected static final String LIVEDATA_REPOSITORY = "liveDataRepository";
  /**
   * The repository of published procedures.
   */
  protected static final String PROCEDURE_REPOSITORY = "procedureRepository";

  /* package */SessionContext(final UserContext userContext) {
    super(userContext);
    // Providers
    setValue(FUNCTION_PROVIDER, AggregatingFunctionProvider.nonCachingInstance());
    setValue(LIVEDATA_PROVIDER, AggregatingLiveDataProvider.nonCachingInstance());
    setValue(PROCEDURE_PROVIDER, AggregatingProcedureProvider.nonCachingInstance());
    // Repositories
    final DefinitionRepository<?> repo = new DefinitionRepository<Object>();
    setValue(FUNCTION_REPOSITORY, new FunctionRepository(repo));
    setValue(LIVEDATA_REPOSITORY, new LiveDataRepository(repo));
    setValue(PROCEDURE_REPOSITORY, new ProcedureRepository(repo));
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

  // Standard context members

  public MessageSender getMessageSender() {
    return getValue(MESSAGE_SENDER);
  }

  public boolean isDebug() {
    return getValue(DEBUG) != null;
  }

  public FunctionRepository getFunctionRepository() {
    return getValue(FUNCTION_REPOSITORY);
  }

  public LiveDataRepository getLiveDataRepository() {
    return getValue(LIVEDATA_REPOSITORY);
  }

  public ProcedureRepository getProcedureRepository() {
    return getValue(PROCEDURE_REPOSITORY);
  }

}

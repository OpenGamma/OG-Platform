/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.concurrent.ScheduledFuture;

import org.fudgemsg.FudgeMsg;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.language.connector.MessageSender;
import com.opengamma.language.connector.StashMessage;
import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.procedure.AggregatingProcedureProvider;
import com.opengamma.language.view.SessionViewClients;

/**
 * A mutable version of {@link SessionContext}.
 */
public class MutableSessionContext extends SessionContext {

  /**
   * Name under which the session engine client's heartbeat sender is bound. Note that the
   * heartbeat sender is defined in the mutable context rather than the read-only version
   * so that only the initializer can access it.
   */
  protected static final String CLIENT_HEARTBEAT = "clientHeartbeat";

  private final SessionContextEventHandler _eventHandler;
  private boolean _initialized;

  /* package */MutableSessionContext(final UserContext userContext, final SessionContextEventHandler eventHandler) {
    super(userContext);
    _eventHandler = eventHandler;
  }

  private SessionContextEventHandler getEventHandler() {
    return _eventHandler;
  }

  // Context initialization

  @Override
  public void initContext(final SessionContextInitializationEventHandler preInitialize) {
    if (_initialized) {
      throw new IllegalStateException("initContext or initContextWithStash already called");
    }
    preInitialize.initContext(this);
    getEventHandler().initContext(this);
    _initialized = true;
  }

  @Override
  public void initContextWithStash(final SessionContextInitializationEventHandler preInitialize,
      final FudgeMsg stash) {
    if (_initialized) {
      throw new IllegalStateException("initContext or initContextWithStash already called");
    }
    preInitialize.initContextWithStash(this, stash);
    getEventHandler().initContextWithStash(this, stash);
    _initialized = true;
  }

  @Override
  public void doneContext() {
    getEventHandler().doneContext(this);
    getUserContext().removeSessionContext(this);
  }

  // Definition providers

  @Override
  public AggregatingFunctionProvider getFunctionProvider() {
    return getFunctionProviderImpl();
  }

  @Override
  public AggregatingLiveDataProvider getLiveDataProvider() {
    return getLiveDataProviderImpl();
  }

  @Override
  public AggregatingProcedureProvider getProcedureProvider() {
    return getProcedureProviderImpl();
  }

  // Standard context members

  public void setClient(final RemoteClient client) {
    removeOrReplaceValue(CLIENT, client);
  }

  public ScheduledFuture<?> getClientHeartbeat() {
    return getValue(CLIENT_HEARTBEAT);
  }

  public void setClientHeartbeat(final ScheduledFuture<?> clientHeartbeat) {
    setValue(CLIENT_HEARTBEAT, clientHeartbeat);
  }

  public void setDebug() {
    setValue(DEBUG, Boolean.TRUE);
  }

  public void setStashMessage(final StashMessage stashMessage) {
    setValue(STASH_MESSAGE, stashMessage);
  }

  public void setMessageSender(final MessageSender messageSender) {
    setValue(MESSAGE_SENDER, messageSender);
  }

  public void setViewClients(final SessionViewClients viewClients) {
    setValue(VIEW_CLIENTS, viewClients);
  }

  // Arbitrary values

  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

}

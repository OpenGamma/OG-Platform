/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.concurrent.ScheduledFuture;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.procedure.AggregatingProcedureProvider;
import com.opengamma.language.view.UserViewClients;
import com.opengamma.livedata.UserPrincipal;

/**
 * A mutable version of {@link UserContext}. 
 */
public class MutableUserContext extends UserContext {

  /**
   * Name under which the user engine client's heartbeat sender is bound. Note that the
   * heartbeat sender is defined in the mutable context rather than the read-only version
   * so that only the initializer can access it.
   */
  protected static final String CLIENT_HEARTBEAT = "clientHeartbeat";

  private final UserContextEventHandler _eventHandler;

  /* package */MutableUserContext(final GlobalContext globalContext, final String userName,
      final UserContextEventHandler eventHandler) {
    super(globalContext, userName);
    _eventHandler = eventHandler;
    eventHandler.initContext(this);
  }

  private UserContextEventHandler getEventHandler() {
    return _eventHandler;
  }

  // Context initialization

  @Override
  protected void doneContext() {
    getEventHandler().doneContext(this);
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

  public void setLiveDataUser(final UserPrincipal liveDataUser) {
    setValue(LIVEDATA_USER, liveDataUser);
  }

  public void setViewClients(final UserViewClients viewClients) {
    setValue(VIEW_CLIENTS, viewClients);
  }

  // Arbitrary values

  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

}

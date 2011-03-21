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
 * A mutable version of {@link SessionContext}.
 */
public class MutableSessionContext extends SessionContext {

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
      final FudgeFieldContainer stash) {
    if (_initialized) {
      throw new IllegalStateException("initContext or initContextWithStash already called");
    }
    preInitialize.initContext(this);
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

  public void setMessageSender(final MessageSender messageSender) {
    setValue(MESSAGE_SENDER, messageSender);
  }

  public void setDebug() {
    setValue(DEBUG, Boolean.TRUE);
  }

  // Arbitrary values

  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

}

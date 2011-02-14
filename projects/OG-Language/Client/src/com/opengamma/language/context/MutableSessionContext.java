/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.language.connector.MessageSender;

/**
 * A mutable version of {@link SessionContext}.
 */
public class MutableSessionContext extends SessionContext {

  private final SessionContextEventHandler _eventHandler;
  private MessageSender _messageSender;
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

  // Standard context members

  @Override
  public MessageSender getMessageSender() {
    return _messageSender;
  }

  public void setMessageSender(final MessageSender messageSender) {
    _messageSender = messageSender;
  }

  // Arbitrary values

  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

}

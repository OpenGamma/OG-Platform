/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

/**
 * A mutable version of {@link UserContext}. 
 */
public class MutableUserContext extends UserContext {

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

  @Override
  protected void doneContext() {
    getEventHandler().doneContext(this);
  }

  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

  // TODO

}

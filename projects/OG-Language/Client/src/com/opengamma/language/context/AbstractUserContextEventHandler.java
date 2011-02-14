/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

/**
 * Partial implementation of lifetime events on a session context that chains other handlers.
 * Previously registered handlers run their initialization steps before this one, and their
 * destruction steps afterwards.
 */
public abstract class AbstractUserContextEventHandler implements UserContextEventHandler {

  private final UserContextEventHandler _previous;

  protected AbstractUserContextEventHandler(final UserContextEventHandler previous) {
    _previous = previous;
  }

  private UserContextEventHandler getPrevious() {
    return _previous;
  }

  @Override
  public final void initContext(final MutableUserContext context) {
    getPrevious().initContext(context);
    initContextImpl(context);
  }

  protected abstract void initContextImpl(MutableUserContext context);

  @Override
  public final void doneContext(final MutableUserContext context) {
    doneContextImpl(context);
    getPrevious().doneContext(context);
  }

  protected abstract void doneContextImpl(MutableUserContext context);

}

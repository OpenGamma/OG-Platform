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

  protected void initContextImpl(MutableUserContext context) {
    // No-op if not implemented
  }

  @Override
  public final void doneContext(final MutableUserContext context) {
    doneContextImpl(context);
    getPrevious().doneContext(context);
  }

  protected void doneContextImpl(MutableUserContext context) {
    // No-op if not implemented
  }

}

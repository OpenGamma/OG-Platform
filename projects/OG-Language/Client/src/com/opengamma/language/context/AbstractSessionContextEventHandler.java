/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeMsg;

import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of lifetime events on a session context that chains other handlers.
 * Previous handler run their initialization step before this one, and the destruction step
 * afterwards. 
 */
public abstract class AbstractSessionContextEventHandler implements SessionContextEventHandler {

  private final SessionContextEventHandler _previous;

  protected AbstractSessionContextEventHandler(final SessionContextEventHandler previous) {
    ArgumentChecker.notNull(previous, "previous");
    _previous = previous;
  }

  private SessionContextEventHandler getPrevious() {
    return _previous;
  }

  @Override
  public final void initContext(final MutableSessionContext context) {
    getPrevious().initContext(context);
    initContextImpl(context);
  }

  protected void initContextImpl(MutableSessionContext context) {
    // No-op if unimplemented
  }

  @Override
  public final void initContextWithStash(final MutableSessionContext context, final FudgeMsg stash) {
    getPrevious().initContextWithStash(context, stash);
    initContextWithStashImpl(context, stash);
  }

  protected void initContextWithStashImpl(MutableSessionContext context, FudgeMsg stash) {
    // Ignore the stash if unimplemented
    initContextImpl(context);
  }

  @Override
  public final void doneContext(final MutableSessionContext context) {
    doneContextImpl(context);
    getPrevious().doneContext(context);
  }

  protected void doneContextImpl(MutableSessionContext context) {
    // No-op if unimplemented
  }

}

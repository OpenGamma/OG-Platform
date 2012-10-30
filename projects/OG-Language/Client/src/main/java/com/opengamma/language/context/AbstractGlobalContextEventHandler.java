/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of lifetime events on a global context that chains other handlers.
 * Previous handlers run their initialization step before this one.
 */
public abstract class AbstractGlobalContextEventHandler implements GlobalContextEventHandler {

  private final GlobalContextEventHandler _previous;

  protected AbstractGlobalContextEventHandler(final GlobalContextEventHandler previous) {
    ArgumentChecker.notNull(previous, "previous");
    _previous = previous;
  }

  private GlobalContextEventHandler getPrevious() {
    return _previous;
  }

  @Override
  public final void initContext(final MutableGlobalContext context) {
    getPrevious().initContext(context);
    initContextImpl(context);
  }

  protected abstract void initContextImpl(MutableGlobalContext context);

}

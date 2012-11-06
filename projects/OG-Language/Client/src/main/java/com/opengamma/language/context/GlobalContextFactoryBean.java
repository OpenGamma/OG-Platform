/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import com.opengamma.util.ArgumentChecker;

/**
 * A factory for providing the global contexts.
 * 
 * The event handlers must be declared before the contexts are created. Handlers declared after
 * a context has been created will not affect it.
 */
public class GlobalContextFactoryBean implements GlobalContextFactory {

  private GlobalContextFactoryBean _extendedFrom;
  private GlobalContextEventHandler _globalContextEventHandler = new NullGlobalContextEventHandler();

  private GlobalContext _globalContext;

  public synchronized GlobalContextFactoryBean getExtendedFrom() {
    return _extendedFrom;
  }

  /**
   * Declares this global context factory as extending another. New global contexts are created by
   * the one extended and the handlers from this factory then applied. Note that only handlers
   * registered when the extension is made are applied, changes to the context factory extended
   * made after this call have no effect.
   * 
   * @param extendedFrom the context factory to extend from, not null and can only be set once
   */
  public synchronized void setExtendedFrom(final GlobalContextFactoryBean extendedFrom) {
    ArgumentChecker.notNull(extendedFrom, "extendedFrom");
    if (_extendedFrom != null) {
      throw new IllegalStateException("extendedFrom already set");
    }
    _extendedFrom = extendedFrom;
    // Prepend the event handlers of the factory we're extending from
    setGlobalContextEventHandler(new AbstractGlobalContextEventHandler(extendedFrom.getGlobalContextEventHandler()) {

      private final GlobalContextEventHandler _chain = getGlobalContextEventHandler();

      @Override
      protected void initContextImpl(MutableGlobalContext context) {
        _chain.initContext(context);
      }

    });
  }

  public synchronized void setGlobalContextEventHandler(final GlobalContextEventHandler globalContextEventHandler) {
    ArgumentChecker.notNull(globalContextEventHandler, "globalContextEventHandler");
    _globalContextEventHandler = globalContextEventHandler;
  }

  public synchronized GlobalContextEventHandler getGlobalContextEventHandler() {
    return _globalContextEventHandler;
  }

  protected synchronized MutableGlobalContext createGlobalContext() {
    if (getExtendedFrom() != null) {
      return getExtendedFrom().createGlobalContext();
    } else {
      return new MutableGlobalContext();
    }
  }

  // GlobalContextFactory

  @Override
  public synchronized GlobalContext getOrCreateGlobalContext() {
    if (_globalContext == null) {
      final MutableGlobalContext newContext = createGlobalContext();
      getGlobalContextEventHandler().initContext(newContext);
      _globalContext = newContext;
    }
    return _globalContext;
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import com.opengamma.util.ArgumentChecker;

/**
 * A factory for providing the user contexts.
 * 
 * The event handlers must be declared before the contexts are created. Handlers declared after
 * a context has been created will not affect it.
 */
public class UserContextFactoryBean implements UserContextFactory {

  private UserContextFactoryBean _extendedFrom;
  private UserContextEventHandler _userContextEventHandler = new NullUserContextEventHandler();
  private GlobalContextFactory _globalContextFactory = new GlobalContextFactoryBean();

  public UserContextFactoryBean() {
  }

  public UserContextFactoryBean(final GlobalContextFactory globalContextFactory) {
    setGlobalContextFactory(globalContextFactory);
  }

  /**
   * Declares this user context factory as extending another. New user contexts are created by
   * the one extended and the handlers from this factory then applied. Note that only handlers
   * registered when the extension is made are applied, changes to the context factory extended
   * made after this call have no effect.
   * 
   * @param extendedFrom the context factory to extend from, not null and can only be set once
   */
  public synchronized void setExtendedFrom(final UserContextFactoryBean extendedFrom) {
    ArgumentChecker.notNull(extendedFrom, "extendedFrom");
    if (_extendedFrom != null) {
      throw new IllegalStateException("extendedFrom already set");
    }
    _extendedFrom = extendedFrom;
    // Prepend the event handlers of the factory we're extending from
    setUserContextEventHandler(new AbstractUserContextEventHandler(extendedFrom.getUserContextEventHandler()) {

      private final UserContextEventHandler _chain = getUserContextEventHandler();

      @Override
      protected void doneContextImpl(MutableUserContext context) {
        _chain.doneContext(context);
      }

      @Override
      protected void initContextImpl(MutableUserContext context) {
        _chain.initContext(context);
      }

    });
  }

  public synchronized UserContextFactoryBean getExtendedFrom() {
    return _extendedFrom;
  }

  public synchronized void setUserContextEventHandler(final UserContextEventHandler userContextEventHandler) {
    ArgumentChecker.notNull(userContextEventHandler, "userContextEventHandler");
    _userContextEventHandler = userContextEventHandler;
  }

  public synchronized UserContextEventHandler getUserContextEventHandler() {
    return _userContextEventHandler;
  }

  public synchronized void setGlobalContextFactory(final GlobalContextFactory globalContextFactory) {
    ArgumentChecker.notNull(globalContextFactory, "globalContextFactory");
    _globalContextFactory = globalContextFactory;
  }

  public synchronized GlobalContextFactory getGlobalContextFactory() {
    return _globalContextFactory;
  }

  protected synchronized MutableUserContext createUserContext(final GlobalContext globalContext, final String userName,
      final UserContextEventHandler eventHandler) {
    if (getExtendedFrom() != null) {
      return getExtendedFrom().createUserContext(globalContext, userName, eventHandler);
    } else {
      return new MutableUserContext(globalContext, userName, eventHandler);
    }
  }

  // UserContextFactory

  @Override
  public synchronized UserContext getOrCreateUserContext(final String userName) {
    ArgumentChecker.notNull(userName, "userName");
    final GlobalContext globalContext = getGlobalContextFactory().getOrCreateGlobalContext();
    // Synchronize on global context to make the getUserContext and addUserContext calls atomic and locked
    // against removeUserContext
    synchronized (globalContext) {
      final UserContext userContext = globalContext.getUserContext(userName);
      if (userContext != null) {
        return userContext;
      }
      final MutableUserContext newContext = createUserContext(globalContext, userName, getUserContextEventHandler());
      globalContext.addUserContext(newContext);
      return newContext;
    }
  }

}

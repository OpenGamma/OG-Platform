/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import com.opengamma.util.ArgumentChecker;

/**
 * A factory for providing the global, user, and session contexts.
 * 
 * The event handlers must be declared before the contexts are created. Handlers declared after
 * a context has been created will not affect it.
 */
public class ContextFactoryBean {

  private UserContextEventHandler _userContextEventHandler = new NullUserContextEventHandler();
  private SessionContextEventHandler _sessionContextEventHandler = new NullSessionContextEventHandler();
  private GlobalContextEventHandler _globalContextEventHandler = new NullGlobalContextEventHandler();

  private GlobalContext _globalContext;

  public synchronized void setUserContextEventHandler(final UserContextEventHandler userContextEventHandler) {
    ArgumentChecker.notNull(userContextEventHandler, "userContextEventHandler");
    _userContextEventHandler = userContextEventHandler;
  }

  public synchronized UserContextEventHandler getUserContextEventHandler() {
    return _userContextEventHandler;
  }

  public synchronized void setSessionContextEventHandler(final SessionContextEventHandler sessionContextEventHandler) {
    ArgumentChecker.notNull(sessionContextEventHandler, "sessionContextEventHandler");
    _sessionContextEventHandler = sessionContextEventHandler;
  }

  public synchronized SessionContextEventHandler getSessionContextEventHandler() {
    return _sessionContextEventHandler;
  }

  public synchronized void setGlobalContextEventHandler(final GlobalContextEventHandler globalContextEventHandler) {
    ArgumentChecker.notNull(globalContextEventHandler, "globalContextEventHandler");
    _globalContextEventHandler = globalContextEventHandler;
  }

  public synchronized GlobalContextEventHandler getGlobalContextEventHandler() {
    return _globalContextEventHandler;
  }

  /**
   * Returns an initialized {@link GlobalContext} object, creating one if it hasn't already been created.
   * 
   * @return the initialized context
   */
  private GlobalContext getOrCreateGlobalContext() {
    if (_globalContext == null) {
      final MutableGlobalContext newContext = new MutableGlobalContext();
      getGlobalContextEventHandler().initContext(newContext);
      _globalContext = newContext;
    }
    return _globalContext;
  }

  /**
   * Returns an initialized {@link UserContext} object, creating one if one has not already been created (and not
   * since destroyed) for the user. A {@link GlobalContext} is created and initialized if one is not already
   * available. 
   * 
   * @param userName the name of the user
   * @return the initialized context
   */
  private UserContext getOrCreateUserContext(final String userName) {
    ArgumentChecker.notNull(userName, "userName");
    final GlobalContext globalContext = getOrCreateGlobalContext();
    // Synchronize on global context to make the getUserContext and addUserContext calls atomic and locked
    // against removeUserContext
    synchronized (globalContext) {
      final UserContext userContext = globalContext.getUserContext(userName);
      if (userContext != null) {
        return userContext;
      }
      final MutableUserContext newContext = new MutableUserContext(globalContext, userName,
          getUserContextEventHandler());
      globalContext.addUserContext(newContext);
      return newContext;
    }
  }

  /**
   * Creates a new {@link SessionContext} object for a user. If the user does not already have an active
   * {@link UserContext}, one is created and initialized by {@link #getOrCreateUserContext}. The session
   * context is not initialized at construction - the caller must initialize it by calling {@link SessionContext.initContext}
   * when it is ready to use it.
   * 
   * @param userName the name of the user
   * @return the initialized context
   */
  public synchronized SessionContext createSessionContext(final String userName) {
    ArgumentChecker.notNull(userName, "userName");
    final UserContext userContext = getOrCreateUserContext(userName);
    final SessionContext newContext = new MutableSessionContext(userContext, getSessionContextEventHandler());
    userContext.addSessionContext(newContext);
    return newContext;
  }

}

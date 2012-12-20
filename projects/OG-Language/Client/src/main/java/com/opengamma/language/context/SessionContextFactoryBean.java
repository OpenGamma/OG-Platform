/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeMsg;

import com.opengamma.util.ArgumentChecker;

/**
 * A factory for providing the session contexts.
 * 
 * The event handlers must be declared before the contexts are created. Handlers declared after
 * a context has been created will not affect it.
 */
public class SessionContextFactoryBean implements SessionContextFactory {

  private SessionContextFactoryBean _extendedFrom;
  private UserContextFactory _userContextFactory = new UserContextFactoryBean();
  private SessionContextEventHandler _sessionContextEventHandler = new NullSessionContextEventHandler();

  public SessionContextFactoryBean() {
  }

  public SessionContextFactoryBean(final UserContextFactory userContextFactory) {
    setUserContextFactory(userContextFactory);
  }

  /**
   * Declares this session context factory as extending another. New session contexts are created by
   * the one extended and the handlers from this factory then applied. Note that only handlers
   * registered when the extension is made are applied, changes to the context factory extended
   * made after this call have no effect.
   * 
   * @param extendedFrom the context factory to extend from, not null and can only be set once
   */
  public synchronized void setExtendedFrom(final SessionContextFactoryBean extendedFrom) {
    ArgumentChecker.notNull(extendedFrom, "extendedFrom");
    if (_extendedFrom != null) {
      throw new IllegalStateException("extendedFrom already set");
    }
    _extendedFrom = extendedFrom;
    // Prepend the event handlers of the factory we're extending from
    setSessionContextEventHandler(new AbstractSessionContextEventHandler(extendedFrom.getSessionContextEventHandler()) {

      private final SessionContextEventHandler _chain = getSessionContextEventHandler();

      @Override
      protected void doneContextImpl(MutableSessionContext context) {
        _chain.doneContext(context);
      }

      @Override
      protected void initContextImpl(MutableSessionContext context) {
        _chain.initContext(context);
      }

      @Override
      protected void initContextWithStashImpl(MutableSessionContext context, FudgeMsg stash) {
        _chain.initContextWithStash(context, stash);
      }

    });
  }

  public synchronized SessionContextFactoryBean getExtendedFrom() {
    return _extendedFrom;
  }

  public synchronized void setUserContextFactory(final UserContextFactory userContextFactory) {
    ArgumentChecker.notNull(userContextFactory, "userContextFactory");
    _userContextFactory = userContextFactory;
  }

  public synchronized UserContextFactory getUserContextFactory() {
    return _userContextFactory;
  }

  public synchronized void setSessionContextEventHandler(final SessionContextEventHandler sessionContextEventHandler) {
    ArgumentChecker.notNull(sessionContextEventHandler, "sessionContextEventHandler");
    _sessionContextEventHandler = sessionContextEventHandler;
  }

  public synchronized SessionContextEventHandler getSessionContextEventHandler() {
    return _sessionContextEventHandler;
  }

  protected synchronized MutableSessionContext createSessionContext(final UserContext userContext,
      final SessionContextEventHandler eventHandler) {
    if (getExtendedFrom() != null) {
      return getExtendedFrom().createSessionContext(userContext, eventHandler);
    } else {
      return new MutableSessionContext(userContext, eventHandler);
    }
  }

  @Override
  public synchronized SessionContext createSessionContext(final String userName, final boolean debug) {
    ArgumentChecker.notNull(userName, "userName");
    final UserContext userContext = getUserContextFactory().getOrCreateUserContext(userName);
    final MutableSessionContext newContext = createSessionContext(userContext, getSessionContextEventHandler());
    if (debug) {
      newContext.setDebug();
    }
    userContext.addSessionContext(newContext);
    return newContext;
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.context;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.language.connector.Conditional;

/**
 * Attaches a context initialization hook based on a conditional. The class can be used for any of
 * {@link SessionContext}, {@link UserContext} or {@link GlobalContext}.
 */
public class ContextInitializationBean implements InitializingBean {

  private Conditional _condition;
  private SessionContextFactoryBean _sessionContextFactory;
  private UserContextFactoryBean _userContextFactory;
  private GlobalContextFactoryBean _globalContextFactory;

  public Conditional getCondition() {
    return _condition;
  }

  public void setCondition(final Conditional condition) {
    _condition = condition;
  }

  public SessionContextFactoryBean getSessionContextFactory() {
    return _sessionContextFactory;
  }

  public void setSessionContextFactory(final SessionContextFactoryBean sessionContextFactory) {
    _sessionContextFactory = sessionContextFactory;
  }

  public UserContextFactoryBean getUserContextFactory() {
    return _userContextFactory;
  }

  public void setUserContextFactory(final UserContextFactoryBean userContextFactory) {
    _userContextFactory = userContextFactory;
  }

  public GlobalContextFactoryBean getGlobalContextFactory() {
    return _globalContextFactory;
  }

  public void setGlobalContextFactory(final GlobalContextFactoryBean globalContextFactory) {
    _globalContextFactory = globalContextFactory;
  }

  /**
   * Called from {@link afterPropertiesSet} before any other action taken.
   */
  protected void assertPropertiesSet() {
  }

  /**
   * Initializes a session context.
   * 
   * @param context the context
   */
  protected void initContext(final MutableSessionContext context) {
  }

  /**
   * Cleans up a session context.
   * 
   * @param context the context
   */
  protected void doneContext(final MutableSessionContext context) {
  }

  /**
   * Initializes a user context.
   * 
   * @param context the context
   */
  protected void initContext(final MutableUserContext context) {
  }

  /**
   * Cleans up a user context 
   *
   * @param context the context
   */
  protected void doneContext(final MutableUserContext context) {
  }

  /**
   * Initializes a global context.
   * 
   * @param context the context
   */
  protected void initContext(final MutableGlobalContext context) {
  }

  // InitializingBean

  /**
   * After the properties are set, an initialization handler is attached to the context factory
   * which will invoke one of the {@link #initContext} methods when it initializes if the
   * condition holds at the point of initialization when tested against that context.
   */
  @Override
  public final void afterPropertiesSet() {
    assertPropertiesSet();
    boolean validFactory = false;
    if (getGlobalContextFactory() != null) {
      getGlobalContextFactory().setGlobalContextEventHandler(
          new AbstractGlobalContextEventHandler(getGlobalContextFactory().getGlobalContextEventHandler()) {
            @Override
            protected void initContextImpl(final MutableGlobalContext context) {
              if (Conditional.holds(getCondition(), context)) {
                ContextInitializationBean.this.initContext(context);
              }
            }
          });
      validFactory = true;
    }
    if (getUserContextFactory() != null) {
      getUserContextFactory().setUserContextEventHandler(
          new AbstractUserContextEventHandler(getUserContextFactory().getUserContextEventHandler()) {

            private boolean _initialised;

            @Override
            protected void initContextImpl(final MutableUserContext context) {
              if (Conditional.holds(getCondition(), context)) {
                ContextInitializationBean.this.initContext(context);
                _initialised = true;
              }
            }

            @Override
            protected void doneContextImpl(final MutableUserContext context) {
              if (_initialised) {
                ContextInitializationBean.this.doneContext(context);
                _initialised = false;
              }
            }

          });
      validFactory = true;
    }
    if (getSessionContextFactory() != null) {
      getSessionContextFactory().setSessionContextEventHandler(
          new AbstractSessionContextEventHandler(getSessionContextFactory().getSessionContextEventHandler()) {

            private boolean _initialised;

            @Override
            protected void initContextImpl(final MutableSessionContext context) {
              if (Conditional.holds(getCondition(), context)) {
                ContextInitializationBean.this.initContext(context);
                _initialised = true;
              }
            }

            @Override
            protected void doneContextImpl(final MutableSessionContext context) {
              if (_initialised) {
                ContextInitializationBean.this.doneContext(context);
                _initialised = false;
              }
            }

          });
      validFactory = true;
    }
    if (!validFactory) {
      throw new IllegalStateException();
    }
  }

}

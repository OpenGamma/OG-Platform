/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.language.context.AbstractGlobalContextEventHandler;
import com.opengamma.language.context.AbstractSessionContextEventHandler;
import com.opengamma.language.context.AbstractUserContextEventHandler;
import com.opengamma.language.context.DefaultGlobalContextEventHandler;
import com.opengamma.language.context.DefaultSessionContextEventHandler;
import com.opengamma.language.context.DefaultUserContextEventHandler;
import com.opengamma.language.context.GlobalContextEventHandler;
import com.opengamma.language.context.GlobalContextFactoryBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextEventHandler;
import com.opengamma.language.context.SessionContextFactoryBean;
import com.opengamma.language.context.UserContextEventHandler;
import com.opengamma.language.context.UserContextFactoryBean;
import com.opengamma.language.invoke.TypeConverterProvider;

public class TestUtils {

  public static final String USERNAME = "Test";

  /**
   * Creates and initializes a session context using the supplied event handlers.
   * 
   * @param globalEventHandler event handler for the global context, or {@code null} if not required
   * @param userEventHandler event handler for the user context, or {@code null} if not required
   * @param sessionEventHandler event handler for the session context, or {@code null} if not required
   * @return the initialized context, not {@code null}
   */
  public static SessionContext createSessionContext(final GlobalContextEventHandler globalEventHandler, final UserContextEventHandler userEventHandler,
      final SessionContextEventHandler sessionEventHandler) {
    final GlobalContextFactoryBean globalContextFactory = new GlobalContextFactoryBean();
    if (globalEventHandler != null) {
      globalContextFactory.setGlobalContextEventHandler(globalEventHandler);
    }
    final UserContextFactoryBean userContextFactory = new UserContextFactoryBean(globalContextFactory);
    if (userEventHandler != null) {
      userContextFactory.setUserContextEventHandler(userEventHandler);
    }
    final SessionContextFactoryBean sessionContextFactory = new SessionContextFactoryBean(userContextFactory);
    if (sessionEventHandler != null) {
      sessionContextFactory.setSessionContextEventHandler(sessionEventHandler);
    }
    final SessionContext ctx = sessionContextFactory.createSessionContext(USERNAME, false);
    ctx.initContext(new DefaultSessionContextEventHandler());
    return ctx;
  }

  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private TypeConverterProvider _typeConverters;
  private ViewProcessor _viewProcessor;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private RemoteClient _globalClient;
  private RemoteClient _userClient;
  private RemoteClient _sessionClient;

  public TestUtils() {
  }

  public void setHistoricalTimeSeriesSource(final HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
  }

  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  public void setTypeConverters(final TypeConverterProvider typeConverters) {
    _typeConverters = typeConverters;
  }

  public TypeConverterProvider getTypeConverters() {
    return _typeConverters;
  }

  public void setViewProcessor(final ViewProcessor viewProcessor) {
    _viewProcessor = viewProcessor;
  }

  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  public void setSecuritySource(final SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setPositionSource(final PositionSource positionSource) {
    _positionSource = positionSource;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

  public void setGlobalClient(final RemoteClient client) {
    _globalClient = client;
  }

  public RemoteClient getGlobalClient() {
    return _globalClient;
  }

  public void setUserClient(final RemoteClient client) {
    _userClient = client;
  }

  public RemoteClient getUserClient() {
    return _userClient;
  }

  public void setSessionClient(final RemoteClient client) {
    _sessionClient = client;
  }

  public RemoteClient getSessionClient() {
    return _sessionClient;
  }

  protected GlobalContextEventHandler createGlobalContextEventHandler() {
    final DefaultGlobalContextEventHandler base = new DefaultGlobalContextEventHandler();
    base.setSystemSettings(System.getProperties());
    return new AbstractGlobalContextEventHandler(base) {
      @Override
      public void initContextImpl(final MutableGlobalContext globalContext) {
        if (getHistoricalTimeSeriesSource() != null) {
          globalContext.setHistoricalTimeSeriesSource(getHistoricalTimeSeriesSource());
        }
        if (getTypeConverters() != null) {
          globalContext.getTypeConverterProvider().addTypeConverterProvider(getTypeConverters());
        }
        if (getViewProcessor() != null) {
          globalContext.setViewProcessor(getViewProcessor());
        }
        if (getSecuritySource() != null) {
          globalContext.setSecuritySource(getSecuritySource());
        }
        if (getPositionSource() != null) {
          globalContext.setPositionSource(getPositionSource());
        }
        if (getGlobalClient() != null) {
          globalContext.setClient(getGlobalClient());
        }
      }
    };
  }

  protected UserContextEventHandler createUserContextEventHandler() {
    final DefaultUserContextEventHandler base = new DefaultUserContextEventHandler();
    return new AbstractUserContextEventHandler(base) {
      @Override
      public void initContextImpl(final MutableUserContext userContext) {
        if (getUserClient() != null) {
          userContext.setClient(getUserClient());
        }
      }
    };
  }

  protected SessionContextEventHandler createSessionContextEventHandler() {
    final DefaultSessionContextEventHandler base = new DefaultSessionContextEventHandler();
    return new AbstractSessionContextEventHandler(base) {
      @Override
      public void initContextImpl(final MutableSessionContext sessionContext) {
        if (getSessionClient() != null) {
          sessionContext.setClient(getSessionClient());
        }
      }
    };
  }

  public SessionContext createSessionContext() {
    return createSessionContext(createGlobalContextEventHandler(), createUserContextEventHandler(), createSessionContextEventHandler());
  }

}

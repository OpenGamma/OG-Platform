/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.language.context.DefaultSessionContextEventHandler;
import com.opengamma.language.context.DefaultUserContextEventHandler;
import com.opengamma.language.context.GlobalContextEventHandler;
import com.opengamma.language.context.GlobalContextFactoryBean;
import com.opengamma.language.context.MutableGlobalContext;
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

  protected GlobalContextEventHandler createGlobalContextEventHandler() {
    return new GlobalContextEventHandler() {
      @Override
      public void initContext(final MutableGlobalContext globalContext) {
        if (getHistoricalTimeSeriesSource() != null) {
          globalContext.setHistoricalTimeSeriesSource(getHistoricalTimeSeriesSource());
        }
        if (getTypeConverters() != null) {
          globalContext.getTypeConverterProvider().addTypeConverterProvider(getTypeConverters());
        }
        if (getViewProcessor() != null) {
          globalContext.setViewProcessor(getViewProcessor());
        }
      }
    };
  }

  protected UserContextEventHandler createUserContextEventHandler() {
    return new DefaultUserContextEventHandler();
  }

  protected SessionContextEventHandler createSessionContextEventHandler() {
    return new DefaultSessionContextEventHandler();
  }

  public SessionContext createSessionContext() {
    return createSessionContext(createGlobalContextEventHandler(), createUserContextEventHandler(), createSessionContextEventHandler());
  }

}

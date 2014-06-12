/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import javax.servlet.ServletContext;

import com.opengamma.util.ArgumentChecker;

/**
 * Keys for use in {@code ServletContext} attributes.
 */
public final class WebPushServletContextUtils {

  /**
   * The key for {@link ConnectionManager}.
   */
  private static final String CONNECTION_MANAGER_KEY = WebPushServletContextUtils.class.getName() + ".ConnectionManager";
  /**
   * The key for {@link LongPollingConnectionManager}.
   */
  private static final String LONG_POLLING_CONNECTION_MANAGER_KEY = WebPushServletContextUtils.class.getName() + ".LongPollingConnectionManager";

  /**
   * Restricted constructor.
   */
  private WebPushServletContextUtils() {
  }

  public static boolean isConnectionManagerAvailable(ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    ConnectionManager mgr = (ConnectionManager) servletContext.getAttribute(CONNECTION_MANAGER_KEY);
    return mgr != null;
  }

  
  //-------------------------------------------------------------------------
  /**
   * Gets the manager from the context.
   * 
   * @param servletContext  the context, not null
   * @return the manager, not null
   */
  public static ConnectionManager getConnectionManager(ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    ConnectionManager mgr = (ConnectionManager) servletContext.getAttribute(CONNECTION_MANAGER_KEY);
    ArgumentChecker.notNull(mgr, "ConnectionManager");
    return mgr;
  }

  /**
   * Sets the manager into the context.
   * 
   * @param servletContext  the context, not null
   * @param mgr  the manager, not null
   */
  public static void setConnectionManager(ServletContext servletContext, ConnectionManager mgr) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    ArgumentChecker.notNull(mgr, "ConnectionManager");
    servletContext.setAttribute(CONNECTION_MANAGER_KEY, mgr);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the manager from the context.
   * 
   * @param servletContext  the context, not null
   * @return the manager, not null
   */
  public static LongPollingConnectionManager getLongPollingConnectionManager(ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    LongPollingConnectionManager mgr = (LongPollingConnectionManager) servletContext.getAttribute(LONG_POLLING_CONNECTION_MANAGER_KEY);
    ArgumentChecker.notNull(mgr, "LongPollingConnectionManager");
    return mgr;
  }

  /**
   * Sets the manager into the context.
   * 
   * @param servletContext  the context, not null
   * @param mgr  the manager, not null
   */
  public static void setLongPollingConnectionManager(ServletContext servletContext, LongPollingConnectionManager mgr) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    ArgumentChecker.notNull(mgr, "LongPollingConnectionManager");
    servletContext.setAttribute(LONG_POLLING_CONNECTION_MANAGER_KEY, mgr);
  }

}

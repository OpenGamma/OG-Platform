/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.server;

import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.financial.tool.ToolContext;

/**
 * A connection to a remote server that provides an easy way to access remote component implementations.
 */
public class RemoteServer extends ToolContext {

  /**
   * Creates a connection to a remote server.
   * @param url The URL of the server. Doesn't require the "/jax" suffix.
   * @param type The {@link RemoteServer} subtype that should be created.
   * @param <T> The {@link RemoteServer} subtype that should be created.
   * @return A remote server populated with remote component instances.
   */
  @SuppressWarnings("unchecked")
  public static <T extends RemoteServer> T create(String url, Class<T> type) {
    return (T) ToolContextUtils.getToolContext(url, type);
  }

  /**
   * Creates a connection to a remote server.
   * @param url The URL of the server. Doesn't require the "/jax" suffix.
   * @return A remote server populated with remote component instances.
   */
  public static RemoteServer create(String url) {
    return create(url, RemoteServer.class);
  }
}

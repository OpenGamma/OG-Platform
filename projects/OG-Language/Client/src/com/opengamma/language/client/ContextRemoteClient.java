/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.client;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.UserContext;

/**
 * Extracts the {@link RemoteClient} instance from the supplied context.
 */
public final class ContextRemoteClient {

  /**
   * Prevents instantiation.
   */
  private ContextRemoteClient() {
  }

  public static RemoteClient get(final SessionContext context, final MasterID master) {
    switch (master) {
      case SESSION:
        return context.getClient();
      case USER:
        return context.getUserContext().getClient();
      case GLOBAL:
        return context.getGlobalContext().getClient();
      default:
        throw new IllegalArgumentException("Invalid master '" + master + "' for session context");
    }
  }

  public static RemoteClient get(final UserContext context, final MasterID master) {
    switch (master) {
      case USER:
        return context.getClient();
      case GLOBAL:
        return context.getGlobalContext().getClient();
      default:
        throw new IllegalArgumentException("Invalid master '" + master + "' for user context");
    }
  }

  public static RemoteClient get(final GlobalContext context, final MasterID master) {
    switch (master) {
      case GLOBAL:
        return context.getClient();
      default:
        throw new IllegalArgumentException("Invalid master '" + master + "' for global context");
    }
  }

}

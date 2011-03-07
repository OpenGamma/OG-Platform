/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import com.opengamma.language.context.SessionContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Constructs {@link Client} instances as connections are received.
 */
public final class DefaultClientFactory implements ClientFactory {

  /**
   * The client context for new connections.
   */
  private final ClientContext _clientContext;

  private DefaultClientFactory(final ClientContext clientContext) {
    ArgumentChecker.notNull(clientContext, "clientContext");
    _clientContext = clientContext;
  }

  private ClientContext getClientContext() {
    return _clientContext;
  }

  public static ClientFactoryFactory getFactory() {
    return new ClientFactoryFactory() {
      @Override
      public ClientFactory createClientFactory(final ClientContext clientContext) {
        return new DefaultClientFactory(clientContext);
      }
    };
  }

  // ClientFactory

  @Override
  public Client createClient(final String inputPipeName, final String outputPipeName,
      final SessionContext sessionContext) {
    return new Client(getClientContext(), inputPipeName, outputPipeName, sessionContext);
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector.debug;

import com.opengamma.language.connector.Client;
import com.opengamma.language.connector.ClientContext;
import com.opengamma.language.connector.ClientFactory;
import com.opengamma.language.connector.ClientFactoryFactory;
import com.opengamma.language.context.SessionContext;

/**
 * Constructs a {@link DebugClient} instance for connection to an external process.
 */
public final class DebugClientFactory extends ClientFactory {
  
  private DebugClientFactory(final ClientContext clientContext) {
    super(clientContext);
  }

  public static ClientFactoryFactory getFactory() {
    return new ClientFactoryFactory() {
      @Override
      public ClientFactory createClientFactory(final ClientContext clientContext) {
        return new DebugClientFactory(clientContext);
      }
    };
  }

  // ClientFactory

  @Override
  public Client createClient(final String inputPipeName, final String outputPipeName,
      final SessionContext sessionContext) {
    return new DebugClient(getClientContext(), inputPipeName, outputPipeName, sessionContext);
  }
}

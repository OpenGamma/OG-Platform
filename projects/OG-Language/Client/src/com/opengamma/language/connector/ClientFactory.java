/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import com.opengamma.language.context.SessionContext;

/**
 * Constructs {@link Client} instances as connections are received.
 */
public interface ClientFactory {

  Client createClient(final String inputPipeName, final String outputPipeName, final SessionContext sessionContext);

}

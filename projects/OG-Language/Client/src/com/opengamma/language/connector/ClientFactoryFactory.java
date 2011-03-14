/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;


/**
 * Constructs {@link ClientFactory} instances for {@link ClientContext}s.
 */
public interface ClientFactoryFactory {

  ClientFactory createClientFactory(ClientContext clientContext);

}

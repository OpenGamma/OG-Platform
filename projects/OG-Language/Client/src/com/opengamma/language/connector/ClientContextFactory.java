/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

/**
 * Constructs a {@link ClientContext} instance
 */
public interface ClientContextFactory {

  ClientContext createClientContext();

}

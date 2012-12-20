/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

/**
 * A factory for providing session contexts.
 */
public interface SessionContextFactory {

  /**
   * Creates a new {@link SessionContext} object for a user.The session context is not
   * initialized at construction - the caller must initialize it by calling
   * {@link SessionContext.initContext} when it is ready to use it.
   * 
   * @param userName  the name of the user
   * @param debug  true if the 'debug' property should be set
   * @return the initialized context
   */
  SessionContext createSessionContext(final String userName, final boolean debug);

}

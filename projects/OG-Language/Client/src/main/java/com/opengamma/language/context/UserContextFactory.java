/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

/**
 * A factory for providing user contexts.
 */
public interface UserContextFactory {

  /**
   * Returns an initialized {@link UserContext} object, creating one if one has not already been created (and not
   * since destroyed) for the user.
   * 
   * @param userName the name of the user
   * @return the initialized context
   */
  UserContext getOrCreateUserContext(final String userName);

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.HashMap;
import java.util.Map;

/**
 * A global information context shared by all client instances. This corresponds to the
 * OpenGamma installation the language integration framework is connecting to.
 */
public abstract class GlobalContext extends AbstractContext {

  private final Map<String, UserContext> _userContexts = new HashMap<String, UserContext>();

  /* package */GlobalContext() {
  }

  /**
   * Adds a user context. To combine the user context operations into an atomic operation,
   * synchronize on the global context object (e.g. for get followed by add).
   * 
   * @param userContext user context to add
   * @throws IllegalStateException if an active context already exists for the user
   */
  protected synchronized void addUserContext(final UserContext userContext) {
    if (_userContexts.get(userContext.getUserName()) != null) {
      throw new IllegalStateException("User context for '" + userContext.getUserName() + "' already exists");
    }
    _userContexts.put(userContext.getUserName(), userContext);
  }

  /**
   * Removes a user context. To combine the user context operations into an atomic operation,
   * synchronize on the global context object (e.g. for get followed by add).
   * 
   * @param userContext user context to remove
   * @throws IllegalStateException if an active context does not exist for the user
   */
  protected synchronized void removeUserContext(final UserContext userContext) {
    if (_userContexts.remove(userContext.getUserName()) == null) {
      throw new IllegalStateException("User context for '" + userContext.getUserName() + "' doesn't exist");
    }
  }

  /**
   * Returns an existing user context. To combine the user context operations into an atomic
   * operation, synchronize on the global context object (e.g. for get followed by add).
   * 
   * @param userName name of the user to search for
   * @return an existing context, or {@code null} if none is available
   */
  protected synchronized UserContext getUserContext(final String userName) {
    return _userContexts.get(userName);
  }

}

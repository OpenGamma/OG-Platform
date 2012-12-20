/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

/**
 * Exposure of write access to a manager for function blacklisting information.
 */
public interface ManageableFunctionBlacklistProvider extends FunctionBlacklistProvider {

  /**
   * Returns an update interface to the blacklist corresponding to the given identifier.
   * 
   * @param identifier blacklist identifier unique within this provider
   * @return the update interface to the blacklist or null if no blacklist was found
   */
  ManageableFunctionBlacklist getBlacklist(String identifier);

}

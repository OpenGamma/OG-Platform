/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

/**
 * Exposure of a manager for function blacklisting information. Function blacklists are named to allow multiple ones to exist from a single provider - for example:
 * <ul>
 * <li>Different lists might be needed for execution suppression and graph construction suppression
 * <li>Different lists might be needed for different view processors
 * <li>Different lists might be needed for different function repositories
 * <li>Different nodes might have different blacklists
 * </ul>
 * The underlying implementation may be document based, but blacklists are potentially interactive resources that the view processor, graph executor and remote calculation nodes are reading and
 * writing from simultaneously.
 */
public interface FunctionBlacklistProvider {

  /**
   * Returns an interface to the blacklist corresponding to the given identifier.
   * 
   * @param identifier blacklist identifier unique within this provider
   * @return the interface to the blacklist or null if no blacklist was found
   */
  FunctionBlacklist getBlacklist(String identifier);

}

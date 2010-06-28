/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;
import java.util.Set;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A master structure of all securities used by the organization.
 */
public interface SecurityMaster {

  /**
   * Finds a specific security by identifier.
   * @param uid  the unique identifier, null returns null
   * @return the security, null if not found
   */
  Security getSecurity(UniqueIdentifier uid);

  /**
   * Finds all securities that match the specified bundle of keys.
   * If there are none specified, this method must return an
   * empty collection, and not {@code null}.
   * @param secKey  the bundle keys to match, not null
   * @return all securities matching the specified key, empty if no matches, not null
   */
  Collection<Security> getSecurities(IdentifierBundle secKey);

  /**
   * Finds the single best-fit security that matches the specified bundle of keys.
   * <p>
   * It is entirely the responsibility of the implementation to determine which
   * security matches best for any given bundle of keys.
   * @param secKey  the bundle keys to match, not null
   * @return the single security matching the bundle of keys, null if not found
   */
  Security getSecurity(IdentifierBundle secKey);

  /**
   * Obtain all the available security types in this security master.
   * <p>
   * The implementation should return the available types, however if this is
   * not possible it may return all potential types.
   * @return the set of available security types, not null
   */
  Set<String> getAllSecurityTypes();

}

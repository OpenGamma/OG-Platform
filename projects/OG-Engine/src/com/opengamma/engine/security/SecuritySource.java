/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A source of securities as accessed by the engine.
 * <p>
 * This interface provides a simple view of securities as needed by the engine.
 * This may be backed by a full-featured security master, or by a much simpler data structure.
 */
@PublicSPI
public interface SecuritySource {

  /**
   * Finds a specific security by identifier.
   * 
   * @param uid  the unique identifier, null returns null
   * @return the security, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Security getSecurity(UniqueIdentifier uid);

  /**
   * Finds all securities that match the specified bundle of keys.
   * <p>
   * The result should consist of all securities that match each specified key.
   * 
   * @param secKey  the bundle keys to match, not null
   * @return all securities matching the specified key, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Collection<Security> getSecurities(IdentifierBundle secKey);

  /**
   * Finds the single best-fit security that matches the specified bundle of keys.
   * <p>
   * It is entirely the responsibility of the implementation to determine which
   * security matches best for any given bundle of keys.
   * 
   * @param secKey  the bundle keys to match, not null
   * @return the single security matching the bundle of keys, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Security getSecurity(IdentifierBundle secKey);

}

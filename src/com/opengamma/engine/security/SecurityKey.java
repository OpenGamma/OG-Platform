/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.Collection;

import com.opengamma.DomainSpecificIdentifier;

/**
 * A way of defining one or more {@link Security} instances from a set
 * of descriptive data about that security.
 * It is up to the {@link SecurityMaster} implementation to determine
 * a mapping strategy to identify whether a particular key matches
 * a particular {@link Security}.
 *
 * @author kirk
 */
public interface SecurityKey extends Serializable {
  /**
   * Obtain any security identifiers desired as part of resolving based on this key.
   * The order in which the results are identified in the collection
   * <em>may</em> be meaningful, depending on the implementation of
   * the {@link SecurityMaster} used to look up securities based on this key.
   * This method <em>must not</em> return {@code null}, but should
   * return an empty {@link Collection} where there are no identifiers
   * for this key.
   * 
   * @return All identifiers for this key.
   */
  Collection<DomainSpecificIdentifier> getIdentifiers();
}

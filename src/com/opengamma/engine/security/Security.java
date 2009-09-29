/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;

import com.opengamma.DomainSpecificIdentifier;

/**
 * 
 *
 * @author kirk
 */
public interface Security {

  /**
   * Obtain all the security identifiers which are part of this
   * {@code Security}'s description.
   * 
   * @return All identifiers for this security.
   */
  Collection<DomainSpecificIdentifier> getIdentifiers();
  
  /**
   * Obtain a SecurityKey that uniquely identifies the security in question
   */
  SecurityKey getIdentityKey();
  
  /**
   * Obtain the text-based type of this Security.
   * @return The text-based type of this security.
   */
  String getSecurityType();
}

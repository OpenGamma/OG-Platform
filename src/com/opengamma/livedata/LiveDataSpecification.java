/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;

import com.opengamma.DomainSpecificIdentifier;

/**
 * 
 *
 * @author kirk
 */
public interface LiveDataSpecification {
  
  Collection<DomainSpecificIdentifier> getIdentifiers();

}

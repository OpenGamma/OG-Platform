/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.DomainSpecificIdentifiers;

/**
 *
 * @author kirk
 */
public interface IdResolver {
  
  /**
   * Transforms a set of IDs into a unique ID. For example, a Bloomberg ticker is 
   * transformed into a Bloomberg unique ID.
   * <p>
   * If the input is already a unique ID, it is returned as it is: no validation is performed.
   * 
   * @return The unique ID. Null if it was not found.
   */
  DomainSpecificIdentifiers resolve(DomainSpecificIdentifiers ids);

}

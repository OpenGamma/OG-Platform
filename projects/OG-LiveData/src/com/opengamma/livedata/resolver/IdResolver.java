/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Transforms a set of IDs into a unique ID.
 */
public interface IdResolver {
  
  /**
   * Transforms a set of IDs into a unique ID. For example, a Bloomberg ticker is 
   * transformed into a Bloomberg unique ID.
   * <p>
   * If the input is already a unique ID, it is returned as it is: no validation is performed.
   * 
   * @param ids input IDs
   * @return the unique ID. Null if it was not found.
   */
  Identifier resolve(IdentifierBundle ids);

}

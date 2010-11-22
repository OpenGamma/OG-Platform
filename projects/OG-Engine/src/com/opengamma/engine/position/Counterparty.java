/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import com.opengamma.id.Identifier;
import com.opengamma.util.PublicAPI;

/**
 * An entity against which a trade was executed.
 * This entity might be an exchange where the final counterparty is unknown.
 */
@PublicAPI
public interface Counterparty {
  /**
   * Returns the counter party identifier
   * 
   * @return the identifier not null
   */
  Identifier getIdentifier();
}

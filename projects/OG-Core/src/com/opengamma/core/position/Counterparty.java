/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.PublicSPI;

/**
 * The entity against which a trade was executed.
 * <p>
 * Trades have a counterparty to link the other side of the deal.
 * This entity might be an exchange where the final counterparty is unknown.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicSPI
public interface Counterparty {
  
  /**
   * Default Identification scheme for Counterparty.
   */
  ExternalScheme DEFAULT_SCHEME = ExternalScheme.of("COUNTER_PARTY");

  /**
   * Gets the external identifier of the counterparty.
   * 
   * @return the counterparty external identifier, not null
   */
  ExternalId getExternalId();

}

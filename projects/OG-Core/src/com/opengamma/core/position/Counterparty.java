/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import com.opengamma.id.ExternalId;
import com.opengamma.util.PublicSPI;

/**
 * The entity against which a trade was executed.
 * <p>
 * Trades have a counterparty to link the other side of the deal.
 * This entity might be an exchange where the final counterparty is unknown.
 */
@PublicSPI
public interface Counterparty {

  /**
   * Gets the external identifier of the counterparty.
   * 
   * @return the identifier, not null
   */
  ExternalId getExternalId();

}

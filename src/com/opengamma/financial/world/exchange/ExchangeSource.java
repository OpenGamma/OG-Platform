/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Easy-to-use interface to the ExchangeMaster
 */
public interface ExchangeSource {
  /**
   * This call should be used if you no that there will be a single result, e.g. if you're using a MIC 
   * @param identifier
   * @return
   */
  public Exchange getSingleExchange(Identifier identifier);

  /**
   * This call should be used if you no that there will be a single result, e.g. if you're using a MIC and other unique Identifiers 
   * (note I mean unique Identifiers, not UniqueIdentifiers) in a bundle
   * @param identifier
   * @return
   */
  public Exchange getSingleExchange(IdentifierBundle identifier);
}

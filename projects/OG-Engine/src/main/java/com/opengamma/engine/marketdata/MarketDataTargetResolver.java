/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicSPI;

/**
 * Attempts to resolve the target of a market data requirement as required for a particular market data provider.
 */
@PublicSPI
public interface MarketDataTargetResolver {

  /**
   * Gets the external ID bundle to be used for a particular value requirement.
   * 
   * @param requirement  the value requirement, not null
   * @return the external ID bundle, null if not resolvable
   */
  ExternalIdBundle getExternalIdBundle(final ValueRequirement requirement);
  
}

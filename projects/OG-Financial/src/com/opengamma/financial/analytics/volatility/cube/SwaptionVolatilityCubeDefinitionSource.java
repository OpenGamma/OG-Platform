/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import javax.time.Instant;

/**
 * A source of swaption volatility cube definitions.
 */
public interface SwaptionVolatilityCubeDefinitionSource {

  /**
   * Gets a swaption volatility cube definition for a name
   * @param name The name of the swaption volatility cube definition, not null
   * @return the definition, null if not found
   */
  SwaptionVolatilityCubeDefinition<?, ?, ?> getDefinition(String name);

  /**
   * Gets a swaption volatility cube definition for a name and version
   * @param name The name of the swaption volatility cube definition, not null
   * @param version The version instant, not null
   * @return the definition, null if not found
   */
  SwaptionVolatilityCubeDefinition<?, ?, ?> getDefinition(String name, Instant version);
}

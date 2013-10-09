/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.id.VersionCorrection;

/**
 * A source of volatility surface definitions.
 * <p>
 * This interface provides a simple view of volatility surface definitions.
 * This may be backed by a full-featured master, or by a much simpler data structure.
 * @deprecated should access config source directly
 */
@Deprecated
public interface VolatilitySurfaceDefinitionSource {

  /**
   * Gets a volatility surface definition for a currency and name.
   * @param name  the name, not null
   * @param instrumentType the name of the type of the instrument, not null
   * @return the definition, null if not found
   */
  VolatilitySurfaceDefinition<?, ?> getDefinition(String name, String instrumentType);

  /**
   * Gets a volatility surface definition for a currency, name and version.
   * @param name  the name, not null
   * @param instrumentType the name of the type of the instrument, not null
   * @param versionCorrection  the version correction, not null
   * @return the definition, null if not found
   */
  VolatilitySurfaceDefinition<?, ?> getDefinition(String name, String instrumentType, VersionCorrection versionCorrection);

}
